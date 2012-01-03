package org.rejna.abet.converter;

import java.util.Vector;

import org.rejna.abet.DataEntry;
import org.rejna.abet.common.Column;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;

public class ColumnConcatener extends Converter {
	protected Vector<Integer> column_list = new Vector<Integer>();
	
	public void addConfiguredColumn(Column column) {
		column_list.add(column.getNumber());
	}
	
	@Override
	public void convert(DataEntry data) throws ConverterException {
		if (column_list.size() == 0)
			enqueue(data);
		DataEntry output = new DataEntry();
		int first_col = column_list.firstElement();
		try {
			for (Object[] entry : data) {
				output.addElement();
	
				for (int i = 0; i < entry.length; i++) {
					if (i == first_col) {
						output.addAttribute();
						StringBuilder concat = new StringBuilder();
						for (int col : column_list) {
							if (entry[col] != null)
								concat.append(entry[col]);
						}
						output.addValue(concat.toString());
					}
					else if (!column_list.contains(i)) {
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
}
