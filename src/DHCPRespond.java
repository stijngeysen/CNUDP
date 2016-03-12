import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class DHCPRespond extends Thread{
	//initialize
    DatagramSocket socket = null;
    DatagramPacket packet = null;	
	
    //Constructor
    public DHCPRespond(DatagramSocket socket, DatagramPacket packet){
    	this.socket = socket;
    	this.packet = packet;
    }
    
	@Override
	public void run() {
		try {
			response();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void response() throws UnknownHostException {
		//process data
		byte[] msg = packet.getData();
		DHCPMessage message = new DHCPMessage(msg);
		
		//For Server
		System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
		System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
		System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
		System.out.println("Server IP:  " + socket.getLocalAddress());
		System.out.println("Client port: " + packet.getPort());
		
		//For Client
		switch(Utils.fromBytes(message.getMessageType())) {
				case 1: //message was a Discover message, we will reply with an offer
					DHCPFunctions.DHCPOffer(socket, message, packet, InetAddress.getByName("192.192.1.102"), 5);
					break;
				case 2: //received an offer (from an other server) or wrong messagetype from client so do nothing
					break;
				case 3: //received a request from a client, reply with an ACK if IP is not in use, with an NAk if IP is in use
					//TODO: ip checken
					DHCPFunctions.DHCPAck(socket, message, packet, InetAddress.getByAddress(message.getYourIP()), 5);
					break;
				case 5: //received an ACK message (from an other server) or wrong messagetype from client so do nothing
					break;
				case 6: //received an NACK message (from an other server) or wrong messagetype from client so do nothing
					break;
				case 7: //received a Relase message
					//TODO: release processen
		}
	}
}