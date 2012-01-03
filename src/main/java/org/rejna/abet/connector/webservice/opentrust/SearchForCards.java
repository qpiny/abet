package org.rejna.abet.connector.webservice.opentrust;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.axis2.AxisFault;
import org.rejna.abet.connector.webservice.opentrust.OTSCMConnectorSOAPServiceStub;
import org.rejna.abet.connector.webservice.opentrust.OTSCMConnectorSOAPServiceStub.HashTable_type0;
import org.rejna.abet.connector.webservice.opentrust.OTSCMConnectorSOAPServiceStub.Item_type0;
import org.rejna.abet.connector.webservice.opentrust.OTSCMConnectorSOAPServiceStub.Message_type0;
import org.rejna.abet.connector.webservice.opentrust.OTSCMConnectorSOAPServiceStub.OTMessageType;
import org.rejna.abet.connector.webservice.opentrust.OTSCMConnectorSOAPServiceStub.SearchForCardsResponse;
import org.rejna.abet.exception.ConnectorException;

public class SearchForCards implements OpentrustService {
	private OTSCMConnectorSOAPServiceStub stub;
	private HashTable_type0 holder_params, card_params, extra_params;
	
	public SearchForCards(String url) throws AxisFault {
		stub = new OTSCMConnectorSOAPServiceStub(url + "/connector.cgi");
		stub._getServiceClient().getOptions().setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
		purgeFilter();
	}
	
	@Override
	public void addFilter(String category, String key, String value) throws ConnectorException {
		Item_type0 item = new Item_type0();
		item.setKey(key);
		item.setValue(value);
		if ("card".equals(category))
			card_params.addItem(item);
		else if ("holder".equals(category))
			holder_params.addItem(item);
		else if ("extra".equals(category))
			extra_params.addItem(item);
		else
			throw new ConnectorException("Invalid category filter :" + category);
	}
	
	@Override
	public void purgeFilter() {
		holder_params = new HashTable_type0();
		card_params = new HashTable_type0();
		extra_params = new HashTable_type0();
	}
	
	@Override
	public Vector<HashMap<String,String>> getResult() throws RemoteException {
		org.rejna.abet.connector.webservice.opentrust.OTSCMConnectorSOAPServiceStub.SearchForCards sfc = new org.rejna.abet.connector.webservice.opentrust.OTSCMConnectorSOAPServiceStub.SearchForCards();
		OTMessageType otmsg = new OTMessageType();
		Message_type0 msg = new Message_type0();
		HashTable_type0 params = new HashTable_type0();
		
		Item_type0 param_item = new Item_type0();
		param_item.setKey("holder");
		param_item.setHashTable(holder_params);
		params.addItem(param_item);
		
		param_item = new Item_type0();
		param_item.setKey("card");
		param_item.setHashTable(card_params);
		params.addItem(param_item);		

		param_item = new Item_type0();
		param_item.setKey("extra");
		param_item.setHashTable(extra_params);
		params.addItem(param_item);		

		msg.setHashTable(params);
		otmsg.setMessage(msg);
		sfc.setInputParameter(otmsg);
		SearchForCardsResponse sfcr = stub.searchForCards(sfc);
		
		Vector<HashMap<String,String>> result = new Vector<HashMap<String,String>>();
		for (Item_type0 item : sfcr.getOutputParameter().getMessage().getArray().getItem()) {
			result.add(constructResult(item.getHashTable().getItem(), new HashMap<String,String>(), ""));
		}
		return result;
	}
	
	protected HashMap<String,String> constructResult(Item_type0[] items, HashMap<String,String> result, String prefix) {
		if (items == null)
			return result;
		for (Item_type0 item : items) {
			if (item.getArray() != null)
				constructResult(item.getArray().getItem(), result, ("/holder_data".equals(prefix) ? "/holder_data" : prefix + "/" + item.getKey()));
			if 	(item.getHashTable() != null)
				constructResult(item.getHashTable().getItem(), result, ("/holder_data".equals(prefix) ? "/holder_data" : prefix + "/" + item.getKey()));
			if (item.getValue() != null) {
				if (prefix.length() > 0)
					result.put(prefix.substring(1) + "/" + item.getKey(), item.getValue());
				else
					result.put(item.getKey(), item.getValue());
			}
		}
		return result;
	}
	
	@Override
	public void close() throws AxisFault {
		if (stub != null) {
			stub.cleanup();
			stub = null;
		}
	}
	
	@Override
	public boolean implementPaging() {
		return true;
	}
}