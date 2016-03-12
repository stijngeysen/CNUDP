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
		while (this.usedIPs.contains(YI)) {
			rand.nextBytes(YI);
		}
		Date date = new Date();
		long time = date.getTime();
		this.usedIPs.add("" + ((int) (long) time) + Utils.fromBytes(YI));
		return YI;
	}
	
	public boolean extendIP(byte[] IP) {
		boolean result = false;
		System.out.println("testing...");
		System.out.println(((this.usedIPs.get(0)).substring(12, this.usedIPs.get(0).length()-1)));
		System.out.println("" + Utils.fromBytes(IP)); //TODO: deze is gelijk aan 0 en ik weet maar niet waarom...
		for(int i=0;i<this.usedIPs.size();i++){
	        if(((this.usedIPs.get(i)).substring(12, this.usedIPs.get(i).length()-1)).equals("" + Utils.fromBytes(IP))) {
	            result=true;
	            Date date = new Date();
	    		long time = date.getTime();
	            this.usedIPs.set(i, "" + ((int) (long) time) + Utils.fromBytes(IP));
	            break;
	        }
		}
		return result;
	}
	
	public void removeExtinctIPs() {
		Date date = new Date();
		long time = date.getTime();
		for(int i=0;i<this.usedIPs.size();i++){
	        if((time - Long.valueOf(this.usedIPs.get(i).substring(0, 12))) < 0){
	            this.usedIPs.remove(i);
	        }
		}
	}
	
	public void removeIP(byte[] IP) {
		String YI = "" + IP;
		for(int i=0;i<this.usedIPs.size();i++){
	        if((this.usedIPs.get(i)).substring(12, this.usedIPs.get(i).length()-1) == YI){
	            this.usedIPs.remove(i);
	            break;
	        }
		}
	}
	
	
}
