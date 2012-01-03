package org.rejna.abet.converter;

import org.apache.log4j.Level;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class EchonetOrga extends Converter {
	private String delim1 = "\\|";
	private String delim2 = "#";
	private String delim3 = ",";
	private int rolecolumn = -1;
	
	public void setDelim1(String delim1) {
		this.delim1 = delim1;
	}

	public void setDelim2(String delim2) {
		this.delim2 = delim2;
	}

	public void setDelim3(String delim3) {
		this.delim3 = delim3;
	}
	
	public void setRolecolumn(int rolecolumn) {
		this.rolecolumn = rolecolumn;
	}

	@Override
	public void convert(DataEntry data) throws ConverterException {
		DataEntry output = new DataEntry();
		try {
			for (Object[] entry : data) {
				output.addElement();
				for (int i = 0; i < entry.length; i++) {
					if (i == rolecolumn) {
						String responsable = "";
						String adjoint = "";
						String secretaire = "";
						String[] persons = entry[i].toString().split(delim1);
						for (String person : persons) {
							String[] pdetail = person.split(delim2);
							if (pdetail.length < 2)
								continue;
							if ("1".equals(pdetail[0]))
								responsable += delim3 + pdetail[1];
							else if ("2".equals(pdetail[0]))
								adjoint += delim3 + pdetail[1];
							else if ("3".equals(pdetail[0]))
								secretaire += delim3 + pdetail[1];
							else
								Log.get().log(LogClass.converter, Level.WARN, getOwningTarget(), "Role type " + pdetail[0] + " not recognized for user " + pdetail[1]);
						}
						output.addAttribute();
						if ("".equals(responsable))
							output.addValue(null);
						else
							output.addValue(responsable.substring(1));
						output.addAttribute();
						if ("".equals(adjoint))
							output.addValue(null);
						else
							output.addValue(adjoint.substring(1));
						output.addAttribute();
						if ("".equals(secretaire))
							output.addValue(null);
						else
							output.addValue(secretaire.substring(1));
					}
					else {
						output.addAttribute();
						output.addValue(entry[i]);
					}
				}
			}
		} catch (ConcurrentAccessException e) {
			throw new ConverterException("Concurrent access error", e);
		}
		enqueue(output);

		
	}
}