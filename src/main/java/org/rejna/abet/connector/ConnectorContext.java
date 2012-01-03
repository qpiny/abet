package org.rejna.abet.connector;

import org.apache.tools.ant.BuildException;
import org.rejna.abet.common.PropertyTask;
import org.rejna.abet.exception.ConnectorException;

public abstract class ConnectorContext extends PropertyTask {
	private Connector connector = null;
	
	public void setConnector(String connector_name) {
		Object obj = getProject().getReference(connector_name);
		try {
			
			connector = (Connector)obj;
		}
		catch (ClassCastException e) {
			throw new BuildException("connector attribute must be a reference of a connector (not " + obj.getClass().getCanonicalName() + ")");
		}
	}
	
	public Connector getConnector() throws ConnectorException {
		if (connector == null)
			throw new ConnectorException(getTaskName() + " : Connector must be set");
		return connector;
	}
	
	public abstract void close();
		

}
