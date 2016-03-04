
public enum DHCPMessageType 
{   DHCPDISCOVER,	//a client broadcasts to locate servers
	DHCPOFFER,	    //a server offers an IP address to the device
	DHCPREQUEST,		//client accepts offers from DHCP server
	DHCPACK,			//server to client + committed IP address
	DHCPNAK,			//server to client to state net address incorrect
	DHCPRELEASE;		//graceful shutdown from client to Server

	private DHCPMessageType() {
	}
}
