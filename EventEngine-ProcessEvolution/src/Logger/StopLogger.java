package Logger;

import siena.HierarchicalDispatcher;
import siena.Notification;
import siena.SienaException;

public class StopLogger {
    public static void main(String[] args) {
	try {
	    HierarchicalDispatcher siena;
	    siena = new HierarchicalDispatcher();

	    siena.setMaster(args[0]); 
	    	    
	    Notification stopLogger = new Notification();
	    stopLogger.putAttribute("id", "stopLogging");
	    	    
	    try {
	    	
	    		siena.publish(stopLogger);
	    		System.out.println("Published: " + stopLogger.toString());
	    	
	    } catch (SienaException ex) {
		System.err.println("Siena error:" + ex.toString());
	    }
	    System.out.println("shutting down.");
	    siena.shutdown();
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
    }
}
