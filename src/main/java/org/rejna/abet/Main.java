package org.rejna.abet;

import java.io.File;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.types.resources.JavaResource;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemManager;
import org.rejna.abet.workflow.LockManager;
import org.rejna.abet.workflow.LockType;
import org.rejna.abet.workflow.persistence.Lock;
import org.rejna.abet.workflow.works.AntTargetAction;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.Resource;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);
	private static ConfigurableApplicationContext ctx;
	
	public static void main(String[] args) {
		new Main("workflow1.bpmn");
	}
	
	public Main(String workflowFileName) {
		ctx = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/applicationContext.xml");
		//ctx.getResources(locationPattern)
		try {
			startWorkflow("task_definitions.xml", "workflow.bpmn", "org.rejna.workflow.test1");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		/*
		//logger.info("Time delta = " + ctx.getBean("DbDateCheck", DbDateCheck.class).getDelta());
		
		//startWorkflow("sample.xml", "workflow1.bpmn", "org.rejna.workflow.workflow1");
		LockManager lockManager = ctx.getBean("LockManager", LockManager.class);
		
		Lock lock1 = lockManager.acquireLock("test", LockType.READ, 60000);
		Lock lock2 = lockManager.acquireLock("test", LockType.READ, 60000);
		//Lock lock2 = new Lock("test2", LockType.READ, 10000);
		lockManager.releaseLock(lock1);
		lockManager.releaseLock(lock2);
		//lockManager.acquireLock(lock2);
		//lockManager.acquireLock(lock2);
		// do something
		//lockManager.releaseLock(lock2);
		//lockManager.releaseLock(lock1);
		*/
	}

	
	
	public void loadWorkflow(KnowledgeBuilder kbuilder, String workflowFileName) {
		kbuilder.add(ResourceFactory.newClassPathResource(workflowFileName),
				ResourceType.BPMN2);
	}
	
	public void registerAntFile(WorkItemManager wim, String antFileName) throws URISyntaxException {
		wim.registerWorkItemHandler("Ant", new AntTargetAction(antFileName));
		wim.registerWorkItemHandler("Lock", ctx.getBean("LockAction", WorkItemHandler.class));
		wim.registerWorkItemHandler("Unlock", ctx.getBean("UnlockAction", WorkItemHandler.class));
	}
	
	public void startWorkflow(String antFileName, String workflowFileName, String workflowName) throws URISyntaxException {
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
