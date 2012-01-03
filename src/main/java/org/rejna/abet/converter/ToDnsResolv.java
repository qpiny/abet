package org.rejna.abet.converter;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.rejna.abet.exception.ConverterException;

public class ToDnsResolv implements ColumnConverter {
	
	@Override
	public Object convert(Object obj, String arg) throws ConverterException {
		try {
			return InetAddress.getByName((String)obj).getHostAddress();
		} catch (UnknownHostException e) {
			throw new ConverterException("Resolv error for " + obj, e);
		}
	}
}
