package ER2FSM.rules;

public class Event implements Cloneable {

	// The id of this event
	private String _signal;

	// Does the event contain a token (true) or not?
	//private Boolean _fired = false;
	public int _tokens = 0;
	
	
	public Event() {
		
	}
	
	public Event(String name) {
		_signal = name;
	}
	
	// Clone creator
	public Event(Event event) {
		_signal = event._signal;
		_tokens = event._tokens;
	}

	public synchronized void setFired() {
		this._tokens++;
	}

	public Boolean isFired() {
		return _tokens > 0;
	}

	public void setSignal(String signal) {
		this._signal = signal;
	}

	public String getSignal() {
		return _signal;
	}

	public void clear() {
		// Consumed?
		this._tokens--;		
	}

	public String toString() {
		return this.getSignal() + "-" + _tokens;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Event)) return false;
		
		Event otherEvent = (Event) other;
		
		return (this._signal == otherEvent._signal); 
		

//				((this._tokens == 0 && otherEvent._tokens == 0) || 
//						(this._tokens > 0 && otherEvent._tokens > 0));
	}
	
		
}
