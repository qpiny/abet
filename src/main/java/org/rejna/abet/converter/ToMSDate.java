package org.rejna.abet.converter;

import org.rejna.abet.exception.ConverterException;

public class ToMSDate implements ColumnConverter {
	
	@Override
	public Object convert(Object obj, String arg) throws ConverterException {
		if (obj == null || "".equals(obj) || "0".equals(obj))
			return null;
		try {
			return new java.sql.Timestamp(Long.parseLong(obj.toString()) / 10000L - 11644473600000L);
		}
		catch (ClassCastException e) {
			throw new ConverterException("Fail to convert " + obj + " in date", e);
		}
	}
}
