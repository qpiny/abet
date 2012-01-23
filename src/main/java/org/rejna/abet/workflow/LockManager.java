package org.rejna.abet.workflow;

import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.apache.log4j.Logger;
import org.rejna.abet.persistence.Lock;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

public class LockManager {
	private static final Logger logger = Logger.getLogger(LockManager.class);
	@Min(0)
	@Max(1)
	private double waitFactor = 0.5;

	@Min(0)
	private long pollInterval = 1000;

	public void setWaitFactor(double waitFactor) {
		this.waitFactor = waitFactor;
	}

	public void setPollInterval(long pollInterval) {
		this.pollInterval = pollInterval;
	}

	public synchronized void acquireLock(Lock lock) {
		logger.info("acquireLock(" + lock + ")");
		Lock l = tryAcquireLock(lock);
		while (l != null) {
			long duration = Math.min(pollInterval, (long) ((l.getExpire()
					.getTime() - System.currentTimeMillis()) * waitFactor));
			if (duration > 0) {
				logger.info("Resource already locked, wait " + duration + " ms");
				try {
					wait(duration);
				} catch (InterruptedException e) {
				}
			}
			logger.info("Retry to acquire lock");
			l = tryAcquireLock(lock);
		}
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = { Exception.class })
	private Lock tryAcquireLock(Lock lock) {
		Lock l = Lock.findLock(lock.getLockName());
		logger.info("lock selected, wait 5s");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		logger.info("continue ...");
		if (l == null)
			lock.persist();
		else if (l.getExpire().before(new Date())) {
			logger.info("Previous lock is expired, remove it");
			l.copyFrom(lock);
			l.flush();
		} else if (lock.getLockType() == LockType.READ
				&& l.getLockType() == LockType.READ)
			l.inc(lock.getExpire());
		else
			return l;
		return null;
	}

	public void releaseLock(Lock lock) {
		releaseLock(lock.getLockName(), lock.getLockType());
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = { Exception.class })
	public synchronized void releaseLock(String lockName, LockType lockType) {
		logger.info("releaseLock(LockName:" + lockName + ", LockType: "
				+ lockType + ")");
		Lock l = Lock.findLock(lockName);
		if (l == null || l.getLockType() != lockType)
			logger.warn("Trying to release non-existant lock (" + lockName
					+ ", " + lockType + ")");
		else {
			l.dec();
			notifyAll();
		}
	}
}
