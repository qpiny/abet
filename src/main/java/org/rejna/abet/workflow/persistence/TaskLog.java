package org.rejna.abet.workflow.persistence;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.format.annotation.DateTimeFormat;
import org.rejna.abet.workflow.TaskStatus;
import javax.persistence.Enumerated;

@RooJavaBean
@RooToString
@RooEntity
public class TaskLog {

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date startAt;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date stopAt;

    private String taskName;

    @Enumerated
    private TaskStatus taskStatus;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] taskOutput;
    
    public TaskLog(String taskName) {
    	setTaskName(taskName);
    	setStartAt(new Date());
    	setTaskStatus(TaskStatus.RUNNING);
    }
    
    public void addProperty(String name, String value) {
    	new TaskProperties(this, name, value).persist();
    }
}
