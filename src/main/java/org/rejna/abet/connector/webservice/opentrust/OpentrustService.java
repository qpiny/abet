package org.rejna.abet.connector.webservice.opentrust;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.axis2.AxisFault;
import org.rejna.abet.exception.ConnectorException;

public interface OpentrustService {
	public boolean implementPaging();
	public void purgeFilter();
	public void addFilter(String category, String key, String value) throws ConnectorException;
	public Vector<HashMap<String,String>> getResult() throws RemoteException, ConnectorException;
	public void close()  throws AxisFault;
}
