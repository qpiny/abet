package org.rejna.abet.converter;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.rejna.abet.DataEntry;
import org.rejna.abet.common.Column;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class StandardConverter extends Converter {
	protected HashMap<Integer,Column> columnConverters = new HashMap<Integer,Column>();
	
	@Override
	public void convert(DataEntry data) throws ConverterException {
		DataEntry output = new DataEntry();
		try {
			for (Object[] entry : data) {
				output.addElement();
				for (int i = 0; i < entry.length; i++) {
					output.addAttribute();
					Column converter = columnConverters.get(i);
					if (converter == null) {
						output.addValue(entry[i]);
					}
					else {
						try {
							output.addValue(converter.convert(entry[i]));
						}
						catch (ConverterException e) {
							Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Conversion error: " + e.getMessage());
							output.addValue(null);
						}
					}
				}
			}
		} catch (ConcurrentAccessException e) {
			throw new ConverterException("Concurrent access error", e);
		}
		enqueue(output);
	}
	
	public void addConfiguredColumn(Column column) {
		columnConverters.put(column.getNumber(), column);
	}
}
