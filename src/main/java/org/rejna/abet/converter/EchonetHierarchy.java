package org.rejna.abet.converter;

import java.util.HashMap;
import java.util.Vector;

import org.rejna.abet.DataEntry;
import org.rejna.abet.exception.ConcurrentAccessException;
import org.rejna.abet.exception.ConverterException;

public class EchonetHierarchy extends Converter {
	private HashMap<Object, Ou> oudata = new HashMap<Object, Ou>();
	private Vector<Object[]> data = new Vector<Object[]>();
	private int hiercolumn, idcolumn, parentscolumn;
	
	public void setIdcolumn(int idcolumn) {
		this.idcolumn = idcolumn;
	}
	
	public void setHiercolumn(int hiercolumn) {
		this.hiercolumn = hiercolumn;
	}
	
	public void setParentscolumn(int parentscolumn) {
		this.parentscolumn = parentscolumn;
	}
	
	@Override
	public void convert(DataEntry entries) throws ConverterException {
		for (Object[] e : entries) {
			//try {
			data.add(e);
			Object id = e[idcolumn];
			if (id != null)
				oudata.put(id, new Ou(id, e[hiercolumn], oudata));
			//}
			//catch (NumberFormatException nfe) {
			//	Log.get().log(LogClass.converter, Level.WARN, getOwningTarget(), "Number conversion error", nfe);
			//}
		}
	}
	
	@Override
	public void getResidue() {
		for (Object[] d: data) {
			try {
				DataEntry entry = new DataEntry();
				int pos = 0;
				
				entry.addElement();
				boolean inserted = false;
				for (Object o : d) {
					if (pos++ == parentscolumn) {
						entry.addAttribute();
						entry.addValue(oudata.get(d[idcolumn]).getParents());
						inserted = true;
					}
					entry.addAttribute();
					entry.addValue(o);
				}
				if (!inserted) {
					entry.addAttribute();
					entry.addValue(oudata.get(d[idcolumn]).getParents());
				}
				enqueue(entry);
			} catch (ConcurrentAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

class Ou {
	private Object id, parent;
	private HashMap<Object, Ou> ous;
	private String parents = null;
	
	public Ou(Object id, Object parent, HashMap<Object, Ou> ous) {
		this.id = id;
		this.parent = parent;
		this.ous = ous;
	}
	
	/*
	public int getId() {
		return Integer.parseInt(data[id_col].toString());
	}
	*/
	
	public String getParents() {
		if (parents == null) {
			Ou parentOU = ous.get(parent);
			if (parentOU != null) {
				parents = parentOU.getParents() + "," + id;
				if (parents.startsWith(","))
					parents = parents.substring(1);
			}
			else if (parent != null)
				parents = parent + "," + id;
			else
				parents = id.toString();
		}
		return parents;
	}
}