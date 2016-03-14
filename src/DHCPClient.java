import java.net.*;
//import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
			System.out.println("CLIENT STEP 1: SEND DISCOVER: ");
			DHCPFunctions.DHCPDiscover(socket);
			System.out.println();
			System.out.println();
			
			
			System.out.println("Client STEP 2: RECEIVE OFFER: ");
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
			if (Utils.fromBytes(message.getMessageOption(53)) != 2) {
				System.out.println("ERROR: No DHCPOffer received.");
				continue;
			}
			
			//Data information
			System.out.println();
			System.out.println("HEX CODE");
			System.out.println(Utils.toHexString(message.makeMessage()));
			System.out.println();
			System.out.println("DATA INFORMATION");
			System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
			System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
			System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
			System.out.println("Your IP: " + InetAddress.getByAddress(message.getYourIP()));
			System.out.println("Server IP: " + InetAddress.getByAddress(message.getServerIP()));
			System.out.println();
			System.out.println();
			
			//Requesting
			while (true) {
				System.out.println("CLIENT STEP 3: SEND REQUEST: ");
				System.out.println();
				DHCPFunctions.DHCPRequest(socket, message, receivePacket);
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
				System.out.println();
				System.out.println("HEX CODE");
				System.out.println(Utils.toHexString(message.makeMessage()));
				System.out.println();
				System.out.println("DATA INFORMATION");
				System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
				System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
				System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
				System.out.println("Your IP: " + InetAddress.getByAddress(message.getYourIP()));
				System.out.println("Server IP: " + InetAddress.getByAddress(message.getServerIP()));
				System.out.println("Seconds:  " + Utils.fromBytes(message.getNumberOfSeconds()));
				System.out.println();
				System.out.println();
				
				break;
			}
			
			//Extended requesting
			//Requesting
			while (true) {
				System.out.println("CLIENT STEP 5: EXTEND REQUEST: ");
				System.out.println();
				System.out.println("HEX CODE");
				System.out.println(Utils.toHexString(message.makeMessage()));
				System.out.println();
				System.out.println("options length: " + message.getOptions().length);
				System.out.print("options hex: ");
				System.out.println(Utils.toHexString(message.getOptions()));
				int IPLeaseTime = Utils.fromBytes(message.getMessageOption(51)); //TODO: hier loopt het ook al mis als dit voor de 2e keer wordt gerund
				TimeUnit.SECONDS.sleep(IPLeaseTime/2);
				
				DHCPFunctions.DHCPExtendedRequest(socket, message, receivePacket);
				
				
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
				message = new DHCPMessage(msg);
				if (Utils.fromBytes(message.getMessageOption(53)) != 5) {
					System.out.println("ERROR: Negative acknowledge received.");
					break;
				}
				
				continue;
			}
			
			DHCPFunctions.DHCPRelease();
			
			
			
//			//Close socket
//			if(! Arrays.equals(msg2, new byte[0])){
//				System.out.println("Socket Closing");
//				socket.close();
//			}
			
		}
	}
}