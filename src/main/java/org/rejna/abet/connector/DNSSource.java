package org.rejna.abet.connector;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public class DNSSource extends ConnectorSource {
	private String filename = null;
	private String domain = "";
	private BufferedReader input = null;
	private DataEntry next_element = null;
	private Pattern pattern = Pattern.compile("\\s+");
	
	public void setFile(String filename) {
		this.filename = filename;
	}
	
	public void setDomain(String domain) {
		this.domain = "." + domain;
	}
	
	@Override
	public void execute() {
		if (filename == null)
			throw new BuildException("filename is not defined in DNSSource"); 
		try {
			input = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			throw new BuildException("File not found", e);
		}
		next_element = fetchNext();
	}
	
	@Override
	public boolean hasMoreElements() {
		if (next_element == null)
			next_element = fetchNext();
		return (next_element != null);
	}

	@Override
	public DataEntry nextElement() {
		DataEntry el;
		if (next_element == null)
			el = fetchNext();
		else
			el = next_element;
		next_element = null;
		if (el == null)
			throw new NoSuchElementException();
		return el;
	}

	@Override
	public void close() {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Error while closing file", e);
			}
			input = null;
		}
	}

	private DataEntry fetchNext() {
		try {
			while (true) {
				String line = input.readLine();
				if (line == null)
					return null;
				String[] tokens = pattern.split(line);
				if (tokens.length != 3)
					continue;
				if (!"A".equals(tokens[1]))
					continue;
				if ("@".equals(tokens[0]))
					continue;
				long ip = parseIP(tokens[2]);
				if (ip == 0) {
					Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Invalid IP format : " + tokens[2]);
					continue;
				}
				DataEntry entry = new DataEntry();
				entry.addAttribute();
				entry.addValue(tokens[0] + domain);
				entry.addAttribute();
				entry.addValue(ip);
				return entry;
			}
		} catch (IOException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Read error", e);
		} catch (ConcurrentAccessException e) {			
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Concurrent error", e);
		}
		return null;
	}
	
	public static long parseIP(String ipaddr) {
		if (ipaddr == null || ipaddr.length() == 0)
			return -1;
		String[] ip_str = ipaddr.split("\\.", -1);
		try {
			long ip_value, quartet_value;
			switch (ip_str.length) {
			case 1:
				ip_value = Long.parseLong(ip_str[0]);
				if ((ip_value < 0L) || (ip_value > 0xFFFFFFFFL))
					return -1;
				return ip_value;
			case 2:
				ip_value = Integer.parseInt(ip_str[0]);
				if ((ip_value < 0L) || (ip_value > 0xFFL))
					return -1;
				quartet_value = Long.parseLong(ip_str[1]);
				if ((quartet_value < 0L) || (quartet_value > 0xFFFFFFL))
					return -1;
				return (ip_value << 6) + quartet_value;
			case 3:
				ip_value = 0;
				for (int i = 0; i < 2; ++i) {
					quartet_value = Long.parseLong(ip_str[i]);
					if (quartet_value < 0L || quartet_value > 0xFFL)
						return -1;
					ip_value = (ip_value << 8) + quartet_value; 
				}
				quartet_value = Integer.parseInt(ip_str[2]);
				if ((quartet_value < 0L) || (quartet_value > 65535L))
					return -1;
				return (ip_value << 16) + quartet_value;
			case 4:
				ip_value = 0;
				for (int i = 0; i < 4; ++i) {
					quartet_value = Long.parseLong(ip_str[i]);
					if ((quartet_value < 0L) || (quartet_value > 0xFFL))
						return -1;
					ip_value = (ip_value << 8) + quartet_value; 
				}
				return ip_value;
			default:
				return -1;
			}
		} catch (NumberFormatException localNumberFormatException) {
			return -1;
		}
	}
}
