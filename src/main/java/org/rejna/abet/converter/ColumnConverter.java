package org.rejna.abet.converter;

import org.rejna.abet.exception.ConverterException;

public interface ColumnConverter {
	public Object convert(Object obj, String arg) throws ConverterException;
}
