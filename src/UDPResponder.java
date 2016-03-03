import java.io.*;
import java.net.*;

public class UDPResponder implements Runnable{

    DatagramSocket socket = null;
    DatagramPacket packet = null;	
	
    //Constructor
    public UDPResponder(DatagramSocket socket, DatagramPacket packet){
    	this.socket = socket;
    	this.packet = packet;
    }
    
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Received2");
		byte[] data = response();
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, packet.getAddress(), packet.getPort());
		//Send the reply packet
		try {
			socket.send(sendPacket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private byte[] response() {
		//process data
		String data = new String(packet.getData());
		System.out.println("Client sent: " + data);
		System.out.println("Client IP:" + packet.getAddress());
		String welcome = new String("Welcome client: ");
		String capitalizedSentence = data.toUpperCase();
		String toSend = welcome + capitalizedSentence;
		return toSend.getBytes();
	}
}
