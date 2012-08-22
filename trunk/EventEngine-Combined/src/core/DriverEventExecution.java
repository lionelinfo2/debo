package core;

import java.io.IOException;
import java.net.InetAddress;



public class DriverEventExecution {

	
	public static void main (String[] args) {
		
		String bpmnFile = "PizzaDelivery-dist.bpmn";
		
		try {
			/*** Start Siena Server ***/
			// Windows only solution!
			String command = "cmd /c start java -cp jars/siena.jar siena.StartServer -port 2348 -udp";
			Process pcSiena = Runtime.getRuntime().exec(command);
					
			// Wait for Siena to boot
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			/*** Start EventEngine ***/
			InetAddress thisIp = InetAddress.getLocalHost();
			String sienaAddress = "udp:" + thisIp.getHostAddress() + ":2348";
			
			command = "cmd /c start java -classpath \"jars/eventengine.jar;jars/siena.jar;libs/xmi.jar;libs/ecore.jar;libs/common.jar;libs/bpmn2.jar;libs/wsClient.jar\" " +
			"eventengine.StartEngine " + bpmnFile + " " + sienaAddress + " udp 0 100";
			Process pcTriggerEventEngine = Runtime.getRuntime().exec(command);
						
			// Wait for EventEngine to boot
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/** Start TriggerEventEngine ***/
			int rpm = 50;
			int minutes = 5;
			
			command = "cmd /c start java -cp jars/siena.jar tests.TriggerEventEngine " + sienaAddress + " " + rpm + " " + minutes;
			Process pcEventEngine = Runtime.getRuntime().exec(command);
			
		     
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
		
	}
	
}
