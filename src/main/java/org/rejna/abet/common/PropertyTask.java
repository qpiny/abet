package org.rejna.abet.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.Task;

public class PropertyTask extends Task implements PropertyTaskMBean {
	private Properties properties = new Properties();
	private HashMap<String,String> property_translation = new HashMap<String,String>();
	private HashMap<String, Integer> stats = new HashMap<String, Integer>();
	
	public Property createProperty() {
		return new Property(properties, property_translation);
	}
	
	public void addPropertyTranslation(String from, String to) {
		property_translation.put(from, to);
	}
	
	public Properties getProperties() {
		return properties;
	}
	
	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
		if (property_translation.containsKey(name))
			properties.setProperty(property_translation.get(name), value);
	}
	
	public String getProperty(String name, String default_value) {
		return properties.getProperty(name, default_value);
	}
	
	public String getProperty(String name) {
		return properties.getProperty(name);
	}
	
	public HashMap<String, Integer> getStats() {
		return stats;
	}
	
	public String printStats() {
		StringBuilder out = new StringBuilder();
		for(Map.Entry<String, Integer> stat: stats.entrySet())
			out.append(" ").append(stat.getKey()).append("=").append(stat.getValue());
		if (out.length() > 0)
			return out.substring(1);
		else
			return "";
	}
	
	public int incStat(String stat) {
		return incStat(stat, 1);
	}
	
	public int incStat(String stat, int inc) {
		Integer value = stats.get(stat);
		if (value == null)
			value = Integer.valueOf(inc);
		else
			value = Integer.valueOf(value.intValue() + inc);
		stats.put(stat, value);
		return value.intValue();		
	}
}
