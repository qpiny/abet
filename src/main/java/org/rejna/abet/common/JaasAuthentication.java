package org.rejna.abet.common;

import java.security.PrivilegedAction;
import java.security.Security;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.rejna.abet.connector.Connector;

public class JaasAuthentication extends Task implements Authenticator {
	private Vector<Task> tasks = new Vector<Task>();
	private String gssmanager = "";
	private String context = null;
	private String jaasconfig = "jaas.conf";
	private String krb5config = "krb5.conf";

	public void setGssmanager(String gssmanager) {
		this.gssmanager = gssmanager;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public void setJaasconfig(String jaasconfig) {
		this.jaasconfig = jaasconfig;
	}

	public void setkrb5config(String krb5config) {
		this.krb5config = krb5config;
	}
	
	public void addConfigured(Task inner_task) {
		tasks.add(inner_task);
	}
	
	public Exception authenticate(final Task task) {
		try {
			System.setProperty("java.security.krb5.conf", krb5config);
			System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
			System.setProperty("java.security.auth.login.config", jaasconfig);
			Security.setProperty("org.ietf.jgss.GSSManager", gssmanager);
	
			LoginContext lc = new LoginContext(context);
			lc.login();
			Exception e = Subject.doAs(lc.getSubject(),
					new PrivilegedAction<Exception>() {
						@Override
						public Exception run() {
							try {
								if (task instanceof Connector)
									((Connector) task).initConnector();
								else
									task.execute();}
							catch (Exception e) { return e; }
							return null;
						}
					});
			return e;
		} catch (Exception e) {
			return e;
		}
	}

	@Override
	public void execute() throws BuildException {
		for (Task t : tasks) {
			if (t instanceof Connector)
				((Connector) t).setConfigurator(this);
			else {
				Exception e = authenticate(t);
				if (e != null)
					throw new BuildException(e);
			}
		}
	}

}
