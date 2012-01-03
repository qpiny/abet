package org.rejna.abet.connector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.tools.ant.BuildException;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.exception.ConnectorException;

public class SQLConnector extends Connector {
	private String driver = null;
	private String url = null;
	private Connection connection = null;
	
	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setUser(String user) {
		setProperty("user", user);
	}
	
	public void setPassword(String password) {
		setProperty("password", password);
	}
	
	public void initConnector() throws ConfigException {
		if (driver == null)
			throw new ConfigException("Driver is not defined for SQLConnector");
		if (url == null)
			throw new ConfigException("url is not defined for SQLConnector");
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, getProperties());
			connection.setAutoCommit(true);
		} catch (ClassNotFoundException e) {
			throw new BuildException(e);
		} catch (SQLException e) {
			throw new BuildException(e);
		}
	}
	
	public Connection getConnection() throws ConfigException, ConnectorException {
		if (connection == null)
			authenticateConnector();
		return connection;		
	}
}
