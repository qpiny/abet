package org.rejna.abet.common;

import java.util.NoSuchElementException;

import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.Transit;
import org.rejna.abet.connector.ConnectorContext;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConnectorException;

enum VariableType {
	single, once, multiple, singleFetched, multipleFetched;
}

public class Variable extends Transit {
	private VariableType type = VariableType.single;
	private DataEntry data = null;
	private Object single_value = null;

	public Variable() {
		setUsethread(false);
	}
	
	public void setType(String type) {
		this.type = VariableType.valueOf(type);
	}

	public synchronized Object getSingleValue() throws ConnectorException {
		if (type == VariableType.single) {
			single_value = getSource().getSingleValue();
			type = VariableType.singleFetched;
			return single_value;
		}
		else if (type == VariableType.singleFetched)
			return single_value;
		throw new ConnectorException(
				"Only variable of type \"single\" supports getSingleValue method");
	}

	@Override
	public TransitElement getSource() {
		if (type == VariableType.once || type == VariableType.single)
			return super.getSource();
		else if (type == VariableType.multiple) {
			if (data == null) {
				try {
					TransitElement source = super.getSource();
					data = new DataEntry();
					while (source.hasMoreElements())
						data.addDataEntry(source.nextElement());
				} catch (NoSuchElementException e) {
				} catch (ConcurrentAccessException e) {
					throw new BuildException("DataEntry error", e);
				}
			}
			return new DataEntrySource(data);
		} else if (type == VariableType.multipleFetched) {
			return new DataEntrySource(data);
		} else
			throw new BuildException("Wrong variable type");
	}
}

class DataEntrySource extends ConnectorContext implements TransitElement {
	private DataEntry entries;
	private boolean fetched = false;

	public DataEntrySource(DataEntry entries) {
		this.entries = entries;
	}

	@Override
	public boolean hasMoreElements() {
		return !fetched;
	}

	@Override
	public DataEntry nextElement() {
		if (fetched)
			throw new NoSuchElementException();
		return entries;
	}

	@Override
	public void execute() {
	}

	@Override
	public void setSource(TransitElement source) throws ConnectorException {
		throw new ConnectorException("DataEntrySource.setSource not supported");
	}

	@Override
	public Object getSingleValue() throws ConnectorException {
		if (entries.isSingle())
			return entries.iterator().next()[0];
		throw new ConnectorException("Not single value");
	}

	@Override
	public void close() {
	}

	@Override
	public void getResidue() {
	}
}