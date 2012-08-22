package ChangeController;

import java.util.HashSet;

import eventengine.ConjunctionRule;
import eventengine.DNFEventRule;
import eventengine.Event;

public class NotificationParser {
		
	public static DNFEventRule parseER(String eventRule) {
		DNFEventRule newRule = new DNFEventRule();
		
		// Empty Event Rule
		if (eventRule.length() == 2) return newRule;
		
		// Retrieve all conjunctions
		String[] conjunctions = eventRule.substring(1,eventRule.length()-1).split(" OR ");
		
		for (String conjunction: conjunctions) {
			ConjunctionRule newConj = new ConjunctionRule();
			
			// Retrieve all events within one conjunction
			String[] events = conjunction.substring(1, conjunction.length()-1).split(" AND ");
			
			for (String event: events) {
				if (event.substring(0, 5).equals("Cond(")) {
					// Condition found
					// Add it to the conjunction
					newConj.setBpmn2Expression(event.substring(5, event.length()-1));
				} else {				
					Event e = new Event();
					e.setSignal(event);
					newConj.addEvent(e);
					// Set BPMN2Event?
				}
			}
			
			// TODO setBpmn2Expression (if any)
			
			newRule.addConjunctionRule(newConj);
		}
		
		return newRule;
	}
	
	public static HashSet<Integer> parsePIIDs(String PIIDS) {
		if (PIIDS.length() == 2) return new HashSet<Integer>();
		return stringArrayToIntSet(PIIDS.substring(1,PIIDS.length()-1).split(", "));
	}
	
	public static HashSet<Integer> stringArrayToIntSet(String[] sArray) {
		HashSet<Integer> iArray = new HashSet<Integer>();
		
		for (String i: sArray) {
			iArray.add(Integer.parseInt(i));
		}
		
		return iArray;
	}
}
