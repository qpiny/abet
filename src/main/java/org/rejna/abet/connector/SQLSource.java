package org.rejna.abet.connector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class SQLSource extends ConnectorSource {
	private LinkedList<SQLQuery> queries = new LinkedList<SQLQuery>();
	private ResultSet data = null;
	private SQLQuery query = null;
	private boolean addheaders = false;
	private Connection connection = null;
	
	public void addQuery(SQLQuery query) {
		queries.add(query);
	}
	
	public void setAddheaders(boolean addheaders) {
		this.addheaders = addheaders;
	}
	
	public Iterable<SQLQuery> getQueries() {
		return new Iterable<SQLQuery>() {
			@Override
			public Iterator<SQLQuery> iterator() {
				return queries.iterator();
			}
			
		};
	}
	
	@Override
	public void execute() {
		try {
			connection = ((SQLConnector)getConnector()).getConnection();
		}
		catch (Exception e) {
			throw new BuildException("SQLConnector initialization failed", e);
		}
		if (connection == null)
			throw new BuildException("SQLSource connector must have an SQLConnector");
	}

	@Override
	public boolean hasMoreElements() {
		return true;
	}

	@Override
	public DataEntry nextElement() {
		if (data == null) {
			query = queries.poll();
			if (query == null)
				throw new NoSuchElementException();
			try {
				data = query.execute(connection, true);
				if (addheaders) {
					DataEntry el = new DataEntry();
					ResultSetMetaData rsmd = data.getMetaData();
					for (int i = 0; i < rsmd.getColumnCount(); i++) {
						el.addAttribute();
						el.addValue(rsmd.getColumnName(i + 1));
					}
					return el;
				}
			} catch (SQLException e) {
				Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "SQL execution error", e);
				throw new NoSuchElementException();
			} catch (ConcurrentAccessException e) {
				Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Concurrent access error", e);
			}
		}
			
		try {
			if (data.next())
				incStat("Fetched");
			else {
				if (query != null) {
					query.close();
					data = null;
				}
				query = queries.poll();
				if (query == null)
					throw new NoSuchElementException();
				data = query.execute(connection, true);
				return nextElement();
			}
			DataEntry el = new DataEntry();
			for (int i = 0; i < data.getMetaData().getColumnCount(); i++) {
				el.addAttribute();
				el.addValue(data.getObject(i + 1));
			}
			return el;
		} catch (SQLException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "SQL execution error", e);
		} catch (ConcurrentAccessException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Invalid state error", e);
		} catch (NoSuchElementException e) {
			throw e;
		} catch (Throwable t) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Unknown error", t);
		}
		throw new NoSuchElementException();
	}
	
	@Override
	public void close() {
		if (query != null)
			query.close();
		data = null;
	}
}
