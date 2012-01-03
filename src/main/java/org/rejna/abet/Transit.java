package org.rejna.abet;

import java.lang.management.ManagementFactory;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;
import org.rejna.abet.common.TransitElement;
import org.rejna.abet.connector.ConnectorDestination;
import org.rejna.abet.connector.ConnectorSource;
import org.rejna.abet.connector.TransitThread;
import org.rejna.abet.converter.Converter;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class Transit extends Task implements TaskContainer {
	private Vector<TransitElement> transit_elements = new Vector<TransitElement>();
	private ConnectorDestination destination = null;
	private boolean disabled = false;	
	private Boolean usethread = null;
	private Boolean usembean = null;
	private MBeanServer mbeanServer;
	
	private static WeakHashMap<Target, Integer> transitCounter = new WeakHashMap<Target, Integer>();
	
	public void setUsethread(boolean usethread) {
		this.usethread = new Boolean(usethread);
	}
	
	public void setUsembean(boolean usembean) {
		this.usembean = usembean;
	}
	
	private void addThread(TransitElement source) {
		TransitThread thread = new TransitThread();
		thread.setSource(source);
		transit_elements.add(thread);
	}
	
	@Override
	public void addTask(Task task) {
		if (task instanceof UnknownElement) {
			task.maybeConfigure();
			task = ((UnknownElement) task).getTask();
		}
		if (usethread == null) {
			try {
				usethread = new Boolean(getProject().getProperty("usethread"));
			} catch (NullPointerException e) {
				usethread = false;
			}
		}
		if (task instanceof ConnectorSource) {
			if (!transit_elements.isEmpty())
				throw new BuildException("Only one source is permitted");
			transit_elements.add((TransitElement)task);
			if (usethread)
				addThread((TransitElement)task);
		}
		else if (task instanceof Converter) {
			Converter converter = (Converter)task;
			if (transit_elements.isEmpty())
				throw new BuildException("Transit must begin with source element");
			else
				converter.setSource(transit_elements.lastElement());
			transit_elements.add(converter);
		}
		else if (task instanceof ConnectorDestination) {
			if (usethread)
				addThread(transit_elements.lastElement());
			destination = (ConnectorDestination)task;
			destination.setSource(transit_elements.lastElement());
		}
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public TransitElement getSource() {
		return transit_elements.lastElement();
	}
	
	@Override
	public void execute() {
		Log log = Log.get();
		if (disabled) {
			log.log(LogClass.config, Level.INFO, getOwningTarget(), "task is disabled");
			return;
		}
		if (usembean == null) {
			try {
				usembean = new Boolean(getProject().getProperty("usembean"));
			} catch (NullPointerException e) {
				usembean = false;
			}
		}
		Target target = getOwningTarget();
		String target_name = (target == null ? "null" : target.getName());
		Integer id = transitCounter.get(target);
		if (id == null)
			id = 1;
		else
			id += 1;
		transitCounter.put(target, id);
		if (usembean) {
			mbeanServer = ManagementFactory.getPlatformMBeanServer();
			try {
				int count = 1;
				for (TransitElement te: transit_elements)
					mbeanServer.registerMBean(te, new ObjectName("org.rejna.abet:type=TransitElement,target=" + target_name + ",transit=transit_" + id + ",component=" + count++));
				if (destination != null)
					mbeanServer.registerMBean(destination, new ObjectName("org.rejna.abet:type=TransitElement,target=" + target_name + ",transit=transit_" + id + ",component=" + count++));
			} catch (Exception e) {
				log.log(LogClass.main, Level.WARN, target, "JMX Mbean initilisation error", e);
			}

		}
		
		
		for (TransitElement te: transit_elements)
			te.execute();
		if (destination != null) { // is variable ?
			destination.execute();
			destination.run();
			for (TransitElement te: transit_elements)
				te.close();
			destination.close();
			log.log(LogClass.connector, Level.INFO, target, transit_elements.firstElement().printStats());
			log.log(LogClass.connector, Level.INFO, target, destination.printStats());
		}
	}	
}