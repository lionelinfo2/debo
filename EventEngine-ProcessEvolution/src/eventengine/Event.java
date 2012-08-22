package eventengine;

public class Event implements Cloneable {

	public enum Fired {
		// Contains a token
		TOKEN,
		// Doesn't contain a token 
		EMPTY,
		// A token is consumed (previously Fired=TRUE)
		CONSUMED
	}
	
	private org.eclipse.bpmn2.EventDefinition _bpmn2EventDefinition;
	
	// The id of this event
	private String _signal;

	// The process instance id for this event
	private String _processInstanceId;
	
	// Does the event contain a token (true) or not?
	//private Boolean _fired = false;
	private Fired _fired = Fired.EMPTY;
	
	
	public Event() {
		
	}
	
	// Clone creator
	public Event(Event event) {
		_bpmn2EventDefinition = event._bpmn2EventDefinition;
		_signal = event._signal;
		_processInstanceId = event._processInstanceId;
		_fired = event._fired;
	}

	public synchronized void setFired(Fired fired) {
		this._fired = fired;
	}

	public Boolean isFired() {
		return _fired.equals(Fired.TOKEN);
	}

	public void setProcessInstanceId(String _processInstanceId) {
		this._processInstanceId = _processInstanceId;
	}

	public String getProcessInstanceId() {
		return _processInstanceId;
	}

	public void setSignal(String signal) {
		this._signal = signal;
	}

	public String getSignal() {
		return _signal;
	}

	public void clear() {
		// Consumed?
		this.setFired(Fired.CONSUMED);		
	}

	public void setBpmn2Event(org.eclipse.bpmn2.EventDefinition bpmn2EventDefinition) {
		this._bpmn2EventDefinition = bpmn2EventDefinition;
	}

	public org.eclipse.bpmn2.EventDefinition getBpmn2Event() {
		return _bpmn2EventDefinition;
	}

	public String toHTML() {
		String rep = "";
		
		if (isFired()) {
			rep = "<font color=green>"; 
		} else if (_fired.equals(Fired.EMPTY)) {
			rep = "<font color=red>";
		} else {
			// Consumed
			rep = "<font color=blue>";
		}
		
		return rep + this.getSignal() + "</font>";
	}
	
	public String toString() {
		return this.getSignal();
	}
	
		
}
