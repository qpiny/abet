package org.rejna.abet.common;

import org.apache.tools.ant.Task;

public interface Authenticator {
	public Exception authenticate(Task task);
}
