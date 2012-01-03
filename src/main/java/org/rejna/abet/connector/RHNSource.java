package org.rejna.abet.connector;

import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.connector.rhn.RHAuth;
import org.rejna.abet.exception.ConnectorException;

public class RHNSource extends ConnectorSource {
	private DataEntry data = null;
	private Vector<RHNQuery> queries = new Vector<RHNQuery>();
	private boolean addheaders = false;

	public void addQuery(RHNQuery query) {
		queries.add(query);
	}
		
	public void setAddheaders(boolean addheaders) {
		this.addheaders = addheaders;
	}
	
	@Override
	public void execute() {
		try {
			DataEntry data = null;
			RHAuth auth = ((RHNConnector) getConnector()).getAuth();
			for (RHNQuery query : queries) {
				data = query.getData(auth, data);
			}
			this.data = data;
			if (!addheaders) {
				this.data.removeFirstElement();
			}
		} catch (ConnectorException e) {
			throw new BuildException("Connector error", e);
		}
	}

	@Override
	public boolean hasMoreElements() {
		return data != null;
	}

	@Override
	public DataEntry nextElement() {
		DataEntry d = data;
		data = null;
		return d;
	}

	@Override
	public void close() {
	}

}
