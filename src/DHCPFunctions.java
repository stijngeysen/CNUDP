import java.net.*;
import java.util.Random;


public class DHCPFunctions{
	//TODO: vragen of Random gebruikt mag worden
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
		int sec = 0; //TODO: nog geen idee wat we we hier mee moeten doen --> 0 is juist, pas na acknowledge wordt hier gebruik van gemaakt
		byte[] CHA = new byte[16];
		rand.nextBytes(CHA);
/*		try {
			InetAddress IP = InetAddress.getLocalHost();
			NetworkInterface network = NetworkInterface.getByInetAddress(IP);
			CHA = network.getHardwareAddress();
		} catch (Exception e) {
			e.printStackTrace();
		}*/ //We tried to get the Hardware Address
		System.out.println("DHCPtype");
		DHCPMessageType.DHCPACK.getBytes();
		DHCPMessage discoverMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				transactionID, Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), new byte[4], new byte[4], 
				new byte[4], CHA, new byte[64], new byte[128], DHCPMessageType.DHCPDISCOVER.getBytes());
		System.out.println(Utils.toHexString(discoverMessage.makeMessage()));
		broadcastMessage(socket, discoverMessage, 1234); //67 is UDP poort voor DHCP server: Client -> server communication
		System.out.println("DHCPDiscover message broadcasted by me (Client)");
	}

	public static void DHCPOffer(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP) {
		//TODO: SI en GI heb ik mijn twijfels nog over
		//op:		2 (reply)
		//htype: 	1 (ethernet)
		//hlen:		6 (IEEE 802 MAC addresses)
		//hops:		0
		//xid:		vorig transactieID
		//sec:		0
		//flags		-32768 (2's complement decimaal voor 1000 0000 0000 0000 , de broadcast flag)
		//CIP		0 (Client heeft nog geen IP)
		//YI		server's IP
		//SI		server's IP of 255.255.255.255 //TODO: moet hier die 255? --> nee, deze code wordt enkel gebruikt voor broadcast
		//GI		byte[4] (niet gebruikt door clients) //TODO: wel voor servers??? --> hangt volgens mij van de verbinding af, dus wel als er sprake is van een router bv
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64]
		//BootFile	byte[128]
		//Options
		int sec = 0; //TODO: nog geen idee wat we we hier mee moeten doen --> blijft 0, wordt pas na ack gebruikt
		byte[] CHA = message.getClientHardwareAddress();
		
		DHCPMessage offerMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], DHCPMessageType.DHCPOFFER.getBytes());
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, offerMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, offerMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
		}
		System.out.println("DHCPOffer message broadcasted by me (Server)");
	}

	public void DHCPRequest(DatagramSocket socket, DHCPMessage message, DatagramPacket packet) {
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
		DHCPMessage requestMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(0, 2), Utils.toBytes(0), new byte[4], message.getServerIP(), 
				new byte[4], message.getClientHardwareAddress(), message.getServerHostName(), new byte[128], DHCPMessageType.DHCPREQUEST.getBytes());

		if (message.getFlags()[0] == 1) {
			broadcastMessage(socket, requestMessage, packet.getPort());
		} else {
			unicastMessage(socket, requestMessage, packet.getPort(), packet.getAddress());
		}
		System.out.println("DHCPRequest message broadcasted by me (Client)");
	}

	public void DHCPAck(DatagramSocket socket, DHCPMessage message, DatagramPacket packet, InetAddress yourIP){
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
		int sec = 0; //TODO: nog geen idee wat we we hier mee moeten doen --> blijft 0, wordt pas na ack gebruikt
		byte[] CHA = message.getClientHardwareAddress();
		
		DHCPMessage acknowledgeMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0, 1), 
				message.getTransactionID(), Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), yourIP.getAddress(), socket.getLocalAddress().getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], DHCPMessageType.DHCPACK.getBytes());
		if (message.getFlags()[0] == 1) { //1e bit van flags = 1 --> broadcast
			broadcastMessage(socket, acknowledgeMessage, packet.getPort()); //normaal is 68 UDP poort voor DHCP client
		} else { // 1e bit van flags = 0 --> unicast
			unicastMessage(socket, acknowledgeMessage, packet.getPort(), packet.getAddress()); //normaal is 68 UDP poort voor DHCP client
		}
		System.out.println("DHCPAcknowledge message broadcasted by me (Server)");
	}

	public void DHCPNak() {
		System.out.println("DHCPNak message unicasted by me (Server)");
	}

	public void DHCPRelease() {
		System.out.println("DHCPRelease message unicasted by me (Client)");
	}

	public static void broadcastMessage(DatagramSocket socket, DHCPMessage message, int deliveryPort){
		try {
			byte[] msg = message.makeMessage();
			InetAddress broadcast = InetAddress.getByName("10.33.14.246"); // 255.255.255.255
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


