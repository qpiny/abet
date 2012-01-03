package org.rejna.abet.converter;

import org.rejna.abet.exception.ConverterException;

public class ToIP implements ColumnConverter {

	@Override
	public Object convert(Object obj, String arg) throws ConverterException {
		if (obj == null || "".equals(obj))
			return null;
		
		if ("ip2long".equals(arg))
			return parseIP(obj.toString());
		else if ("long2ip".equals(arg)) {
			int ip = Integer.parseInt(obj.toString());
			return (ip >> 24) + "." +
				((ip >> 16) & 0xff) + "." +
				((ip >> 8) & 0xff) + "." +
				(ip & 0xff);
		}
		else
			throw new ConverterException("Invalid argument for IP converter");
	}
	
	public static long parseIP(String ipaddr) {
		if (ipaddr == null || ipaddr.length() == 0)
			return -1;
		String[] ip_str = ipaddr.split("\\.", -1);
		try {
			long ip_value, quartet_value;
			switch (ip_str.length) {
			case 1:
				ip_value = Long.parseLong(ip_str[0]);
				if ((ip_value < 0L) || (ip_value > 0xFFFFFFFFL))
					return -1;
				return ip_value;
			case 2:
				ip_value = Integer.parseInt(ip_str[0]);
				if ((ip_value < 0L) || (ip_value > 0xFFL))
					return -1;
				quartet_value = Long.parseLong(ip_str[1]);
				if ((quartet_value < 0L) || (quartet_value > 0xFFFFFFL))
					return -1;
				return (ip_value << 6) + quartet_value;
			case 3:
				ip_value = 0;
				for (int i = 0; i < 2; ++i) {
					quartet_value = Long.parseLong(ip_str[i]);
					if (quartet_value < 0L || quartet_value > 0xFFL)
						return -1;
					ip_value = (ip_value << 8) + quartet_value; 
				}
				quartet_value = Integer.parseInt(ip_str[2]);
				if ((quartet_value < 0L) || (quartet_value > 65535L))
					return -1;
				return (ip_value << 16) + quartet_value;
			case 4:
				ip_value = 0;
				for (int i = 0; i < 4; ++i) {
					quartet_value = Long.parseLong(ip_str[i]);
					if ((quartet_value < 0L) || (quartet_value > 0xFFL))
						return -1;
					ip_value = (ip_value << 8) + quartet_value; 
				}
				return ip_value;
			default:
				return -1;
			}
		} catch (NumberFormatException localNumberFormatException) {
			return -1;
		}
	}

}
