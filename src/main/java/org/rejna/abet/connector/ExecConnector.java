package org.rejna.abet.connector;

import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.exception.ConnectorException;

interface ExecConnector {
	public ExecContext run(String command) throws ConfigException, ConnectorException;
}