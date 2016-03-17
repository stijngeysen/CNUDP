import java.net.*;
import java.util.Random;

/**
 * DHCP Functions
 * Class which contains operations a DHCP-server or -client use.
 * All implemented transactions: Discover, offer, request, acknowledge, negative acknowledge & release.
 * These transactions all have a standard format:
 * 		- op		1 byte		Message op code / message type (1 = BOOTREQUEST, 2 = BOOTREPLY)
 * 		- htype		1 byte		Hardware address type (1 for 10mb ethernet)
 * 		- hlen		1 byte		Hardware address length (6 for 10mb ethernet)
 * 		- hops		1 byte		0, optionally used by relay agents
 * 		- xid		4 bytes		Transaction ID, a random number chosen by the client
 * 		- sec		2 bytes		Filled in by client, seconds elapsed since client began address acquisition or renewal process
 * 		- flags		2 bytes		-32768 (2's complement decimal for 1000 0000 0000 0000 , the broadcast flag)
 * 		- CIP		4 bytes		Client IP address
 * 		- YI		4 bytes		'your' (client) IP address
 * 		- SI		4 bytes		IP address of next server to use in bootstrap
 * 		- GI		4 bytes		Relay agent IP address
 * 		- CHA		4 bytes		Client hardware address (MAC)
 * 		- SName		16 bytes	Optional server host name
 * 		- BootFile	32 bytes	Boot file name
 * 		- Options	  var		Optional parameters field, see below
 * Options begin with a magic cookie (hex 63.82.53.63) and end with code 255.
 * All options used in this project:
 * 		- 50	Requested IP Address	Used in DHCPRequest
 * 		- 51	IP Adress Lease Time	Used in DHCPOffer and DHCPAck
 * 		- 53	DHCP Message Type		Used everywhere
 * 		- 54	Server Identifier		Used in DHCPOffer, DHCPRequest, DHCPAck, DHCPNak and DHCPRelease
 * 
 * @author Geysen Stijn & Moons Marnix
 *
 */
public class DHCPFunctions{
	static Random rand = new Random();
	static int leaseTime = 5;

	/**
	 * DHCP Discover
	 * Client broadcast to locate available servers.
	 * 		- op		1 (request)
	 * 		- htype		1 for 10mb ethernet
	 * 		- hlen		6 for 10mb ethernet
	 * 		- hops		0
	 * 		- xid		random
	 * 		- sec		0
	 * 		- flags		-32768
	 * 		- CIP		0
	 * 		- YI		byte[4]
	 * 		- SI		255.255.255.255 is normally used for broadcast, but for not disrupting the network infrastructure we use 10.33.14.246
	 * 		- GI		byte[4]
	 * 		- CHA		random
	 * 		- SName		byte[64]
	 * 		- BootFile	byte[128]
	 * 		- Options	53
	 * Options 
	 * @param socket
	 */
	public static void DHCPDiscover(DatagramSocket socket){
		//op:		1 (request) (1 = bootrequest, 2 = bootreply)
		//htype: 	1 (ethernet) (hardware address type)
		//hlen:		6 (IEEE 802 MAC addresses) (hardware address length)
		//hops:		0 (optionnaly used by relay agents)
		//xid:		random (transaction id)
		//sec:		0 (seconds elapsed since client began address acquisition or renewal process)
		//flags		-32768 (2's complement decimaal voor 1000 0000 0000 0000 , de broadcast flag)
		//CIP		0 (Client heeft nog geen IP)
		//YI		byte[4] (your IP-address)
		//SI		255.255.255.255 is normally used for broadcast, but for not disrupting the network infrastructure we use 10.33.14.246
		//GI		byte[4] (niet gebruikt door clients) (relay agent IP address)
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64] (optional server name)
		//BootFile	byte[128]
		//Options	messageType (code 53)
		byte[] transactionID = new byte[4];
		rand.nextBytes(transactionID); //random transactionID van 4 bytes
		int sec = 0;
		byte[] CHA = new byte[16];
		rand.nextBytes(CHA);
/*		try {
			InetAddress IP = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(IP);
			CHA = network.getHardwareAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}*/ //We tried to get the Hardware Address
		
		byte[] options = new byte[8];
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPDISCOVER)
				, 0, options, 4, 3);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 7, 1);		
		
		DHCPMessage discoverMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				transactionID, Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), new byte[4], new byte[4], 
				new byte[4], CHA, new byte[64], new byte[128], options);
		
//		System.out.println(Utils.toHexString(discoverMessage.makeMessage()));
		
		broadcastMessage(socket, discoverMessage, 1234); //67 is UDP poort voor DHCP server: Client -> server communication
		System.out.println("DHCPDiscover message broadcasted by me (Client)");
		System.out.println("The transactionID was: " + Utils.fromBytes(discoverMessage.getTransactionID()));
	}

	/**
	 * DHCP Offer
	 * Server to client in response to DHCPDISCOVER with offer of configuration parameters.
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 * @param yourIP
	 */
	public static void DHCPOffer(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP) {
		//op:		2 (reply)
		//htype: 	1 (ethernet)
		//hlen:		6 (IEEE 802 MAC addresses)
		//hops:		0
		//xid:		vorig transactieID
		//sec:		0
		//flags		-32768 (2's complement decimaal voor 1000 0000 0000 0000 , de broadcast flag)
		//CIP		0 (Client heeft nog geen IP)
		//YI		server's IP
		//SI		server's IP of 255.255.255.255
		//GI		byte[4] (niet gebruikt door clients)
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64]
		//BootFile	byte[128]
		//Options
		int sec = 0;
		byte[] CHA = message.getClientHardwareAddress();
		
		byte[] options = new byte[20];
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPOFFER)
				, 0, options, 4, 3);
		System.arraycopy(DHCPMessage.makeMessageLeaseTimeOption(leaseTime)
				, 0, options, 7, 6);
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 13, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 19, 1);	
		
		DHCPMessage offerMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], options);
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, offerMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, offerMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
		}
		System.out.println("DHCPOffer message broadcasted by me (Server)");
	}

/*	public static void DHCPRequest(DatagramSocket socket, DHCPMessage message, DatagramPacket packet) {
		//op:		1 (request) (1 = bootrequest, 2 = bootreply)
		//htype: 	1 (ethernet) (hardware address type)
		//hlen:		6 (IEEE 802 MAC addresses) (hardware address length)
		//hops:		0 (optionnaly used by relay agents)
		//xid:		vorig transactieID
		//sec:		0 (seconds elapsed since client began address acquisition or renewal process)
		//flags		0x8000 (broadcast) of 0x0000 (unicast)
		//CIP		0 (Client heeft nog geen IP)
		//YI		byte[4] (your IP-address)
		//SI		unicastaddress naar server
		//GI		byte[4] (niet gebruikt door clients) (relay agent IP address)
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64] (optional server name)
		//BootFile	byte[128]
		//Options	var
		int sec = 0;
		
		byte[] options = new byte[26];
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPREQUEST)
				, 0, options, 4, 3);
		System.arraycopy(DHCPMessage.makeMessageIDOption(50, message.getYourIP())
				, 0, options, 7, 6);
		System.arraycopy(DHCPMessage.makeMessageLeaseTimeOption(10000), 0, options, 13, 6); //hier toegevoegd, 1000 leasetime voorlopig hardcoded
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 19, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 25, 1);	
		
		DHCPMessage requestMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(0, 2), Utils.toBytes(0), new byte[4], message.getServerIP(), 
				new byte[4], message.getClientHardwareAddress(), message.getServerHostName(), new byte[128], options);

		//message is the received message, not the newly constructed one
		if (message.getFlags()[0] == 1) {
			broadcastMessage(socket, requestMessage, packet.getPort());
		} else {
			unicastMessage(socket, requestMessage, packet.getPort(), packet.getAddress());
		}
		System.out.println("DHCPRequest message broadcasted by me (Client)");
	}*/
	
	/**
	 * DHCP Request
	 * Client message to servers either (a) requesting offered parameters from one server and implicitly declining offers from all others, 
	 * (b) confirming correctness of previously allocated address after, e.g., system reboot, or (c) extending the lease on a particular network address.
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 * @param yourIP
	 */
	public static void DHCPRequest(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, byte[] yourIP) {		
		//op:		1 (request) (1 = bootrequest, 2 = bootreply)
		//htype: 	1 (ethernet) (hardware address type)
		//hlen:		6 (IEEE 802 MAC addresses) (hardware address length)
		//hops:		0 (optionnaly used by relay agents)
		//xid:		vorig transactieID
		//sec:		0 (seconds elapsed since client began address acquisition or renewal process)
		//flags		0x8000 (broadcast) of 0x0000 (unicast)
		//CIP		0 (Client heeft nog geen IP)
		//YI		byte[4] (your IP-address)
		//SI		unicastaddress naar server
		//GI		byte[4] (niet gebruikt door clients) (relay agent IP address)
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64] (optional server name)
		//BootFile	byte[128]
		//Options	var
		int sec = 0;
		
		byte[] options = new byte[26];
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPREQUEST)
				, 0, options, 4, 3);
		System.arraycopy(DHCPMessage.makeMessageIDOption(50, message.getYourIP()) 
				, 0, options, 7, 6);
		System.arraycopy(DHCPMessage.makeMessageLeaseTimeOption(leaseTime), 0, options, 13, 6);
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 19, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 25, 1);
		
		DHCPMessage requestMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(0, 2), yourIP, new byte[4], message.getServerIP(), 
				new byte[4], message.getClientHardwareAddress(), message.getServerHostName(), new byte[128], options);

		if (message.getFlags()[0] == 1) {
			broadcastMessage(socket, requestMessage, packet.getPort());
		} else {
			unicastMessage(socket, requestMessage, packet.getPort(), packet.getAddress());
		}
		if (Utils.fromBytes(requestMessage.getMessageOption(50)) == Utils.fromBytes(requestMessage.getClientIP()))
		{
			System.out.println("DHCPExtendedRequest message broadcasted by me (Client)");
		} else {
			System.out.println("DHCPRequest message broadcasted by me (Client)");
		}
	}

	/**
	 * DHCP Acknowledge
	 * Server to client with configuration parameters, including committed network address.
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 * @param yourIP
	 * @param clientIP
	 */
	public static void DHCPAck(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP, byte[] clientIP){
		//op:		2 (reply)
		//htype: 	1 (ethernet)
		//hlen:		6 (IEEE 802 MAC addresses)
		//hops:		0
		//xid:		vorig transactieID
		//sec:		0
		//flags		-32768 (2's complement decimaal voor 1000 0000 0000 0000 , de broadcast flag)
		//CIP		0 (Client heeft nog geen IP)  or ClientIP (for example for Nak by extend Request)
		//YI		server's IP
		//SI		server's IP of 255.255.255.255
		//GI		byte[4]
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64]
		//BootFile	byte[128]
		//Options
		int sec = 0;
		byte[] CHA = message.getClientHardwareAddress();
		
		byte[] options = new byte[20];
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPACK)
				, 0, options, 4, 3);
		System.arraycopy(DHCPMessage.makeMessageLeaseTimeOption(leaseTime)
				, 0, options, 7, 6);
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 13, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 19, 1);
		
		DHCPMessage acknowledgeMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), clientIP, yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], options);
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, acknowledgeMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, acknowledgeMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
		}
		System.out.println("DHCPAcknowledge message broadcasted by me (Server)");
//		System.out.println("Option field was: ");
//		System.out.println(Utils.toHexString(message.getOptions()));
	}

	/**
	 * DHCP Negative Acknowledge
	 * Server to client indicating client's notion of network address is incorrect (e.g., client has moved to new subnet) or client's lease as expired
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 * @param yourIP
	 * @param clientIP
	 */
	public static void DHCPNak(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP, byte[] clientIP) {
		//op:		2 (reply)
		//htype: 	1 (ethernet)
		//hlen:		6 (IEEE 802 MAC addresses)
		//hops:		0
		//xid:		vorig transactieID
		//sec:		0
		//flags		-32768 (2's complement decimaal voor 1000 0000 0000 0000 , de broadcast flag)
		//CIP		0 (Client heeft nog geen IP) or ClientIP (for example for Nak by extend Request)
		//YI		server's IP
		//SI		server's IP of 255.255.255.255
		//GI		byte[4]
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64]
		//BootFile	byte[128]
		//Options
		int sec = 0;
		byte[] CHA = message.getClientHardwareAddress();
		
		byte[] options = new byte[14];
		//magic cookie
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		//53: messageType
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPNAK)
				, 0, options, 4, 3);
		//54: serverID
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 7, 6);
		//255: end option
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 13, 1);
		
		DHCPMessage negativeAcknowledgeMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), clientIP, yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], options);
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, negativeAcknowledgeMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, negativeAcknowledgeMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
		}
		System.out.println("DHCPNak message unicasted by me (Server)");
//		System.out.println("Option field was: ");
//		System.out.println(Utils.toHexString(negativeAcknowledgeMessage.getOptions()));
	}

	/**
	 * DHCP Release
	 * Client to server relinquishing network address and cancelling remaining lease.
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 */
	public static void DHCPRelease(DatagramSocket socket, DHCPMessage message, DatagramPacket packet) {
		//op:		1 (request) (1 = bootrequest, 2 = bootreply)
		//htype: 	1 (ethernet) (hardware address type)
		//hlen:		6 (IEEE 802 MAC addresses) (hardware address length)
		//hops:		0 (optionnaly used by relay agents)
		//xid:		previous transactieID
		//sec:		0 (seconds elapsed since client began address acquisition or renewal process)
		//flags		0x0000 (unicast)
		//CIP		ClientIp
		//YI		byte[4] (your IP-address)
		//SI		unicastaddress naar server
		//GI		byte[4] (niet gebruikt door clients) (relay agent IP address)
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64] (optional server name)
		//BootFile	byte[128]
		//Options	var
		int sec = 0;
		
		//Compose the options packet
		byte[] options = new byte[14];
		//magic Cookie
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		//53: MessageType
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPRELEASE)
				, 0, options, 4, 3);
		//54: ServerID
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 7, 6);
		//255: end option
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 13, 1);	
		
		DHCPMessage releaseMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(0, 2), message.getClientIP(), new byte[4], message.getServerIP(), 
				new byte[4], message.getClientHardwareAddress(), message.getServerHostName(), new byte[128], options);

		//message is the received message, not the newly constructed one
		unicastMessage(socket, releaseMessage, packet.getPort(), packet.getAddress());	
		System.out.println("DHCPRelease message unicasted by me (Client)");
//		System.out.println("HEX: ");
//		System.out.println(Utils.toHexString(releaseMessage.makeMessage()));
	}

	public static void broadcastMessage(DatagramSocket socket, DHCPMessage message, int deliveryPort){
		try {
			InetAddress broadcast = InetAddress.getByName("0.0.0.0"); // 255.255.255.255		10.33.14.246     0.0.0.0
			unicastMessage(socket, message, deliveryPort, broadcast);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void unicastMessage(DatagramSocket socket, DHCPMessage message, int port, InetAddress address) {
		try {
			byte[] msg = message.makeMessage();
			DatagramPacket sendPacket = new DatagramPacket(msg, msg.length, address, port);
			socket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


