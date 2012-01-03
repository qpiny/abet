package org.rejna.abet.converter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.log4j.Level;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class One4AllLogs extends Converter {
	// convert One4All log line to :
	// Date LoggerName Action OldType Type Profile ComputerName Logon Certificate CommonName Slaves

	private String proxy = "no-proxy";
	
	public void setProxy(String proxy) {
		this.proxy = proxy;
	}
	
	@Override
	public void convert(DataEntry data) throws ConverterException {
		DataEntry output = new DataEntry();
		for (Object[] entry : data) {
			try {
				output.addElement();
				Hashtable<String, String> attributes = new Hashtable<String, String>();
				if (entry.length != 1) {
					Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "One4AllLogs converter cannot be use on multi column line");
					continue;
				}
				
				Scanner scanner = new Scanner(entry[0].toString());
				/* scan date */
				attributes.put("Date", scanner.next() + " " + scanner.next() + " " + scanner.next());
				String logger_name = scanner.next();
				if (proxy.equals(logger_name)) {
					logger_name = scanner.next();
					if (!"Kiwi_Syslog_Server".equals(scanner.next()))
						Log.get().log(LogClass.converter, Level.WARN, getOwningTarget(), "One4AllLogs converter has found log proxy address but without the key \"Kiwi_Syslog_Server\"");
					try {
						InetAddress addr = InetAddress.getByName(logger_name);
						logger_name = addr.getHostName();
					} catch (UnknownHostException e) {
						Log.get().log(LogClass.converter, Level.WARN, getOwningTarget(), "DNS resolution of " + logger_name + " failed");
					}
				}
				
				int dot_index = logger_name.indexOf('.');
				String hostname = logger_name;
				if (dot_index != -1)					
					try {
						hostname = logger_name.substring(0, dot_index);
						// if is IP address
						Integer.parseInt(hostname);
						hostname = logger_name;
					} catch (NumberFormatException e) {
					}
				attributes.put("LoggerName", hostname);
				String action = scanner.next();
				if ("lock".equals(action) ||
						"logout".equals(action) ||
						"restart".equals(action) ||
						"shutdown".equals(action)) {
					attributes.put("Origin", scanner.next());
					if (scanner.hasNext())
						attributes.put("ComputerName", scanner.next());
				}
				else if ("unlock".equals(action) ||
						"login".equals(action)) {
					attributes.put("Origin", scanner.next());
					String auth = scanner.next();
					if ("password_connection".equals(auth))
						attributes.put("CommonName", scanner.next());
					else if (scanner.hasNext()) { // certificate
						attributes.put("Certificate", auth);
						StringBuilder common_name = new StringBuilder();
						String cn = scanner.next();
						while (scanner.hasNext() && cn.indexOf('\\') == -1) {
							common_name.append(" ").append(cn);
							cn = scanner.next();
						}
						if ((cn.indexOf('\\') == -1) || scanner.hasNext()) {
							attributes.put("CommonName", common_name.append(" ").append(cn).toString());
						}
						else {
							attributes.put("CommonName", common_name.toString());
							attributes.put("Logon", cn);
						}
					}
					else // host from
						attributes.put("ComputerName", auth);
				}
				else if ("add".equals(action) ||
						"remove".equals(action)) {
					attributes.put("ComputerName", scanner.next());
					attributes.put("Slaves", scanner.next()); // TODO remove [();]				
				}
				else if ("switch".equals(action)) {
					scanner.useDelimiter("\\p{javaWhitespace}+to\\p{javaWhitespace}+");
					attributes.put("OldType", scanner.next());
					attributes.put("Type", scanner.next());
				}
				else if ("config".equals(action)) {
					String name = "";
					StringBuffer value = new StringBuffer();
					while (scanner.hasNext()) {
						String token = scanner.next();
						int i = token.indexOf('=');
						if (i >= 0) {
							if (name.length() > 0)
								attributes.put(name, value.toString());
							name = token.substring(0, i);
							value = new StringBuffer(token.substring(i + 1));
						}
						else
							value.append(" ").append(token);
					}
					if (name.length() > 0)
						attributes.put(name, value.toString());
				}
				else if ("signing".equals(action)) {
					scanner.useDelimiter(" *[><]+");
					action += scanner.next();
					if ("signing certificate revoked".equals(action)) {
						String ca = scanner.next();
						attributes.put("CommonName", scanner.next());
						attributes.put("Certificate", scanner.next() + "+" + ca);
					}
				}
				else if ("signature".equals(action)) {
					scanner.useDelimiter(" *[><]+");
					action += scanner.next();
					if ("signature verified".equals(action) ||
							"signature failed".equals(action) ||
							"signature invalid".equals(action)) {
						attributes.put("File", scanner.next());
						if (scanner.hasNext()) {
							String ca = scanner.next();
							attributes.put("CommonName", scanner.next());
							attributes.put("Certificate", scanner.next() + "+" + ca);
						}
					}
					else {
						if ("signature success".equals(action)) {
							attributes.put("File", scanner.next());
							scanner.next(); // skip the date
						}
						else
							attributes.put("Comment", scanner.next());
						String sn = scanner.next();
						attributes.put("CommonName", getRdn(scanner.next()));
						attributes.put("Certificate", sn + getRdn(scanner.next()));
					}
				}
				else if ("Slave".equals(action)) { //Slave Network Control failed
					scanner.useDelimiter("$");
					action += scanner.next();
				}
				else if ("update".equals(action)) { // update SNC
					scanner.useDelimiter(" *[><]+");
					action += scanner.next();
					attributes.put("ComputerName", scanner.next());
				}
				else {
					Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Parse error (action not recognized): " + entry[0].toString());
					continue;					
				}
				
				//Log.converter.info(attributes.toString());
				
				if (scanner.hasNext()) {
					Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Parse error (not entirely parse): " + entry[0].toString() + " | " + scanner.next());
					continue;
				}
				// Aug 21 23:30:05
				Calendar cal = Calendar.getInstance();
				int current_year = cal.get(Calendar.YEAR);
				int current_month = cal.get(Calendar.MONTH);
				cal.setTime(new SimpleDateFormat("MMM dd HH:mm:ss", Locale.US).parse(attributes.get("Date"), new ParsePosition(0)));
				if (cal.get(Calendar.MONTH) == Calendar.DECEMBER && current_month == Calendar.JANUARY)
					cal.set(Calendar.YEAR, current_year - 1);
				else
					cal.set(Calendar.YEAR, current_year);
				attributes.put("Action", action);
				output.addAttribute(); output.addValue(new java.sql.Timestamp(cal.getTimeInMillis()));
				output.addAttribute(); output.addValue(attributes.get("LoggerName"));
				output.addAttribute(); output.addValue(attributes.get("Action"));
				output.addAttribute(); output.addValue(attributes.get("Origin"));
				output.addAttribute(); output.addValue(attributes.get("OldType"));
				output.addAttribute(); output.addValue(attributes.get("Type"));
				output.addAttribute(); output.addValue(attributes.get("Profile"));
				output.addAttribute(); output.addValue(attributes.get("ComputerName"));
				output.addAttribute(); output.addValue(attributes.get("Logon"));
				output.addAttribute(); output.addValue(attributes.get("Certificate"));
				output.addAttribute(); output.addValue(attributes.get("CommonName"));
				output.addAttribute(); output.addValue(attributes.get("Slaves"));
				output.addAttribute(); output.addValue(attributes.get("File"));
				output.addAttribute(); output.addValue(attributes.get("Comment"));
			} catch (InputMismatchException e) {
				Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Parse error (input mismatch) : " + entry[0].toString(), e);
			} catch (NoSuchElementException e) {
				Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Parse error (input mismatch) : " + entry[0].toString(), e);
			} catch (ConcurrentAccessException e) {
				Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Error ", e);
			}
		}
		enqueue(output);
	}
		
	private String getRdn(String dn) {
		Scanner scanner = new Scanner(dn);
		scanner.useDelimiter("[=,]");
		scanner.next();
		return scanner.next();
	}
}
