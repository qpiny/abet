package org.rejna.abet.workflow.persistence;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.LockModeType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.rejna.abet.workflow.LockType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooEntity(table = "Locks")
public class Lock implements Serializable {

	private static final long serialVersionUID = -4471303259620725434L;

	@NotNull
	private String lockName;
	
	@NotNull
	private LockType lockType;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date expire;
	
	private long lockManagerId;

	public Lock(String lockName, LockType lockType, long duration, long lockManagerId) {
		setLockName(lockName);
		setLockType(lockType);
		setExpire(new Date(System.currentTimeMillis() + duration));
		setLockManagerId(lockManagerId);
	}

	@Deprecated
	public void copyFrom(Lock lock) {
		setLockName(lock.getLockName());
		setLockType(lock.getLockType());
		setExpire(lock.getExpire());
	}
	
    public static Lock findLock(String lockName, LockModeType lockMode) {
    	if (lockName == null || lockName.length() == 0) return null;
        return entityManager().find(Lock.class, lockName, lockMode);
    }    
}
