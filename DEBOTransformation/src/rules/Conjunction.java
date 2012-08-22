package rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.impl.Bpmn2FactoryImpl;

public class Conjunction {
	
	private Collection<Expression> _conditions = new ArrayList<Expression>(); 
	private Collection<Event> _events = new ArrayList<Event>();

	
	public Conjunction(Event e) {
		add_event(e);
	}
	
	public Conjunction(Expression exp) {
		add_condition(exp);
	}
	
	public Conjunction() {
		
	}
	
	public Collection<Expression> get_conditions() {
		return _conditions;
	}
	
	public void set_conditions(Collection<Expression> _conditions) {
		this._conditions = _conditions;
	}
	
	public Collection<Event> get_events() {
		return _events;
	}
	
	public void set_events(Collection<Event> _events) {
		this._events = _events;
	}
	
	public void add_event(Event event) {
		this.get_events().add(event);
	}
	
	public void add_condition(Expression exp) {
		this.get_conditions().add(exp);
	}
	
	public void setBpmn2Expression(Expression expression) {
		if (expression == null) return; 
		
		this.add_condition(expression);
	}
	
	public void setBpmn2Expression(String expression) {
		Expression expr = Bpmn2FactoryImpl.eINSTANCE.createExpression();
		Documentation doc = Bpmn2FactoryImpl.eINSTANCE.createDocumentation();
		doc.setText(expression);
		expr.getDocumentation().add(doc);
		
		setBpmn2Expression(expr);
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
		Iterator<Event> it = this.get_events().iterator();

		if(it.hasNext()) {
			text = it.next().toString();
		}

		for (;it.hasNext(); ) {
			text = text + " AND " + it.next().toString();
		}
		
		// Conditions
		Iterator<Expression> it2 = this.get_conditions().iterator();

		for (;it2.hasNext(); ) {
			for (Documentation doc: it2.next().getDocumentation()) {
				text = text + " AND Cond(" + doc.getText() + ")";
			}			
		}

		return "(" + text + ")";
	}
	
	public String toShortString() {
		String text = "";

		// Events
		Iterator<Event> it = this.get_events().iterator();

		if(it.hasNext()) {
			text = it.next().toShortString();
		}

		for (;it.hasNext(); ) {
			text = text + "-AND-" + it.next().toShortString();
		}
		
		// Conditions
		Iterator<Expression> it2 = this.get_conditions().iterator();

		for (;it.hasNext(); ) {
			text = text + "-AND-" + it.next().toString();
		}

		return text;
	}

	public boolean contains(Event e) {
		return this.get_events().contains(e);
	}
	
	public boolean contains(Expression cond) {
		return false;
	}
	
	/**
	 * Return the event included in this rule similar to the event ev
	 * 
	 * @param ev
	 * @return
	 */
	public Event retrieveSimilar(Event ev, Set<Event> visited) {
		for (Event event : get_events()) {
			if (event.equals(ev) && !visited.contains(event)) return event;
		}
		
		return null;		
	}
	
	public boolean containsCondition(String condition) {
		for (Expression exp: this.get_conditions()) {
			for (Documentation doc: exp.getDocumentation()) {
				if (doc.getText().equals(condition)) return true;
			}
		}
		
		return false;
	}
	
	public boolean equals(Conjunction conj) {
		if (this.get_events().size() != conj.get_events().size() ||
				this.get_conditions().size() != conj.get_conditions().size()) 
			return false;
		
		Set<Event> visited = new HashSet<Event>();
		
		for (Event e: this.get_events()) {
			// Each event has to be in other rule
			Event sim = conj.retrieveSimilar(e, visited);
			if (sim == null) return false;
					
			visited.add(sim);
		}
		
		// TODO compare conditions
		for (Expression exp: this.get_conditions()) {
			for (Documentation doc : exp.getDocumentation()) {
				if(!conj.containsCondition(doc.getText())) return false;
			}
		}
		
		return true;
	}
	
	public boolean evaluateOn(Collection<String> events) {
		boolean validated = true;
		// Each event included in the conjunction should be present in the event trace
		for (Event e: get_events()) {
			// Substring because each event is named: "SignalX"
			if (!events.contains(e.get_signal().getName().substring(6))) validated = false;
		}
		
		return validated;
	}
	
	
	
	
}
