package org.rejna.abet.connector;

import org.apache.tools.ant.BuildException;

public class ExecSource extends CSVSourceBase {
	private String command = null;
	private ExecContext context;
	
	public void addText(String command) {
		this.command = command;
	}
	
	@Override
	public void execute() {
		if (command == null)
			throw new BuildException("command is not defined in ExecSource");
		try {
			context = ((ExecConnector)getConnector()).run(command);
		} catch (Exception e) {
			throw new BuildException("ExecConnector initialization fails", e);
		}
		if (context == null)
			throw new BuildException("ExecConnector initialization fails");
		source = context.getReader();
		super.execute();
	}
	
	@Override
	public void close() {
		super.close();
		if (context != null)
			context.close();
	}
}
