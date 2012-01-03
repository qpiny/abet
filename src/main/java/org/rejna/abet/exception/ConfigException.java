package org.rejna.abet.exception;

public class ConfigException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1179557778643692783L;

	public ConfigException(String message) {
		super(message);
	}
	public ConfigException(String message, Throwable t) {
		super(message, t);
	}
}
