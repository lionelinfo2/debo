package ChangeController;

import java.util.HashSet;
import java.util.SortedSet;

import javax.xml.stream.util.EventReaderDelegate;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.Expression;

import eventengine.ConjunctionRule;
import eventengine.DNFEventRule;
import eventengine.Debug;
import eventengine.Event;
import eventengine.EventEngine;
import eventengine.ProcessInstance;
import siena.AttributeConstraint;
import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notification;
import siena.NotificationBuffer;
import siena.Op;
import siena.SienaException;



/**
 * Fragment specific change controller
 * Regulates any specific changes from a specific fragment engine 
 * e.g. suspend, resume, getRunningProcessIDs, redeploy
 */
public class FragmentChangeListener implements Runnable{

	NotificationBuffer _controlBuffer = new NotificationBuffer();
	EventEngine _engine;
		
	public FragmentChangeListener(EventEngine en) {
		_engine = en;
		
		// Subscribe to CTRL notifications
		this.subscribe(_engine.getName(), _engine.getHierarchicalDispatcher());
	}
	
	@Override
	public void run() {
		
		Notification e;
		while(true) {
			try {
				e = _controlBuffer.getNotification(-1);
				
				String action = e.getAttribute("action").stringValue();
				if (action.equals("suspend")) {
					// Suspend
					
					_engine.setSuspended(true);
					_engine.getGuiController().setEngineTitle(_engine.getName() + " SUSPENDED");
					
				} else if (action.equals("resume")) {
					// Resume
					
					synchronized (_engine) {
						_engine.setSuspended(false);
						_engine.notify();
						_engine.getGuiController().setEngineTitle(_engine.getName());
					}
					
				} else if (action.equals("getRunningProcessIDs")) {
					System.out.println("getRunningProcessIDS: " + _engine.getName());
					
					// Retrieve all process instance events in the buffer					
					HashSet<Integer> PIIDS = new HashSet<Integer>();
					for (Notification n: _engine.getNotificationBuffer().getAllNotifications()) {
						PIIDS.add(n.getAttribute("processInstanceId").intValue());
					}
					// add all instances in already existing (i.e. instances in progress)
					synchronized (_engine.getProcessInstances()) {
						for (ProcessInstance pi: _engine.getProcessInstances()) {
							if (!pi.done()) {
								PIIDS.add(Integer.valueOf(pi.getInstanceId()));
							}
						}
					}
																		
					// Publish the results
					Notification result = new Notification();
					result.putAttribute("id", "CTRL");
					result.putAttribute("fragment", "ChangeManager");
					result.putAttribute("action", "PIIDS");
					result.putAttribute("PIIDS", PIIDS.toString());
					result.putAttribute("source", _engine.getName());
					this.publish(result);
					
				} else if (action.equals("redeploy")) {
					// Redeploy the new event rule
					
					// PIIDS which have to run with the old event rule
					HashSet<Integer> PIIDS = NotificationParser.parsePIIDs(e.getAttribute("PIIDS").stringValue());
					DNFEventRule er = NotificationParser.parseER(e.getAttribute("eventRule").stringValue());
					
					_engine.redeploy(er, PIIDS);
					
					// TODO if deleted fragment, also unsubscribe for CTRL messages! 
//					if (er.getAllEvents().isEmpty()) {
//						this.unsubscribe();
//					}
								
				}
				
				
				
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		
	}
	
	
	private void subscribe(String fragmentName, HierarchicalDispatcher siena) {
		// Subscribe to two filters, one specifying the CTRL for ALL
		// and other filter specifying the CTRL for this specific fragment
				
		try {
			Filter f1 = new Filter();
			f1.addConstraint("id", "CTRL");
			f1.addConstraint("fragment", "ALL");
			siena.subscribe(f1, _controlBuffer);
			
			Filter f2 = new Filter();
			f2.addConstraint("id", "CTRL");
			f2.addConstraint("fragment", fragmentName);
			siena.subscribe(f2, _controlBuffer);				
		} catch (SienaException e) {
			e.printStackTrace();
		}
	}
	
	private void unsubscribe() {
		Filter f1 = new Filter();
		f1.addConstraint("id", "CTRL");
		f1.addConstraint("fragment", "ALL");
		_engine.getHierarchicalDispatcher().unsubscribe(f1,_controlBuffer);

		Filter f2 = new Filter();
		f2.addConstraint("id", "CTRL");
		f2.addConstraint("fragment", _engine.getName());
		_engine.getHierarchicalDispatcher().unsubscribe(f2, _controlBuffer);				
	}
	
	private void publish(Notification n) {
		System.out.println("Publishing: " + n);
		
		try {
			_engine.getHierarchicalDispatcher().publish(n);
		} catch (SienaException e) {
			e.printStackTrace();
		}
	}
	
	

	
	
}
