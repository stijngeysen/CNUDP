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
					//extend Request => CHA = Requested IP (optie 50)
					
					//new IP
					if (message.getMessageOption(50) != message.getClientHardwareAddress()) { //oorspronkelijk optie 54 != null (ServerID), ik denk dat het 50 (Requested IP) moet zijn
						byte[] IP = this.usedIPs.askIP();
						DHCPFunctions.DHCPAck(socket, message, packet, InetAddress.getByAddress(IP), 5); //5 is de hardcoded leasetime
					}
					//extend
					else {
						byte[] IP = message.getMessageOption(50); //Ik denk dat hier requested IP moet
						System.out.println("TODO IP: " + Utils.fromBytes(IP));
						if (this.usedIPs.extendIP(IP)) { //TODO: extendIP wordt hier gebruikt
							DHCPFunctions.DHCPAck(socket, message, packet, InetAddress.getByAddress(IP), 5); //5 is de hardcoded leasetime
						} else {
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