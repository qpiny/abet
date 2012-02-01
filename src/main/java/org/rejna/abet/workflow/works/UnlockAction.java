package org.rejna.abet.workflow.works;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.rejna.abet.workflow.LockManager;
import org.rejna.abet.workflow.persistence.Lock;

public class UnlockAction implements WorkItemHandler {

	private LockManager lockManager;
	
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	
	@Override
	public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
		Long lockId = (Long) wi.getParameter("lock");
		if (lockId == null)
			throw new RuntimeException("lock attribute of UnlockAction is missing");
		Lock lock = Lock.findLock(lockId);
		if (lock == null)
			throw new RuntimeException("lock not found");
		lockManager.releaseLock(lock);
	}

	@Override
	public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
	}

}
