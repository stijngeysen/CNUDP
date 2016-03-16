import java.util.Date;
import java.util.ArrayList;
import java.util.Random;

public class UsedIPs {
	
	static Random rand = new Random();
	
	public UsedIPs() {
		
	}
	
	public static ArrayList<String> usedIPs = new ArrayList<String>();
	
	public byte[] askIP() {
		byte[] YI = new byte[4];
		rand.nextBytes(YI);
		while (containIP(YI)) {
			rand.nextBytes(YI);
		}
		return YI;
	}
	
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
	
	public void addIP(byte[] IP){
		Date date = new Date();
		long time = date.getTime();
		UsedIPs.usedIPs.add("" + (time) + Utils.fromBytes(IP));
	}
	
	public boolean extendIP(byte[] IP) {
		boolean result = false;
		System.out.println("testing...");
		System.out.println(UsedIPs.usedIPs.size());
		for(int i=0;i<UsedIPs.usedIPs.size();i++){
        	System.out.println(i);
    		System.out.println(((UsedIPs.usedIPs.get(i)).substring(13, UsedIPs.usedIPs.get(i).length())));
    		System.out.println("" + Utils.fromBytes(IP)); //TODO: deze is gelijk aan 0 en ik weet maar niet waarom...
    		System.out.println("HEX format IP usedIP:");
    		System.out.println(Utils.toHexString(IP));
	        if(((UsedIPs.usedIPs.get(i)).substring(13, UsedIPs.usedIPs.get(i).length())).equals("" + Utils.fromBytes(IP))) {
	            result=true;
	            Date date = new Date();
	    		long time = date.getTime();
	            UsedIPs.usedIPs.set(i, "" + (time) + Utils.fromBytes(IP));
	            break;
	        }
		}
		return result;
	}
	
	public void removeExtinctIPs() {
		Date date = new Date();
		long time = date.getTime();
		for(int i=0;i<UsedIPs.usedIPs.size();i++){
			System.out.println(UsedIPs.usedIPs.get(0).substring(0, 12));
	        if((time - Long.valueOf(UsedIPs.usedIPs.get(i).substring(0, 12))) < 0){
	            UsedIPs.usedIPs.remove(i);
	        }
		}
	}
	
	public void removeIP(byte[] IP) {
		String YI = "" + IP;
		for(int i=0;i<UsedIPs.usedIPs.size();i++){
	        if((UsedIPs.usedIPs.get(i)).substring(12, UsedIPs.usedIPs.get(i).length()-1) == YI){
	            UsedIPs.usedIPs.remove(i);
	            break;
	        }
		}
	}
	
	
}
