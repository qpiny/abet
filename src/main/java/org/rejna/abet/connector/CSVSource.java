package org.rejna.abet.connector;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.tools.ant.BuildException;

public class CSVSource extends CSVSourceBase {
	public void setUrl(String url) {
		try {
			source = new InputStreamReader(new URL(url).openStream());
		} catch (Exception e) {
			throw new BuildException("CVS file can't be read", e);
		}
	}
	
	public void setFile(String file) {
		try {
			source = new FileReader(file);
		} catch (FileNotFoundException e) {
			throw new BuildException("CVS file can't be read", e);
		}
	}	
}
