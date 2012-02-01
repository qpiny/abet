package org.rejna.abet.workflow;

import java.util.Date;
import java.util.List;
import java.util.Random;

//import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.rejna.abet.workflow.persistence.Lock;

public class LockManagerImpl implements LockManager {
	private static final Logger logger = Logger.getLogger(LockManagerImpl.class);
	@Min(0)
	@Max(1)
	private double waitFactor = 0.5;

	@Min(0)
	private long pollIntervalMin = 1000; // 1 sec
	
	@Min(0)
	private long pollIntervalMax = 30000; // 30 sec
	
	private long id;
	
	private AbstractPlatformTransactionManager txManager;
	
	public LockManagerImpl() {
		id = new Random().nextLong();
	}

	public void setWaitFactor(double waitFactor) {
		this.waitFactor = waitFactor;
	}

	public void setPollIntervalMin(long pollIntervalMin) {
		this.pollIntervalMin = pollIntervalMin;
	}

	public void setPollIntervalMax(long pollIntervalMax) {
		this.pollIntervalMax = pollIntervalMax;
	}
	
	public void setTransactionManager(AbstractPlatformTransactionManager transactionManager) {
		this.txManager = transactionManager;
	}

	@Transactional(rollbackFor = { Exception.class })
	public synchronized void releaseLock(Lock lock) {
		logger.info("releaseLock(LockName:" + lock.getLockName() + ", LockType: "
				+ lock.getLockType() + ")");
		lock.remove();
		notifyAll();
	}
	
	private List<Lock> getReadLock(String lockName) {
		return Lock.entityManager().createQuery("SELECT l FROM Lock l WHERE l.lockName = ?1 AND l.lockType = ?2", Lock.class)
				.setParameter(1, lockName)
				.setParameter(2, LockType.WRITE)
				.setLockMode(LockModeType.PESSIMISTIC_READ)
				.getResultList();
	}
	
	private List<Lock> getWriteLock(String lockName) {
		return Lock.entityManager().createQuery("SELECT l FROM Lock l WHERE l.lockName = ?1", Lock.class)
				.setParameter(1, lockName)
				.setLockMode(LockModeType.PESSIMISTIC_READ)
				.getResultList();
	}

	public Lock acquireLock(String lockName, LockType lockType, long duration) {
		boolean thisLockManager = false;
		long now = 0;
		long maxWait = 0;
		Lock lock = null;
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("LockTransaction");
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		TransactionStatus txStatus = null;
		while (lock == null) {
			thisLockManager = false;
			maxWait = 0;
			now = new Date().getTime();
			synchronized(this) {
				try {
					txStatus = txManager.getTransaction(def);
					List<Lock> locks = null;
					switch(lockType) {
					case READ:
						locks = getReadLock(lockName);
						break;
					case WRITE:
						locks = getWriteLock(lockName);
						break;
					}
					for (Lock l : locks) {
						long wait = l.getExpire().getTime() - now;
						if (wait < maxWait)
							continue;
						maxWait = wait;
						if (l.getLockManagerId() == id)
							thisLockManager = true;
					}
					if (maxWait == 0) {
						lock = new Lock(lockName, lockType, duration, id);
						lock.persist();
					}
					txManager.commit(txStatus);
				} catch (Exception e) {
					txManager.rollback(txStatus);
					logger.warn("acquireLock fails", e);
				}
	
				try {
					if (lock == null) {
						if (!thisLockManager) {
							maxWait = (long) (maxWait * waitFactor);
							if (maxWait > pollIntervalMax)
								maxWait = pollIntervalMax;
							else if (maxWait < pollIntervalMin)
								maxWait = pollIntervalMin;
						}
						wait(maxWait);
					}
				} catch (InterruptedException e) {
				}
			}
		}
		return lock;
	}
	
}
