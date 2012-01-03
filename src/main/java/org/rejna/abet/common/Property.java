package org.rejna.abet.common;

import java.util.HashMap;
import java.util.Properties;

public class Property {
	private String name = null;
	private String value = null;
	private Properties properties;
	private HashMap<String,String> property_translation = new HashMap<String,String>();
	
	public Property(Properties properties, HashMap<String,String> property_translation) {
		this.properties = properties;
		this.property_translation = property_translation;
	}
	
	public void addText(String value) {
		setValue(value);
	}
	
	private void setProperty() {
		properties.setProperty(name, value);
		if (property_translation.containsKey(name))
			properties.setProperty(property_translation.get(name), value);
	}
	
	public void setName(String name) {
		this.name = name;
		if (value != null)
			setProperty();
	}
	
	public void setValue(String value) {
		this.value = value;
		if (name != null)
			setProperty();
	}
}
