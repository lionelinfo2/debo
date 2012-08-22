package eventengine;

import gui.GUIController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notification;
import siena.NotificationBuffer;
import siena.SienaException;
import siena.comm.TCPPacketReceiver;
import siena.comm.UDPPacketReceiver;
import ChangeController.FragmentChangeListener;
import Logger.Logger;

/**
 * This class implements one event engine, i.e. an engine that starts
 * when its event rule evaluates to true.
 * Typically this engine only contains one executable task.
 */
public class EventEngine implements Runnable {
	
	// TODO create list of event rule (hashmap with ID or Current and RULE)
	private DNFEventRule _currentEventRule;
	private HashMap<DNFEventRule,HashSet<Integer>> _oldEventRules = new HashMap<DNFEventRule, HashSet<Integer>>();
	
	private Task _action;
	private Event _endEvent;
	private List<ProcessInstance> _processInstances = new ArrayList<ProcessInstance>();
	
	private HierarchicalDispatcher _siena;
	private NotificationBuffer _buf = new NotificationBuffer();
	private GUIController _guiController;
	
	private HashMap<String, NotificationBuffer> _instanceBuffers = new HashMap<String, NotificationBuffer>();
	
	private Logger _log;
	
	public int amountOfdataToSend;
	
	public boolean _conditionalDataGeneration = false;
	public boolean _suspended = false;
	
	public EventEngine(DNFEventRule eventRule, Task action, Event endEvent) {
		this.setEventRule(eventRule);
		this.setAction(action);
		this.setEndEvent(endEvent);	
		
		// create event subscription
		// subscribe event
		// create filter
		// siena.subscribe(filter, this)
	}
	
	public EventEngine() {
		// TODO Auto-generated constructor stub
		
	}
		
	/**
	 * Thread to run this event engine
	 * The engine waits until there is an event in the Notification Buffer.
	 * If an event is available this event is handled, and directed to the correct process instance
	 */
	@Override
	public void run() {
		getGuiController().printDebug("Started Engine " + this.getName());
		//initLogging();
		
		// Start Change Management Controller
		FragmentChangeListener cl = new FragmentChangeListener(this);
		Thread t = new Thread(cl);
		t.start();
				
		Notification e;
		while(true) {
			try {
				
				// Suspended?
				if (!_suspended) {

					e = _buf.getNotification(-1);

//					System.out.println("Received: " + e);
					notify(e);

				} else {
					// Wait
					synchronized (this) {
						System.out.println("SUSPENDED");
						this.wait();
						System.out.println("RESUMED");
					}
				}

			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void notify(Notification e) { 
		NotificationBuffer instanceBuf = _instanceBuffers.get(e.getAttribute("processInstanceId").stringValue());
		
		// Get process instance corresponding to this event
//		ProcessInstance processInstance = getProcessInstanceWithId(e.getAttribute("processInstanceId").stringValue());
		
		/** TODO check for correct event rule */
		if (instanceBuf == null) {
			instanceBuf = new NotificationBuffer();
			_instanceBuffers.put(e.getAttribute("processInstanceId").stringValue(), instanceBuf);
			// Process instance is not found
			// Create new process instance and route event

			// TODO retreive the correct event rule, if PIID element of oldRuleSet
			// -> use oldEventRule
			// else, use current Rule
			ProcessInstance processInstance = new ProcessInstance(e.getAttribute("processInstanceId").stringValue(), getEventRule(e.getAttribute("processInstanceId").intValue()), getAction(), getEndEvent(), this, instanceBuf);
			
			synchronized (getProcessInstances()) {
				// Only add when GUI hasn't got the lock
				getProcessInstances().add(processInstance);
			}
			
			// Run the process Instance
			Thread t = new Thread(processInstance);
			t.start();

		}
		
		// Notify the process instance of arrival of a new event notification
		instanceBuf.notify(e);
				
		getGuiController().updateInstanceInformation();
	
	}

	public void notify(Notification[] s) throws SienaException {
		// We don't need sequences of events		
	}
	
	public void redeploy(DNFEventRule newRule, HashSet<Integer> oldPIIDS) {
		// Redeploy the the event engine with a new event rule
		// a list of old PIIDS is kept, which need to be run with the old event rule
		// newRule becomes the current rule
		
		// If new events are added -> new subscriptions are needed
		// newRule.getAllEvents diff (all) Old DNFEventRule.getAllEvents
		//   -> resubscribe
		
		// Unsubscribe for old events
		this.unsubscribe(NotificationFactory.getFilters(_currentEventRule));
				
		// Resubscribe for the events for the old event rule
		if (oldPIIDS.size() != 0) {
			// only if the old event rule needs to be kept
			this.subscribe(NotificationFactory.getFilters(_currentEventRule, oldPIIDS));
			// TODO subscribe for oldER event rules? i.e. event rules already in the _oldEventRules set?
			// Only necessary if (fast) succeeding process changes
			// -> not necessary because still subscribed (unsubscription is only done for current event rule)
		}
		
		_oldEventRules.put(_currentEventRule, oldPIIDS);
		_currentEventRule = newRule;
		
		// Subscribing for the new events
		this.subscribe(NotificationFactory.getFilters(_currentEventRule));
		
		// TODO, don't subscribe with PIID for events also in new rule?
		
		this._guiController.initiateInformationPanel();
		
	}
	
	private void unsubscribe(List<Filter> filters) {
		try{
			for (Filter f: filters) {
				this.getHierarchicalDispatcher().unsubscribe(f, _buf);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
	
	public void subscribe(List<Filter> filters) {
		try{
			for (Filter f: filters) {
				// System.out.println("Subscribing " + getName() + " for: " + f);
				
				this.getHierarchicalDispatcher().subscribe(f, _buf);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		
	}
	
	private DNFEventRule getEventRule(int instanceID) {
		for (DNFEventRule rule: _oldEventRules.keySet()) {
			if (_oldEventRules.get(rule).contains(instanceID)) {
				return rule;
			}
		}
		
		return getEventRule();
	}
	
	
	
	/*********************
	 * GETTER AND SETTER *	
	 ********************/	
	 	
	public void setEndEvent(Event endEvent) {
		_endEvent = endEvent;
	}

	public void setAction(Task action) {
		_action = action;
	}

	public void setEventRule(DNFEventRule eventRule) {
		_currentEventRule = eventRule;
	}
	
	public DNFEventRule getEventRule() {
		return _currentEventRule;
	}
	
	public Task getAction() {
		return _action;
	}
	
	public Event getEndEvent() {
		return _endEvent;
	}
	
	public void setController(GUIController controller) {
		_guiController = controller;
	}
	
	public GUIController getGuiController() {
		return _guiController;
	}
	
	public List<ProcessInstance> getProcessInstances() {
		return _processInstances;
	}
	
	public String getName() {
		return getAction().getName();
	}
	
	/**
	 * Returns the process instances contained in this event engine with some process
	 * instance id.
	 * returns null if process instance is not found
	 * @param id
	 * @return
	 */
	public ProcessInstance getProcessInstanceWithId(String id) {
		for (ProcessInstance process: getProcessInstances()) {
			if (process.getInstanceId().equals(id)) {
				return process;
			}
		}
		
		return null;
	}
	
	public String toString() {
		// TODO for each ER in HashMap
		String text = "<html>";
		
		for (DNFEventRule rule: _oldEventRules.keySet()) {
			text = text + "<font color=blue>--- PIIDs: </font>" + _oldEventRules.get(rule).toString() + "<br/>";
			text = text + this.ruleToString(rule) + "<br/>";
		}
		
		// Add current rule
		text = text + "<font color=green>--- Current</font><br/>";
		text = text + this.ruleToString(_currentEventRule);
		
		return text + "</html>";

	}
	
	public String ruleToString(DNFEventRule rule) {
		return rule.toString() + " -> " + getAction().toString() + " -> " + getEndEvent().toString();
	}

	public String toNitroDate(long time) {
		Date date = new Date(time);
		
		int millis = (int) (date.getTime() % 1000l);
		
		
		return null;
	}
	
	public void publish(String signal, String instanceId, long start, long end) {
		Notification e = new Notification();
	    e.putAttribute("id", signal);
	    e.putAttribute("processInstanceId", instanceId);
	    e.putAttribute("startTime", start);
	    e.putAttribute("endTime", end);
	    
	    e.putAttribute("time",System.currentTimeMillis());
	    // TODO time in date format:  YYYY/MM/DD HH:MM:SS.ms
	    
	    
	    // Add condition data attribute for XOR gateway conditions
	    int cond;
	    if (_conditionalDataGeneration) {
	    	cond = (int) (Math.random()*100);
	    } else {
	    	cond = -1;
	    }
	    	    
	    e.putAttribute("condition", cond);
	    
	    // Add attributes for data testing
	    
//	    for (int i=0;i<amountOfdataToSend;i++){
//	    	// 512 Bytes
//	    	String bytes512 = "/FJ1/1KF47mR6VVHhvwpP9p0GHys51KDi4r5Od39Uh33DQdFRg0/kRpjtYJFjxPDCd1LMdiq5fgpUUpdnXaBTtOazpz1IDOPWCe4HgScBVODaHA0pFQ72ADctr6LpuIRW0AWhi79Dsx0JOXhKzA8ypRMVwk5ezf9igAvHvjEyA9+SH9QFY0wX1n6EuqWIUij60/zmVV02KhDHpVE7GR6W87ACQKhYES9Ne8agxcp7I8eE0zzyjPwXS3bWzSit7ajAMsjIR/zHbn880XQ88yStCTpYbX+earPTfBuJAefj7VufSJmrUHio+W4pX9hG6fz2T+EzWpSOjArBrl9P5CD+JhTxqHAsX4m69HLsg81WjR9mNzAG+JI31ah2AshRgG+ni6teJ9HpvNN65CEnKeftzUQMAM0hv8Q3h7WUbZDv5nI5dPt5SzPXg4O3hcfmGpdiXgZlGpCx4fYwOx7vSaRe/9miRgDh+o5dvSp55+9dTPk/z1jhn8X5N7KuLcqyqTuUWd1zLud0BiPbpwkmCsgxhhqPsrnGe5ejK5c1KM4UbVwo5RsplGbPqjWCUl87Chz19hmwf2/yvwESG/sAm/h8zSwTJyv4mgG1pU4sDBsU207JF9Sz6BHaGsqxYPW7gRAFFv5A5YGQY5uuCLqi7I+Nz/DXTJ2aqmsavxm8+Eg0DU=";
//	    	e.putAttribute("data" + i, bytes512);		
//	    }
	    
	    // If id=end and processInstanceId = total -> send ending event
	    
	    
	    // if suspended, don't send end event
	    publish(e);
	}
	
	public void publish(Notification e) {
//		System.out.println("SENDING: " + e);
		
		synchronized (_siena) {
			try {
				_siena.publish(e);
			} catch (SienaException ex) {
				System.err.println("Siena error:" + ex.toString());
			}
		}
		
		this.log(e);		
	}
	
	private void log(Notification e) {
		// LOGGING
		//if (e.getAttribute("id").stringValue().equals("SignalEnd")) {
		
		if (_log != null) {
			_log.write(e);		
		}
			
		//}
		
	}

	public void setHierarchicalDispatcher(HierarchicalDispatcher siena) {
		_siena = siena;
	}
	
	public HierarchicalDispatcher getHierarchicalDispatcher() {
		return _siena;
	}

	public void setNotificationBuffer(NotificationBuffer buf) {
		_buf = buf;
	}
	
	public NotificationBuffer getNotificationBuffer(){
		return _buf;
	}

	public void setLogger(Logger logger) {
		_log = logger;
		
	}
	
	public synchronized void setSuspended(boolean suspeneded) {
		_suspended = suspeneded;
	}
	

	
}
