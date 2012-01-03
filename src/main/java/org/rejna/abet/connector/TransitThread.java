package org.rejna.abet.connector;

import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

import org.rejna.abet.DataEntry;
import org.rejna.abet.common.TransitElement;
import org.rejna.abet.exception.ConnectorException;

public class TransitThread extends Thread implements TransitElement, TransitThreadMBean {
	private TransitElement source;
	private LinkedBlockingQueue<DataEntry> queue = new LinkedBlockingQueue<DataEntry>();
	private static final DataEntry END_OF_QUEUE = new DataEntry();
	private boolean started = false;
	
	public void setSource(TransitElement source) {
		this.source = source;
	}
	
	@Override
	public void execute() {
		started = true;
		start();
	}
	
	@Override
	public void run() {
		started = true;
		try {
			while (source.hasMoreElements())
				queue.offer(source.nextElement());
		} catch (NoSuchElementException e) {
		}
		source.getResidue();
		try {
			while (source.hasMoreElements())
				queue.offer(source.nextElement());
		} catch (NoSuchElementException e) {
		} finally {
			queue.offer(END_OF_QUEUE);
		}
	}
	
	@Override
	public boolean hasMoreElements() {
		if (!started) {
			started = true;
			start();
		}
		DataEntry head = queue.peek();
		return head != END_OF_QUEUE;
	}

	@Override
	public DataEntry nextElement() {
		if (!started) {
			started = true;
			start();
		}
		try {
			DataEntry el = queue.take();
			if (el == END_OF_QUEUE) {
				queue.offer(el);
				throw new NoSuchElementException();
			}
			return el;
		} catch (InterruptedException e) {
			throw new NoSuchElementException();
		}
	}
	
	@Override
	public void close() {
		source.close();
	}
	
	@Override
	public void getResidue() {
		source.getResidue();
	}
	
	@Override
	public int getQueueSize() {
		return queue.size();
	}

	@Override
	public Object getSingleValue() throws ConnectorException {
		return source.getSingleValue();
	}

	@Override
	public String printStats() {
		return "no stats";
	}
}
