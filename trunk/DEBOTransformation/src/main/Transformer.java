package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.Signal;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.Task;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import rules.Conjunction;
import rules.DNFEventRule;
import rules.Event;
import util.EMFHelper;

public class Transformer {

	/*********************
	 * Singleton methods *
	 *********************/
	private static Transformer _transformerInstance;
	
	public static Transformer getInstance() {
		if (_transformerInstance == null) {
			_transformerInstance = new Transformer();
		}
		return _transformerInstance;
	}
	
	
	
	/****************
	 * Main methods *
	 ****************/
	
	private EMFHelper _emfHelper = new EMFHelper();
	// Coupling between tasks, start events and signals (publish events)
	private HashMap<FlowNode, Signal> _signals = new HashMap<FlowNode, Signal>();
	private HashMap<Event, SignalEventDefinition> _eventDefinitions = new HashMap<Event, SignalEventDefinition>();
	
	/**
	 * Decentralice the file inputFile and put decentralized entities in outputFile
	 */
	public void decentralize(String inputFile, String outputFile) {
		this.decentralize(_emfHelper.getRootDefinitionElement(_emfHelper.load(inputFile)), 
					outputFile);		
	}
	
	/**
	 * Decentralize a BPMN process diagram represented by the root object Definitions
	 * Save the decentraliced entities in the outputFile
	 * 
	 * @param definitions
	 * @param outputFile
	 */
	public void decentralize(Definitions definitions, String outputFile) {
		Definitions decentralized = this.decentralize(definitions);
		
		try {
			_emfHelper.save(outputFile, decentralized);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Decentralize a BPMN diagram represented by the root object Definitions
	 * 
	 * Contains the main algorithm!
	 * 
	 * Creates one file containing all the split processes
	 * 
	 * @param oldModel
	 * @return
	 */
	public Definitions decentralize(Definitions oldModel) {
		// Create definitions element
		Definitions decModel = Bpmn2Factory.eINSTANCE.createDefinitions();
		decModel.setExporter("Exporter");
		decModel.setExporterVersion("1");
		decModel.setName("Name");
		decModel.setTargetNamespace("tns1");
		
		// Look for process element
		for(RootElement rootElement: oldModel.getRootElements()) {
			if (rootElement instanceof org.eclipse.bpmn2.Process) {
				// Look for tasks
				for(FlowElement flowElement: ((Process) rootElement).getFlowElements()) {
					
					if (flowElement instanceof Task) {
						Task t = (Task) flowElement;
						DNFEventRule rule = this.getEventRule(t);
						
						// Create the process and add it to the rootElements of the new definitions object
						decModel.getRootElements().add(this.createProcess(t, rule));
						// Add the output signal to the new model
						decModel.getRootElements().add(this.getSignal(t));
		
						// Don't forget to add EventDefinitionsRefs... they need a container too
						for (Conjunction con: rule.getEventRule()) {
							for (Event e: con.get_events()) {
								// For each event that occurs in more than one conjunction (start event)
								// -> add an eventdefinition on root level, which is referred to in the start event
								//		using the eventDefinitionRef attribute
								if (rule.countEventOccurenceInConjunctions(e) > 1) {
									decModel.getRootElements().add(this.getSignalEventDefinition(e));
								}
							}
						}
						
					}
					
					// Add the signal for the start of the process
					if (flowElement instanceof StartEvent) {
						// Create, if not existing, a signal event for this start event
						// and add it to the new decentralized model
						decModel.getRootElements().add(this.getSignal((StartEvent) flowElement));
					}
					
				}
			}
		}
		
		
		
		
		return decModel;
		
	}
	
	/**
	 * Get event rule for a specific task
	 * Search each incoming sequence flow for necessary signal event
	 * Place rules in disjunction 
	 * 
	 * @param t
	 * @return
	 */
	public DNFEventRule getEventRule(Task t) {
		DNFEventRule rule = new DNFEventRule();
		
		for(SequenceFlow sf: t.getIncoming()) {
			rule.or(this.getEventRule(sf));
		}
		
		return rule;
	}
	
	/**
	 * Get event rule for a specific Sequence Flow
	 * if source = task
	 * 	return signal of that task
	 * if source = AND-Gateway
	 * 	return conjunction of incoming SF rules
	 * if source = XOR-Gateway
	 * 	return disjunction of incoming SF rules
	 * if source = start-event
	 * 	return signal of start event
	 * 
	 * @param sf
	 * @return
	 */
	private DNFEventRule getEventRule(SequenceFlow sf) {
		DNFEventRule rule = new DNFEventRule();
		
		FlowNode source = sf.getSourceRef();
		if (source instanceof Task) {
			Event ev = new Event(this.getSignal(source));
			rule.add(new Conjunction(ev));
		} else if (source instanceof StartEvent) {
			Event ev = new Event(this.getSignal(source));
			rule.add(new Conjunction(ev));
		} else if (source instanceof ExclusiveGateway) {
			// Loop over previous sequence flow
			for (SequenceFlow sf2: source.getIncoming()) {
				rule.or(this.getEventRule(sf2));
			}

			// Add the condition (if any) on the current sequence flow in conjunction with
			// the other signal events
			if (sf.getConditionExpression() != null) {
				DNFEventRule rule2 = new DNFEventRule();
				rule2.add(new Conjunction((Expression) sf.getConditionExpression()));
				rule.and(rule2);
			}
			
		} else if (source instanceof ParallelGateway) {
			for (SequenceFlow sf2: source.getIncoming()) {
				rule.and(this.getEventRule(sf2));
			}
		}
		
		return rule;
	}
	
	private Signal getSignal(FlowNode fn) {
		if (!_signals.containsKey(fn)) {
			Signal s = Bpmn2Factory.eINSTANCE.createSignal();
			if (fn instanceof StartEvent) {
				s.setId("SignalStartProcess");
			} else {
				s.setId("Signal" + fn.getName());
			}
			
			//s.setId(String.valueOf(UUID.randomUUID()));
			//s.setName("Signal-" + fn.getName());
			s.setName(s.getId());
			
			_signals.put(fn, s);
		}
		
		return _signals.get(fn);
	}
	
	private Process createProcess(Task t, DNFEventRule rule) {
		// DEBUG TODO
		System.out.println("Creating Process for task: " + t.getName());
		System.out.println("+++ " + rule.toString());
		
		// Create process element
		Process p = Bpmn2Factory.eINSTANCE.createProcess();
		p.setId(String.valueOf(UUID.randomUUID()));
		p.setName("Process-" + t.getName());
		
		// Create task
		// Link task to process element
		Task newTask = EcoreUtil.copy(t);
		newTask.getIncoming().clear();
		newTask.getOutgoing().clear();
		p.getFlowElements().add(newTask);
		
		// Create End Event + output EventDefinition
		EndEvent endEvent = Bpmn2Factory.eINSTANCE.createEndEvent();
			SignalEventDefinition signalEventDefinition = Bpmn2Factory.eINSTANCE.createSignalEventDefinition();
			signalEventDefinition.setId(String.valueOf(UUID.randomUUID()));
			signalEventDefinition.setSignalRef(this.getSignal(t));
		endEvent.getEventDefinitions().add(signalEventDefinition);
		endEvent.setId("EndEvent-" + String.valueOf(UUID.randomUUID()));
		endEvent.setName("EndEvent");
		p.getFlowElements().add(endEvent);
		
		// Create Start Events + input EventDefinitions
		Collection<FlowElement> startEvents = this.createStartEventsAndSequenceFlows(rule, newTask);
		p.getFlowElements().addAll(startEvents);
		
		// Create Sequence Flows
		// Outgoing
		SequenceFlow endSequenceFlow = this.createSequenceFlow(newTask,endEvent);
		p.getFlowElements().add(endSequenceFlow);

		return p;
	}
	
	/**
	 * Create a start event for each conjunction in the DNF-EventRule
	 * For each start event create a sequence flow from this event to the 'to'-flownode
	 * 				+ add the respective conditions on the sequence flow
	 * 				Multiple conditions are placed in conjunction on the sequence flow,
	 * 				with 'AND' as the formal representation of the conjunction.
	 * 
	 * @param rule
	 * @param to
	 * @return
	 */
	private Collection<FlowElement> createStartEventsAndSequenceFlows(DNFEventRule rule, FlowNode to) {
		ArrayList<FlowElement> resultList = new ArrayList<FlowElement>();
				
		for(Conjunction con: rule.getEventRule()) {
			// Create start event
			StartEvent se = Bpmn2Factory.eINSTANCE.createStartEvent();
			se.setId(String.valueOf(UUID.randomUUID()));
			se.setName(con.toShortString());
			
			// Create Event Definitions for each event in the conjunction
			int i = 0;
			for (Event e: con.get_events()) {
				if(rule.countEventOccurenceInConjunctions(e) > 1){
					se.getEventDefinitionRefs().add(this.getSignalEventDefinition(e));
				} else {
					se.getEventDefinitions().add(this.getSignalEventDefinition(e));				
				}
				i++;
			}
			// Parallel Multiple marker?
			if (i > 1) {
				se.setParallelMultiple(true);
			}
						
			// Add start event to the resulting list
			resultList.add(se);
			
			// Create Sequence Flow
			SequenceFlow sf = this.createSequenceFlow(se, to);
			
			// Add conditional expressions to the sequence flow
			// For now: only informal expression expressed in the documentation
			// of the Expression supported (BPMN-EMF doesn't read the body of the FormalExpression)
			i = 0;
			Expression exp = Bpmn2Factory.eINSTANCE.createExpression();
			exp.setId(String.valueOf(UUID.randomUUID()));
			
			for (Expression e: con.get_conditions()) {
				exp.getDocumentation().addAll(e.getDocumentation());
				i++;
			}
			// Conditional expression?
			if (i>0) {
				sf.setConditionExpression(exp);
			}
			
			// Add sequence flow to the resulting list
			resultList.add(sf);
		}
		
		return resultList;
	}
	
	private SignalEventDefinition getSignalEventDefinition(Event e) {
		if (!_eventDefinitions.containsKey(e)) {
			SignalEventDefinition signalEventDefinition = Bpmn2Factory.eINSTANCE.createSignalEventDefinition();
			signalEventDefinition.setId(String.valueOf(UUID.randomUUID()));
			signalEventDefinition.setSignalRef(e.get_signal());
			
			_eventDefinitions.put(e, signalEventDefinition);
		}
		
		return _eventDefinitions.get(e);
	}
	
	/**
	 * Create sequence flow with random id
	 * @param from
	 * @param to
	 * @return
	 */
	private SequenceFlow createSequenceFlow(FlowNode from, FlowNode to) {
		SequenceFlow sf = Bpmn2Factory.eINSTANCE.createSequenceFlow();
		sf.setSourceRef(from);
		sf.setTargetRef(to);
		sf.setId(String.valueOf(UUID.randomUUID()));
		sf.setName("from-" + from.getName() + "-to-" + to.getName());
		
		return sf;
	}
	
	
}
