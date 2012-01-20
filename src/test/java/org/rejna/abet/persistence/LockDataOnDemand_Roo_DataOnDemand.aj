// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.rejna.abet.persistence;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.rejna.abet.persistence.Lock;
import org.rejna.abet.workflow.LockType;
import org.springframework.stereotype.Component;

privileged aspect LockDataOnDemand_Roo_DataOnDemand {
    
    declare @type: LockDataOnDemand: @Component;
    
    private Random LockDataOnDemand.rnd = new SecureRandom();
    
    private List<Lock> LockDataOnDemand.data;
    
    public Lock LockDataOnDemand.getNewTransientLock(int index) {
        Lock obj = new Lock();
        setCount(obj, index);
        setExpire(obj, index);
        setLockType(obj, index);
        return obj;
    }
    
    public void LockDataOnDemand.setCount(Lock obj, int index) {
        int count = 0;
        obj.setCount(count);
    }
    
    public void LockDataOnDemand.setExpire(Lock obj, int index) {
        Date expire = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setExpire(expire);
    }
    
    public void LockDataOnDemand.setLockType(Lock obj, int index) {
        LockType lockType = null;
        obj.setLockType(lockType);
    }
    
    public Lock LockDataOnDemand.getSpecificLock(int index) {
        init();
        if (index < 0) index = 0;
        if (index > (data.size() - 1)) index = data.size() - 1;
        Lock obj = data.get(index);
        return Lock.findLock(obj.getLockName());
    }
    
    public Lock LockDataOnDemand.getRandomLock() {
        init();
        Lock obj = data.get(rnd.nextInt(data.size()));
        return Lock.findLock(obj.getLockName());
    }
    
    public boolean LockDataOnDemand.modifyLock(Lock obj) {
        return false;
    }
    
    public void LockDataOnDemand.init() {
        data = Lock.findLockEntries(0, 10);
        if (data == null) throw new IllegalStateException("Find entries implementation for 'Lock' illegally returned null");
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<org.rejna.abet.persistence.Lock>();
        for (int i = 0; i < 10; i++) {
            Lock obj = getNewTransientLock(i);
            try {
                obj.persist();
            } catch (ConstraintViolationException e) {
                StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> it = e.getConstraintViolations().iterator(); it.hasNext();) {
                    ConstraintViolation<?> cv = it.next();
                    msg.append("[").append(cv.getConstraintDescriptor()).append(":").append(cv.getMessage()).append("=").append(cv.getInvalidValue()).append("]");
                }
                throw new RuntimeException(msg.toString(), e);
            }
            obj.flush();
            data.add(obj);
        }
    }
    
}