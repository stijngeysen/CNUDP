
public enum DHCPMessageType 
{   DHCPDISCOVER(1),		//a client broadcasts to locate servers
	DHCPOFFER(2),	    	//a server offers an IP address to the device
	DHCPREQUEST(3),		//client accepts offers from DHCP server
	DHCPACK(5),			//server to client + committed IP address
	DHCPNAK(6),			//server to client to state net address incorrect
	DHCPRELEASE(7),		//graceful shutdown from client to Server
	INVALID(8);
	
	private final int value;
	
	DHCPMessageType(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}

	public byte[] getBytes() {
		byte[] msg = new byte[4];
		System.arraycopy(Utils.toBytes(53, 1), 0, msg, 0, 1);
		System.arraycopy(Utils.toBytes(1, 1), 0, msg, 1, 1);
		switch (this.getValue()) {
			case 1: System.arraycopy(Utils.toBytes(1, 1), 0, msg, 2, 1); break;
			case 2: System.arraycopy(Utils.toBytes(2, 1), 0, msg, 2, 1); break;
			case 3: System.arraycopy(Utils.toBytes(3, 1), 0, msg, 2, 1); break;
			case 5: System.arraycopy(Utils.toBytes(5, 1), 0, msg, 2, 1); break;
			case 6: System.arraycopy(Utils.toBytes(6, 1), 0, msg, 2, 1); break;
			case 7: System.arraycopy(Utils.toBytes(7, 1), 0, msg, 2, 1); break;
			default: 
		}
		System.arraycopy(Utils.toBytes(255, 1), 0, msg, 3, 1);
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
