package org.rejna.abet.workflow.works;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.rejna.abet.workflow.LockManager;
import org.rejna.abet.workflow.LockType;
import org.rejna.abet.workflow.persistence.Lock;

public class LockAction implements WorkItemHandler {
	private final static Logger logger = Logger.getLogger(LockAction.class); 
	private LockManager lockManager;
	
	public void setLockManager(LockManager lockManager) {
		this.lockManager = lockManager;
	}
	
	@Override
	public void executeWorkItem(final WorkItem wi, final WorkItemManager wim) {
		try {
			final String lockName = (String) wi.getParameter("lockName");
			if (lockName == null)
				throw new RuntimeException("lockName attribute of LockAction is missing");
		
		
			final LockType lockType = LockType.valueOf((String) wi.getParameter("lockType"));
			
			long d = 600000;
			try {
				d = Long.parseLong(wi.getParameter("duration").toString());
			} catch (Exception e) {
				logger.warn("Duration is invalid. Use default value");
			}
			final long duration = d;
			
			logger.info("acquireLock " + lockName + " (" + lockType + ") " + duration + "ms");
			new Thread(new Runnable() {

				@Override
				public void run() {
					Lock lock = lockManager.acquireLock(lockName, lockType, duration);
					Map<String, Object> result = new HashMap<String, Object>();
					result.put("lock", lock.getId());
					wim.completeWorkItem(wi.getId(), result);
				}
			}).start();
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("lockType attribute must be either READ of WRITE");
		}
	}

	@Override
	public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
	}

}
