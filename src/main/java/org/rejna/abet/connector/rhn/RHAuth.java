package org.rejna.abet.connector.rhn;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class RHAuth {
	private String session_key = null;
	private String url;
	private String username;
	private String password;
	private XmlRpcClient client;
	
	public RHAuth(String url, String username, String password) throws MalformedURLException, XmlRpcException {
		this.url = url;
		this.username = username;
		this.password = password;
		relogin();
	}
	
	public void relogin() throws MalformedURLException, XmlRpcException {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(url));
		client = new XmlRpcClient();
		client.setConfig(config);

		session_key = (String) client.execute("auth.login", new String[] { username, password });
	}
	
	public int logout() throws XmlRpcException {
		return (Integer) client.execute("auth.logout", new String[] { session_key });
	}
	
	public Object execute(Object ... args) throws XmlRpcException {
		String method = (String) args[0];
		args[0] = session_key;
		return client.execute(method, args);
	}	
}
