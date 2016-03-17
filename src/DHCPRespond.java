import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class DHCPRespond extends Thread{
	//initialize
    DatagramSocket socket = null;
    DatagramPacket packet = null;
    UsedIPs usedIPs = null;
    
	//leaseTime (has to be the same as the leasetime in DHCPFunctions.java
	static int leaseTime = 5;
	
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
    		//Delete IP's which exist for too long
    		usedIPs.removeExtinctIPs(leaseTime); //TODO: optimaliseren
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void response() throws UnknownHostException {
		//process data
		byte[] msg = packet.getData();
		DHCPMessage message = new DHCPMessage(msg);
		
		//Print text to console Server
		System.out.println("Client IP: " + InetAddress.getByAddress(message.getClientIP()));

		//For Client
		switch(Utils.fromBytes(message.getMessageOption(53))) { //reply according to the received messageType
				case 1: //message was a Discover message, we will reply with an offer
					byte[] YI = this.usedIPs.askIP();
					DHCPFunctions.DHCPOffer(socket, message, packet, InetAddress.getByAddress(YI));
					break;
				case 2: //received an offer (from an other server) or wrong messagetype from client so do nothing
					break;
				case 3: //received a request from a client, reply with an ACK if IP is not in use, with an NAk if IP is in use
					//Distinguish between extend Request and normal request (for new IP)
					//extend Request => ClientIP = Requested IP (optie 50)
					if (Utils.fromBytes(message.getMessageOption(50)) != Utils.fromBytes(message.getClientIP())){ //new Request
						byte[] IP = message.getMessageOption(50);
						if (! usedIPs.containIP(IP)) { //Check if requested IP already in use and reply with ack/nak
							this.usedIPs.addIP(IP);
							DHCPFunctions.DHCPAck(socket, message, packet, InetAddress.getByAddress(IP), new byte[4]);
						}
						else {
							DHCPFunctions.DHCPNak(socket, message, packet, InetAddress.getByAddress(IP), new byte[4]);
						}
					}
					else{ //extend request
						byte[] IP = message.getMessageOption(50); //RequestedIP option
						if (this.usedIPs.extendIP(IP)) { //extend request approved
							DHCPFunctions.DHCPAck(socket, message, packet, InetAddress.getByAddress(IP), message.getClientIP());
						}
						else{ //extend request not approved
							DHCPFunctions.DHCPNak(socket, message, packet, InetAddress.getByAddress(IP), message.getClientIP());
						}
							
					}
					break;
				case 5: //received an ACK message (from an other server) or wrong messagetype from client so do nothing
					break;
				case 6: //received an NACK message (from an other server) or wrong messagetype from client so do nothing
					break;
				case 7: //received a Release message
					this.usedIPs.removeIP(message.getClientIP());
		}
	}
	
	
	
	
	
}