package org.rejna.abet.connector.rhn;

public class RHErrata {
	//private RHAuth auth;
	private int id;
	private String date;
	private String advisory_synopsis;
	private String advisory_type;
	private String advisory_name;
	
	public RHErrata(RHAuth auth, int id, String date, String advisory_name, String advisory_type, String advisory_synopsis) {
		//this.auth = auth;
		this.id = id;
		this.date = date;
		this.advisory_name = advisory_name;
		this.advisory_synopsis = advisory_synopsis;
		this.advisory_type = advisory_type;
	}

	public int getId() {
		return id;
	}

	public String getDate() {
		return date;
	}

	public String getAdvisory_synopsis() {
		return advisory_synopsis;
	}

	public String getAdvisory_type() {
		return advisory_type;
	}

	public String getAdvisory_name() {
		return advisory_name;
	}
	
	@Override
	public String toString() {
		return id + " " + advisory_name + " " + advisory_type + " " + advisory_synopsis;
	}
}
