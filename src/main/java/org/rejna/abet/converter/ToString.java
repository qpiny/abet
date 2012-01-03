package org.rejna.abet.converter;

import java.sql.Clob;
import java.sql.SQLException;

import org.rejna.abet.exception.ConverterException;

public class ToString implements ColumnConverter {
	
	@Override
	public Object convert(Object obj, String arg) throws ConverterException {
		try {
			if (obj == null)
				return null;
			if (obj instanceof Clob) {
				Clob c = (Clob)obj;
				return c.getSubString(1, (int) c.length());
			}
			return obj.toString();
		} catch (SQLException e) {
			throw new ConverterException("Error while converting clob into string", e);
		}
	}
}
