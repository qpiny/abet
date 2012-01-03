package org.rejna.abet.exception;

public class ConcurrentAccessException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7799139141194062153L;
	
	public ConcurrentAccessException() {
		super();
	}
	public ConcurrentAccessException(String message) {
		super(message);
	}
	public ConcurrentAccessException(String message, Throwable t) {
		super(message, t);
	}
}
