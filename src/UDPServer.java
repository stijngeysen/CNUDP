import java.io.*;
import java.net.*;

public class UDPServer {
	//port of the server
	static int port = 18765;
	//packet length
	static int lengte = 512;
	//Main class
	public static void main(String[] args) throws IOException {
		DatagramSocket welcomeSocket = new DatagramSocket(port);
        //initialize data arrays
        byte[] receiveData = new byte[lengte];
        byte[] sendData = new byte[lengte];
        
		while (! welcomeSocket.isClosed())
		{
    		//Construct new datagrampacket to receive
    		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    		//get the next packet
    		welcomeSocket.receive(receivePacket);
    		//get the IP of the sender
    		InetAddress IPAddress = receivePacket.getAddress();
    		
    		//process data
    		String data = new String(receivePacket.getData());
    		System.out.println("RECEIVED: " + data);
    		System.out.println("IP:" + IPAddress);
    		String welcome = new String("Welcome client: ");
    		String capitalizedSentence = data.toUpperCase();
    		String toSend = welcome + capitalizedSentence;
    		sendData  = toSend.getBytes();
    		
    		//find out to which port to send the answer
    		int receivePort = receivePacket.getPort();
    		//Construct sendPacket
    		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receivePort);
    		//Send the reply packet
    		welcomeSocket.send(sendPacket);
		}
		welcomeSocket.close();
	}
}
