package org.rejna.abet.connector;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.exception.ConnectorException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class RSQLDestination extends ConnectorDestination {
	private Vector<SQLTable> tables = new Vector<SQLTable>();
	private Connection connection = null;
	private boolean purge = false;
	
	public void setPurge(boolean purge) {
		this.purge = purge;
	}
	
	public SQLTable createTable() {
		SQLTable t = new SQLTable();
		tables.add(t);
		return t;
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

	public void purge() throws ConfigException {
		for (SQLTable table : tables)
			incStat("Deleted", table.purge(connection));
	}
	
	@Override
	public void addRow(DataEntry element) throws ConnectorException, ConfigException {
		if (purge) {
			purge = false;
			purge();
		}
		for (Object[] data : element) {
			for (SQLTable table : tables) {
				try {
					incStat("Fetched", table.addRow(connection, data));
				} catch (SQLException e) {
					if (e.getErrorCode() == 2627 || // Violation of PRIMARY KEY constraint
							e.getErrorCode() == 2601) // Cannot insert duplicate key
						incStat("AlreadyExist");
					else {
						StringBuffer datastr = new StringBuffer("Insert error (");
						incStat("Error" + Integer.toString(e.getErrorCode()));
						for (int i = 0; i < data.length; i++) {
							datastr.append(" \"");
							datastr.append(data[i]);
							datastr.append("\"");
						}
						datastr.append(")");
						Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), datastr.toString(), e);
					}
				}
			}
		}
		
	}

	@Override
	public void close() {
		for (SQLTable table : tables)
			table.close();
	}
}

