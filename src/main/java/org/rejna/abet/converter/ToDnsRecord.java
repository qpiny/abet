package org.rejna.abet.converter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;

import org.rejna.abet.exception.ConverterException;

public class ToDnsRecord implements ColumnConverter {
	
	@Override
	public Object convert(Object obj, String acceptedType) throws ConverterException {
		//logger.debug("DnsRecord=" + obj.getClass().getCanonicalName());
		if (",parseDN,".equals(acceptedType))
			return parseDN((String)obj);
		byte[] data = (byte[])obj;
		if (obj != null) {
			int type = (data[3] << 8) | (data[2] & 0xff);
			try {
				if ((type == 1 && (acceptedType == null || acceptedType.contains(",A,"))) ||
						(type == 28 && (acceptedType == null || acceptedType.contains(",AAAA,")))) {
					int len = (data[1] << 8) | (data[0] & 0xff);
					byte[] addr = Arrays.copyOfRange(data, data.length - len, data.length);
					return InetAddress.getByAddress(addr).getHostAddress();
				}
				else if (type == 12 && (acceptedType == null || acceptedType.contains(",PTR,"))) {
					int start = data.length - ((data[1] << 8) | (data[0] & 0xff)) + 2;
					int len = data[start];
					StringBuffer output = new StringBuffer();
					while (len != 0) {
						output.append(new String(Arrays.copyOfRange(data, start + 1, start + 1 + len))).append(".");
						start += len + 1;
						len = data[start];
					}
					//logger.debug("output = " + output);
					return output;
				}
			} catch (UnknownHostException e) {
				throw new ConverterException("Unknown host error", e);
			}
		}
		return null;
	}

	private Object parseDN(String dnstr) throws ConverterException {
		try {
			Name dn = new LdapName(dnstr);
			String first = dn.get(dn.size() - 1);
			first = first.substring(first.indexOf('=') + 1);
			if ("@".equals(first))
				first = "0";
			else if (first.startsWith(".."))
				return null;
			String second = dn.get(dn.size() - 2);
			second = second.substring(second.indexOf('=') + 1);
			if (second.endsWith("in-addr.arpa")) {
				try {
					byte[] addr = InetAddress.getByName(first + "." + second.substring(0, second.length() - 13)).getAddress();
					int a = addr[0] & 0xFF;
					int b = addr[1] & 0xFF;
					int c = addr[2] & 0xFF;
					int d = addr[3] & 0xFF;
					return d + "." + c + "." + b + "." + a;   
				} catch (UnknownHostException e) {
					throw new ConverterException("Unknown host error", e);
				}	
			}
			else
				return first;
				
		} catch (InvalidNameException e) {
			throw new ConverterException("Invalid name", e);
		} catch (Throwable t) {
			throw new ConverterException("Unknown error", t);
		}
	}	
}
