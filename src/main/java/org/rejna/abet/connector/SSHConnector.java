package org.rejna.abet.connector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.log4j.Level;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.exception.ConnectorException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class SSHConnector extends Connector implements ExecConnector, UserInfo {
	private JSch jsch;
	private Session session;
	private String user = null;
	private String host = null;
	private int port = 22;

	public void setUser(String user) {
		this.user = user;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public void initConnector() throws ConnectorException {
		try {
			session = jsch.getSession(user, host, port);
			session.setUserInfo(this);
			session.connect();
		} catch (JSchException e) {
			throw new ConnectorException("SSH connection error", e);
		}
	}
	
	@Override
	public ExecContext run(String command) throws ConfigException, ConnectorException {
		try {
			if (session == null)
				authenticateConnector();
			final ChannelExec channel = (ChannelExec) session.openChannel("exec");
			channel.setCommand(command);
			channel.setPty(true);

			// channel.setInputStream(null);
			// channel.setOutputStream(null);
			// channel.setErrStream(null);

			channel.connect();

			return new ExecContext() {
				
				@Override
				public Reader getReader() {
					try {
						return new InputStreamReader(channel.getInputStream());
					} catch (IOException e) {
						Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Read of command output error", e);
					}
					return null;
				}

				@Override
				public void close() {
					channel.disconnect();
				}
				
			};

		} catch (JSchException e) {
			throw new ConnectorException("SSH error", e);
		}
	}

	@Override
	public String getPassphrase() {
		return null;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public boolean promptPassphrase(String s) {
		return false;
	}

	@Override
	public boolean promptPassword(String s) {
		return false;
	}

	@Override
	public boolean promptYesNo(String s) {
		return true;
	}

	@Override
	public void showMessage(String s) {
		Log.get().log(LogClass.connector, Level.INFO, getOwningTarget(),"showMessage(" + s + ")");
	}

	public void close() {
		session.disconnect();
	}
}