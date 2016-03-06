
public enum DHCPMessageType 
{   DHCPDISCOVER,		//a client broadcasts to locate servers
	DHCPOFFER,	    	//a server offers an IP address to the device
	DHCPREQUEST,		//client accepts offers from DHCP server
	DHCPACK,			//server to client + committed IP address
	DHCPNAK,			//server to client to state net address incorrect
	DHCPRELEASE;		//graceful shutdown from client to Server
	
	public DHCPMessageType type;

	public byte[] getBytes() {
		switch (this.type) {
			case DHCPDISCOVER: return Utils.toBytes(0, 3);
			case DHCPOFFER: return Utils.toBytes(1, 3);
			case DHCPREQUEST: return Utils.toBytes(2, 3);
			case DHCPACK: return Utils.toBytes(3, 3);
			case DHCPNAK: return Utils.toBytes(4, 3);
			default: return Utils.toBytes(5, 3);
		}
	}
	
	public static DHCPMessageType getType(byte[] bytes) {
		switch (Utils.fromBytes(bytes)) {
			case 0: return DHCPDISCOVER;
			case 1: return DHCPOFFER;
			case 2: return DHCPREQUEST;
			case 3: return DHCPACK;
			case 4: return DHCPNAK;
			default: return DHCPRELEASE;
		}
	}
}
