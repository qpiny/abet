package org.rejna.abet.common;

public class StringContainer {
	private String value = null;
	
	public void addText(String value) {
		this.value = value;
	}
	
	public String get() {
		return value;
	}
}
