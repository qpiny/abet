package org.rejna.abet.workflow;

import java.io.File;

import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

public class AntTargetExecution implements WorkItemHandler {
	private Project project = new Project();
	
	public AntTargetExecution(File buildFile) {
		project.addBuildListener(createLogger());
		project.init();
		ProjectHelper.configureProject(project, buildFile);
	}
	
	@Override
	public void abortWorkItem(WorkItem wi, WorkItemManager wim) {
	}

	@Override
	public void executeWorkItem(final WorkItem wi, final WorkItemManager wim) {
		final Target target = (Target)project.getTargets().get(wi.getParameter("TargetName"));
		new Thread(new Runnable() {

			@Override
			public void run() {
				target.performTasks();
				wim.completeWorkItem(wi.getId(), null);
			}
			
		}).start();
	}

	public static void main(String args[]) {
		System.out.println("-->begin");
		final Project project = new Project();
		final File buildFile = new File("sample.xml");

		project.addBuildListener(createLogger());
		// project.setDefaultInputStream(System.in);
		//project.setInputHandler(new DefaultInputHandler());
		project.init();

		/*
		PropertyHelper propertyHelper = (PropertyHelper) PropertyHelper
				.getPropertyHelper(project);
		new ResolvePropertyMap(project, propertyHelper,
				propertyHelper.getExpanders()).resolveAllProperties(new HashMap(),
				null, false);

	
		project.setUserProperty(MagicNames.ANT_FILE,
				buildFile.getAbsolutePath());
		project.setUserProperty(MagicNames.ANT_FILE_TYPE,
				MagicNames.ANT_FILE_TYPE_FILE);
*/
		ProjectHelper.configureProject(project, buildFile);
		Target target = (Target)project.getTargets().get("start");
		target.performTasks();
		//project.executeTarget("start");
		System.out.println("-->end");
	}

	private static BuildListener createLogger() {
		BuildLogger logger = new DefaultLogger();

	    logger.setMessageOutputLevel(Project.MSG_INFO);
	    logger.setOutputPrintStream(System.out);
	    logger.setErrorPrintStream(System.err);
	    logger.setEmacsMode(false);
	    return logger;
	}

}
