package core;

import io.SignavioParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.Transformer;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.Task;
import org.jdom.JDOMException;

import rules.DNFEventRule;
import siena.Filter;
import siena.HierarchicalDispatcher;
import siena.Notification;
import siena.NotificationBuffer;
import siena.SienaException;
import util.EMFHelper;



public class ChangeController implements Runnable{

	private NotificationBuffer _buf = new NotificationBuffer();
	private HierarchicalDispatcher _siena;
	private boolean _subscribed = false;
//	private currentProcessDescription;
			
	public ChangeController(HierarchicalDispatcher siena) {
		_buf = new NotificationBuffer();
		this.setHierarchicalDispatcher(siena);
		this.subscribe();
		
	}
	
	public ChangeController() {
		
	}
	
	@Override
	public void run() {
		// Catch any event notification for the Change Manager
		Notification e;
		while(true) {
			try {
				e = _buf.getNotification(-1);
				
				// Debug info
				System.out.println("Received: " + e.toString());
				
				
				// Do something
				
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void setHierarchicalDispatcher(HierarchicalDispatcher siena) {
		_siena = siena;
	}
	
	public HierarchicalDispatcher getHierarchicalDispatcher() {
		return _siena;
	}
	
	public Process startSiena() {
		String command = "cmd /c start java -cp jars/siena.jar siena.StartServer -port 2348 -udp";
		return runExternalProgram(command);
	}
	
	public Process startEventEngine(String bpmnFile, String sienaAddress, String taskTime) {
		/*** Start EventEngine ***/
		// Xss (java stack size) made smaller to allow more threads
		String command = "cmd /c start java -classpath \"jars/eventengine.jar;jars/siena.jar;libs/xmi.jar;libs/ecore.jar;libs/common.jar;libs/bpmn2.jar;libs/wsClient.jar\" " +
			"eventengine.StartEngine " + bpmnFile + " " + sienaAddress + " udp 0 0 " + taskTime;
		return runExternalProgram(command);	
	}
	
	public Process startClientTriggering(int rpm, int minutes, String sienaAddress) {
		/** Start TriggerEventEngine ***/
		String command = "cmd /c start java -cp jars/siena.jar tests.TriggerEventEngine " + sienaAddress + " " + rpm + " " + minutes;
		return runExternalProgram(command);	
		
	}
	
	private Process runExternalProgram(String cmd) {
		Process pc = null;
		try {
			pc = Runtime.getRuntime().exec(cmd);	
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return pc;
	}
	
	private void subscribe() {
		try {
			Filter f1 = new Filter();
			f1.addConstraint("id", "CTRL");
			f1.addConstraint("fragment", "ChangeManager");
			_siena.subscribe(f1, _buf);
							
		} catch (SienaException e) {
			e.printStackTrace();
		}
		
		_subscribed = true;
	}

	public void publishAction(String target, String action) {
		Notification e = new Notification();
		e.putAttribute("id", "CTRL");
		e.putAttribute("fragment", target);
		e.putAttribute("action", action);

		this.publish(e);
	}
	
	// TESTING
	public void publishRedeploy(String target, String PIIDs, String ER) {

		Notification e = new Notification();
		e.putAttribute("id", "CTRL");
		e.putAttribute("fragment", target);
		e.putAttribute("action", "redeploy");
		e.putAttribute("PIIDS", PIIDs);
		e.putAttribute("eventRule", ER);

		this.publish(e);

	}
	
	public void publishNewFragment(String taskName, String PIIDs, String ER, boolean dataGen) {
		Notification e = new Notification();
		e.putAttribute("id", "CTRL");
		e.putAttribute("fragment","new");
		e.putAttribute("task", taskName);
		e.putAttribute("action", "redeploy");
		e.putAttribute("eventRule", ER);
		e.putAttribute("PIIDS", PIIDs);
		e.putAttribute("dataGen", dataGen);
		
		this.publish(e);
	}
	
	public void publishSuspend(Set<String> fragmentsToSuspend) {
		for (String fragment: fragmentsToSuspend) {
			publishAction(fragment, "suspend");
		}
	}
	
	public void publishResume(Set<String> fragmentsToResume) {
		for (String fragment: fragmentsToResume) {
			publishAction(fragment, "resume");
		}
	}
	
	public void publishGetRunningProcessInstances(Set<String> toInspect) {
		for (String fragment: toInspect) {
			publishAction(fragment, "getRunningProcessIDs");
		}
	}
	
	public void publish(Notification n) {
		System.out.println("publishing " + n.toString());

		try{
			_siena.publish(n);
		} catch (Exception ex) {
			System.err.println("Siena error:" + ex.toString());
		}
	}
	
	private void executeChangeProtocol(Set<String> toSuspend, Set<String> changeRegion, HashMap<String, DNFEventRule> toRedeploy, HashMap<Task, DNFEventRule> newFragments) {
		/** SUSPENDING */
		this.publishSuspend(toSuspend);
//		this.publishAction("ALL", "suspend");
		
		/** SLEEPING TO WAIT FOR SUSPEND */
		// TODO change at event engine side
		// retrieve all instances currently doing a task and suspend the task?
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/** GET RUNNING PROCESS INSTANCE IIDS */
		this.publishGetRunningProcessInstances(changeRegion);
		
		/** RETRIEVE ALL PIIDS */
		int received = 0;
		Set<String> PIIDs = new HashSet<String>();
		while (received != changeRegion.size()) {
			// We are still waiting for notifications of fragments in the change region
			try {
				// TODO add timeout?
				Notification e = _buf.getNotification(-1);
				
				System.out.println("Received: " + e.toString());
				
				if(e.getAttribute("action").stringValue().equals("PIIDS")) {
					PIIDs.addAll(parsePIIDs(e.getAttribute("PIIDS").stringValue()));
					received++;
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("PIIDs: " + PIIDs);
		
		/** REDEPLOY WITH NEW EVENT RULE */
		for (String fragment: toRedeploy.keySet()) {
			publishRedeploy(fragment, PIIDs.toString(), toRedeploy.get(fragment).toString());
		}
		
		/** DEPLOY NEW FRAGMENTS */
		for (Task t: newFragments.keySet()) {
			// Datagenerator?
			boolean dataGen = false;
			if (t.getDocumentation().size() > 0) {
				dataGen = t.getDocumentation().get(0).getText().equals("data-generator");			
			}
			
			publishNewFragment(t.getName(), PIIDs.toString(), newFragments.get(t).toString(), dataGen);
		}		
		
		
		/** SLEEPING TO WAIT FOR REDEPLOY */
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		/** RESUME */
		// TODO uncomment
//		this.publishResume(toSuspend);
		this.publishAction("ALL", "resume");
		
		
	}
	
	public List<String> parsePIIDs(String stringPIIDs) {
		// If empty list
		if (stringPIIDs.length() == 2) return new ArrayList<String>();
		
		return new ArrayList<String>(Arrays.asList(stringPIIDs.substring(1, stringPIIDs.length()-1).split(", ")));
	}

	public void redeploy(String currentProcessDescription, String changedProcessDescription, boolean signavioFile) {
		// Subscribe first
		// @pre Siena HierarchicalDispatcher is set
		if (!_subscribed) this.subscribe();		
		
		File currentModelFile = new File(currentProcessDescription);
		File changedModelFile = new File(changedProcessDescription);
		
		Set<String> changeRegion = new HashSet<String>();
		Set<String> toSuspend = new HashSet<String>();
		HashMap<String,DNFEventRule> fragmentsToRedeploy = new HashMap<String,DNFEventRule>();
		HashMap<Task,DNFEventRule> newFragmentsToDeploy = new HashMap<Task, DNFEventRule>(); 
		
		BPMNChangeRegion crConvertor = new BPMNChangeRegion();
		
		if (signavioFile) {
			try {
				currentModelFile = SignavioParser.process(currentProcessDescription);
				changedModelFile = SignavioParser.process(changedProcessDescription);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			}
		}
		// Load the current process description to look for border regions
		// Do this here to avoid duplicate loading of BPMN files
		EMFHelper _emfHelper = new EMFHelper();
		Definitions currentModelDef = _emfHelper.getRootDefinitionElement(_emfHelper.load(currentModelFile));
		Definitions changedModelDef = _emfHelper.getRootDefinitionElement(_emfHelper.load(changedModelFile));
		
		// Calculate the change region
		changeRegion = crConvertor.getMinimalChangeRegion(currentModelDef, changedModelDef);
		
		// Retrieve the border tasks (also need to be suspended)
		Set<String> borderTasks = getBorderTasksUpward(crConvertor.currentBPMN2PNConvertor._nameToNode, changeRegion);
		
		// Suspend change region fragments of current process deployment
		// + border fragments
		toSuspend.addAll(changeRegion);
		toSuspend.retainAll(crConvertor.currentBPMN2PNConvertor._net.getAllNodes());
		toSuspend.addAll(borderTasks);
		
		// TODO output
		System.out.println("Change Region: " + changeRegion);
		System.out.println("Border Tasks: " + borderTasks);
		System.out.println("To Suspend: " + toSuspend);
	
		// For each task(!) in change region in old and new process def
		// calculate event rule: Transformer.getEventRule(Task t)
		// Compare the two event rules: DNFEventRule.equals(ER)
		// If different, redeploy
				
		// Names and Nodes links can be retrieved from the BPMN2PN transformation 
		// in the BPMNChangeRegion
		
		// expand with border regions in downward flow!
		// Needed to incorporate 'lost' states during change region calculation
		// Check in changed process model for added tasks and current model for deleted tasks
		Set<String> toInspect = new HashSet<String>();
		toInspect.addAll(changeRegion);
		toInspect.addAll(getBorderTasksDownward(crConvertor.newBPMN2PNConvertor._nameToNode, changeRegion));
		toInspect.addAll(getBorderTasksDownward(crConvertor.currentBPMN2PNConvertor._nameToNode, changeRegion));
		
		System.out.println("To Inspect: " + toInspect);
		
		for (String elInChangeRegion: toInspect) {
			// Retrieve the event rule for the old and new and check if something is changed
			Task taskCurrent = (Task) crConvertor.currentBPMN2PNConvertor._nameToNode.get(elInChangeRegion);
			Task taskChanged = (Task) crConvertor.newBPMN2PNConvertor._nameToNode.get(elInChangeRegion);
			
			if (taskChanged == null) {
				// Deleted task!
				fragmentsToRedeploy.put(elInChangeRegion,createEmptyRule());
			} else if (taskCurrent == null) {
				// Added task!
				DNFEventRule ruleChanged = (new Transformer()).getEventRule(taskChanged);
				newFragmentsToDeploy.put(taskChanged,ruleChanged);
			} else {
				DNFEventRule ruleCurrent = (new Transformer()).getEventRule(taskCurrent);
				DNFEventRule ruleChanged = (new Transformer()).getEventRule(taskChanged);
				
				if (!ruleCurrent.equals(ruleChanged)) fragmentsToRedeploy.put(elInChangeRegion,ruleChanged);
			}
		}
		// TODO added tasks
		
		System.out.println("Fragments to redeploy: " + fragmentsToRedeploy);
		System.out.println("New Fragments: " + newFragmentsToDeploy);
		
		// Inspection only for fragments in current configuration
		changeRegion.retainAll(crConvertor.currentBPMN2PNConvertor._net.getAllNodes());
		this.executeChangeProtocol(toSuspend, changeRegion, fragmentsToRedeploy, newFragmentsToDeploy);
		
		if (signavioFile) {
			// Cleanup
			currentModelFile.delete();
			changedModelFile.delete();
		}
	}
	
	private DNFEventRule createEmptyRule() {
		return new DNFEventRule();
	}

	/**
	 * Returns the border tasks names from a given region inside a model
	 * 
	 * @param model
	 * 			BPMN model
	 * @param region
	 * 			List of task names
	 */
	private Set<String> getBorderTasksUpward(HashMap<String, FlowNode> nameToNode, Set<String> region) {
		Set<String> borderTasks = new HashSet<String>();
		
		for (String task : region) {
			if (nameToNode.containsKey(task)) {
				borderTasks.addAll(retrieveTasksInUpwardsFlow(nameToNode.get(task)));
			} // else task is an added task, no need to find border tasks			
		}
		
		borderTasks.removeAll(region);
		return borderTasks;
	}
	
	private Set<String> retrieveTasksInUpwardsFlow(FlowNode el) {
		return retrieveTasksInUpwardsFlow(el, new HashSet<FlowNode>());
	}
	
	private Set<String> retrieveTasksInUpwardsFlow(FlowNode el, Set<FlowNode> visited) {
		Set<String> tasks = new HashSet<String>();
		
		for (SequenceFlow sf: el.getIncoming()) {
			
			if (sf.getSourceRef() instanceof Task) {
				tasks.add(sf.getSourceRef().getName());
			} else {
				// If gateway
				// Recursive search for tasks in upwards flow
				if(!visited.contains(el)) {
					visited.add(el);
					tasks.addAll(retrieveTasksInUpwardsFlow(sf.getSourceRef(), visited));
				}
			}
		}
		
		return tasks;
	}
	
	private Set<String> getBorderTasksDownward(HashMap<String, FlowNode> nameToNode, Set<String> region) {
		Set<String> borderTasks = new HashSet<String>();
		
		for (String task : region) {
			if (nameToNode.containsKey(task)) {
				borderTasks.addAll(retrieveTasksInDownwardFlow(nameToNode.get(task)));
			} // else task is an added task, no need to find border tasks			
		}
		
		borderTasks.removeAll(region);
		return borderTasks;
	}
	
	private Set<String> retrieveTasksInDownwardFlow(FlowNode el) {
		return retrieveTasksInDownwardFlow(el, new HashSet<FlowNode>());
	}
	
	private Set<String> retrieveTasksInDownwardFlow(FlowNode el, Set<FlowNode> visited) {
		Set<String> tasks = new HashSet<String>();
		
		for (SequenceFlow sf: el.getOutgoing()) {
			if (sf.getTargetRef() instanceof Task) {
				tasks.add(sf.getTargetRef().getName());
			} else {
				// If gateway
				// Recursive search for tasks in upwards flow
				if (!visited.contains(el)) {
					visited.add(el);
					tasks.addAll(retrieveTasksInDownwardFlow(sf.getTargetRef(), visited));
				}
			}
		}

		return tasks;
	}

	
}
