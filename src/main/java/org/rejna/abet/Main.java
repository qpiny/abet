package org.rejna.abet;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemManager;
import org.hibernate.cfg.Configuration;
import org.rejna.abet.persistence.DbDateCheck;
import org.rejna.abet.persistence.Lock;
import org.rejna.abet.workflow.AntTargetExecution;
import org.rejna.abet.workflow.LockManager;
import org.rejna.abet.workflow.LockType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);
	private ConfigurableApplicationContext ctx;
	
	public static void main(String[] args) {
		new Main("workflow1.bpmn");
	}
	
	public Main(String workflowFileName) {
		ctx = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext.xml");
		
		//logger.info("Time delta = " + ctx.getBean("DbDateCheck", DbDateCheck.class).getDelta());
		
		//startWorkflow("sample.xml", "workflow1.bpmn", "org.rejna.workflow.workflow1");
		LockManager lockManager = new LockManager();
		Lock lock1 = new Lock("test", LockType.READ, 10000);
		//Lock lock2 = new Lock("test2", LockType.READ, 10000);
		lockManager.acquireLock(lock1);
		lockManager.acquireLock(lock1);
		//lockManager.acquireLock(lock2);
		//lockManager.acquireLock(lock2);
		// do something
		//lockManager.releaseLock(lock2);
		lockManager.releaseLock(lock1);
		
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
