package kabouter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notification;
import siena.NotificationBuffer;
import siena.SienaException;
import core.ChangeController;

class StreamGobbler extends Thread
{
	InputStream is;
	String type;

	StreamGobbler(InputStream is, String type)
	{
		this.is = is;
		this.type = type;
	}

	public void run()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			while ( (line = br.readLine()) != null)
				System.out.println(type + ">" + line);    
		} catch (IOException ioe)
		{
			ioe.printStackTrace();  
		}
	}
}

public class Kabouter {

	private static boolean _done = false;
	
	private static String _dir = "C:\\Users\\n06038\\Desktop\\SuspensionTest\\";
	private static String _bpmnFileV1Dist = "SuspensionTestClaimHandlingV1-Dist.bpmn";
	private static String _bpmnFileV1 = "SuspensionTestClaimHandlingV1.bpmn";
	private static String _bpmnFileV2 = "SuspensionTestClaimHandlingV2.bpmn";
	
	private static String endSignal = "SignalEND";
	
	private static String logDir = "C:\\Users\\n06038\\workspace\\EventEngine-Combined\\logs";
	private static String destDir = "C:\\Users\\n06038\\Desktop\\logs\\";
		
	public static void main(String args[]) {
		
		ArrayList<String> toSuspend = new ArrayList<String>();
		toSuspend.add("CheckClientHistory");
		robustnessCycle(500,4,90000,30000,toSuspend,"C:\\Users\\n06038\\Desktop\\logs\\10000\\Parallel\\",10000);
		
		toSuspend.clear();
		toSuspend.add("AssessClaim");
		robustnessCycle(500,4,90000,30000,toSuspend,"C:\\Users\\n06038\\Desktop\\logs\\10000\\Blocking\\",10000);
		
		toSuspend.clear();
		toSuspend.add("ALL");
		robustnessCycle(500,4,90000,30000,toSuspend,"C:\\Users\\n06038\\Desktop\\logs\\10000\\ALL\\",10000);

		toSuspend.clear();
		toSuspend.add("InformClient");
		robustnessCycle(500,4,90000,30000,toSuspend,"C:\\Users\\n06038\\Desktop\\logs\\10000\\Choice\\",10000);
		
		
		/*** Varying CR position ***/
//		String destName;
//		
////		for (int i=2; i<=8; i++) {
//			// for 7 CR positions
//		int i=8;
//			_dir = "C:\\Users\\n06038\\Desktop\\CRPlacement\\";
//			_bpmnFileV1Dist = "CRPlacementV1-Dist.bpmn";
//			_bpmnFileV1 = "CRPlacementV1.bpmn";
//			_bpmnFileV2 = "CRPlacementV" + i + ".bpmn";
//			
//			for (int it=0; it<3; it++) {
//				// 3 runs
//				destName = destDir + "800rpm-7st-var(CR)\\" + i + "CRpos-ALL-run" + it;
//				executionCycle(800, 3, 90000, true, destName, 7000);
//				destName = destDir + "800rpm-7st-var(CR)\\" + i + "CRpos-CR-run" + it;
//				executionCycle(800, 3, 90000, false, destName, 7000);
//			}
//		}
		
		
		
////		int[] rpm = {100,200,300,400,500,600,700,800,900,
////				1000,1100,1200,1300,1400,1500,1600,1700,1800,1900,2000};
//		/**** Varying task times ****/
//		int rpm = 800;
//		int minutes = 4;
//		boolean suspendAll;		
//		String destName;
//		
//		int[] tasktime = {1000,3000,5000,7000,9000,11000,13000,14000};
//		
//		// SUSPEND ALL
//		suspendAll = true;
//		for (int i=0; i<tasktime.length;i++) {
//			// 3 iterations
//			for (int it=0; it<3;it++){
//				destName = destDir + "rpm800-var(taskTime)\\" + tasktime[i] + "taskTime-ALL-run" + it;
//				executionCycle(rpm,minutes, 120000, suspendAll,destName,tasktime[i]);
//			}
//		}
//		
//		// SUSPEND CHANGE REGION
//		suspendAll = false;
//		for (int i=0; i<tasktime.length;i++) {
//			// 3 iterations
//			for (int it=0; it<3;it++){
//				destName = destDir + "rpm800-var(taskTime)\\" + tasktime[i] + "taskTime-CR-run" + it;
//				executionCycle(rpm,minutes, 120000, suspendAll,destName,tasktime[i]);
//			}
//		}
//
//		/**** Random task times, varying rpm ****/
//		int[] rpmRange = {1700,1900};
//		
//		// SUSPEND ALL
//		for (int i=0; i<rpmRange.length;i++) {
//			// 3 iterations
//			for (int it=0; it<3;it++){
//				String destName = destDir + "tRand-var(RPM)\\" + rpmRange[i] + "rpm-ALL-run" + it;
//				executionCycle(rpmRange[i],3, 90000, true,destName,0);
//				destName = destDir + "tRand-var(RPM)\\" + rpmRange[i] + "rpm-CR-run" + it;
//				executionCycle(rpmRange[i],3, 90000, false,destName,0);
//			}
//		}
		
	}
	
	private static void executionChangeCycle(int rpm, int minutes, int changeAfterMinutes, boolean all, String destName, int tasktime) {
		// Init
		// Start SIENA
		Process pcSiena = startSiena();
		
		sleep(2000);
		
		// Create SIENA Dispatcher
		HierarchicalDispatcher siena = createHierarchicalDispatcher();
		ChangeController controller = new ChangeController();
		controller.setHierarchicalDispatcher(siena);
		NotificationBuffer buf = new NotificationBuffer();
		subscribe(endSignal, String.valueOf(rpm*minutes-1), siena, buf);
		
		// Start Event Engine
		Process pcEventEngine = startEventEngine(_dir + _bpmnFileV1Dist, getSienaAddress(), tasktime);
		
		sleep(2000);
		
		// Start Client
		Process pcClient = startClientTriggering(rpm, minutes, getSienaAddress());
		
		sleep(changeAfterMinutes);
		
		// Redeploy
		if (all) {
			controller.publishAction("ALL", "suspend");
		}
		controller.redeploy(_dir + _bpmnFileV1, _dir + _bpmnFileV2, true);
		
		// Let it run
		waitTillEnd(buf);
		// Copy log files to other folder
		try {
			FileUtils.copyDirectory(new File(logDir), new File(destName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pcEventEngine.destroy();
		pcSiena.destroy();
		
		System.out.println("FINISHED");
	}
	
	private static void robustnessCycle(int rpm, int minutes, int changeAfterMinutes, int toWait, ArrayList<String> toSuspend, String destName, int tasktime) {
		// Init
		// Start SIENA
		Process pcSiena = startSiena();
		
		sleep(2000);
		
		// Create SIENA Dispatcher
		HierarchicalDispatcher siena = createHierarchicalDispatcher();
		ChangeController controller = new ChangeController();
		controller.setHierarchicalDispatcher(siena);
		NotificationBuffer buf = new NotificationBuffer();
		subscribe(endSignal, String.valueOf(rpm*minutes-1), siena, buf);
		
		// Start Event Engine
		Process pcEventEngine = startEventEngine(_dir + _bpmnFileV1Dist, getSienaAddress(), tasktime);
		
		sleep(2000);
		
		// Start Client
		Process pcClient = startClientTriggering(rpm, minutes, getSienaAddress());
		
		sleep(changeAfterMinutes);
		
		// Redeploy
		for(String fragment: toSuspend) {
			controller.publishAction(fragment, "suspend");
		}
		
		sleep(toWait);
		
		controller.publishAction("ALL", "resume");
		
		// Let it run
		waitTillEnd(buf);
		// Copy log files to other folder
		try {
			FileUtils.copyDirectory(new File(logDir), new File(destName));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pcEventEngine.destroy();
		pcSiena.destroy();
		
		System.out.println("FINISHED");
	}
	
	private static void waitTillEnd(NotificationBuffer buf) {
		System.out.println("waiting");
		try {
			Notification n = buf.getNotification(-1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Ending Wait");
	}
	
	private static void subscribe(String id, String piid, HierarchicalDispatcher siena, NotificationBuffer buf) {
		Filter f = new Filter();
		f.addConstraint("id", id);
		f.addConstraint("processInstanceId", piid);
		
		try {
			siena.subscribe(f, buf);
		} catch (SienaException e) {
			e.printStackTrace();
		}
	}
	
	public static Process startSiena() {
		String command = "javaw -cp jars/siena.jar siena.StartServer -port 2348 -udp";
		return runExternalProgram(command);
	}
	
	public static Process startEventEngine(String bpmnFile, String sienaAddress, int tasktime) {
		/*** Start EventEngine ***/
		// Xss (java stack size) made smaller to allow more threads
		String command = "javaw -Xss100k -classpath \"jars/eventengine.jar;jars/siena.jar;libs/xmi.jar;libs/ecore.jar;libs/common.jar;libs/bpmn2.jar;libs/wsClient.jar\" " +
			"eventengine.StartEngine " + bpmnFile + " " + sienaAddress + " udp 0 0 " + tasktime;
		return runExternalProgram(command);	
	}
	
	public static Process startClientTriggering(int rpm, int minutes, String sienaAddress) {
		/** Start TriggerEventEngine ***/
		String command = "cmd /c start java -cp jars/siena.jar tests.TriggerEventEngine " + sienaAddress + " " + rpm + " " + minutes;
		return runExternalProgram(command);	
		
	}
	
	private static Process runExternalProgram(String cmd) {
		Process pc = null;
		try {
			pc = Runtime.getRuntime().exec(cmd);	
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		// any error message?
        StreamGobbler errorGobbler = new 
            StreamGobbler(pc.getErrorStream(), "ERROR");            
        
        // any output?
        StreamGobbler outputGobbler = new 
            StreamGobbler(pc.getInputStream(), "OUTPUT");
            
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
		
		return pc;
	}
	
	private static String getSienaAddress() {
		InetAddress thisIp;
		String sienaAddress = "";

		try {
			thisIp = InetAddress.getLocalHost();
			sienaAddress = "udp:" + thisIp.getHostAddress() + ":2348";
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		
		return sienaAddress;
	}
	
	private static HierarchicalDispatcher createHierarchicalDispatcher() {
		HierarchicalDispatcher siena = null;
		
		try {
			siena = new HierarchicalDispatcher();
			siena.setMaster(getSienaAddress());
					
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		
		return siena;
	}
	
	private static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
