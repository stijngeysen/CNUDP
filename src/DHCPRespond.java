import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class DHCPRespond extends Thread{
	//initialize
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    UsedIPs usedIPs = null;
	
    //Constructor
    public DHCPRespond(DatagramSocket socket, DatagramPacket packet, UsedIPs usedIPs){
    	this.socket = socket;
    	this.packet = packet;
    	this.usedIPs = usedIPs;
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
		switch(Utils.fromBytes(message.getMessageOption(53))) {
				case 1: //message was a Discover message, we will reply with an offer
					byte[] YI = this.usedIPs.askIP();
					DHCPFunctions.DHCPOffer(socket, message, packet, InetAddress.getByAddress(YI), 5); //5 is de hardcoded leasetime
					break;
				case 2: //received an offer (from an other server) or wrong messagetype from client so do nothing
					break;
				case 3: //received a request from a client, reply with an ACK if IP is not in use, with an NAk if IP is in use
					//Distinguish between extend Request and normal request (for new IP)
					//extend Request => YourIP = Requested IP (optie 50)
					System.out.println("option 50" + Utils.fromBytes(message.getMessageOption(50)));
					System.out.println("clientIP" + Utils.fromBytes(message.getClientIP()));
					if (Utils.fromBytes(message.getMessageOption(50)) != Utils.fromBytes(message.getClientIP())){
						System.out.println("New IP");
						byte[] IP = message.getMessageOption(50);
						if (! usedIPs.containIP(IP)) {
							this.usedIPs.addIP(IP);
							DHCPFunctions.DHCPAck(socket, message, packet, InetAddress.getByAddress(IP), 5);
						}
						else {
							DHCPFunctions.DHCPNak(socket, message, packet, InetAddress.getByAddress(IP));
						}
					}
					else{
						byte[] IP = message.getMessageOption(50); //RequestedIP option
						if (this.usedIPs.extendIP(IP)) {
							System.out.println("Extend IP ack");
							DHCPFunctions.DHCPAck(socket, message, packet, InetAddress.getByAddress(IP), 5);
						}
						else{
							System.out.println("extend IP NAK");
							DHCPFunctions.DHCPNak(socket, message, packet, InetAddress.getByAddress(IP));
						}
							
					}
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