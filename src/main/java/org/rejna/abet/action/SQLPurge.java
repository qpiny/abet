package org.rejna.abet.action;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.connector.RSQLDestination;
import org.rejna.abet.exception.ConfigException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class SQLPurge extends RSQLDestination {
	
	@Override
	public void execute() {
		super.execute();
		try {
			purge();
		} catch (ConfigException e) {
			throw new BuildException(e);
		}
		Log.get().log(LogClass.connector, Level.INFO, getOwningTarget(), printStats());
	}
}
