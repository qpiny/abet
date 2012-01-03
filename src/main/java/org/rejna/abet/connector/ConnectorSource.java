package org.rejna.abet.connector;

import org.rejna.abet.DataEntry;
import org.rejna.abet.common.TransitElement;
import org.rejna.abet.exception.ConnectorException;

public abstract class ConnectorSource extends ConnectorContext implements TransitElement {
	
	@Override
	public final void setSource(TransitElement source) {
		throw new UnsupportedOperationException("setSource method is not supported by ConnectorSource element");
	}
	
	public Object getSingleValue() throws ConnectorException {
		DataEntry value;
		if (hasMoreElements())
			value = nextElement();
		else
			throw new ConnectorException("No value");
		if (value.isSingle())
			return value.iterator().next()[0];
		throw new ConnectorException("Not single value");
	}
	
	public void getResidue() {
	}
}
