import java.net.*;
//import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * DHCP Client
 * Client which sends discover, request, extended requests and release messages.
 * 
 * @author Geysen Stijn & Moons Marnix
 *
 */
class DHCPClient
{
	//Client Port (Server -> client communication, normally 68)
	protected static int port = 1235; 
	//packet length (min 236 + options)
	static int lengte = 512;
	
	
	public static void main(String args[]) throws Exception
	{
		DatagramSocket socket = new DatagramSocket(port);
		while(! socket.isClosed()){
			System.out.println();
			System.out.println("CLIENT STEP 1: SEND DISCOVER: ");
			DHCPFunctions.DHCPDiscover(socket);
			System.out.println();
			
			System.out.println();
			System.out.println("Client STEP 2: RECEIVE OFFER: ");
			System.out.println();
			//initialize empty data arrays (if this is placed outside the while loop,
			//the byte array will contain bytes of previous, longer messages if a short messages
			//has to be processed
			byte[] receiveData = new byte[lengte];

			DatagramPacket receivePacket = new DatagramPacket(receiveData, lengte);
			//Receive the response from server
			socket.receive(receivePacket);
			
			//process data
			byte[] msg = receivePacket.getData();
			DHCPMessage message = new DHCPMessage(msg);
			//System.out.println(Utils.toHexString(msg));
			if (Utils.fromBytes(message.getMessageOption(53)) != 2) {
				System.out.println("ERROR: No DHCPOffer received.");
				continue;
			}
			
			//Data information
			System.out.println();
//			System.out.println("HEX CODE Offer Packet");
//			System.out.println(Utils.toHexString(message.makeMessage()));
//			System.out.println();
			System.out.println("DATA INFORMATION Offer Packet");
			System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
			System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
			System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
			System.out.println("Your IP: " + InetAddress.getByAddress(message.getYourIP()));
			System.out.println("LeaseTime: " + Utils.fromBytes(message.getMessageOption(51)));			
			System.out.println("Server IP: " + InetAddress.getByAddress(message.getServerIP()));
			System.out.println();
			System.out.println();
			
			//Requesting
			while (true) {
				System.out.println("CLIENT STEP 3: SEND REQUEST: ");
				System.out.println();
				DHCPFunctions.DHCPRequest(socket, message, receivePacket, new byte[4]);
				System.out.println();
				
				System.out.println("CLIENT STEP 4: RECEIVE ACK/ NAK: ");
				System.out.println();
				//initialize empty data arrays (if this is placed outside the while loop,
				//the byte array will contain bytes of previous, longer messages if a short messages
				//has to be processed
				byte[] requestData = new byte[lengte];
	
				DatagramPacket requestPacket = new DatagramPacket(requestData, lengte);
				//Receive the response from server
				socket.receive(requestPacket);
				
				//process data
				byte[] msg2 = requestPacket.getData();
				message = new DHCPMessage(msg2);
				if (Utils.fromBytes(message.getMessageOption(53)) != 5) {
					System.out.println("ERROR: Negative acknowledge received.");
					continue;
				}
				
				//Data information
//				System.out.println();
//				System.out.println("HEX CODE ACK Packet");
//				System.out.println(Utils.toHexString(message.makeMessage()));
				System.out.println();
				System.out.println("DATA INFORMATION Ack Packet");
				System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
				System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
				System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
				System.out.println("Your IP: " + InetAddress.getByAddress(message.getYourIP()));
				System.out.println("LeaseTime: " + Utils.fromBytes(message.getMessageOption(51)));
				System.out.println("Server IP: " + InetAddress.getByAddress(message.getServerIP()));
				System.out.println("Seconds:  " + Utils.fromBytes(message.getNumberOfSeconds()));
				System.out.println();
				System.out.println();
				
				break;
			}
			
			//Extended requesting
			//Requesting
			int nrExtend = 5; //nr of times you want to extend the IP
			for (int i=0; i<nrExtend; i++) {
				System.out.println("CLIENT STEP 5: EXTEND REQUEST: ");
				System.out.println();
				TimeUnit.SECONDS.sleep(5/2); //om niet te lang te moeten wachten IPLeasetime veranderd in 10 sec

				DHCPFunctions.DHCPRequest(socket, message, receivePacket, message.getYourIP());


				System.out.println();
				System.out.println("CLIENT STEP 6: RECEIVE EXTENDREQUEST ANSWER: ");
				System.out.println();
				//initialize empty data arrays (if this is placed outside the while loop,
				//the byte array will contain bytes of previous, longer messages if a short messages
				//has to be processed
				byte[] extendedRequestData = new byte[lengte];

				DatagramPacket extendedRequestPacket = new DatagramPacket(extendedRequestData, lengte);
				//Receive the response from server
				socket.receive(extendedRequestPacket);

				//process data
				msg = extendedRequestPacket.getData();

//				System.out.println("HEX CODE Extend Request Answer");
//				System.out.println(Utils.toHexString(message.makeMessage()));
				System.out.println();
				message = new DHCPMessage(msg);
				System.out.println("Your IP: " + InetAddress.getByAddress(message.getYourIP()));
				if (Utils.fromBytes(message.getMessageOption(53)) != 5) {
					System.out.println("ERROR: Negative acknowledge received.");
					break;
				}
			}
				
				TimeUnit.SECONDS.sleep(5/2); // nog eens slapen alvoor release te sturen
				
				System.out.println();
				System.out.println("CLIENT STEP 7: RELEASE IP: ");
				System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
				System.out.println();
				
				//Releasing
				DHCPFunctions.DHCPRelease(socket, message, receivePacket);
			
			
			
			
			
//			//Close socket
//			if(! Arrays.equals(msg2, new byte[0])){
//				System.out.println("Socket Closing");
//				socket.close();
//			}
			
		}
	}
}
