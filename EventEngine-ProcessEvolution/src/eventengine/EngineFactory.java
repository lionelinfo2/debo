package eventengine;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.ThrowEvent;

import Logger.Logger;

public class EngineFactory {
	
	
	public static EventEngine getEventEngine(Process process) {
		
		EventEngine engine = new EventEngine();
		// Create event rule
		DNFEventRule rule = new DNFEventRule();
		engine.setEventRule(rule);
		
		// TODO new format for Event Rules!
		for(FlowElement element: process.getFlowElements()) {
			
			if (element instanceof CatchEvent) {
				Debug.debug("Catch Event found: " + element.getId());
				ConjunctionRule conjunction = new ConjunctionRule();
				
				// Use as well as event definitions as event definition REFS
				List<EventDefinition> eventDefRefList = ((CatchEvent) element).getEventDefinitionRefs();
				List<EventDefinition> eventDefList = ((CatchEvent) element).getEventDefinitions();
				List<EventDefinition> eventDefs = new ArrayList<EventDefinition>();
				eventDefs.addAll(eventDefList);
				eventDefs.addAll(eventDefRefList);				
				
				for (EventDefinition eventDef: eventDefs) {
					Debug.debug("Event Definition found: " + eventDef.getId());
					
					eventengine.Event newEvent = new eventengine.Event();
					newEvent.setBpmn2Event(eventDef);
					
					if (eventDef instanceof SignalEventDefinition 
							&& ((SignalEventDefinition) eventDef).getSignalRef() != null) {
						newEvent.setSignal(((SignalEventDefinition) eventDef).getSignalRef().getId());
					} else {
						newEvent.setSignal(eventDef.getId());
					}
					
					// Add event to conjunction rule
					conjunction.addEvent(newEvent);
					
				}
				
				
				
				// Always one outgoing sequence flow per catch event!
				// TODO change if more!
				conjunction.setBpmn2Expression(((CatchEvent) element).getOutgoing().get(0).getConditionExpression());
				
				if (conjunction.getBpmn2Expression() != null) {
					Debug.debug("+++Expression: " + conjunction.getRegionStart() + "--" + conjunction.getRegionEnd());
				}
				
				rule.addConjunctionRule(conjunction);
				
				
			} else if (element instanceof ThrowEvent) {
				Debug.debug("Throw Event found: " + element.getId());
				// Create end event
				eventengine.Event endEvent = new eventengine.Event();
				// Always 1 throw event!
				EventDefinition eventDef = ((ThrowEvent) element).getEventDefinitions().get(0);
				endEvent.setBpmn2Event(eventDef);
				
				if (eventDef instanceof SignalEventDefinition 
						&& ((SignalEventDefinition) eventDef).getSignalRef() != null) {
					endEvent.setSignal(((SignalEventDefinition) eventDef).getSignalRef().getId());
				} else {
					endEvent.setSignal(eventDef.getId());
				}
				
				engine.setEndEvent(endEvent);				
				
			} else if (element instanceof Task) {
				Debug.debug("Task found: " + element.getId());
				// Create task
				
				eventengine.Task newTask = new eventengine.Task();
				newTask.setBpmn2Task((Task) element);
				newTask.setName(element.getName());
				
				engine.setAction(newTask);
				
				// Data generator task?
				if (((Task) element).getDocumentation().size() > 0) {
					if (((Task) element).getDocumentation().get(0).getText().equals("data-generator")) {
						engine._conditionalDataGeneration = true;
					}
				}
			
			} else {
				Debug.debug("Unknown flow element found: " + element.getId());
			}
		}
			
		
		return engine;
	}

	public static EventEngine getEventEngine(Process element, Logger logger) {
		
		EventEngine eng = EngineFactory.getEventEngine(element);
//		eng.setLogger(logger);
		
		return eng;
	}
}
