package org.rejna.abet.connector;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.common.StringContainer;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class LdapSource extends ConnectorSource {
	private int page_size = 1000;
	private String filter = null;
	private String base = null;
	private int scope = SearchControls.SUBTREE_SCOPE;
	//private String[] attributes = null;
	private Vector<String> attributes = new Vector<String>();
	private LdapContext ldapContext = null;
	private NamingEnumeration<SearchResult> results = null;

	public void setPagesize(int page_size) {
		this.page_size = page_size;
	}

	public void addConfiguredFilter(StringContainer filter) {
		this.filter = filter.get();
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void setScope(String scope) {
		if ("onelevel".equals(scope))
			this.scope = SearchControls.ONELEVEL_SCOPE;
		else if ("object".equals(scope))
			this.scope = SearchControls.OBJECT_SCOPE;
		else
			this.scope = SearchControls.SUBTREE_SCOPE;
	}

	public void addConfiguredAttribute(StringContainer attribute) {
		this.attributes.add(attribute.get());
	}

	@Override
	public void execute() {
		try {
			ldapContext = ((LdapConnector) getConnector()).getConnection();
		} catch (Exception e) {
			new BuildException("LdapConnector fails", e);
		}
		if (ldapContext == null)
			throw new BuildException(
					"LdapSource connector must have an LdapConnector");
		try {
			if (page_size > 0)
				ldapContext
						.setRequestControls(new Control[] { new PagedResultsControl(
								page_size, Control.NONCRITICAL) });
			results = ldapContext.search(base, filter, new SearchControls(scope, 0,
					0, attributes.toArray(new String[0]), false, false));
		} catch (NamingException e) {
			throw new BuildException("Ldap search error", e);
		} catch (IOException e) {
			throw new BuildException("Ldap search error", e);
		}
	}

	@Override
	public boolean hasMoreElements() {
		return true;
	}

	@Override
	public DataEntry nextElement() {
		try {
			if (results == null)
				throw new NoSuchElementException();
			
			if (!results.hasMoreElements()) {
				Control[] controls = ldapContext.getResponseControls();
				if (controls != null) {
					byte[] cookie = null;
					for (int i = 0; i < controls.length && cookie == null; i++) {
						if (controls[i] instanceof PagedResultsResponseControl) {
							PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
							cookie = prrc.getCookie();
						}
					}
					if (cookie != null) {
						ldapContext
								.setRequestControls(new Control[] { new PagedResultsControl(
										page_size, cookie, Control.CRITICAL) });
						results.close();
						results = ldapContext.search(base, filter, new SearchControls(scope, 0,
								0, attributes.toArray(new String[0]), false, false));
					}
				}
			}
			
			SearchResult result;
			try {
				result = results.next();
			} catch (NullPointerException e) {
				results.close();
				results = null;
				throw new NoSuchElementException();
			}
			DataEntry entry = new DataEntry();
			for (String attribute_name: attributes) {
				entry.addAttribute();
				if ("dn".equals(attribute_name))
					entry.addValue(result.getNameInNamespace());
				else {
					Attribute attr = result.getAttributes().get(attribute_name);
					if (attr == null)
						entry.addValue(null);
					else {
						NamingEnumeration<?> values = attr.getAll();
						while (values.hasMore())
							entry.addValue(values.next());
						values.close();
					}
				}
			}
			incStat("Fetched");
			return entry;
		} catch (NamingException e) {
			incStat("NamingError");
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Ldap search error", e);
		} catch (ConcurrentAccessException e) {
			incStat("ConcurrentAccessError");
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Ldap search error", e);
		} catch (IOException e) {
			incStat("IOException");
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Ldap search error", e);
		}
		throw new NoSuchElementException();
	}

	@Override
	public void close() {
		try {
			if (results != null)
				results.close();
		} catch (NamingException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Ldap close error", e);
		}

	}

}