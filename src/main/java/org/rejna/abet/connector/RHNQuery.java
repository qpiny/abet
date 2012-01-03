package org.rejna.abet.connector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.Task;
import org.rejna.abet.DataEntry;
import org.rejna.abet.common.StringContainer;
import org.rejna.abet.common.Variable;
import org.rejna.abet.connector.rhn.RHAuth;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConnectorException;

public class RHNQuery extends Task {
	private String prefix = "";
	private Vector<String> arguments = new Vector<String>();
	private Vector<String> attributes = new Vector<String>();
	
	public void setMethod(String method) {
		arguments.add(method);
	}
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public void addConfiguredArg(StringContainer arg) {
		arguments.add(arg.get());
	}
	
	public void addConfiguredAttribute(StringContainer attribute) {
		attributes.add(attribute.get());
	}
	
	@SuppressWarnings("rawtypes")
	private DataEntry addData(RHAuth auth, Vector<String> fields, Object[] data) throws ConnectorException {
		final Pattern pattern = Pattern.compile("[$@]\\{[^}]*}");
		try {
			Vector<Object> args = new Vector<Object>(arguments.size());
			for (String a : arguments) {
				boolean added = false;
				Matcher matcher = pattern.matcher(a);
				while (matcher.find()) {
					boolean var = a.charAt(matcher.start()) == '$';
					String param = a.substring(matcher.start() + 2, matcher.end() -1);
					Object value;
					if (var)
						value = ((Variable)getProject().getReference(param)).getSingleValue();
					else {
						int i = fields.indexOf(param);
						value = data[i];
					}
					if (matcher.start() == 0 && matcher.end() == a.length()) {
						args.add(value);
						added = true;
					}
					else {
						a = a.substring(0, matcher.start()) + value + (a.length() > matcher.end() ? a.substring(matcher.end()) : "");
						matcher = pattern.matcher(a);
					}
				}
				if (!added)
					args.add(a);
			}
			//HashMap<String, Object>[] result = (HashMap<String, Object>[])
			Object[] result;
			Object r = auth.execute(args.toArray());
			if (r.getClass().isArray())
				result = (Object[]) r;
			else
				result = new Object[] { r };
			DataEntry output = new DataEntry();
			if (result.length == 0) {
				output.addElement();
				for (Object o : data) {
					output.addAttribute();
					output.addValue(o);
				}
				for (int i = 0; i < attributes.size(); i++) {
					output.addAttribute();
					output.addValue(null);
				}
			}
			for (Object e: result) {
				HashMap entry = (HashMap) e;
				output.addElement();
				if (data != null) {
					for (Object o : data) {
						output.addAttribute();
						output.addValue(o);
					}
				}
				for (String attr : attributes) {
					output.addAttribute();
					output.addValue(entry.get(attr));
				}
			}
			return output;
		} catch (Exception e) {
			throw new ConnectorException("RHN connector fails", e);
		}
	}
	
	public DataEntry getData(RHAuth auth, DataEntry data) throws ConnectorException {
		DataEntry output = new DataEntry();
		Vector<String> fields = new Vector<String>();
		Iterator<Object[]> ite = null;
		
		try {
			output.addElement();
			if (data != null) { // extract field name
				ite = data.iterator();
				if (ite.hasNext())
					for (Object field: ite.next()) {
						String v = String.valueOf(field);
						fields.add(v);
						output.addAttribute();
						output.addValue(v);
					}
			}
			
			for (String a : attributes) {
				fields.add(prefix + '/' + a);
				output.addAttribute();
				output.addValue(prefix + '/' + a);
			}

			if (ite != null) {
				while (ite.hasNext())
					output.addDataEntry(addData(auth, fields, ite.next()));
			}
			else
				output.addDataEntry(addData(auth, fields, null));
		} catch (ConcurrentAccessException e) {
			throw new ConnectorException("Impossible fail", e);
		}
		return output;
	}
	
	/*
		Pattern pattern = Pattern.compile("\\$!?\\{[^\\}]*}");
		Matcher matcher = pattern.matcher(attr);
		try {
			while (matcher.find()) {
				boolean inplace = query.charAt(matcher.start() + 1) != '!';
				String param = query.substring(matcher.start() + (inplace ? 2 : 3), matcher.end() - 1);
				String value;
				if (inplace)
					value = ((Variable)getProject().getReference(param)).getSingleValue().toString();
				else {
					value = "?";
					parameters.add(((Variable)getProject().getReference(param)).getSingleValue());
				}
				query = query.substring(0, matcher.start()) + value + (query.length() > matcher.end() ? query.substring(matcher.end()) : "");
				matcher = pattern.matcher(query);
			}
			this.query = query;
		
		
		
		attributes.add();
	}
	*/

}
