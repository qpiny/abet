package org.rejna.abet;

import java.io.File;

import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemManager;
import org.rejna.abet.persistence.Lock;
import org.rejna.abet.workflow.AntTargetExecution;
import org.rejna.abet.workflow.LockManager;
import org.rejna.abet.workflow.LockType;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext.xml");
		new Main("workflow1.bpmn");
	}
	
	public Main(String workflowFileName) {
		//startWorkflow("sample.xml", "workflow1.bpmn", "org.rejna.workflow.workflow1");
		LockManager lockManager = new LockManager();
		Lock lock = new Lock("test", LockType.WRITE, 10000);
		lockManager.acquireLock(lock);
		// do something
		lockManager.releaseLock(lock);
	}

	
	
	public void loadWorkflow(KnowledgeBuilder kbuilder, String workflowFileName) {
		kbuilder.add(ResourceFactory.newClassPathResource(workflowFileName),
				ResourceType.BPMN2);
	}
	
	public void registerAntFile(WorkItemManager wim, String antFileName) {
		wim.registerWorkItemHandler("Ant", new AntTargetExecution(new File(antFileName)));
	}
	
	public void startWorkflow(String antFileName, String workflowFileName, String workflowName) {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder();
		
		loadWorkflow(kbuilder, workflowFileName);
		
		StatefulKnowledgeSession ksession = kbuilder.newKnowledgeBase().newStatefulKnowledgeSession();
		
		registerAntFile(ksession.getWorkItemManager(), antFileName);
		//ksession.getWorkItemManager().registerWorkItemHandler("LockManager",
		//		new LockManager());
		// start a new process instance
		ksession.startProcess(workflowName);
	}
}
