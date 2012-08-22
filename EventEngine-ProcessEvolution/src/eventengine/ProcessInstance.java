package eventengine;

import java.util.concurrent.SynchronousQueue;

import eventengine.Event.Fired;

import siena.Notification;
import siena.NotificationBuffer;

public class ProcessInstance implements Runnable {
	DNFEventRule _eventRule;
	Task _action;
	State _state;
	String _instanceId;
	Event _endEvent;
	EventEngine _engine;
	// Notification Buffer used to pass notifications from the event engine Thread to the
	// process instance thread
	NotificationBuffer _buf;
	Boolean _instanceDone = false;


	public ProcessInstance(String instanceId, DNFEventRule eventRule,
			Task action, Event endEvent, EventEngine engine, NotificationBuffer newBuf) {
		_instanceId = instanceId;
		_eventRule = new DNFEventRule(eventRule);
		_action = new Task(action);
		_endEvent = new Event(endEvent);
		_engine = engine;
		_buf = newBuf;
	}


	public String getInstanceId() {
		return _instanceId;
	}


	public void notify(Notification e) {
		getEventRule().setEventFired(e.getAttribute("id").stringValue());
		int condition = e.getAttribute("condition").intValue();
		
		ConjunctionRule validRule = getEventRule().getNextValidConjunctionRule(condition);
		while(validRule != null) {
			Debug.debug("Valid rule found");
			getAction().doTask();
			validRule.clearAll();
			validRule = getEventRule().getNextValidConjunctionRule(condition);
			throwEndEvent();
			getEngine().getGuiController().printDebug("Task [" + this.getAction().getName() + "] Performed");
			getEngine().getGuiController().updateInstanceInformation();
		}
	}

	/**
	 * Run the process instance 
	 * If a notification is available, process this notification
	 * This enables concurrent runs of process instances
	 */
	public void run() {
		// TODO: stop thread when throwevent? -> only when no
		// instance can run two times!
		
		Notification e;
		while (!_instanceDone) {
//		while(true) { 
			try {
				e = _buf.getNotification(-1);

				notify(e);
			} catch(InterruptedException exp) {
				exp.printStackTrace();
			}
		}
	}

	public synchronized void throwEndEvent() {
		// don't throw if suspended
		while (_engine._suspended) {
	    	try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
	    }		
		
		_instanceDone = true;
		getEngine().publish(getEndEvent().getSignal(), getInstanceId(), getAction().getStartTime(), getAction().getEndTime());
		getEndEvent().setFired(Fired.TOKEN);
		
		// TODO remove from event engine list?
	}



	private EventEngine getEngine() {
		return _engine;
	}


	public DNFEventRule getEventRule() {
		return _eventRule;
	}

	public Task getAction() {
		return _action;
	}

	public Event getEndEvent() {
		return _endEvent;
	}
	
	public boolean done() {
		return _instanceDone;
	}


	// Update Event Rule
	// Evaluate event rule
	// Do action is applicable
	// if action, reset event rule
	// Do again until no more rule evaluates to true!

}
