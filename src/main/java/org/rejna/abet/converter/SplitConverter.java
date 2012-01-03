package org.rejna.abet.converter;

import java.util.HashMap;

import org.rejna.abet.DataEntry;
import org.rejna.abet.common.Column;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;

public class SplitConverter extends Converter {
	private HashMap<Integer, Column> splitElements = new HashMap<Integer, Column>();
	
	@Override
	public void convert(DataEntry data) throws ConverterException {
		DataEntry output = new DataEntry();
		try {
			for (Object[] entry : data) {
				output.addElement();
				for (int i = 0; i < entry.length; i++) {
					
					Column se = splitElements.get(i);
					if (se != null) {
						int addedColumn = 0;
						if (entry[i] != null) {
							for (String e : entry[i].toString().split(se.getSeparator(), se.getCount())) {
								output.addAttribute();
								output.addValue(e);
								addedColumn++;
							}
							while (addedColumn < se.getCount()) {
								output.addAttribute();
								output.addValue(null);
								addedColumn++;
							}
						}
					}
					else {
						output.addAttribute();
						output.addValue(entry[i]);
					}
				}					
			}
		} catch (ConcurrentAccessException e) {
			throw new ConverterException("Concurrent access error", e);
		}
		enqueue(output);
	}
	
	public void addConfiguredColumn(Column column) {
		splitElements.put(column.getNumber(), column);
	}
}
