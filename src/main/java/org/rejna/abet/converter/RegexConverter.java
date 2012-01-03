package org.rejna.abet.converter;

import java.util.Vector;

import org.rejna.abet.DataEntry;
import org.rejna.abet.common.Column;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;

public class RegexConverter extends Converter {
	protected Vector<Column> columns = new Vector<Column>();
	
	public void addConfiguredColumn(Column column) {
		columns.add(column);
	}
	
	@Override
	public void convert(DataEntry data) throws ConverterException {
		DataEntry output = new DataEntry();
		try {
			for (Object[] entry : data) {
				for (Column col : columns)
					if (entry[col.getNumber()] != null)
						entry[col.getNumber()] = entry[col.getNumber()].toString().replaceAll(col.getFrom(), col.getTo());
					  
				output.addElement();
				for (Object e : entry) {
					output.addAttribute();
					output.addValue(e);
				}
			}
		} catch (ConcurrentAccessException e) {
			throw new ConverterException("Concurrent access error", e);
		}
		enqueue(output);
	}
}