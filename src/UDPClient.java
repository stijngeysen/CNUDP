import java.io.*;
import java.net.*;

class UDPClient
{
	//port of the server
	static int port = 18765;
	//packet length
	static int lengte = 512;
   public static void main(String args[]) throws Exception
   {
       //initialize data arrays
       byte[] receiveData = new byte[lengte];
       byte[] sendData = new byte[lengte];
       
       //Make new client Socket for UDP connections
       DatagramSocket clientSocket = new DatagramSocket();
       //Get the UDP server address (destination address)
       InetAddress IPAddress = InetAddress.getByName("localhost");
       
	  //Get input message from User
      BufferedReader inFromUser =
         new BufferedReader(new InputStreamReader(System.in));
      String sentence = inFromUser.readLine();
      sendData = sentence.getBytes();
      
      //Construct new DatagramPacket to send
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      //Send the packet
      clientSocket.send(sendPacket);
      
      //Construct DatagramPacket for response from server
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      //Receive the response from server
      clientSocket.receive(receivePacket);
      
      //Process response
      InetAddress ServerAddress = receivePacket.getAddress();
      System.out.println("Server IP: " + ServerAddress);
      String modifiedSentence = new String(receivePacket.getData());
      System.out.println("Message from the server:" + modifiedSentence);
      
      //Client is done, so close the connections
      clientSocket.close();
   }
}