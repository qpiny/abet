package org.rejna.abet.log;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.LogLog;

public class AntConsoleAppender extends WriterAppender {
	private String target = "system.out";

	public AntConsoleAppender() {
	}

	public AntConsoleAppender(Layout layout) {
		this(layout, "System.out");
	}

	public AntConsoleAppender(Layout layout, String target) {
		setLayout(layout);
		activateOptions();
	}

	@Override
	public void activateOptions() {
		if ("system.err".equalsIgnoreCase(target))
			setWriter(createWriter(Log.get().getErrorPrintStream()));
		else {
			if (!"system.out".equalsIgnoreCase(target)) {
				LogLog.warn("[" + target
						+ "] should be System.out or System.err.");
				LogLog.warn("Using System.out by default.");
			}
			setWriter(createWriter(Log.get().getOutputPrintStream()));
		}
		super.activateOptions();
	}

	public void setTarget(String target) {
		this.target = target;
		activateOptions();
	}

}
