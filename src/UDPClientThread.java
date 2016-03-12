import java.io.*;
import java.net.*;

class UDPClientThread
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
       //For localhost (same PC)
       //InetAddress IPAddress = InetAddress.getByName("localhost");
       //Otherwise: terminal input:
       System.out.println("Give Server IP (if Server is on same PC, give 'localhost' or "
       		+ "127.0.0.1): ");
       BufferedReader ip =
    	         new BufferedReader(new InputStreamReader(System.in));
       String serverIP = ip.readLine();
       InetAddress IPAddress = InetAddress.getByName(serverIP);
       
	  //Get input message from User
       System.out.println("Give Data: ");
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
   
   public void DHCPDiscover() throws IOException {
		byte[] sendData = new byte[lengte];
		InetAddress broadcast;
		DatagramSocket welcomeSocket = new DatagramSocket(port);
		try {
			broadcast = InetAddress.getByName("255.255.255.255");
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 68);
			welcomeSocket.send(sendPacket);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		welcomeSocket.close();
	}
}