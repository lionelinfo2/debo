package ER2FSM.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Conjunction implements Cloneable {
	
	// List of events that are in conjunction
	private Set<Event> _events = new HashSet<Event>();
	
	private List<Condition> _conditions = new LinkedList<Condition>(); 
		
	public Conjunction() {
		
	}
	
	public Conjunction(Conjunction conjunction) {
		for (Event event: conjunction.getEvents()) {
			_events.add(new Event(event));
		}
		
		this._conditions.addAll(conjunction.get_conditions());
	}


	public boolean evaluate() {
		if (this.getEvents().size()==0) return false;
		
		boolean result = true;
		
		for(Event event: this.getEvents()) {
			result = result && event.isFired(); 
		}
		
		return result;
	}

	
	// Set an event with a specific Id to true
	public void setEventFired(String eventId) {
		for (Event e: getEvents()) {
			if (e.getSignal().equals(eventId)) {
				// Event contains a token
				if (!(this.get_conditions().size()>0 && e.isFired())) {
					// Only fire 'again' if this doesn't contain a condition
					e.setFired();
				}
			}
		}		 
	}
	
	public void setEventFiredAgain(String eventId) {
		for (Event e: getEvents()) {
			if (e.getSignal().equals(eventId) && e.isFired()) {
				// Event contains a token
				e.setFired();
			}
		}		 		
	}

	public void addEvent(Event event) {
		this.getEvents().add(event);
	}

	public Set<Event> getEvents() {
		return _events;
	}

	private void setEvents(Set<Event> _events) {
		this._events = _events;
	}


	public void clearAll() {
		for(Event element: this.getEvents()) {
			element.clear();
		}
	}


	public void addCondition(String expression) {
		// split condition to extract the XOR gateway identifier
		String[] condGateway = expression.split(",");
		
		Condition cond = new Condition(condGateway[0],condGateway[1]);
		
		this.get_conditions().add(cond);
	}
	
	public List<Condition> get_conditions() {
		return _conditions;
	}
	
	
	public String toString() {
		String text = "";

		// Events
		Iterator<Event> it = this.getEvents().iterator();

		if(it.hasNext()) {
			text = it.next().toString();
		}

		for (;it.hasNext(); ) {
			text = text + " AND " + it.next().toString();
		}
		
		// Conditions
		Iterator<Condition> it2 = this.get_conditions().iterator();

		for (;it2.hasNext(); ) {
			text = text + " AND Cond(" + it2.next() + ")";
						
		}

		return "(" + text + ")";
	}

	public boolean evaluateOn(Collection<String> trace) {
		// Each event included in the conjunction should be present in the event trace
		if (this.getEvents().size()==0) return false;
		return trace.containsAll(this.getEventSignals());
	}

	public Set<String> getEventSignals() {
		Set<String> eventNames = new HashSet<String>();
		
		for (Event e: getEvents()) {
			eventNames.add(e.getSignal());
		}
		
		return eventNames;
	}

	public boolean isSubSetOf(Conjunction c) {
		// true if each event in this conjunction is also included in 'conj'
		return c.getEventSignals().containsAll(this.getEventSignals());
	}

	public boolean isConditionsSuperSetOf(Collection<String> conditions) {
		// true if each condition in the provided set is also
		// included as a condition in this conjunction
		return this.get_conditions().containsAll(conditions);
	}
	
	public boolean contains(Event e) {
		for (Event ev: this.getEvents()) {
			if (ev.equals(e)) return true;
		}
		
		return false;
	}

	public Event getEvent(Event e) {
		for (Event ev: this.getEvents()) {
			if (ev.equals(e)) return ev;
		}
		
		return null;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Conjunction)) return false;
		
		Conjunction otherConj = (Conjunction) other;
		
		if (!this.get_conditions().equals(otherConj.get_conditions())) return false;
		
		if (!this.getEventSignals().equals(otherConj.getEventSignals())) return false;
		
		return true;
	}

	public void setMax(Conjunction otherConj) {
		for (Event thisEvent: this.getEvents()) {
			Event otherEvent = otherConj.getEvent(thisEvent);
			
			if (thisEvent._tokens < otherEvent._tokens) {
				thisEvent._tokens = otherEvent._tokens;
			}
		}
		
	}

	
}
