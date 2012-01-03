package org.rejna.abet.connector;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.axis2.AxisFault;
import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.common.StringContainer;
import org.rejna.abet.connector.webservice.opentrust.OpentrustService;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConnectorException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class OpentrustScmSource extends ConnectorSource {
	private OpentrustService svc = null;
	private String service = "searchForCards";
	private int page_size = -1;
	private Vector<String> attributes = new Vector<String>();
	private Vector<FilterElement> filterElements = new Vector<FilterElement>();
	private int offset = 0;
	private Iterator<HashMap<String, String>> result;

	public void setService(String service) {
		this.service = service;
	}

	public FilterElement createFilter() {
		FilterElement fe = new FilterElement();
		filterElements.add(fe);
		return fe;
	}

	public void setPagesize(int page_size) {
		this.page_size = page_size;
	}

	public void addConfiguredAttribute(StringContainer attribute) {
		attributes.add(attribute.get());
	}

	@Override
	public void execute() {
		try {
			svc = ((OpentrustScmConnector) getConnector()).getService(service);
		} catch (Exception e) {
			throw new BuildException(
					"OpentrustScmConnector initialization failed", e);
		}
		if (svc == null)
			throw new BuildException(
					"OpentrustScmSource connector must have an OpentrustScmConnector");

		try {
			for (FilterElement fe : filterElements)
				svc.addFilter(fe.type, fe.name, fe.value);
			getMoreResult();
		} catch (ConnectorException e) {
			throw new BuildException("Wrong service filter", e);
		}
	}

	@Override
	public boolean hasMoreElements() {
		try {
			if (result == null)
				return false;
			if (result.hasNext())
				return true;
			if (offset != -1) {
				getMoreResult();
				return hasMoreElements();
			}
		} catch (ConnectorException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Error while retrieve data from webservice", e);
			return false;
		}

		return false;
	}

	@Override
	public DataEntry nextElement() {
		if (!hasMoreElements())
			throw new NoSuchElementException();
		DataEntry entry = new DataEntry();
		HashMap<String, String> data = result.next();
		try {
			for (String attribute : attributes) {
				entry.addAttribute();
				entry.addValue(data.get(attribute));
			}
		} catch (ConcurrentAccessException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Concurrent access error", e);
			throw new NoSuchElementException();
		}
		incStat("Fetched");
		return entry;
	}

	@Override
	public void close() {
		try {
			svc.close();
		} catch (AxisFault e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Error while closing webservice stub", e);
		}
	}

	private void getMoreResult() throws ConnectorException {
		try {
			result = null;
			if (svc.implementPaging()) {
				svc.addFilter("extra", "limit", Integer.toString(page_size));
				svc.addFilter("extra", "offset", Integer.toString(offset));
			}
			Vector<HashMap<String, String>> vresult = svc.getResult();
			if (vresult == null)
				return;
			if (svc.implementPaging() && vresult.size() == page_size)
				offset += page_size;
			else
				offset = -1;
			result = vresult.iterator();
		} catch (RemoteException e) {
			throw new ConnectorException("SCM webservice error", e);
		}
	}
}

class FilterElement {
	public String type = null;
	public String name = null;
	public String value = null;

	public void setType(String type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}
}