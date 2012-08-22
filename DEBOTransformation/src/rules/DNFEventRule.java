package rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class DNFEventRule {

	private Collection<Conjunction> _dnfRule = new ArrayList<Conjunction>();
	
	public Collection<Conjunction> getEventRule() {
		return _dnfRule;
	}
	
	public void add(Conjunction conj) {
		this.getEventRule().add(conj);
	}
	
	/**
	 * Perform the OR-operation between two DNFEventRules
	 * this.DNFEventRule will hold the result
	 */
	public void or(DNFEventRule rule2) {
		this.getEventRule().addAll(rule2.getEventRule());
	}
	
	/**
	 * Perform the AND-operation between two DNFEventRules
	 * this.DNFEventRule will hold the result 
	 */
	public void and(DNFEventRule rule2) {
		ArrayList<Conjunction> newConjunctionList = new ArrayList<Conjunction>();
				
		if (this.getEventRule().size() > 0) {
			for(Conjunction conjA: this.getEventRule()) {
				for (Conjunction conjB: rule2.getEventRule()) {
					// Add new conjunction to the new list
					newConjunctionList.add(conjA.and(conjB));
				}
			}
			
			// Assign this dnf_rule the new conjunction list
			this._dnfRule = newConjunctionList;
		} else {
			this._dnfRule = rule2.getEventRule();
		}
	}
	
	public String toString() {
		String text = "";
		
		Iterator<Conjunction> it = this.getEventRule().iterator();
		
		if(it.hasNext()) {
			text = it.next().toString();
		}
		
		for (;it.hasNext(); ) {
			text = text + " OR " + it.next().toString();
		}
		
		return "[" + text + "]";
	}

	/**
	 * Count how many times a specific event is present in how many conjunctions in this
	 * DNFEventRule
	 * This is necessary to build the BPMN xml file: is an eventDefinitionsRef needed or not?
	 * (reference to the same event definition from separate start events (conjunctions))
	 * @param e
	 * @return
	 */
	public int countEventOccurenceInConjunctions(Event e) {
		int i = 0;
		for(Conjunction con: this.getEventRule()) {
			if(con.contains(e)) {
				i++;
			}
		}
		
		return i;
	}
	
	/**
	 * Does this event rule contain a specific conjunction?
	 * 
	 * @param conj
	 * @return
	 */
	public boolean contains(Conjunction conj) {
		for (Conjunction c: getEventRule()) {
			if (c.equals(conj)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Two event rules are equal if they have the same number of conjuntions, and
	 * each conjunction in one rule is also found in another rule.
	 * @param erToCompare
	 * @return
	 */
	public boolean equals(DNFEventRule erToCompare) {
		if (this.getEventRule().size() != erToCompare.getEventRule().size()) return false;
		
		for (Conjunction conj: this.getEventRule()) {
			// Each conjunction has to be found in other DNF Event Rule
			if (!erToCompare.contains(conj)) return false;
		}
		
		return true;
	}
	
	/**
	 * Evaluate this rule on a trace of completed events
	 * True if all elements in a conjunction are contained in the trace
	 * @param eventList
	 * @return
	 */
	public Conjunction evaluateOn(Collection<String> eventList){
		for (Conjunction conj: getEventRule()) {
			if (conj.evaluateOn(eventList)) return conj;
		}
		
		return null;		
	}
	
	
	
	
	
}
