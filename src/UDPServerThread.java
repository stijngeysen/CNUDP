import java.io.*;
import java.net.*;

public class UDPServerThread {
	//port of the server
	static int port = 18765;
	//packet length
	static int lengte = 512;
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
    		(new UDPResponderThread(welcomeSocket, receivePacket)).start();
		}
		welcomeSocket.close();
	}
	
	
}
