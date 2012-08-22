package ER2FSM.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ER2FSM.rules.Conjunction;

/**
 * Event rule in Disjunctive Normal Form
 */
public class DNFEventRule {
	
	// Event rule expressed in Disjunctive Normal Form
	// = disjunction of conjunctions
	private List<Conjunction> _conjunctions = new ArrayList<Conjunction>();
	
	public String fragment = "";
	
	public DNFEventRule() {
	
	}
	
	public DNFEventRule(DNFEventRule eventRule) {
		for (Conjunction conjunction: eventRule.getConjunctions()) {
			_conjunctions.add(new Conjunction(conjunction));
		}
		
		this.fragment = eventRule.fragment;
	}

	/**
	 * Returns the conjunction rule that is true (all events are fired)
	 * The condition attribute provides a data element for User defined condition tests (if any)
	 * 
	 * @return 	null if no conjunction rule is valid (all events in the conjunction are true)
	 * 			Conjunction the first conjunction rule that is valid in the DNFEventRule 
	 */
	public Conjunction getNextValidConjunction() {
				
		// Stop if found, return!
		for(Conjunction conjunction: this.getConjunctions()) {
			if (conjunction.evaluate()) 
				return conjunction;
		}
		
		return null;
	}
	
	public List<Conjunction> getAllValidConjunctions() {
		ArrayList<Conjunction> validConjunctions = new ArrayList<Conjunction>();
		
		for(Conjunction conjunction: this.getConjunctions()) {
			if (conjunction.evaluate()) 
				validConjunctions.add(conjunction);
		}
		
		return validConjunctions;
	}
	
	/**
	 * Evaluate the disjunctive rule
	 * 
	 * @return true if all events in a conjunction rule contained in this event rule equal true
	 */
//	public boolean evaluate() {
//		boolean result = false;
//		
//		for (Conjunction conjunction: this.getDisjunctionRule()) {
//			result = result || conjunction.evaluate();
//		}
//		
//		return result;
//	}
	
	/**
	 * set the event that is fired on true
	 * @param eventId
	 */
	public void setEventFired(String eventId) {
		for(Conjunction element: getConjunctions()) {
			element.setEventFired(eventId);
		}
	}
	
	public void setEventFiredAgain(String eventId) {
		for(Conjunction element: getConjunctions()) {
			element.setEventFiredAgain(eventId);
		}
		
	}
	
	/**
	 * Clear all events
	 */
	public void clearAll() {
		for(Conjunction element: this.getConjunctions()) {
			element.clearAll();
		}
	}
	
	/**
	 * Return all events contained in this event rule
	 * Order is of no importance
	 * @return
	 */
	public List<Event> getAllEvents() {
		ArrayList<Event> events = new ArrayList<Event>();
		
		for(Conjunction rule: this.getConjunctions()) {
			events.addAll(rule.getEvents());
		} 
		
		return events;
	}
	
	public void addConjunction(Conjunction rule) {
		this.getConjunctions().add(rule);
	}


	public List<Conjunction> getConjunctions() {
		return _conjunctions;
	}
	
	/**
	 * Evaluate this rule on a trace of completed events
	 * True if all elements in a conjunction are contained in the trace
	 * @param eventList
	 * @return
	 */
	public Conjunction evaluateOn(Collection<String> eventList){
		for (Conjunction conj: getConjunctions()) {
			if (conj.evaluateOn(eventList)) return conj;
		}
		
		return null;		
	}
	
	public boolean equals(Object otherRule) {
		if (!(otherRule instanceof DNFEventRule)) return false;
		
		if (((DNFEventRule) otherRule).getConjunctions().size() != this.getConjunctions().size()) return false;
		
		for (Conjunction conj: this.getConjunctions()) {
			if (!((DNFEventRule) otherRule).contains(conj)) return false;
		}
		
		return true;
	}
	
	public boolean contains(Conjunction otherConj) {
		for (Conjunction conj: this.getConjunctions()) {
			if (conj.equals(otherConj)) return true;
		}
		
		return false;
	}

	
	public String toString() {
		String text = fragment + " <[";
		
		Iterator<Conjunction> it = this.getConjunctions().iterator();
		
		if(it.hasNext()) {
			text = text + it.next().toString();
		}
		
		for (;it.hasNext(); ) {
			text = text + " OR " + it.next().toString();
		}
		
		return text + "]>";
	}

	public void setMax(DNFEventRule otherRule) {
		for (int i=0;i<this.getConjunctions().size();i++) {
			Conjunction thisConj = this.getConjunctions().get(i); 
			Conjunction otherConj = otherRule.getConjunctions().get(i);
			
			thisConj.setMax(otherConj);
		}		
	}

	

	
	
	
	
	
}
