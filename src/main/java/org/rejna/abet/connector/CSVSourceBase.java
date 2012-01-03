package org.rejna.abet.connector;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

import au.com.bytecode.opencsv.CSVReader;

public class CSVSourceBase extends ConnectorSource {
	private CSVReader csvreader;
	private char quote = '\"';
	private char separator = ',';
	private char escape = '\\';
	protected Reader source = null;
	private int skipline = 0;
	private int maxcol = -1;
	private int mincol = -1;
	private String[] line = null;
	
	public void setQuote(String quote) {
		if ("NULL".equals(quote))
			this.quote = 0;
		else
			this.quote = quote.charAt(0);
	}
	
	public void setSeparator(String separator) {
		if ("NULL".equals(separator))
			this.separator = 0;
		else
			this.separator = separator.charAt(0);
	}
	
	public void setEscape(String escape) {
		if ("NULL".equals(escape))
			this.escape = 0;
		else
			this.escape = escape.charAt(0);
	}
	
	public void setSkipline(int skipline) {
		this.skipline = skipline;
	}
	
	public void setMaxcol(int maxcol) {
		this.maxcol = maxcol;
	}
	
	public void setMincol(int mincol) {
		this.mincol = mincol;
	}
	
	@Override
	public void execute() {
		if (source == null)
			throw new BuildException("CSV based connector " + getTaskName() + " has no source");
		csvreader = new CSVReader(source,
				separator,
				quote,
				escape,
				skipline);
	}
	
	@Override
	public boolean hasMoreElements() {
		if (line == null) {
			try {
				line = csvreader.readNext();
			} catch (IOException e) {
				Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Error while reading CSV file", e);
				return false;
			}
		}
		return (line != null);
	}

	@Override
	public DataEntry nextElement() {
		try {
			if (!hasMoreElements())
				throw new NoSuchElementException();
			DataEntry entry = new DataEntry();
			incStat("Fetched");
			int ncol = line.length;
			if (maxcol > 0 && ncol > maxcol) {
				Log.get().log(LogClass.connector, Level.WARN, getOwningTarget(), "Too many columns for entry, ignoring. " + Arrays.toString(line));
				line = null;
				if (!hasMoreElements())
					throw new NoSuchElementException();

			}
			if (mincol > 0 && mincol > ncol) {
				Log.get().log(LogClass.connector, Level.WARN, getOwningTarget(), "Too few columns for entry, ignoring. " + Arrays.toString(line));
				line = null;
				if (!hasMoreElements())
					throw new NoSuchElementException();

			}
			for (int i = 0; i < ncol; i++) {
				entry.addAttribute();
				entry.addValue(line[i]);
			}
			return entry;
		} catch (ConcurrentAccessException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Concurrent error", e);
			return null;
		}
		finally {
			line = null;
		}
	}

	@Override
	public void close() {
		try {
			csvreader.close();
		} catch (IOException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Error while closing CSV file", e);
		}
	}

}
