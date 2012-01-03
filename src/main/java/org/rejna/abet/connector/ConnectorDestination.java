package org.rejna.abet.connector;

import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.rejna.abet.DataEntry;
import org.rejna.abet.common.TransitElement;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.exception.ConnectorException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public abstract class ConnectorDestination extends ConnectorContext implements
		Runnable {
	private TransitElement source = null;

	public void setSource(TransitElement source) {
		this.source = source;
	}
	
	public TransitElement getSource() {
		return source;
	}

	@Override
	public void run() {
		try {
			try {
				while (source.hasMoreElements())
					addRow(source.nextElement());
			} catch (NoSuchElementException e) {
			}
		} catch (Exception e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Transit execution error", e);
		}
		close();
	}

	public abstract void addRow(DataEntry element) throws ConnectorException, ConfigException;
}
