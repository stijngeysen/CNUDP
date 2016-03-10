import java.util.Arrays;
//TODO: vragen of deze klasse gebruikt mag worden

public class DHCPMessage {
	//CONSTRUCT empty message
	public DHCPMessage(){
		
	}
	
	public DHCPMessage(byte[] msg){
		this.decodeMsg(msg);
	}

	public DHCPMessage(byte[] opCode, byte[] hardwareType, byte[] hardwareAddressLength, byte[] hopCount, byte[] transactionID,
			byte[] numberOfSeconds, byte[] flags, byte[] clientIP, byte[] yourIP, byte[] serverIP, byte[] gatewayIP, 
			byte[] clientHardwareAddress, byte[] serverHostName, byte[] bootFileName, byte[] options){
		
		this.setOpCode(opCode);
		this.setHardwareType(hardwareType);
		this.setHardwareAddressLength(hardwareAddressLength);
		this.setHopCount(hopCount);
		this.setTransactionID(transactionID);
		this.setNumberOfSeconds(numberOfSeconds);
		this.setFlags(flags);
		this.setClientIP(clientIP);
		this.setYourIP(yourIP);
		this.setServerIP(serverIP);
		this.setGatewayIP(gatewayIP);
		this.setClientHardwareAddress(clientHardwareAddress);
		this.setServerHostName(serverHostName);
		this.setBootFileName(bootFileName);
		this.setOptions(options);
	}
	
	protected byte[] opCode = new byte[1];
	protected byte[] hardwareType = new byte[1];
	protected byte[] hardwareAddressLength = new byte[1];
	protected byte[] hopCount = new byte[1];
	protected byte[] transactionID = new byte[4];
	protected byte[] numberOfSeconds = new byte[2];
	protected byte[] flags = new byte[2];
	protected byte[] clientIP = new byte[4];
	protected byte[] yourIP = new byte[4];
	protected byte[] serverIP = new byte[4];
	protected byte[] gatewayIP = new byte[4];
	protected byte[] clientHardwareAddress = new byte[16];
	protected byte[] serverHostName = new byte[64];
	protected byte[] bootFileName = new byte[128];
	protected byte[] options = new byte[312];
	
	public byte[] getOpCode() {
		return opCode;
	}
	public void setOpCode(byte[] opCode) {
		this.opCode = opCode;
	}
	public byte[] getHardwareType() {
		return hardwareType;
	}
	public void setHardwareType(byte[] hardwareType) {
		this.hardwareType = hardwareType;
	}
	public byte[] getHardwareAddressLength() {
		return hardwareAddressLength;
	}
	public void setHardwareAddressLength(byte[] hardwareAddressLength) {
		this.hardwareAddressLength = hardwareAddressLength;
	}
	public byte[] getHopCount() {
		return hopCount;
	}
	public void setHopCount(byte[] hopCount) {
		this.hopCount = hopCount;
	}
	public byte[] getTransactionID() {
		return transactionID;
	}
	public void setTransactionID(byte[] transactionID) {
		this.transactionID = transactionID;
	}
	public byte[] getNumberOfSeconds() {
		return numberOfSeconds;
	}
	public void setNumberOfSeconds(byte[] numberOfSeconds) {
		this.numberOfSeconds = numberOfSeconds;
	}
	public byte[] getFlags() {
		return flags;
	}
	public void setFlags(byte[] flags) {
		this.flags = flags;
	}
	public byte[] getClientIP() {
		return clientIP;
	}
	public void setClientIP(byte[] clientIP) {
		this.clientIP = clientIP;
	}
	public byte[] getYourIP() {
		return yourIP;
	}
	public void setYourIP(byte[] yourIP) {
		this.yourIP = yourIP;
	}
	public byte[] getServerIP() {
		return serverIP;
	}
	public void setServerIP(byte[] serverIP) {
		this.serverIP = serverIP;
	}
	public byte[] getGatewayIP() {
		return gatewayIP;
	}
	public void setGatewayIP(byte[] gatewayIP) {
		this.gatewayIP = gatewayIP;
	}
	public byte[] getClientHardwareAddress() {
		return clientHardwareAddress;
	}
	public void setClientHardwareAddress(byte[] clientHardwareAddress) {
		this.clientHardwareAddress = clientHardwareAddress;
	}
	public byte[] getServerHostName() {
		return serverHostName;
	}
	public void setServerHostName(byte[] serverHostName) {
		this.serverHostName = serverHostName;
	}
	public byte[] getBootFileName() {
		return bootFileName;
	}
	public void setBootFileName(byte[] bootFileName) {
		this.bootFileName = bootFileName;
	}
	public byte[] getOptions() {
		return options;
	}
	public void setOptions(byte[] options) {
		this.options = options;
	}
	
	
	
	//Compose a msg
	public byte[] makeMessage(){
		byte[] msg = new byte[236 + this.getOptions().length];
		System.arraycopy(this.getOpCode(), 0, msg, 0, 1);
		System.arraycopy(this.getHardwareType(), 0, msg, 1, 1);
		System.arraycopy(this.getHardwareAddressLength(), 0, msg, 2, 1);
		System.arraycopy(this.getHopCount(), 0, msg, 3, 1);
		System.arraycopy(this.getTransactionID(), 0, msg, 4, 4);
		System.arraycopy(this.getNumberOfSeconds(), 0, msg, 8, 2);
		System.arraycopy(this.getFlags(), 0, msg, 10, 2);
		System.arraycopy(this.getClientIP(), 0, msg, 12, 4);
		System.arraycopy(this.getYourIP(), 0, msg, 16, 4);
		System.arraycopy(this.getServerIP(), 0, msg, 20, 4);
		System.arraycopy(this.getGatewayIP(), 0, msg, 24, 4);;
		System.arraycopy(this.getClientHardwareAddress(), 0, msg, 28, 16);
		System.arraycopy(this.getServerHostName(), 0, msg, 44, 64);
		System.arraycopy(this.getBootFileName(), 0, msg, 108, 128);
		System.arraycopy(this.getOptions(), 0, msg, 236, this.getOptions().length);
		return msg;
	}
	
	//Decode a messages
	private void decodeMsg(byte[] msg) {
		this.setOpCode(Arrays.copyOfRange(msg, 0, 1));	
		this.setHardwareType(Arrays.copyOfRange(msg, 1, 2));
		this.setHardwareAddressLength(Arrays.copyOfRange(msg, 2, 3));
		this.setHopCount(Arrays.copyOfRange(msg, 3, 4));
		this.setTransactionID(Arrays.copyOfRange(msg, 4, 8));
		this.setNumberOfSeconds(Arrays.copyOfRange(msg, 8, 10));
		this.setFlags(Arrays.copyOfRange(msg, 10, 12));
		this.setClientIP(Arrays.copyOfRange(msg, 12, 16));
		this.setYourIP(Arrays.copyOfRange(msg, 16, 20));
		this.setServerIP(Arrays.copyOfRange(msg, 20, 24));
		this.setGatewayIP(Arrays.copyOfRange(msg, 24, 28));
		this.setClientHardwareAddress(Arrays.copyOfRange(msg, 28, 44));
		this.setServerHostName(Arrays.copyOfRange(msg, 44, 108));
		this.setBootFileName(Arrays.copyOfRange(msg, 108, 236));
		this.setOptions(Arrays.copyOfRange(msg, 236, msg.length));
	}
	
	//print message type //TODO: gebruik option message type
	public void printMessageType(){
		byte[] b = new byte[1];
		for (int i=0; i<this.getOptions().length; i++) {
			b[0] = this.getOptions()[i];
			if (Utils.fromBytes(b) == 53){
				System.out.println("Options field found!");
				b[0] = this.getOptions()[i+2];
				System.out.println("Type Hex Value: " + Utils.toHexString(b));
			}
		}
	}
	
	
	public byte[] makeMessageTypeOption(DHCPMessageType messageType){
		byte[] msg = new byte[3];
		System.arraycopy(Utils.toBytes(53, 1), 0, msg, 0, 1);
		System.arraycopy(Utils.toBytes(1, 1), 0, msg, 1, 1);
		System.arraycopy(Utils.toBytes(messageType.getValue(), 1), 0, msg, 2, 1);
		return msg;		
	}
	
	public byte[] makeEndOption(){
		byte[] msg = new byte[1];
		System.arraycopy(Utils.toBytes(255, 1), 0, msg, 0, 1);
		return msg;		
	}
	
	
}
