package ChangeController;

import eventengine.DNFEventRule;
import eventengine.Event;
import eventengine.EventEngine;
import eventengine.NotificationFactory;
import eventengine.Task;
import gui.GUIController;
import gui.GUIEventEngine;

import java.util.List;
import java.util.Set;

import Logger.Logger;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notification;
import siena.NotificationBuffer;
import siena.SienaException;

public class NewFragmentListener implements Runnable {

	NotificationBuffer _buf = new NotificationBuffer();
	GUIEventEngine _gui;
	HierarchicalDispatcher _siena;
	int _taskTime;

	public NewFragmentListener(GUIEventEngine gui, HierarchicalDispatcher siena, int time) {
		_gui = gui;
		_siena = siena;
		_taskTime = time;

		// Subscribe to CTRL notifications
		this.subscribe();
	}

	@Override
	public void run() {
		Notification e;
		while(true) {
			try {
				e = _buf.getNotification(-1);

				String action = e.getAttribute("action").stringValue();
				if (action.equals("redeploy")) {
					// id = "CTRL"
					// fragment = "new"
					// action = "redeploy"
					// eventRule = "ER"
					// PIIDs = "PIID"
					// task = "name"
					// dataGen = "true/false"

					// endsignal = Signal + taskName

					// Create EventEngine
					EventEngine engine = new EventEngine();

					// Set rule
					DNFEventRule currentEventRule = NotificationParser.parseER(e.getAttribute("eventRule").stringValue());	
					engine.setEventRule(currentEventRule);				 

					// Set end event
					Event endEvent = new Event();
					endEvent.setSignal("Signal" + e.getAttribute("task").stringValue());
					engine.setEndEvent(endEvent);

					// Set task
					Task task = new Task();
					task.setName(e.getAttribute("task").stringValue());
					engine.setAction(task);
					
					engine.getAction().setTaskTime(_taskTime);

					// Set data generator
					engine._conditionalDataGeneration = e.getAttribute("dataGen").booleanValue();
					
					// Logger
					Logger logger = new Logger("NewFragment" + "-" + engine.getName() + ".log");
					engine.setLogger(logger);
					
					// Create a controller for this engine that handles GUI events
					GUIController contr = new GUIController(engine, _gui);
					
					// Initial state is suspend
//					engine.setSuspended(true);
					
					// Create subscription filters
					// Only subscribe to events with PIID not in the change region
					Set<Integer> PIIDs = NotificationParser.parsePIIDs(e.getAttribute("PIIDS").stringValue());
					List<Filter> filters = NotificationFactory.getFiltersExcludingPIIDs(engine.getEventRule(),PIIDs);
					engine.setHierarchicalDispatcher(_siena);
					engine.subscribe(filters);					

					// Run the engine
					Thread t = new Thread(engine);
					t.start();
				}
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}

	}

	private void subscribe() {
		// Subscribe to new fragment events

		try {
			Filter f1 = new Filter();
			f1.addConstraint("id", "CTRL");
			f1.addConstraint("fragment", "new");
			_siena.subscribe(f1, _buf);
		} catch (SienaException e) {
			e.printStackTrace();
		}
	}

}
