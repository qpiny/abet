package org.rejna.abet.converter;

public class ToNull implements ColumnConverter {
	
	@Override
	public Object convert(Object obj, String arg) {
		return null;
	}
}
