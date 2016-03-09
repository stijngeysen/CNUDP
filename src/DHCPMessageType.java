
public enum DHCPMessageType 
{   DHCPDISCOVER,		//a client broadcasts to locate servers
	DHCPOFFER,	    	//a server offers an IP address to the device
	DHCPREQUEST,		//client accepts offers from DHCP server
	DHCPACK,			//server to client + committed IP address
	DHCPNAK,			//server to client to state net address incorrect
	DHCPRELEASE,		//graceful shutdown from client to Server
	INVALID;
	
	public byte[] getBytes() {
		byte[] msg = new byte[3];
		System.arraycopy(Utils.toBytes(53, 1), 0, msg, 0, 1);
		System.arraycopy(Utils.toBytes(1, 1), 0, msg, 1, 1);
		switch (this) {
			case DHCPDISCOVER: System.arraycopy(Utils.toBytes(1, 1), 0, msg, 1, 1);
			case DHCPOFFER: System.arraycopy(Utils.toBytes(2, 1), 0, msg, 1, 1);
			case DHCPREQUEST: System.arraycopy(Utils.toBytes(3, 1), 0, msg, 1, 1);
			case DHCPACK: System.arraycopy(Utils.toBytes(5, 1), 0, msg, 1, 1);
			case DHCPNAK: System.arraycopy(Utils.toBytes(6, 1), 0, msg, 1, 1);
			case DHCPRELEASE: System.arraycopy(Utils.toBytes(7, 1), 0, msg, 1, 1);
			default: 
		}
		return msg;
	}
	
	public static DHCPMessageType getType(byte[] bytes) {
		byte[] msg = new byte[1];
		msg[0] = bytes[2];
		switch (Utils.fromBytes(msg)) {
			case 1: return DHCPDISCOVER;
			case 2: return DHCPOFFER;
			case 3: return DHCPREQUEST;
			case 5: return DHCPACK;
			case 6: return DHCPNAK;
			case 7: return DHCPRELEASE;
			default: return INVALID;
		}
	}
}
