package org.rejna.abet.converter;

public class ToInteger implements ColumnConverter {
	
	@Override
	public Object convert(Object obj, String arg) {
		if (obj == null)
			return null;
		return new java.lang.Integer(obj.toString());
	}
}
