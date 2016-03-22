import java.util.Timer;
import java.util.TimerTask;

/**
 * Remove Extinct IP's Timer
 * Check every leaseTime seconds for extincted IP's in usedIPs.
 * 
 * @author Geysen Stijn & Moons Marnix
 *
 */
public class RemoveExtinctIPsTimer {
	
	public RemoveExtinctIPsTimer(final UsedIPs usedIPs, final int leaseTime) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
		    @Override
		    public void run()
		    {
		    	System.out.println("REMOVING");
				usedIPs.removeExtinctIPs(leaseTime);
		    }
		}, 0, 1000*leaseTime);
	}
}
