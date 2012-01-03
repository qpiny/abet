package org.rejna.abet.common;

import java.util.Vector;

import org.rejna.abet.converter.ColumnConverter;
import org.rejna.abet.exception.ConverterException;

public class Column {
	private Class<ColumnConverter> column_type = null;
	private Vector<String> attributes = new Vector<String>();
	private String arg = null;
	private String separator = null;
	private int count = -1;
	private int number = -1;
	private String name = null;
	private String from = null;
	private String to = null;
	private String match = null;
	
	public String getMatch() {
		return match;
	}

	public void setMatch(String match) {
		this.match = match;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNumber() {
		return number;
	}

	public String getArg() {
		return arg;
	}

	public String getSeparator() {
		return separator;
	}

	public int getCount() {
		return count;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public void setType(Class<ColumnConverter> column_type) {
		this.column_type = column_type;
	}
	
	public void setArg(String arg) {
		this.arg = arg;
	}
	
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public void addConfiguredAttribute(StringContainer attribute) {
		attributes.add(attribute.get());
	}
	
	public Vector<String> getAttributes() {
		return attributes;
	}
	
	public Object convert(Object obj) throws ConverterException {
		if (column_type == null)
			throw new ConverterException("Column has no type");
		try {
			return column_type.newInstance().convert(obj, arg);
		} catch (InstantiationException e) {
			throw new ConverterException("Convertion error", e);
		} catch (IllegalAccessException e) {
			throw new ConverterException("Convertion error", e);
		}
	}
}
