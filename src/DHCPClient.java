import java.net.*;
import java.util.Date;
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
			DHCPFunctions.DHCPDiscover(socket);
			
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
			System.out.println("DATA INFORMATION");
			System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
			System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
			System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
			System.out.println("Your IP: " + InetAddress.getByAddress(message.getYourIP()));
			System.out.println("Server IP: " + InetAddress.getByAddress(message.getServerIP()));
			System.out.println();
			
			//Requesting
			while (true) {
				DHCPFunctions.DHCPRequest(socket, message, receivePacket);
				
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
				System.out.println("DATA INFORMATION");
				System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
				System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
				System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
				System.out.println("Your IP: " + InetAddress.getByAddress(message.getYourIP()));
				System.out.println("Server IP: " + InetAddress.getByAddress(message.getServerIP()));
				System.out.println("Seconds:  " + Utils.fromBytes(message.getNumberOfSeconds()));
				System.out.println();
				
				break;
			}
			
			//Extended requesting
			//Requesting
			while (true) {
				int IPLeaseTime = Utils.fromBytes(message.getMessageOption(51));
				TimeUnit.SECONDS.sleep(IPLeaseTime/2);
				
				DHCPFunctions.DHCPExtendedRequest(socket, message, receivePacket);
				
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