package org.rejna.abet.connector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Level;
import org.rejna.abet.common.Column;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class SQLTable extends SQLQuery {
	private String table = null;
	private Vector<Column> columns = new Vector<Column>();
	private StringBuilder insert = new StringBuilder();
	private StringBuilder values = new StringBuilder();
	
	public void setName(String table) {
		super.addText("SELECT * FROM " + table);
		this.table = table;
	}
	
	@Override
	public void addText(String table) {
		if (this.table == null) {
			super.addText("SELECT * FROM " + table);
			this.table = table;
		}
	}
	
	public void addConfiguredColumn(Column column) {
		insert.append(',').append(column.getName());
		values.append(",?");
		columns.add(column);
	}
	
	@Override
	public String getQuery() {
		if (columns.isEmpty())
			return super.getQuery();
		else
			return "INSERT INTO " + table + "(" + insert.substring(1) + ") VALUES (" + values.substring(1) + ")";
	}
	
	public int addRow(Connection connection, Object[] data) throws SQLException {
		//Log.connector.info("Query: " + getQuery());
		PreparedStatement stmt = connection.prepareStatement(getQuery());
		boolean has_non_null_value = false;
		// TODO check if ID exists
		int i = 1;
		for (Column column: columns) {
			Object value = data[column.getNumber()];
			if (value != null) {
				has_non_null_value = true;
				stmt.setString(i++, value.toString());
				// stmt.setObject(column.getNumber(), value);
			}
			else
				stmt.setString(i++, null);
			
		}
		
		if (has_non_null_value)
			return stmt.executeUpdate();
		
		return 0;
	}

	
	public int purge(Connection connection) throws ConfigException {
		if (table == null)
			throw new ConfigException("Only table can be purged");

		Statement stmt = null;
		int deletedRows = 0;
		try {
			stmt = connection.createStatement();
			//stmt.executeUpdate("TRUNCATE TABLE " + table);
			deletedRows = stmt.executeUpdate("DELETE FROM " + table);
		} catch (SQLException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Fail to purge the table using DELETE", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Fail to close DELETE statement", e);
			}
		}
		return deletedRows;
	}
}