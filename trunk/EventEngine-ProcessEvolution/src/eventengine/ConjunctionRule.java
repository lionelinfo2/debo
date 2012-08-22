package eventengine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.bpmn2.Expression;

import eventengine.Event.Fired;

public class ConjunctionRule implements Cloneable {
	
	private org.eclipse.bpmn2.Event _bpmn2Event;
	
	// List of events that are in conjunction
	private List<Event> _events = new ArrayList<Event>();
	
	private Expression _bpmn2Expression;
	private boolean _expression = false;
	private int _regionStart;
	private int _regionEnd;
	private int _condition = -1;
	
	public ConjunctionRule() {
		
	}
	
	public ConjunctionRule(ConjunctionRule conjunction) {
		for (Event event: conjunction.getEvents()) {
			_events.add(new Event(event));
		}
		
		this.setBpmn2Expression(conjunction.getBpmn2Expression());
		if (conjunction._expression) {
			this.setBpmn2Expression(conjunction.getCondition());
		}
	}


	public boolean evaluate(int condition) {
		boolean result = true;
		
		for(Event event: this.getEvents()) {
			result = result && event.isFired(); 
		}
		
		if (_expression) {
			if (condition != -1) _condition = condition;
			
			// Conditional expression included
			// evaluate the condition on the region provided
			
			Debug.debug("+++Condition Evaluated: " + _condition + " : " + 
					getRegionStart() + "--" + getRegionEnd() + " : " + ((_condition >= getRegionStart()) && (_condition <= getRegionEnd())));
			
			result = result && ((_condition >= getRegionStart()) && (_condition <= getRegionEnd()));
		}
		
		return result;
	}

	
	// Set an event with a specific Id to true
	public void setEventFired(String eventId) {
		boolean found = false;
		
		for (Iterator<Event> it = this.getEvents().iterator(); it.hasNext() && !found; ) {
			Event current = it.next();
			if (current.getSignal().equals(eventId)) {
				// Event contains a token
				current.setFired(Fired.TOKEN);
				found=true;
			}
		}		 
	}

	public void addEvent(Event event) {
		this.getEvents().add(event);
	}

	public List<Event> getEvents() {
		return _events;
	}

	private void setEvents(List<Event> _events) {
		this._events = _events;
	}


	public void clearAll() {
		for(Event element: this.getEvents()) {
			element.clear();
		}
		
		_condition = -1;
		
	}


	public void setBpmn2Event(org.eclipse.bpmn2.Event _bpmn2Event) {
		this._bpmn2Event = _bpmn2Event;
	}


	public org.eclipse.bpmn2.Event getBpmn2Event() {
		return _bpmn2Event;
	}
	
	public void setBpmn2Expression(Expression expression) {
		if (expression == null) return; 
		
		_bpmn2Expression = expression;
		
		// Only one documentation object containing the probability
		String expr = expression.getDocumentation().get(0).getText();
		setBpmn2Expression(expr);		
	}
	
	public void setBpmn2Expression(String expression) {
		String[] region = expression.split("--");

		_regionStart = Integer.parseInt(region[0]);
		_regionEnd = Integer.parseInt(region[1]);
		_expression = true;
	}
	
	public String getCondition() {
		return getRegionStart() + "--" + getRegionEnd();
	}
	
	public int getRegionStart() {
		return _regionStart;
	}
	
	public int getRegionEnd(){
		return _regionEnd;
	}
	
	public Expression getBpmn2Expression() {
		return _bpmn2Expression;
	}

	public String toHTML() {
		String rep = "";
		
		for (Event event: getEvents()) {
			if (rep.equals("")) {
				rep = rep + event.toHTML();
			} else {
				rep = rep + " AND " + event.toHTML();
			}
		}
		
		if (_expression) {
			rep = rep + " ++ Cond(" + getRegionStart() + " - " + _condition + " - " + getRegionEnd() + ")";
		}
		
		return rep;
	}
	
	public String toString() {
		String rep = "";
		
		for (Event event: getEvents()) {
			if (rep.equals("")) {
				rep = rep + event.toString();
			} else {
				rep = rep + " AND " + event.toString();
			}
		}
		
		if (_expression) {
			rep = rep + " ++ Cond(" + getRegionStart() + " -- " + getRegionEnd() + ")";
		}
		
		return rep;
	}
	
	
}
