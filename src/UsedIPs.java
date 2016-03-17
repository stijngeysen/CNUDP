import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Random;

/**
 * Used IP's
 * Class which has an arraylist to save all the used 'client IPs' with the added or
 * extended times of these IP's. This class also contains methods to add, extend and
 * remove 'client IPs'.
 * 
 * @author Geysen Stijn & Moons Marnix
 *
 */
public class UsedIPs {
	
	static Random rand = new Random();
	
	/**
	 * Used IP's
	 * Initialize with an empty arraylist usedIPs.
	 */
	public UsedIPs() {
		
	}
	
	public static ArrayList<String> usedIPs = new ArrayList<String>();
	
	/**
	 * Ask IP
	 * Return an IP which is not yet inside the arraylist.
	 * 
	 * @return
	 */
	public byte[] askIP() {
		byte[] YI = new byte[4];
		rand.nextBytes(YI);
		while (containIP(YI)) {
			rand.nextBytes(YI);
		}
		return YI;
	}
	
	/**
	 * Contain IP
	 * Returns if the given IP is inside the saved arraylist.
	 * 
	 * @param IP
	 * @return
	 */
	public boolean containIP(byte[] IP){
		boolean result = false;
		for(int i=0;i<UsedIPs.usedIPs.size();i++){
	        if(((UsedIPs.usedIPs.get(i)).substring(13, UsedIPs.usedIPs.get(i).length())).equals("" + Utils.fromBytes(IP))) {
	            result=true;
	            break;
	        }
		}
		return result;
	}
	
	/**
	 * Add IP
	 * Add the given IP inside the arraylist.
	 * 
	 * @param IP
	 */
	public void addIP(byte[] IP){ //TODO: het is zo mogelijk om dezelfde IP 2 keer toe te voegen
		Date date = new Date();
		long time = date.getTime();
		UsedIPs.usedIPs.add("" + (time) + Utils.fromBytes(IP));
	}
	
	/**
	 * Extend IP
	 * Returns true if the given IP hasn't expired yet and extends the given IP.
	 * 
	 * @param IP
	 * @return
	 */
	public boolean extendIP(byte[] IP) {
		boolean result = false;
		for(int i=0;i<UsedIPs.usedIPs.size();i++){
	        if(((UsedIPs.usedIPs.get(i)).substring(13, UsedIPs.usedIPs.get(i).length())).equals("" + Utils.fromBytes(IP))) {
	        	try {
					System.out.println("IP EXTENDED!!!!: " + InetAddress.getByAddress(IP));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
	            result=true;
	            Date date = new Date();
	    		long time = date.getTime();
	            UsedIPs.usedIPs.set(i, "" + (time) + Utils.fromBytes(IP));
	            break;
	        }
		}
		return result;
	}
	
	/**
	 * Remove Extinct IP's
	 * Remove all the IP's from the arraylist which exist longer then the given leaseTime.
	 * 
	 * @param leaseTime
	 */
	public void removeExtinctIPs(int leaseTime) {
		Date date = new Date();
		long time = date.getTime();
		for(int i=0; i<UsedIPs.usedIPs.size(); i++){
	        if((time - Long.valueOf(UsedIPs.usedIPs.get(i).substring(0, 13))) > 1000*leaseTime){ //convert from sec to msec
	        	System.out.println("Lease Time of this IP is expired: the IP will be removed from the list of leased IPs: ");
	        	System.out.println(((UsedIPs.usedIPs.get(i)).substring(13, UsedIPs.usedIPs.get(i).length())));
	            UsedIPs.usedIPs.remove(i);
	        }
		}
	}
	
	/**
	 * Remove IP
	 * Remove the given IP from the arraylist.
	 * 
	 * @param IP
	 */
	public void removeIP(byte[] IP) {
		for(int i=0;i<UsedIPs.usedIPs.size();i++){
	        if(((UsedIPs.usedIPs.get(i)).substring(13, UsedIPs.usedIPs.get(i).length())).equals("" + Utils.fromBytes(IP))){
	        	try {
					System.out.println("IP removed because released: " + InetAddress.getByAddress(IP));
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
	            UsedIPs.usedIPs.remove(i);
	            break;
	        }
		}
	}
	
	
}
