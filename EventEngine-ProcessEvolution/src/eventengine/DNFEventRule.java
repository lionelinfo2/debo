package eventengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Event rule in Disjunctive Normal Form
 */
public class DNFEventRule {
	
	// Event rule expressed in Disjunctive Normal Form
	// = disjunction of conjunctions
	private List<ConjunctionRule> _disjunctionRule = new ArrayList<ConjunctionRule>();
	
	public DNFEventRule() {
	
	}
	
	public DNFEventRule(DNFEventRule eventRule) {
		for (ConjunctionRule conjunction: eventRule.getDisjunctionRule()) {
			_disjunctionRule.add(new ConjunctionRule(conjunction));
		}
	}

	/**
	 * Returns the conjunction rule that is true (all events are fired)
	 * The condition attribute provides a data element for User defined condition tests (if any)
	 * 
	 * @return 	null if no conjunction rule is valid (all events in the conjunction are true)
	 * 			ConjunctionRule the first conjunction rule that is valid in the DNFEventRule 
	 */
	public ConjunctionRule getNextValidConjunctionRule(int condition) {
				
		// Stop if found, return!
		for(ConjunctionRule conjunction: this.getDisjunctionRule()) {
			if (conjunction.evaluate(condition)) 
				return conjunction;
		}
		
		return null;
	}
	
	/**
	 * Evaluate the disjunctive rule
	 * 
	 * @return true if all events in a conjunction rule contained in this event rule equal true
	 */
//	public boolean evaluate() {
//		boolean result = false;
//		
//		for (ConjunctionRule conjunction: this.getDisjunctionRule()) {
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
		for(ConjunctionRule element: getDisjunctionRule()) {
			element.setEventFired(eventId);
		}
	}
	
	/**
	 * Clear all events
	 */
	public void clearAll() {
		for(ConjunctionRule element: this.getDisjunctionRule()) {
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
		
		for(ConjunctionRule rule: this.getDisjunctionRule()) {
			events.addAll(rule.getEvents());
		} 
		
		return events;
	}
	
	public void addConjunctionRule(ConjunctionRule rule) {
		this.getDisjunctionRule().add(rule);
	}


	public List<ConjunctionRule> getDisjunctionRule() {
		return _disjunctionRule;
	}


	private void setDisjunctionRule(List<ConjunctionRule> _disjunctionRule) {
		this._disjunctionRule = _disjunctionRule;
	}
	
	public synchronized String toHTML() {
		String rep = "";
		
		for(ConjunctionRule conjuntion: getDisjunctionRule()) {
			if (rep.equals("")) {
				rep = rep + "(" + conjuntion.toHTML() + ")";
			} else {
				rep = rep + " OR " + "(" + conjuntion.toHTML() + ")";
			}
		}
		
		return rep;
	}
	
	public String toString() {
		String rep = "";
		
		for(ConjunctionRule conjuntion: getDisjunctionRule()) {
			if (rep.equals("")) {
				rep = rep + "(" + conjuntion.toString() + ")";
			} else {
				rep = rep + " OR " + "(" + conjuntion.toString() + ")";
			}
		}
		
		return rep;
	}

	
	
	
	
	
}
