package org.rejna.abet.connector;

import org.apache.axis2.AxisFault;
import org.apache.tools.ant.BuildException;
import org.rejna.abet.connector.webservice.opentrust.GroupManager;
import org.rejna.abet.connector.webservice.opentrust.OpentrustService;
import org.rejna.abet.connector.webservice.opentrust.RightsManager;
import org.rejna.abet.connector.webservice.opentrust.SearchForCards;
import org.rejna.abet.exception.ConnectorException;

public class OpentrustScmConnector extends Connector {
	private String url = null;
	private String keystoretype = "PKCS12";
	private String keystore = "keystore.jks";
	private String keystorepassword = "changeme";
	private String truststore = "cacert.jks";

	public OpentrustScmConnector() {
		super();
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setKeystoretype(String keystoretype) {
		this.keystoretype = keystoretype;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	public void setKeystorepassword(String keystorepassword) {
		this.keystorepassword = keystorepassword;
	}

	public void setTruststore(String truststore) {
		this.truststore = truststore;
	}

	public OpentrustService getService(String service) throws ConnectorException {
		if (url == null)
			throw new ConnectorException("url is not defined for OpentrustConnector");
		try {
			System.setProperty("javax.net.ssl.keyStoreType", keystoretype);
			System.setProperty("javax.net.ssl.keyStore", keystore);
			System.setProperty("javax.net.ssl.keyStorePassword",
					keystorepassword);
			System.setProperty("javax.net.ssl.trustStore", truststore);
			if ("searchForCards".equals(service))
				return new SearchForCards(url);
			else if ("rightManager".equals(service))

				return new RightsManager(url);

			else if ("groupManager".equals(service))
				return new GroupManager(url);
		} catch (AxisFault e) {
			throw new BuildException("Webservice initialisation fail", e);
		}
		throw new ConnectorException("unknown service");
	}
	
	@Override
	public void initConnector() {
	}
}