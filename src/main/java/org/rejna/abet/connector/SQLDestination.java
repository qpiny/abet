package org.rejna.abet.connector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.exception.ConnectorException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class SQLDestination extends ConnectorDestination {
	private SQLTable table = null; 
	private boolean purge = false;
	private ResultSet data = null;
	private Connection connection = null;

	public void setPurge(boolean purge) {
		this.purge = purge;
	}
	
	public void addTable(SQLTable table) {
		if (this.table != null)
			throw new BuildException("Only one query is permitted for destination");
		this.table = table;
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
	public void addRow(DataEntry elements) throws ConnectorException, ConfigException {
		try {
			if (data == null) {
				if (purge) {
					incStat("Deleted", table.purge(connection));
					purge = false;
				}
				data = table.execute(connection, false);
				data.moveToInsertRow();
			}
		} catch (SQLException e) {
			throw new BuildException("SQL error", e);
		}
		try {
			for (Object[] element: elements) {
				for (int i = 0; i < element.length; i++)
					data.updateObject(i + 1, element[i]);
				data.insertRow();
				incStat("Inserted");
			}
		} catch (SQLException e) {
			if (e.getErrorCode() == 2627 || // Violation of PRIMARY KEY constraint
					e.getErrorCode() == 2601) // Cannot insert duplicate key
				incStat("AlreadyExist");
			else {
				incStat("Error" + Integer.toString(e.getErrorCode()));
				Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Insert error (" + elements + ")", e);
			}
		}
	}

	@Override
	public void close() {
		if (table != null)
			table.close();
	}
}
