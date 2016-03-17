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
 * 		- flags		2 bytes		0x8000 (broadcast) or 0x0000 (unicast)
 * 		- CIP		4 bytes		Client IP address
 * 		- YI		4 bytes		'your' (client) IP address
 * 		- SI		4 bytes		IP address of next server to use in bootstrap
 * 		- GI		4 bytes		Relay agent IP address
 * 		- CHA		4 bytes		Client hardware address (MAC)
 * 		- SName		64 bytes	Optional server host name
 * 		- BootFile	128 bytes	Boot file name
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
	 * 		- flags		-32768 (broadcast)
	 * 		- CIP		0
	 * 		- YI		byte[4]
	 * 		- SI		255.255.255.255 is normally used for broadcast, but for not disrupting the network infrastructure we use 10.33.14.246
	 * 		- GI		byte[4]
	 * 		- CHA		random
	 * 		- SName		byte[64]
	 * 		- BootFile	byte[128]
	 * 		- Options	53
	 * Option 53: DHCPDISCOVER
	 * 
	 * @param socket
	 */
	public static void DHCPDiscover(DatagramSocket socket){
		byte[] transactionID = new byte[4];
		rand.nextBytes(transactionID); //random transactionID of 4 bytes
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
		
		broadcastMessage(socket, discoverMessage, 1234); //67 is UDP poort voor DHCP server: Client -> server communication
		System.out.println("DHCPDiscover message broadcasted by me (Client)");
		System.out.println("The transactionID was: " + Utils.fromBytes(discoverMessage.getTransactionID()));
	}

	/**
	 * DHCP Offer
	 * Server to client in response to DHCPDISCOVER with offer of configuration parameters.
	 * 		- op		2 (reply)
	 * 		- htype		1 for 10mb ethernet
	 * 		- hlen		6 for 10mb ethernet
	 * 		- hops		0
	 * 		- xid		previous transactionID
	 * 		- sec		0
	 * 		- flags		received flag options
	 * 		- CIP		0
	 * 		- YI		'client's IP'
	 * 		- SI		Server's IP or 255.255.255.255 for broadcast
	 * 		- GI		byte[4]
	 * 		- CHA		client's CHA
	 * 		- SName		byte[64]
	 * 		- BootFile	byte[128]
	 * 		- Options	51, 53, 54
	 * Option 51: set lease time
	 * Option 53: DHCPOFFER
	 * Option 54: set serverIP
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 * @param yourIP
	 */
	public static void DHCPOffer(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP) {
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
				message.getTransactionID(), Utils.toBytes(sec, 2), message.getFlags(), Utils.toBytes(0), yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], options);
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, offerMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
			System.out.println("DHCPOffer message broadcasted by me (Server)");
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, offerMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
			System.out.println("DHCPOffer message unicasted by me (Server)");
		}
	}
	
	/**
	 * DHCP Request
	 * Client message to servers either (a) requesting offered parameters from one server and implicitly declining offers from all others, 
	 * (b) confirming correctness of previously allocated address after, e.g., system reboot, or (c) extending the lease on a particular network address.
	 * 		- op		1 (request)
	 * 		- htype		1 for 10mb ethernet
	 * 		- hlen		6 for 10mb ethernet
	 * 		- hops		0
	 * 		- xid		previous transactionID
	 * 		- sec		0
	 * 		- flags		received flag options
	 * 		- CIP		yourIP
	 * 		- YI		byte[4]
	 * 		- SI		Server's IP or 255.255.255.255 for broadcast
	 * 		- GI		byte[4]
	 * 		- CHA		client's CHA
	 * 		- SName		byte[64]
	 * 		- BootFile	byte[128]
	 * 		- Options	50, 53, 54
	 * Option 50: 'client's IP address'
	 * Option 53: DHCPREQUEST
	 * Option 54: set serverIP
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 * @param yourIP
	 */
	public static void DHCPRequest(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, byte[] yourIP) {		
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
				message.getTransactionID(), Utils.toBytes(sec, 2), message.getFlags(), yourIP, new byte[4], message.getServerIP(), 
				new byte[4], message.getClientHardwareAddress(), message.getServerHostName(), new byte[128], options);

		if (message.getFlags()[0] == 1) {
			broadcastMessage(socket, requestMessage, packet.getPort());
			if (Utils.fromBytes(requestMessage.getMessageOption(50)) == Utils.fromBytes(requestMessage.getClientIP()))
			{
				System.out.println("DHCPExtendedRequest message broadcasted by me (Client)");
			} else {
				System.out.println("DHCPRequest message broadcasted by me (Client)");
			}
		} else {
			unicastMessage(socket, requestMessage, packet.getPort(), packet.getAddress());
			if (Utils.fromBytes(requestMessage.getMessageOption(50)) == Utils.fromBytes(requestMessage.getClientIP()))
			{
				System.out.println("DHCPExtendedRequest message unicasted by me (Client)");
			} else {
				System.out.println("DHCPRequest message unicasted by me (Client)");
			}
		}
	}

	/**
	 * DHCP Acknowledge
	 * Server to client with configuration parameters, including committed network address.
	 * 		- op		2 (reply)
	 * 		- htype		1 for 10mb ethernet
	 * 		- hlen		6 for 10mb ethernet
	 * 		- hops		0
	 * 		- xid		previous transactionID
	 * 		- sec		0
	 * 		- flags		received flags options
	 * 		- CIP		client IP address
	 * 		- YI		'client's IP'
	 * 		- SI		Server's IP or 255.255.255.255 for broadcast
	 * 		- GI		byte[4]
	 * 		- CHA		client's CHA
	 * 		- SName		byte[64]
	 * 		- BootFile	byte[128]
	 * 		- Options	51, 53, 54
	 * Option 51: set lease time
	 * Option 53: DHCPACK
	 * Option 54: set serverIP
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 * @param yourIP
	 * @param clientIP
	 */
	public static void DHCPAck(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP, byte[] clientIP){
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
				message.getTransactionID(), Utils.toBytes(sec, 2), message.getFlags(), clientIP, yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], options);
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, acknowledgeMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
			System.out.println("DHCPAcknowledge message broadcasted by me (Server)");
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, acknowledgeMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
			System.out.println("DHCPAcknowledge message unicasted by me (Server)");
		}
	}

	/**
	 * DHCP Negative Acknowledge
	 * Server to client indicating client's notion of network address is incorrect (e.g., client has moved to new subnet) or client's lease as expired
	 * 		- op		2 (reply)
	 * 		- htype		1 for 10mb ethernet
	 * 		- hlen		6 for 10mb ethernet
	 * 		- hops		0
	 * 		- xid		previous transactionID
	 * 		- sec		0
	 * 		- flags		received flag options
	 * 		- CIP		client IP address
	 * 		- YI		'client's IP'
	 * 		- SI		Server's IP or 255.255.255.255 for broadcast
	 * 		- GI		byte[4]
	 * 		- CHA		client's CHA
	 * 		- SName		byte[64]
	 * 		- BootFile	byte[128]
	 * 		- Options	53, 54
	 * Option 53: DHCPNAK
	 * Option 54: set serverIP
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 * @param yourIP
	 * @param clientIP
	 */
	public static void DHCPNak(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP, byte[] clientIP) {
		int sec = 0;
		byte[] CHA = message.getClientHardwareAddress();
		
		byte[] options = new byte[14];
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPNAK)
				, 0, options, 4, 3);
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 7, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 13, 1);
		
		DHCPMessage negativeAcknowledgeMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), message.getFlags(), clientIP, yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], options);
		
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, negativeAcknowledgeMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
			System.out.println("DHCPNak message broadcasted by me (Server)");
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, negativeAcknowledgeMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
			System.out.println("DHCPNak message unicasted by me (Server)");
		}
//		System.out.println("Option field was: ");
//		System.out.println(Utils.toHexString(negativeAcknowledgeMessage.getOptions()));
	}

	/**
	 * DHCP Release
	 * Client to server relinquishing network address and cancelling remaining lease.
	 * 		- op		1 (request)
	 * 		- htype		1 for 10mb ethernet
	 * 		- hlen		6 for 10mb ethernet
	 * 		- hops		0
	 * 		- xid		previous transactionID
	 * 		- sec		0
	 * 		- flags		received flag options
	 * 		- CIP		client IP address
	 * 		- YI		byte[4]
	 * 		- SI		Server's IP or 255.255.255.255 for broadcast
	 * 		- GI		byte[4]
	 * 		- CHA		client's CHA
	 * 		- SName		byte[64]
	 * 		- BootFile	byte[128]
	 * 		- Options	53, 54
	 * Option 53: DHCPRELEASE
	 * Option 54: set serverIP
	 * 
	 * @param socket
	 * @param message
	 * @param packet
	 */
	public static void DHCPRelease(DatagramSocket socket, DHCPMessage message, DatagramPacket packet) {
		int sec = 0;
		
		byte[] options = new byte[14];
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPRELEASE)
				, 0, options, 4, 3);
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 7, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 13, 1);	
		
		DHCPMessage releaseMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), message.getFlags(), message.getClientIP(), new byte[4], message.getServerIP(), 
				new byte[4], message.getClientHardwareAddress(), message.getServerHostName(), new byte[128], options);

		//message is the received message, not the newly constructed one
		unicastMessage(socket, releaseMessage, packet.getPort(), packet.getAddress());	
		System.out.println("DHCPRelease message unicasted by me (Client)");
//		System.out.println("HEX: ");
//		System.out.println(Utils.toHexString(releaseMessage.makeMessage()));
	}

	/**
	 * Broadcast Message
	 * Broadcast not allowed in labs, so it is replaced by a unicast.
	 * 
	 * @param socket
	 * @param message
	 * @param deliveryPort
	 */
	public static void broadcastMessage(DatagramSocket socket, DHCPMessage message, int deliveryPort){
		try {
			InetAddress broadcast = InetAddress.getByName("0.0.0.0"); // 255.255.255.255		10.33.14.246     0.0.0.0
			unicastMessage(socket, message, deliveryPort, broadcast);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Unicast Message
	 * 
	 * 
	 * @param socket
	 * @param message
	 * @param port
	 * @param address
	 */
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


