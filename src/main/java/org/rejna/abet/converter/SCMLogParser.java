package org.rejna.abet.converter;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

class Stats {
	int preperso = 0;
	int enrollment = 0;
	int revocation = 0;
	int recyclage = 0;
	Stats() {
		preperso = 0; enrollment = 0; revocation = 0; recyclage = 0;
	}
}

public class SCMLogParser extends Converter {
	private Object current_date = null;
	private String current_location = null;
	private HashMap<String, Stats> cumul = new HashMap<String, Stats>();
	private Stats stats = null;
	private Stats global_stats = new Stats();
	private HashMap<String, Integer> states = new HashMap<String, Integer>();

	@Override
	public void convert(DataEntry data) throws ConverterException {
		DataEntry output = new DataEntry();
		for (Object[] entry : data) {
			String location = entry[2].toString();
			String card_serial = entry[3].toString();
			if (!entry[0].equals(current_date) || !location.equals(current_location)) {
				
				try {
					if (current_date != null && current_location != null) {
						output.addElement();
						output.addAttribute(); // Date
						output.addValue(current_date);
						output.addAttribute(); // Location
						output.addValue(current_location);
						output.addAttribute(); // Preperso
						output.addValue(stats.preperso);
						output.addAttribute(); // Enrollment
						output.addValue(stats.enrollment);
						output.addAttribute(); // Revocation
						output.addValue(stats.revocation);
						output.addAttribute(); // Recyclage
						output.addValue(stats.recyclage);
					}
					if (!entry[0].equals(current_date)) {
						output.addElement();
						output.addAttribute(); // Date
						output.addValue(current_date);
						output.addAttribute(); // Location
						output.addValue("global");
						output.addAttribute(); // Preperso
						output.addValue(global_stats.preperso);
						output.addAttribute(); // Enrollment
						output.addValue(global_stats.enrollment);
						output.addAttribute(); // Revocation
						output.addValue(global_stats.revocation);
						output.addAttribute(); // Recyclage
						output.addValue(global_stats.recyclage);
					}
				} catch (ConcurrentAccessException e) {
					Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Impossible ?!");
				}
				current_date = entry[0];
				if (!location.equals(current_location)) {
					if (current_location != null)
						cumul.put(current_location, stats);
					current_location = location;
					stats = cumul.get(current_location);
					if (stats == null)
						stats = new Stats();
				}
			}
			Integer state = states.get(card_serial);
			if (state == null)
				state = 0;
			if ("CARD_PREPERSONALIZATION".equals(entry[1])) {
				stats.preperso++;
				global_stats.preperso++;
				state = 1;
			}
			else if ("CARD_ENROLLMENT".equals(entry[1])) {
				stats.enrollment++;
				global_stats.enrollment++;
				state = 2;
			}
			else if ("CARD_REVOKE".equals(entry[1])) {
				stats.revocation++;
				global_stats.revocation++;
				state = 0;
			}
			else if ("CARD_RECYCLED".equals(entry[1])){
				if (state == 1) {
					stats.recyclage++;
					global_stats.recyclage++;
					state = 0;
				}
			}
			else
				Log.get().log(LogClass.converter, Level.WARN, getOwningTarget(), "Invalid SCM title (" + entry[1] + ")");
			states.put(card_serial, state);
		}				
		enqueue(output);
	}
	
	@Override
	public void getResidue() {
		DataEntry output = new DataEntry();
		if (current_date != null && current_location != null && stats != null) {
			try {
				output.addElement();
				output.addAttribute(); // Date
				output.addValue(current_date);
				output.addAttribute(); // Location
				output.addValue(current_location);
				output.addAttribute(); // Preperso
				output.addValue(stats.preperso);
				output.addAttribute(); // Enrollment
				output.addValue(stats.enrollment);
				output.addAttribute(); // Revocation
				output.addValue(stats.revocation);
				output.addAttribute(); // Recyclage
				output.addValue(stats.recyclage);
				
				output.addElement();
				output.addAttribute(); // Date
				output.addValue(current_date);
				output.addAttribute(); // Location
				output.addValue("global");
				output.addAttribute(); // Preperso
				output.addValue(global_stats.preperso);
				output.addAttribute(); // Enrollment
				output.addValue(global_stats.enrollment);
				output.addAttribute(); // Revocation
				output.addValue(global_stats.revocation);
				output.addAttribute(); // Recyclage
				output.addValue(global_stats.recyclage);
			} catch (ConcurrentAccessException e) {
				Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Impossible ?!");
			}
		}
		enqueue(output);
	}

}
