package org.rejna.abet.common;

import java.util.Enumeration;

import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConnectorException;

public interface TransitElement extends Enumeration<DataEntry> {
	public void execute();
	public void setSource(TransitElement source) throws ConnectorException;
	public Object getSingleValue() throws ConnectorException;
	public void getResidue();
	public void close();
	public String printStats();
}
