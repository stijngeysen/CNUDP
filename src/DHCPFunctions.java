import java.net.*;
import java.util.Random;


public class DHCPFunctions{
	static Random rand = new Random();

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
		
		System.out.println(Utils.toHexString(discoverMessage.makeMessage()));
		
		broadcastMessage(socket, discoverMessage, 1234); //67 is UDP poort voor DHCP server: Client -> server communication
		System.out.println("DHCPDiscover message broadcasted by me (Client)");
		System.out.println("Option field was: ");
		System.out.println(Utils.toHexString(discoverMessage.getOptions()));
		System.out.println("The transactionID was: " + Utils.fromBytes(discoverMessage.getTransactionID()));
	}

	public static void DHCPOffer(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP, int IPLeaseTime) {
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
		System.arraycopy(DHCPMessage.makeMessageLeaseTimeOption(IPLeaseTime)
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
		System.out.println("Option field was: ");
		System.out.println(Utils.toHexString(message.getOptions()));
	}

	public static void DHCPRequest(DatagramSocket socket, DHCPMessage message, DatagramPacket packet) {
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
		System.arraycopy(DHCPMessage.makeMessageLeaseTimeOption(10000), 0, options, 13, 6); //TODO: hier toegevoegd, 1000 leasetime voorlopig hardcoded
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
		System.out.println("Option field was: ");
		System.out.println(Utils.toHexString(requestMessage.getOptions()));
		System.out.println("The transactionID was: " + Utils.fromBytes(requestMessage.getTransactionID()));
	}
	
	public static void DHCPExtendedRequest(DatagramSocket socket, DHCPMessage message, DatagramPacket packet) {
		//TODO:
		// UIT ietf.org:
		// Client Hardware Address in requested Ip address (optie 50)
		// ciaddr (CIP) moet leeg zijn
		// client ID moet zelfde zijn als bij process waarbij IP bekomen werd als gebruikt
		// => het enige verschil met regular Request dat ik zie is dat requeste IP == CHA 
		//		(dus mogelijk kunnen deze 2 functies samengevoegd worden, maar ik hou ze nog even apart om het testen te vereenvoudigen)
		// maar dit werkt niet, werkt wel als CHA vervangen door YourIP, maar dan is deze functie net hetzelfde als de normale Request.
		// Geen idee of dit ook de bedoeling is
		
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
		System.arraycopy(DHCPMessage.makeMessageLeaseTimeOption(10000), 0, options, 13, 6); //TODO: hier toegevoegd, 1000 leasetime voorlopig hardcoded
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP())
				, 0, options, 19, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 25, 1);	
		
		DHCPMessage extendedRequestMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(0, 2), message.getClientIP(), new byte[4], message.getServerIP(), 
				new byte[4], message.getClientHardwareAddress(), message.getServerHostName(), new byte[128], options);

		if (message.getFlags()[0] == 1) {
			broadcastMessage(socket, extendedRequestMessage, packet.getPort());
		} else {
			unicastMessage(socket, extendedRequestMessage, packet.getPort(), packet.getAddress());
		}
		System.out.println("DHCPExtendedRequest message broadcasted by me (Client)");
		System.out.println("Option field was: ");
		System.out.println(Utils.toHexString(extendedRequestMessage.getOptions()));
		System.out.println("The transactionID was: " + Utils.fromBytes(extendedRequestMessage.getTransactionID()));
	}

	public static void DHCPAck(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP, int IPLeaseTime){
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
		System.arraycopy(DHCPMessage.makeMessageLeaseTimeOption(IPLeaseTime)
				, 0, options, 7, 6);
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP()) //TODO: wrs niet getServerIP()??
				, 0, options, 13, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 19, 1);
		
		DHCPMessage acknowledgeMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], options);
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, acknowledgeMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, acknowledgeMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
		}
		System.out.println("DHCPAcknowledge message broadcasted by me (Server)");
		System.out.println("Option field was: ");
		System.out.println(Utils.toHexString(message.getOptions()));
	}

	public static void DHCPNak(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP) {
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
		//GI		byte[4]
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64]
		//BootFile	byte[128]
		//Options
		int sec = 0;
		byte[] CHA = message.getClientHardwareAddress();
		
		byte[] options = new byte[14];
		System.arraycopy(DHCPMessage.makeMagicCookie()
				, 0, options, 0, 4);
		System.arraycopy(DHCPMessage.makeMessageTypeOption(DHCPMessageType.DHCPNAK)
				, 0, options, 4, 3);
		System.arraycopy(DHCPMessage.makeMessageIDOption(54, message.getServerIP()) //TODO: wrs niet getServerIP()??
				, 0, options, 7, 6);
		System.arraycopy(DHCPMessage.makeEndOption(), 0, options, 13, 1);
		
		DHCPMessage negativeAcknowledgeMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], options);
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, negativeAcknowledgeMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, negativeAcknowledgeMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
		}
		System.out.println("DHCPNak message unicasted by me (Server)");
		System.out.println("Option field was: ");
		System.out.println(Utils.toHexString(negativeAcknowledgeMessage.getOptions()));
	}

	public static void DHCPRelease() { //TODO
		System.out.println("DHCPRelease message unicasted by me (Client)");
		System.out.println("Not yet implemented");
	}

	public static void broadcastMessage(DatagramSocket socket, DHCPMessage message, int deliveryPort){
		try {
			byte[] msg = message.makeMessage();
			InetAddress broadcast = InetAddress.getByName("10.33.14.246"); // 255.255.255.255		10.33.14.246
			DatagramPacket sendPacket = new DatagramPacket(msg, msg.length, broadcast, deliveryPort);
			socket.send(sendPacket);
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


