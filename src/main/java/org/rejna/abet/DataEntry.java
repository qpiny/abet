package org.rejna.abet;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.rejna.abet.exception.ConcurrentAccessException;

public class DataEntry implements Iterable<Object[]>{
	private Vector<Vector<Object>> data;
	private Vector<Object[]> expanded_data;
	private Vector<Object> current_attribute;
	private Boolean is_single = null;
	private boolean locked = false;
	
	public DataEntry() {
		data = new Vector<Vector<Object>>();
		current_attribute = null;
		expanded_data = new Vector<Object[]>();
	}
	
	public void addAttribute() throws ConcurrentAccessException {
		if (locked)
			throw new ConcurrentAccessException();
		current_attribute = new Vector<Object>();
		data.add(current_attribute);
	}
	
	public void addValue(Object value) throws ConcurrentAccessException  {
		if (current_attribute == null || locked)
			throw new ConcurrentAccessException();
		current_attribute.add(value);	
		if (is_single == null)
			is_single = Boolean.TRUE;
		else
			is_single = Boolean.FALSE;
	}

	public void addElement() throws ConcurrentAccessException {
		if (locked)
			throw new ConcurrentAccessException();
		expandData(expanded_data, new Object[data.size()], data.elements(), 0);
		data = new Vector<Vector<Object>>();
	}
	
	public void removeFirstElement() {
		lock();
		expanded_data.remove(0);
	}
	
	public void addDataEntry(DataEntry entries) throws ConcurrentAccessException  {
		for (Object[] entry: entries) {
			addElement();
			for (Object value: entry) {
				addAttribute();
				addValue(value);
			}
		}
	}
	
	public boolean isSingle() {
		if (is_single == null)
			return false;
		return is_single.booleanValue();
	}
	
	private void lock() {
		if (!locked) {
			locked = true;
			expandData(expanded_data, new Object[data.size()], data.elements(), 0);
			data = null;
		}
	}
	
	@Override
	public Iterator<Object[]> iterator() {
		lock();
		return expanded_data.iterator();
	}

	protected void expandData(Vector<Object[]> expanded_data, Object[] row, Enumeration<Vector<Object>> attr_enum, int index) {
		try {
			Enumeration<Object> value_enum = attr_enum.nextElement().elements();
			while (value_enum.hasMoreElements()) {
				row[index] = value_enum.nextElement();
				if (attr_enum.hasMoreElements())
					expandData(expanded_data, row, attr_enum, index + 1);
				else
					expanded_data.add(row.clone());
			}
		} catch (NoSuchElementException e) {
		}
	}
	
	@Override
	public String toString() {
		StringBuffer datastr = new StringBuffer();
		for (Object[] row : this) {
			for (int i = 0; i < row.length; i++) {
				datastr.append(" \"");
				datastr.append(row[i]);
				datastr.append("\"");
			}
			datastr.append("\n");
		}
		return datastr.toString();
	}
}
