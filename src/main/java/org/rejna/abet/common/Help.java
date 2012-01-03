package org.rejna.abet.common;

import java.util.Hashtable;

import org.apache.log4j.Level;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Target;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class Help extends Task {

	@Override
	public void execute() {
		Log log = Log.get();
		
		log.log(LogClass.main, Level.INFO, "", getProject().getDescription());
		@SuppressWarnings("unchecked")
		Hashtable<String,Target> targets = getProject().getTargets();
		for (Target target: targets.values()) {
			if (target.getName().indexOf('.') != -1)
				continue;
			String description = target.getDescription();
			if (description == null)
				description = target.getName();
			log.log(LogClass.main, Level.INFO, target, description);
		}
		//getProject().
	}
}
