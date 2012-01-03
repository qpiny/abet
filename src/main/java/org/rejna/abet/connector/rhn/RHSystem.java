package org.rejna.abet.connector.rhn;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcException;

public class RHSystem {
	private int id;
	private String name;
	private Date last_checkin;
	private String ip_address = null;
	private RHAuth auth;
	
	public RHSystem(RHAuth auth, int id, String name, Date last_checkin) {
		this.auth = auth;
		this.id = id;
		this.name = name;
		this.last_checkin = last_checkin;
	}
	
	@SuppressWarnings("rawtypes")
	public static Iterable<RHSystem> listSystems(RHAuth auth) throws XmlRpcException {
		Vector<RHSystem> systems = new Vector<RHSystem>();
		
		Object[] system_list = (Object[]) auth.execute("system.listSystems");
		for (Object s : system_list) {
			HashMap system = (HashMap) s;
			int id = (Integer) system.get("id");
			String name = (String) system.get("name");
			Date date = (Date) system.get("last_checkin");
			systems.add(new RHSystem(auth, id, name, date));
		}
		return systems;
	}
	
	
	@Override
	public String toString() {
		return id + " " + name + " (" + last_checkin + ")";
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getLast_checkin() {
		return last_checkin;
	}
	
	@SuppressWarnings("rawtypes")
	public String getIPAddress() throws XmlRpcException {
		if (ip_address == null) {		
			HashMap ret = (HashMap) auth.execute("system.getNetwork", id);
			ip_address = (String) ret.get("ip");
		}
		return ip_address;
			
	}
	
	@SuppressWarnings("rawtypes")
	public Iterable<RHErrata> getSecurityErrata() throws XmlRpcException {
		Vector<RHErrata> errata = new Vector<RHErrata>();
		
		Object[] errata_list = (Object[]) auth.execute("system.getRelevantErrataByType", id, "Security Advisory");
		for (Object e : errata_list) {
			HashMap err = (HashMap) e;
			int id = (Integer) err.get("id");
			String date = (String) err.get("date");
			String advisory_synopsis = (String) err.get("advisory_synopsis");
			String advisory_type = (String) err.get("advisory_type");
			String advisory_name = (String) err.get("advisory_name");
			errata.add(new RHErrata(auth, id, date, advisory_name, advisory_type, advisory_synopsis));
		}
		return errata;
	}

}
