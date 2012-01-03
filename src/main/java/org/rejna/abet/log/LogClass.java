package org.rejna.abet.log;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

public enum LogClass {
	main(Logger.getLogger("DataTransit.Main")),
	converter(Logger.getLogger("DataTransit.Converter")),
	config(Logger.getLogger("DataTransit.Config")),
	variable(Logger.getLogger("DataTransit.Variable")),
	connector(Logger.getLogger("DataTransit.Connector"));
	
	private Logger logger;
	
	private LogClass(Logger logger) {
		this.logger = logger;
	}
	
	protected void log(Priority priority, String message) {
		log(priority, message, null);
	}

	protected void log(Priority priority, String message, Throwable throwable) {
		logger.log(priority, message, throwable);
	}
}