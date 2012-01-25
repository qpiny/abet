package org.rejna.abet.workflow;

import org.rejna.abet.workflow.persistence.Lock;

public interface LockManager {
	void releaseLock(Lock lock);
	Lock acquireLock(String lockName, LockType lockType, long duration);
}
