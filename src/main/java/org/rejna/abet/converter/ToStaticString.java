package org.rejna.abet.converter;

import org.rejna.abet.exception.ConverterException;

public class ToStaticString implements ColumnConverter {
	
	@Override
	public Object convert(Object obj, String value) throws ConverterException {
		return value;
	}
}
