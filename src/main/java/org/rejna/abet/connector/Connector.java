package org.rejna.abet.connector;


import org.rejna.abet.common.Authenticator;
import org.rejna.abet.common.PropertyTask;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.exception.ConnectorException;

public abstract class Connector extends PropertyTask {
	private Authenticator authenticator = null;
	
	public void setConfigurator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}
	
	public void authenticateConnector() throws ConfigException, ConnectorException {
		if (authenticator != null) {
			Exception e = authenticator.authenticate(this);
			if (e != null)
				throw new ConnectorException("Connector initialisation error", e);
		}
		else
			initConnector();
	}
	
	public abstract void initConnector() throws ConfigException, ConnectorException;
	// TODO add close method
}
