package org.rejna.abet.connector;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.tools.ant.BuildException;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.exception.ConnectorException;

public class LdapConnector extends Connector {
	private LdapContext ldapContext = null;
	
	public LdapConnector() {
		addPropertyTranslation("url", Context.PROVIDER_URL);
		addPropertyTranslation("authentication", Context.SECURITY_AUTHENTICATION);
		addPropertyTranslation("user", Context.SECURITY_PRINCIPAL);
		addPropertyTranslation("password", Context.SECURITY_CREDENTIALS);
		addPropertyTranslation("referral", Context.REFERRAL);
	}
	
	public void setUrl(String url) {
		setProperty(Context.PROVIDER_URL, url);
	}
	
	public void setAuthentication(String authentication) {
		setProperty(Context.SECURITY_AUTHENTICATION, authentication);
	}
	
	public void setUser(String user) {
		setProperty(Context.SECURITY_PRINCIPAL, user);
	}
	
	public void setPassword(String password) {
		setProperty(Context.SECURITY_CREDENTIALS, password);
	}
	
	@Override
	public void initConnector() {
		setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		try {
			ldapContext = new InitialLdapContext(getProperties(), null);
		} catch (NamingException e) {
			throw new BuildException("LDAP connection error", e);
		}
	}
	
	public LdapContext getConnection() throws ConfigException, ConnectorException {
		if (ldapContext == null)
			authenticateConnector();
		return ldapContext;		
	}
}