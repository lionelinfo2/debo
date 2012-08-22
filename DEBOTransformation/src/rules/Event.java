package rules;

import org.eclipse.bpmn2.Signal;

public class Event {

	public static int idCounter = 0;
	
	private int _id;
	private Signal _signal;
	
	public Event(int id, Signal s) {
		set_id(id);
		set_signal(s);
	}
	
	public Event(Signal s) {
		set_id(Event.idCounter);
		Event.idCounter++;
		
		set_signal(s);
	}
	
	public Event() {}
	
	public int get_id() {
		return _id;
	}
	
	public void set_id(int _id) {
		this._id = _id;
	}
	
	public Signal get_signal() {
		return _signal;
	}
	
	public void set_signal(Signal _signal) {
		this._signal = _signal;
	}
	
	public String toString() {
//		return "<" + this.get_id() + "," + this.get_signal().getName() + ">";
		return this.get_signal().getName();
	}
	
	public String toShortString() {
		return this.get_signal().getName();
	}
	
	public boolean equals(Event ev) {
		return ev.get_signal().getId().equals(this.get_signal().getId());
	}
}
