package org.rejna.abet.connector;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.tools.ant.BuildException;

public class LocalConnector extends Connector implements ExecConnector {

	@Override
	public ExecContext run(String command) {
		try {
			final Process process = Runtime.getRuntime().exec(command);
			return new ExecContext() {

				@Override
				public Reader getReader() {
					return new InputStreamReader(process.getInputStream());
				}

				@Override
				public void close() {
					process.destroy();
					
				}
			};

		} catch (IOException e) {
			throw new BuildException("Execution fail", e);
		}
	}
	
	@Override
	public void initConnector() {
	}
}