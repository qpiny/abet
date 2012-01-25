// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.rejna.abet.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.rejna.abet.persistence.LockDataOnDemand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect LockIntegrationTest_Roo_IntegrationTest {
    
    declare @type: LockIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: LockIntegrationTest: @ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext.xml");
    
    declare @type: LockIntegrationTest: @Transactional;
    
    @Autowired
    private LockDataOnDemand LockIntegrationTest.dod;
    
    @Test
    public void LockIntegrationTest.testCountLocks() {
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to initialize correctly", dod.getRandomLock());
        long count = org.rejna.abet.persistence.Lock.countLocks();
        org.junit.Assert.assertTrue("Counter for 'Lock' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void LockIntegrationTest.testFindLock() {
        org.rejna.abet.persistence.Lock obj = dod.getRandomLock();
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to initialize correctly", obj);
        java.lang.Long id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to provide an identifier", id);
        obj = org.rejna.abet.persistence.Lock.findLock(id);
        org.junit.Assert.assertNotNull("Find method for 'Lock' illegally returned null for id '" + id + "'", obj);
        org.junit.Assert.assertEquals("Find method for 'Lock' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void LockIntegrationTest.testFindAllLocks() {
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to initialize correctly", dod.getRandomLock());
        long count = org.rejna.abet.persistence.Lock.countLocks();
        org.junit.Assert.assertTrue("Too expensive to perform a find all test for 'Lock', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        java.util.List<org.rejna.abet.persistence.Lock> result = org.rejna.abet.persistence.Lock.findAllLocks();
        org.junit.Assert.assertNotNull("Find all method for 'Lock' illegally returned null", result);
        org.junit.Assert.assertTrue("Find all method for 'Lock' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void LockIntegrationTest.testFindLockEntries() {
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to initialize correctly", dod.getRandomLock());
        long count = org.rejna.abet.persistence.Lock.countLocks();
        if (count > 20) count = 20;
        java.util.List<org.rejna.abet.persistence.Lock> result = org.rejna.abet.persistence.Lock.findLockEntries(0, (int) count);
        org.junit.Assert.assertNotNull("Find entries method for 'Lock' illegally returned null", result);
        org.junit.Assert.assertEquals("Find entries method for 'Lock' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void LockIntegrationTest.testFlush() {
        org.rejna.abet.persistence.Lock obj = dod.getRandomLock();
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to initialize correctly", obj);
        java.lang.Long id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to provide an identifier", id);
        obj = org.rejna.abet.persistence.Lock.findLock(id);
        org.junit.Assert.assertNotNull("Find method for 'Lock' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyLock(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        obj.flush();
        org.junit.Assert.assertTrue("Version for 'Lock' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void LockIntegrationTest.testMerge() {
        org.rejna.abet.persistence.Lock obj = dod.getRandomLock();
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to initialize correctly", obj);
        java.lang.Long id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to provide an identifier", id);
        obj = org.rejna.abet.persistence.Lock.findLock(id);
        boolean modified =  dod.modifyLock(obj);
        java.lang.Integer currentVersion = obj.getVersion();
        org.rejna.abet.persistence.Lock merged =  obj.merge();
        obj.flush();
        org.junit.Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        org.junit.Assert.assertTrue("Version for 'Lock' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
    }
    
    @Test
    public void LockIntegrationTest.testPersist() {
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to initialize correctly", dod.getRandomLock());
        org.rejna.abet.persistence.Lock obj = dod.getNewTransientLock(Integer.MAX_VALUE);
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to provide a new transient entity", obj);
        org.junit.Assert.assertNull("Expected 'Lock' identifier to be null", obj.getId());
        obj.persist();
        obj.flush();
        org.junit.Assert.assertNotNull("Expected 'Lock' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void LockIntegrationTest.testRemove() {
        org.rejna.abet.persistence.Lock obj = dod.getRandomLock();
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to initialize correctly", obj);
        java.lang.Long id = obj.getId();
        org.junit.Assert.assertNotNull("Data on demand for 'Lock' failed to provide an identifier", id);
        obj = org.rejna.abet.persistence.Lock.findLock(id);
        obj.remove();
        obj.flush();
        org.junit.Assert.assertNull("Failed to remove 'Lock' with identifier '" + id + "'", org.rejna.abet.persistence.Lock.findLock(id));
    }
    
}
