package org.rejna.abet.connector.webservice.opentrust;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.axis2.AxisFault;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.Message_type0;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.OTMessageType;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.GetGroupMembers;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.GetGroupMembersResponse;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.HashTable_type0;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.Item_type0;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.ListGroups;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.ListGroupsResponse;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.GetGroups;
import org.rejna.abet.connector.webservice.opentrust.OTSCMRightsConnectorSOAPServiceStub.GetGroupsResponse;
import org.rejna.abet.exception.ConnectorException;

public class RightsManager implements OpentrustService {
	private OTSCMRightsConnectorSOAPServiceStub stub;
	private String card_id = null;
	private String group_id = null;
	private String group_name = null;
	
	public RightsManager(String url) throws AxisFault {
		stub = new OTSCMRightsConnectorSOAPServiceStub(url + "/rights.cgi");
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
	}
	
	protected Item_type0[] listGroups() throws RemoteException {
		Message_type0 msg = new Message_type0();
		OTMessageType otmsg = new OTMessageType();
		otmsg.setMessage(msg);
		
		ListGroups lg = new ListGroups(); 
		lg.setInputParameter(otmsg);
		ListGroupsResponse lgr = stub.listGroups(lg);
		
		return lgr.getOutputParameter().getMessage().getArray().getItem();
	}
	
	protected Item_type0[] getGroupMembers(String gtype, String group) throws RemoteException {
		HashTable_type0 params = new HashTable_type0();

		Item_type0 param_item = new Item_type0();
		param_item.setKey(gtype);
		param_item.setValue(group);
		params.addItem(param_item);		

		Message_type0 msg = new Message_type0();
		msg.setHashTable(params);
		OTMessageType otmsg = new OTMessageType();
		otmsg.setMessage(msg);
		
		GetGroupMembers ggm = new GetGroupMembers();
		ggm.setInputParameter(otmsg);
		GetGroupMembersResponse ggmr = stub.getGroupMembers(ggm);
		return ggmr.getOutputParameter().getMessage().getArray().getItem();
	}
	
	protected Item_type0[] getGroups(String card_id) throws RemoteException {
		HashTable_type0 params = new HashTable_type0();

		Item_type0 param_item = new Item_type0();
		param_item.setKey("card_id");
		param_item.setValue(card_id);
		params.addItem(param_item);		

		Message_type0 msg = new Message_type0();
		msg.setHashTable(params);
		OTMessageType otmsg = new OTMessageType();
		otmsg.setMessage(msg);
		
		GetGroups gg = new GetGroups();
		gg.setInputParameter(otmsg);
		GetGroupsResponse ggr = stub.getGroups(gg);
		return ggr.getOutputParameter().getMessage().getArray().getItem();
		
	}
	
	@Override
	public void close() throws AxisFault {
		if (stub != null) {
			stub.cleanup();
			stub = null;
		}
	}

	@Override
	public void addFilter(String category, String key, String value)
			throws ConnectorException {
		if ("param".equals(category)) {
			if ("card_id".equals(key))
				card_id = value;
			else if ("group_id".equals(key))
				group_id = value;
			else if ("group_name".equals(key))
				group_name = value;
			else
				throw new ConnectorException("Filter key not recognized : " + key);
		}
	}
	
	@Override
	public void purgeFilter() {
		card_id = null;
		group_id = null;
		group_name = null;
	}

	@Override
	public Vector<HashMap<String, String>> getResult() throws RemoteException {
		Item_type0 items[];
		Vector<HashMap<String,String>> result = new Vector<HashMap<String,String>>();
		if (group_id != null)
			items = getGroupMembers("group_id", group_id); 
		else if (group_name != null)
			items = getGroupMembers("group_name", group_name);
		else if (card_id != null)
			items = getGroups(card_id);
		else
			items = listGroups();
		 
		if (items != null)
			for (Item_type0 item: items) {
				HashMap<String, String> element = new HashMap<String, String>();
				for (Item_type0 attr: item.getHashTable().getItem())
					element.put(attr.getKey(), attr.getValue());
				result.add(element);
			}
		return result;
	}
	
	@Override
	public boolean implementPaging() {
		return false;
	}
}
