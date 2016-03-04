import java.net.*;
import java.util.Random;


public class DHCPFunctions {
	//TODO: vragen of Random gebruikt mag worden
	static Random rand = new Random();

	public static void DHCPDiscover(DatagramSocket socket){
		//op:		1 (request)
		//htype: 	1 (ethernet)
		//hlen:		6 (IEEE 802 MAC addresses)
		//hops:		0
		//xid:		random
		//sec:		0 (//TODO: wat moet hier eigenlijk)
		//flags		-32768 (2's complement decimaal voor 1000 0000 0000 0000 , de broadcast flag)
		//CIP		0 (Client heeft nog geen IP)
		//YI		byte[4]
		//SI		byte[4] of 255.255.255.255 //TODO: moet hier die 255?
		//GI		byte[4] (niet gebruikt door clients)
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64]
		//BootFile	byte[128] 
		//Options
		byte[] transactionID = new byte[4];
		rand.nextBytes(transactionID); //random transactionID van 4 bytes
		int sec = 0; //TODO: nog geen idee wat we we hier mee moeten doen
		byte[] CHA = new byte[16]; //TODO: wat moet hier?

		DHCPMessage discoverMessage = new DHCPMessage(Utils.toBytes(1, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0), 
				transactionID, Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), new byte[4], new byte[4], 
				new byte[4], CHA, new byte[64], new byte[128], new byte[0]);

		broadcastMessage(discoverMessage, 18765, socket); //67 is UDP poort voor DHCP server: Client -> server communication
		System.out.println("DHCPDiscover message broadcasted by me (Client)");
	}

	public static void DHCPOffer(DatagramSocket socket, byte[] transactionID, InetAddress yourIP, InetAddress serverIP, int clientPort) {
		//op:		2 (reply)
		//htype: 	1 (ethernet)
		//hlen:		6 (IEEE 802 MAC addresses)
		//hops:		0
		//xid:		vorig transactieID
		//sec:		0 (//TODO: wat moet hier eigenlijk)
		//flags		-32768 (2's complement decimaal voor 1000 0000 0000 0000 , de broadcast flag)
		//CIP		0 (Client heeft nog geen IP)
		//YI		byte[4]
		//SI		byte[4] of 255.255.255.255 //TODO: moet hier die 255?
		//GI		byte[4] (niet gebruikt door clients) //TODO: wel voor servers???
		//Client Hardware Address (MAC)	bv 01:23:45:67:89:ab (16 bytes)
		//SName		byte[64]
		//BootFile	byte[128] 
		//Options
		int sec = 0; //TODO: nog geen idee wat we we hier mee moeten doen
		byte[] CHA = new byte[16]; //TODO: wat moet hier?
		
		DHCPMessage discoverMessage = new DHCPMessage(Utils.toBytes(2, 1), Utils.toBytes(1, 1), Utils.toBytes(6, 1), Utils.toBytes(0), 
				transactionID, Utils.toBytes(sec, 2), Utils.toBytes(-32768, 2), Utils.toBytes(0), yourIP.getAddress(), serverIP.getAddress(), 
				new byte[4], CHA, new byte[64], new byte[128], new byte[0]);

		broadcastMessage(discoverMessage, clientPort, socket); //normaal is 68 UDP poort voor DHCP client
		System.out.println("DHCPOffer message broadcasted by me (Server)");
	}

	public void DHCPRequest() {
		System.out.println("DHCPRequest message broadcasted by me (Client)");
	}

	public void DHCPAck(){
		System.out.println("DHCPAcknowledge message broadcasted by me (Server)");
	}

	public void DHCPNak() {
		System.out.println("DHCPNak message unicasted by me (Server)");
	}

	public void DHCPRelease() {
		System.out.println("DHCPRelease message unicasted by me (Client)");
	}

	public static void broadcastMessage(DHCPMessage message, int deliveryPort, DatagramSocket socket){
		try {
			byte[] msg = message.makeMessage();
			InetAddress broadcast = InetAddress.getByName("255.255.255.255");
			DatagramPacket sendPacket = new DatagramPacket(msg, msg.length, broadcast, deliveryPort);
			socket.send(sendPacket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
