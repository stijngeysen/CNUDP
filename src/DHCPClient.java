import java.net.*;
import java.util.Arrays;

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
			System.out.println("Message received! ");
			message.getMessageType();
			
			//Data information
			System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
			System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
			System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
			System.out.println("Your IP: " + InetAddress.getByAddress(message.getYourIP()));
			System.out.println("Server IP: " + InetAddress.getByAddress(message.getServerIP()));
			
			System.out.println();
			
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
			DHCPMessage message2 = new DHCPMessage(msg2);
			System.out.println("Message received! ");
			message2.getMessageType();
			
			//Data information
			System.out.println("Transaction ID: " + Utils.fromBytes(message2.getTransactionID()));
			System.out.println("Hardware Address Length: " + Utils.fromBytes(message2.getHardwareAddressLength()));
			System.out.println("Client IP: " + InetAddress.getByAddress(message2.getClientIP()));
			System.out.println("Your IP: " + InetAddress.getByAddress(message2.getYourIP()));
			System.out.println("Server IP: " + InetAddress.getByAddress(message2.getServerIP()));
			System.out.println("Seconds:  " + Utils.fromBytes(message2.getNumberOfSeconds()));
			
			System.out.println();
			
			//Close socket
			if(! Arrays.equals(msg2, new byte[0])){
				System.out.println("Socket Closing");
				socket.close();
			}
			
		}
	}
}