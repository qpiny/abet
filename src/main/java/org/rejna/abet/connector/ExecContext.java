package org.rejna.abet.connector;

import java.io.Reader;

public interface ExecContext {
	public Reader getReader();
	public void close();
}
