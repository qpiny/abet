package org.rejna.abet.workflow.works;

import java.util.HashMap;
import java.util.Map;

import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.rejna.abet.workflow.LockManager;
import org.rejna.abet.workflow.LockType;
import org.rejna.abet.workflow.persistence.Lock;

public class LockAction implements WorkItemHandler {

	private LockManager lockManager;
	
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	
	@Override
	public void executeWorkItem(WorkItem wi, WorkItemManager wim) {
		String lockName = (String) wi.getParameter("lockName");
		if (lockName == null)
			throw new RuntimeException("lockName attribute of LockAction is missing");
		Object lt = wi.getParameter("lockType");
		if (lt == null)
			throw new RuntimeException("lockType attribute of LockAction is missing");
		try {
			LockType lockType = LockType.valueOf((String) lt);
			long duration = Long.parseLong(wi.getParameter("duration").toString());
			Lock lock = lockManager.acquireLock(lockName.toString(), lockType, duration);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("lock", lock.getId());
			wim.completeWorkItem(wi.getId(), result);
		} catch (NumberFormatException e) {
			throw new RuntimeException("duration attribute must be a valid number");
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("lockType attribute must be either READ of WRITE");
		}
	}

	@Override
	public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
	}

}
