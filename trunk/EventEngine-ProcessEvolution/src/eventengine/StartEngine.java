package eventengine;

import gui.GUIController;
import gui.GUIEventEngine;

import java.util.List;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;

import ChangeController.NewFragmentListener;
import Logger.Logger;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.NotificationBuffer;
import siena.comm.TCPPacketReceiver;
import siena.comm.UDPPacketReceiver;



public class StartEngine {
	
//	private static String address = "udp:10.33.160.166:2347";
	
	private static String address;
	private static int thisPort;
	private static String thisConnection;
	private static int amountOfDataToSend;
	private static String bpmnFile;
	private static int taskTime;
	
	public static void main (String args[]) {
		if (args.length != 6) {
			System.out.println("Give the BPMN2.0 decentraliced file-path as argument");
			System.out.println("Usage: java EventEngine file.bpmn2 masterAdress thisConnection thisPort amountOfDataToSend taskTime");
		} else {
			
			bpmnFile = args[0];
			address = args[1];
			thisConnection = args[2];
			thisPort = Integer.parseInt(args[3]);
			amountOfDataToSend = Integer.parseInt(args[4]);
			taskTime = Integer.parseInt(args[5]);
			
			// Load an XML file
			EMFLoader loader = new EMFLoader();
			Definitions definitions = loader.load(bpmnFile);
			
			// Create the gui
			GUIEventEngine gui = new GUIEventEngine();
			// Show the gui
			gui.setVisible(true);
			
			for(RootElement element: definitions.getRootElements()) {
				if (element instanceof EventDefinition) {
					Debug.debug("CATCHED EVENTDEFINITION " + element.getId());
					
				}
				
				if (element instanceof org.eclipse.bpmn2.Process) {
					Debug.debug(element.getId());
					
					// Create the engines
					EventEngine engine = EngineFactory.getEventEngine((Process) element, null);
					List<Filter> filters = NotificationFactory.getFilters(engine.getEventRule());
					
					// Subscribe the engine for notifications with siena
					subscribe(filters, engine);
					
					// Create a controller for this engine that handles GUI events
					GUIController contr = new GUIController(engine, gui);
					
					// TEST Data to send
					engine.amountOfdataToSend = amountOfDataToSend;
					// Set Task Time (TODO)
					engine.getAction().setTaskTime(taskTime);
					
					// Logger
					Logger logger = new Logger((bpmnFile.split("\\\\")[bpmnFile.split("\\\\").length-1]).split("\\.")[0] + "-" + engine.getName() + ".log");
					engine.setLogger(logger);
										
					// Run the engine
					Thread t = new Thread(engine);
					t.start();
					
			
				}
			}
			
			// Create and run the NewFragmentListener
			NewFragmentListener newFragmentListener = new NewFragmentListener(gui, getHierarchicalDispatcher(), taskTime);
			Thread t = new Thread(newFragmentListener);
			t.start();
			
			// Leaf this process running
			while(true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}		
		}
	}

	/**
	 * Subscribe an engine to some notifications
	 * @param filters The notifications that the engine subscribes to
	 * @param engine The subscriber
	 */
	private static void subscribe(List<Filter> filters, EventEngine engine) {
			
//			NotificationBuffer buf = new NotificationBuffer();
//			
//			for (Filter f: filters) {
//				siena.subscribe(f, buf);
//				Debug.debug("Subscribing engine " + engine.getAction().getBpmn2Task().getId() + " to " + f.toString());
//			}	
//			
			engine.setHierarchicalDispatcher(getHierarchicalDispatcher());
			engine.subscribe(filters);
			
//			engine.setNotificationBuffer(buf);
		
		
	}
	
	private static HierarchicalDispatcher getHierarchicalDispatcher() {
		HierarchicalDispatcher siena = new HierarchicalDispatcher();

		try{
			// Set Receivers
			if (thisConnection.equals("tcp")) {
				siena.setReceiver(new TCPPacketReceiver(thisPort));
			} else {
				siena.setReceiver(new UDPPacketReceiver(thisPort));
			}

			siena.setMaster(address);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		return siena;
	}
}
