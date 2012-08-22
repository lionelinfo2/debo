package ER2FSM.bak;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.impl.Bpmn2FactoryImpl;

public class Conjunction {
	
	private Collection<String> _conditions = new ArrayList<String>(); 
	private Collection<String> _events = new ArrayList<String>();

	
	public Conjunction(String e) {
		add_event(e);
	}
	
	public Conjunction() {
		
	}
	
	public Collection<String> get_conditions() {
		return _conditions;
	}
	
	public void set_conditions(Collection<String> _conditions) {
		this._conditions = _conditions;
	}
	
	public Collection<String> get_events() {
		return _events;
	}
	
	public void set_events(Collection<String> _events) {
		this._events = _events;
	}
	
	public void add_event(String event) {
		this.get_events().add(event);
	}
	
	public void add_condition(String exp) {
		this.get_conditions().add(exp);
	}
	
	public void setBpmn2Expression(String expression) {
		this.add_condition(expression);
	}

	/**
	 * Performs the AND-operation on two conjunctions
	 * Result is a new conjunction -> this ensures that the current conjunction doesn't get
	 * overriden. The current conjunction is still needed for calculation in other AND-loop
	 * @param conjB
	 */
	public Conjunction and(Conjunction conjB) {
		Conjunction newConjunction = new Conjunction();
		
		// Add conditions
		newConjunction.get_conditions().addAll(this.get_conditions());
		newConjunction.get_conditions().addAll(conjB.get_conditions());
		
		// Add Events
		newConjunction.get_events().addAll(this.get_events());
		newConjunction.get_events().addAll(conjB.get_events());
		
		return newConjunction;
	}
	
	public String toString() {
		String text = "";

		// Events
		Iterator<String> it = this.get_events().iterator();

		if(it.hasNext()) {
			text = it.next();
		}

		for (;it.hasNext(); ) {
			text = text + " AND " + it.next();
		}
		
		// Conditions
		Iterator<String> it2 = this.get_conditions().iterator();

		for (;it2.hasNext(); ) {
			text = text + " AND Cond(" + it2.next() + ")";
						
		}

		return "(" + text + ")";
	}
	
	public String toShortString() {
		String text = "";

		// Events
		Iterator<String> it = this.get_events().iterator();

		if(it.hasNext()) {
			text = it.next();
		}

		for (;it.hasNext(); ) {
			text = text + "-AND-" + it.next();
		}
		
		// Conditions
		Iterator<String> it2 = this.get_conditions().iterator();

		for (;it2.hasNext(); ) {
			text = text + "-AND-" + it2.next();
		}

		return text;
	}

	public boolean containsEvent(String event) {
		return this.get_events().contains(event);
	}
	
	public boolean containsCondition(String cond) {
		return this.get_conditions().contains(cond);
	}
	
	/**
	 * Return the event included in this rule similar to the event ev
	 * 
	 * @param ev
	 * @return
	 */
	public String retrieveSimilar(String ev, Set<String> visited) {
		for (String event : get_events()) {
			if (event.equals(ev) && !visited.contains(event)) return event;
		}
		
		return null;		
	}
	
	public boolean equals(Conjunction conj) {
		if (this.get_events().size() != conj.get_events().size() ||
				this.get_conditions().size() != conj.get_conditions().size()) 
			return false;
		
		Set<String> visited = new HashSet<String>();
		
		for (String e: this.get_events()) {
			// Each event has to be in other rule
			String sim = conj.retrieveSimilar(e, visited);
			if (sim == null) return false;
					
			visited.add(sim);
		}
		
		// TODO compare conditions
		for (String exp: this.get_conditions()) {
			if(!conj.containsCondition(exp)) return false;
		}
		
		return true;
	}
	
	public boolean evaluateOn(Collection<String> trace) {
		// Each event included in the conjunction should be present in the event trace
		return trace.containsAll(this.get_events());
	}

	public boolean isSubSetOf(Conjunction conj) {
		// true if each event in this conjunction is also included in 'conj'
		return conj.get_events().containsAll(this.get_events());
	}

	public boolean isConditionsSuperSetOf(Collection<String> conditions) {
		// true if each condition in the provided set is also
		// included as a condition in this conjunction
		return this.get_conditions().containsAll(conditions);
	}
	
	
	
	
}
