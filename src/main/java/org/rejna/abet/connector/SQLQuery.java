package org.rejna.abet.connector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.rejna.abet.common.Variable;
import org.rejna.abet.exception.ConnectorException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class SQLQuery extends Task {
	private String query = null;
	private Vector<Object> parameters = new Vector<Object>();
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;

	public void addText(String query) {
		Pattern pattern = Pattern.compile("\\$!?\\{[^\\}]*}");
		Matcher matcher = pattern.matcher(query);
		try {
			while (matcher.find()) {
				boolean inplace = query.charAt(matcher.start() + 1) != '!';
				String param = query.substring(matcher.start() + (inplace ? 2 : 3), matcher.end() - 1);
				String value;
				if (inplace)
					value = ((Variable)getProject().getReference(param)).getSingleValue().toString();
				else {
					value = "?";
					parameters.add(((Variable)getProject().getReference(param)).getSingleValue());
				}
				query = query.substring(0, matcher.start()) + value + (query.length() > matcher.end() ? query.substring(matcher.end()) : "");
				matcher = pattern.matcher(query);
			}
			this.query = query;
		} catch (ConnectorException e) {
			throw new BuildException(e);
		}
	}
	
	public void addParameter(String param) {
		parameters.add(param);
	}
	
	public String getQuery() {
		return query;
	}

	public ResultSet execute(Connection connection, boolean readonly) throws SQLException {
		if (readonly)
			pstmt = connection.prepareStatement(getQuery(),
					ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		else {
			pstmt = connection.prepareStatement(getQuery(),
							ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_UPDATABLE);
		}
		int index = 1;
		for (Object param: parameters) {
			pstmt.setObject(index++, param);
		}
		if (pstmt.execute())
			return pstmt.getResultSet();
		// get the first resultSet that is not update result
		while (!pstmt.getMoreResults() && pstmt.getUpdateCount() != -1);
		rs = pstmt.getResultSet();
		if (!readonly)
			rs.moveToInsertRow();
		return rs;
	}
	
	public void close() {
		try {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
		} catch (SQLException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Error while closing SQL context", e);
		}
	}

}