package org.rejna.abet.converter;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.rejna.abet.exception.ConverterException;

public class ToDate implements ColumnConverter {

	@Override
	public Object convert(Object obj, String format_pattern) throws ConverterException {
		if (obj == null || "".equals(obj))
			return null;
		
		try {
			if (format_pattern == null)
				return new java.sql.Timestamp(new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(obj.toString(), new ParsePosition(0)).getTime());
			else if ("timestamp".equals(format_pattern))
				return new java.sql.Timestamp(1000 * Long.valueOf(obj.toString()));
			else
				return new java.sql.Timestamp(new SimpleDateFormat(format_pattern, Locale.US).parse(obj.toString(), new ParsePosition(0)).getTime());
		}
		catch (Exception e) {
			throw new ConverterException("Can't convert \"" + obj + "\" to date with pattern \"" + format_pattern + "\"", e);
		}
	}
}
