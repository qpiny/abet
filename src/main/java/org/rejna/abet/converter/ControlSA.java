package org.rejna.abet.converter;

import java.util.HashMap;
import java.util.Vector;

import org.rejna.abet.DataEntry;
import org.rejna.abet.common.Column;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;

public class ControlSA extends Converter {
	protected HashMap<Integer,Vector<String>> columns = new HashMap<Integer,Vector<String>>();
	
	public void addConfiguredColumn(Column column) {
		columns.put(column.getNumber(), column.getAttributes());
	}
	
	@Override
	public void convert(DataEntry data) throws ConverterException {
		DataEntry output = new DataEntry();
		try {
			for (Object[] entry : data) {
				output.addElement();
				for (int i = 0; i < entry.length; i++) {
					Vector<String> attr_def = columns.get(i);
					if (attr_def != null) {
						HashMap<String,String> attrs = new HashMap<String,String>();
						for (String attr : entry[i].toString().split(";;")) {
							String[] name_value = attr.split("=", 2);
							attrs.put(name_value[0], name_value[1]);
						}
						for (String attr_name : attr_def) {
							output.addAttribute();
							output.addValue(attrs.get(attr_name));
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
}