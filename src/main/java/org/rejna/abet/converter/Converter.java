package org.rejna.abet.converter;

import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Level;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.DataEntry;
import org.rejna.abet.common.PropertyTask;
import org.rejna.abet.common.TransitElement;
import org.rejna.abet.exception.ConverterException;
import org.rejna.abet.log.Log;
import org.rejna.abet.log.LogClass;

public abstract class Converter extends PropertyTask implements TransitElement, ConverterMBean {
	protected TransitElement source;
	private LinkedBlockingQueue<DataEntry> queue = new LinkedBlockingQueue<DataEntry>();
	private static final DataEntry END_OF_QUEUE = new DataEntry();
	private boolean residue_fetched = false;
	
	@Override
	public void setSource(TransitElement source) {
		this.source = source;
	}
	
	public abstract void convert(DataEntry data) throws ConverterException;
	
	public void enqueue(DataEntry data) {
		queue.offer(data);
	}

	@Override
	public boolean hasMoreElements() {
		if (!residue_fetched)
			return true;
		DataEntry head = queue.peek();
		return head != END_OF_QUEUE;
	}

	@Override
	public DataEntry nextElement() {
		DataEntry el = END_OF_QUEUE;
		try {
			try {
				while (queue.isEmpty()) {
					DataEntry entry = source.nextElement();
					try {
						convert(entry);
					} catch (Exception e) {
						Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Transit converter error", e);
					}
				}
				el = queue.take();
			} catch (NoSuchElementException e) {
			}
			if (el == END_OF_QUEUE) {
				if (residue_fetched) {
					queue.offer(el);
					throw new NoSuchElementException();
				}
				getResidue();
				queue.offer(el);
				residue_fetched = true;
				return nextElement();
			}
		} catch (InterruptedException e) {
			Log.get().log(LogClass.converter, Level.ERROR, getOwningTarget(), "Process interrupted, data may be incomplete");
		}
		if (el == END_OF_QUEUE)
			throw new NoSuchElementException();
		return el;
	}
	
	public void getResidue() {
	}
	
	public Object getSingleValue() {
		DataEntry value;
		if (hasMoreElements())
			value = nextElement();
		else
			throw new BuildException("No value");
		if (value.isSingle())
			return value.iterator().next()[0];
		throw new BuildException("Not single value");
	}
	
	@Override
	public void close() {
		source.close();
	}
	
	@Override
	public int getQueueSize() {
		return queue.size();
	}
}
