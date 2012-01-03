package org.rejna.abet.exception;

public class ConverterException extends Exception {

	private static final long serialVersionUID = -876175537393419592L;

	public ConverterException(String message) {
		super(message);
	}
	
	public ConverterException(String message, Throwable t) {
		super(message, t);
	}

}
