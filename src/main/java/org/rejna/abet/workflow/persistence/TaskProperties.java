package org.rejna.abet.workflow.persistence;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import javax.persistence.ManyToOne;

@RooJavaBean
@RooToString
@RooEntity
public class TaskProperties {

    @ManyToOne
    private TaskLog taskLog;

    private String propName;

    private String propValue;
    
    protected TaskProperties(TaskLog taskLog, String propName, String propValue) {
    	setTaskLog(taskLog);
    	setPropName(propName);
    	setPropValue(propValue);
    }
}
