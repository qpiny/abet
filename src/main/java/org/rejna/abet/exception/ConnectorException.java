package org.rejna.abet.exception;

public class ConnectorException extends Exception {

	private static final long serialVersionUID = 818736453650348884L;

	public ConnectorException(String message) {
		super(message);
	}
	
	public ConnectorException(String message, Throwable t) {
		super(message, t);
	}
}
