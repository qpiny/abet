package org.rejna.abet.converter;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.rejna.abet.DataEntry;
import org.rejna.abet.common.Column;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;

public class FilterConverter extends Converter {
	protected static Logger logger = Logger.getLogger(FilterConverter.class.getName());
	protected Vector<Column> columns = new Vector<Column>();
	
	@Override
	public void convert(DataEntry data) throws ConverterException {
		DataEntry output = new DataEntry();
		try {
			for (Object[] entry : data) {
				boolean match = true;
				for (Column col: columns)
					if (entry[col.getNumber()] == null || !entry[col.getNumber()].toString().matches(col.getMatch())) {
						match = false;
						break;
					}
				if (match) {
					output.addElement();
					for (Object e : entry) {
						output.addAttribute();
						output.addValue(e);
					}
				}
			}
		} catch (ConcurrentAccessException e) {
			throw new ConverterException("Concurrent access error", e);
		}
		enqueue(output);
	}
}