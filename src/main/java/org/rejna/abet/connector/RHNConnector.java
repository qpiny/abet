package org.rejna.abet.connector;

import java.net.MalformedURLException;

import org.apache.tools.ant.BuildException;
import org.apache.xmlrpc.XmlRpcException;
import org.rejna.abet.connector.rhn.RHAuth;

public class RHNConnector extends Connector {
	private RHAuth auth = null;
	private String user = null;
	private String password = null;
	private String url = null;
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public RHAuth getAuth() {
		try {
			auth = new RHAuth(url, user, password);
		} catch (XmlRpcException e) {
			throw new BuildException("XML-RPC initialisation fail", e);
		} catch (MalformedURLException e) {
			throw new BuildException("Invalid URL", e);
		}
		return auth;
	}
	
	@Override
	public void initConnector() {
	}
	/*
	@Override
	public void close() {
		try {
			auth.logout();
		} catch (XmlRpcException e) {
			Log.connector.error("Error while closing webservice stub", e);
		} finally {
			auth = null;
		}
	}
	*/

}