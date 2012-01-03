package org.rejna.abet.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.StringUtils;
import org.rejna.abet.DataEntry;
import org.rejna.abet.common.TransitElement;
import org.rejna.abet.connector.ConnectorSource;
import org.rejna.abet.exception.ConcurrentAccessException;

public class Log extends ConnectorSource implements BuildLogger,
		TransitElement {
	// private int messageOutputLevel = 0;
	private PrintStream outputPrintStream = System.out;
	private PrintStream errorPrintStream = null; //System.err;
	// private boolean emacsMode = false;
	private long startTime;
	private boolean buildWarning = false;
	private Boolean finished = false;
	private HashMap<LogClass, Level> logThreshold = new HashMap<LogClass, Level>();
	private HashMap<Target, Long> profileData = new HashMap<Target, Long>();
	private StringBuffer buffer = new StringBuffer();
	private String LINE_SEP = System.getProperty("line.separator");
	private static Log instance = null;

	public Log() {
		if (instance == null) {
			instance = this;
			BasicConfigurator.configure();
		}
	}

	@Override
	public void execute() {
		for (LogClass lc : LogClass.values())
			instance.logThreshold.put(lc,
					Level.toLevel(getProperty(lc.name()), Level.WARN));
		if (!getProject().getBuildListeners().contains(instance)) {
			getProject().addBuildListener(instance);
			buildStarted(null);
		}
	}

	public static Log get() {
		return instance;
	}

	public void setXmlconfig(String filename) {
		try {
			DOMConfigurator.configure(filename);
		} catch (Exception e) {
			log(LogClass.config, Level.WARN, getOwningTarget(),
					"Log4j configuration error (" + e.getMessage()
							+ "). Use default parameters");
		}
	}

	public void setPropconfig(String filename) {
		try {
			PropertyConfigurator.configure(filename);
		} catch (Exception e) {
			log(LogClass.config, Level.WARN, getOwningTarget(),
					"Log4j configuration error (" + e.getMessage()
							+ "). Use default parameters");
		}
	}

	public void log(LogClass logClass, Level level, Target target,
			String message) {
		log(logClass, level, target, message, null);
	}

	public void log(LogClass logClass, Level level, Target target,
			String message, Throwable throwable) {
		String targetName = null;
		if (target != null)
			targetName = target.getName();
		log(logClass, level, targetName, message, throwable);
	}

	public void log(LogClass logClass, Level level, String target,
			String message) {
		log(logClass, level, target, message, null);
	}

	public void log(LogClass logClass, Level level, String target,
			String message, Throwable throwable) {
		if (this != instance)
			instance.log(logClass, level, target, message, throwable);
		else {
			Level threshold = logThreshold.get(logClass);
			if (threshold != null && level.isGreaterOrEqual(threshold))
				buildWarning = true;
			if (message != null) {
				String prefix = "";
				if (target != null && target.length() > 0)
					prefix = String.format("[%1$-15s] ", target);
				try {
					BufferedReader r = new BufferedReader(new StringReader(
							message));
					String line = r.readLine();
					while (line != null) {
						logClass.log(level, prefix + line);
						buffer.append(prefix + line).append(LINE_SEP);
						line = r.readLine();
					}
				} catch (IOException e) {
					logClass.log(level, prefix + message);
					buffer.append(prefix + message).append(LINE_SEP);
				}
			}
			
			if (throwable != null) {
				StringWriter sw = new StringWriter();
				throwable.printStackTrace(new PrintWriter(sw, true));
				log(logClass, level, target, sw.toString(), null);
			}
		}
	}

	public PrintStream getOutputPrintStream() {
		return outputPrintStream;
	}

	public PrintStream getErrorPrintStream() {
		return errorPrintStream;
	}

	@Override
	public void setMessageOutputLevel(int messageOutputLevel) {
		// instance.messageOutputLevel = messageOutputLevel;
	}

	@Override
	public void setOutputPrintStream(PrintStream outputPrintStream) {
		instance.outputPrintStream = new PrintStream(outputPrintStream, true);
	}

	@Override
	public void setEmacsMode(boolean emacsMode) {
		// this.emacsMode = emacsMode;
	}

	@Override
	public void setErrorPrintStream(PrintStream errorPrintStream) {
		instance.errorPrintStream = new PrintStream(errorPrintStream, true);
	}

	@Override
	public void buildStarted(BuildEvent event) {
		instance.startTime = System.currentTimeMillis();
	}

	@Override
	public void buildFinished(BuildEvent event) {
		if (this != instance)
			instance.buildFinished(event);
		else {
			Throwable error = event.getException();
			StringBuffer message = new StringBuffer();
			if (error == null) {
				message.append(StringUtils.LINE_SEP);
				message.append("BUILD SUCCESSFUL");
			} else {
				buildWarning = true;
				message.append(StringUtils.LINE_SEP);
				message.append("BUILD FAILED");
				message.append(StringUtils.LINE_SEP);
			}
			message.append(StringUtils.LINE_SEP);
			message.append("Total time: ");
			message.append(DateUtils.formatElapsedTime(System
					.currentTimeMillis() - this.startTime));
			message.append(LINE_SEP);
			String msg = event.getMessage();
			if (msg != null)
				message.append(msg);
			log(LogClass.main, (error == null ? Level.INFO : Level.ERROR),
					event.getTarget(), message.toString(), error);

			if (buildWarning)
				errorPrintStream.println(buffer);
		}
	}

	@Override
	public void targetStarted(BuildEvent event) {
		instance.profileData.put(event.getTarget(), System.currentTimeMillis());
		log(LogClass.main, Level.INFO, event.getTarget().getName(), "started", null);		
	}

	@Override
	public void targetFinished(BuildEvent event) {
		Target target = event.getTarget();
		long start = instance.profileData.remove(target);
		log(LogClass.main, Level.INFO, target.getName(), "finished in "
				+ DateUtils.formatElapsedTime(System.currentTimeMillis() - start), null);
	}

	@Override
	public void taskStarted(BuildEvent paramBuildEvent) {
	}

	@Override
	public void taskFinished(BuildEvent paramBuildEvent) {
	}

	private Level priorityToLevel(int priority) {
		switch (priority) {
		case 0:
			return Level.ERROR;
		case 1:
			return Level.WARN;
		case 2:
			return Level.INFO;
		case 3:
			return Level.DEBUG;
		case 4:
			return Level.TRACE;
		default:
			log(LogClass.main, Level.ERROR, getOwningTarget(),
					"Unknwon priority (" + priority + ").");
			return Level.ERROR;
		}
	}

	@Override
	public void messageLogged(BuildEvent event) {
		// Target target = event.getTarget();
		// log(LogClass.main, Level.INFO, (target == null ? null :
		// target.getName()), event.getMessage(), event.getException());
		// if (event == null)
		// if (event.)
		log(LogClass.main, priorityToLevel(event.getPriority()),
				event.getTarget(), event.getMessage(), event.getException());
	}

	@Override
	public boolean hasMoreElements() {
		if (this != instance)
			return instance.hasMoreElements();
		else {
			synchronized (finished) {
				while (!finished)
					try {
						finished.wait();
					} catch (InterruptedException e) {
					}
			}
			return buildWarning;
		}
	}

	@Override
	public DataEntry nextElement() {
		if (this != instance)
			return instance.nextElement();
		else {
			synchronized (finished) {
				while (!finished)
					try {
						finished.wait();
					} catch (InterruptedException e) {
					}
			}
			if (!buildWarning)
				throw new NoSuchElementException();
			DataEntry entry = new DataEntry();
			try {
				entry.addAttribute();
				entry.addValue(buffer);
			} catch (ConcurrentAccessException e) {
				// impossible fail
			}
			return entry;
		}
	}

	@Override
	public void close() {
	}
}
