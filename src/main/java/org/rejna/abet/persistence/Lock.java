package org.rejna.abet.persistence;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Date;

import javax.persistence.Id;
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

	@Id
	private String lockName;

	@NotNull
	private LockType lockType;

	@NotNull
	private int count = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(style = "M-")
	private Date expire;

	public Lock(String lockName, LockType lockType, long duration) {
		setLockName(lockName);
		setLockType(lockType);
		setCount(1);
		setExpire(new Date(System.currentTimeMillis() + duration));
	}

	public void inc(long duration) {
		inc(new Date(System.currentTimeMillis() + duration));
	}

	public void inc(Date e) {
		setCount(count + 1);
		if (e.after(getExpire()))
			setExpire(e);
		flush();
	}

	public void dec() {
		if (count <= 0)
			throw new InvalidParameterException(
					"Lock count can't be decremented");
		if (count == 1)
			remove();
		else {
			setCount(count - 1);
			flush();
		}
	}

	public void copyFrom(Lock lock) {
		setLockName(lock.getLockName());
		setLockType(lock.getLockType());
		setCount(lock.getCount());
		setExpire(lock.getExpire());
	}

}
