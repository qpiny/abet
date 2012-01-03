package org.rejna.abet.connector;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConnectorException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVDestination extends ConnectorDestination {
	private CSVWriter csvwriter;
	private char quote = '\"';
	private char separator = ',';
	private char escape = '\\';
	private Writer destination = null;
	
	public void setQuote(String quote) {
		if ("NULL".equals(quote))
			this.quote = CSVWriter.NO_QUOTE_CHARACTER;
		else
			this.quote = quote.charAt(0);
	}
	
	public void setSeparator(String separator) {
		this.separator = separator.charAt(0);
	}
	
	public void setEscape(String escape) {
		if ("NULL".equals(escape))
			this.escape = CSVWriter.NO_ESCAPE_CHARACTER;
		else
			this.escape = escape.charAt(0);
	}
	
	public void setFile(String file) {
		try {
			destination = new FileWriter(file);
		} catch (IOException e) {
			throw new BuildException("CVS file can't be write", e);
		}
	}
	
	@Override
	public void execute() {
		csvwriter = new CSVWriter(destination,
				separator,
				quote,
				escape);
	}
	

	@Override
	public void close() {
		try {
			csvwriter.close();
		} catch (IOException e) {
			Log.get().log(LogClass.connector, Level.ERROR, getOwningTarget(), "Error while closing CSV file", e);
		}
	}

	@Override
	public void addRow(DataEntry element) throws ConnectorException {
		for (Object[] data : element) {
			String[] line = new String[data.length];
			for (int i = 0; i < data.length; i++) {
				if (data[i] == null)
					line[i] = "";
				else
					line[i] = data[i].toString();
			}
			csvwriter.writeNext(line);
		}
	}

}
