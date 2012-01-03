package org.rejna.abet.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.connector.SQLConnector;
import org.rejna.abet.connector.SQLQuery;
import org.rejna.abet.connector.SQLSource;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class SQLExec extends SQLSource{

	@Override
	public void execute() {
		Connection connection = null;
		try {
			connection = ((SQLConnector)getConnector()).getConnection();
		} catch (Exception e) {
		}
		if (connection == null)
			throw new BuildException("SQLExec connector must have an SQLConnector");
		for (SQLQuery query: getQueries()) {
			try {
				query.execute(connection, true);
			} catch (SQLException e) {
				Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "SQL error: ", e);
			}
		}
	}
	
	@Override
	public boolean hasMoreElements() {
		return false;
	}

	@Override
	public DataEntry nextElement() {
		throw new NoSuchElementException();
	}
}
