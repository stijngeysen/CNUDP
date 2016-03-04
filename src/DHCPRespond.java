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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void response() throws UnknownHostException {
		//process data
		byte[] msg = packet.getData();
		DHCPMessage message = new DHCPMessage(msg);
		message.printMessageType();
		
		//For Server
		System.out.println("Transaction ID: " + Utils.fromBytes(message.getTransactionID()));
		System.out.println("Hardware Address Length: " + Utils.fromBytes(message.getHardwareAddressLength()));
		System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));
		System.out.println("Server IP:  " + socket.getLocalAddress());
		System.out.println("Client port: " + packet.getPort());
		
		//For Client
		DHCPFunctions.DHCPOffer(socket, message.getTransactionID(), InetAddress.getByName("192.192.1.102"), socket.getLocalAddress(), packet.getPort());
	}
}