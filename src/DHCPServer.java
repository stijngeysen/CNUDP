import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;


public class DHCPServer {
	//port of the server (normally 67 for DHCP)
	static int port = 1234;
	//max packet length (min 236 + options)
	static int lengte = 512;
	//used IP's
	static UsedIPs usedIPs = new UsedIPs();
	//Main class
	public static void main(String[] args) throws IOException {
		DatagramSocket welcomeSocket = new DatagramSocket(port);
        
		while (! welcomeSocket.isClosed())
		{
	        //initialize empty data arrays (if this is placed outside the while loop,
			//the byte array will contain bytes of previous, longer messages if a short messages
			//has to be processed
	        byte[] receiveData = new byte[lengte];
	        
    		//Construct new datagrampacket to receive
    		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    		//get the next packet
    		welcomeSocket.receive(receivePacket);
    		//Run new Thread for Response
    		(new DHCPRespond(welcomeSocket, receivePacket, usedIPs)).start();
		}
		welcomeSocket.close();
	}
	
}
