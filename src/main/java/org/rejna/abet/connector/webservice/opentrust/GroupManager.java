package org.rejna.abet.connector.webservice.opentrust;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.axis2.AxisFault;
import org.rejna.abet.exception.ConnectorException;

public class GroupManager implements OpentrustService {
	private Vector<String> groups = new Vector<String>();
	private String url;
			
	public GroupManager(String url) {
		this.url = url;
	}
	
	@Override
	public boolean implementPaging() {
		return false;
	}

	@Override
	public void addFilter(String category, String key, String value)
			throws ConnectorException {
		if ("param".equals(category)) {
			if ("group_name".equals(key))
				groups.add(value);
			else
				throw new ConnectorException("Filter key not recognized : "
						+ key);
		}
	}
	
	@Override
	public void purgeFilter() {
		groups.clear();
	}

	@Override
	public Vector<HashMap<String, String>> getResult() throws RemoteException, ConnectorException {
		RightsManager rm = new RightsManager(url);
		SearchForCards sfc = new SearchForCards(url);
		
		if (groups.isEmpty()) {
			for (HashMap<String,String> g: rm.getResult())
				groups.add(g.get("group_name"));
			
		}
		Vector<HashMap<String, String>> result = new Vector<HashMap<String, String>>();
		for (String group : groups) { 
			rm.purgeFilter();
			rm.addFilter("param", "group_name", group);
			for (HashMap<String,String> entry : rm.getResult()) {
				entry.put("group", group);
				result.add(entry);
				if (entry.containsKey("card_id")) {
					sfc.purgeFilter();
					sfc.addFilter("card", "id", entry.get("card_id"));
					for (HashMap<String,String> e: sfc.getResult())
						entry.putAll(e);
				}
			}
		}
		rm.close();
		sfc.close();
		return result;
	}

	@Override
	public void close() throws AxisFault {

	}

}
