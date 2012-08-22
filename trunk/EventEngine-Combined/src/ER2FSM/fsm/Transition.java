package ER2FSM.fsm;

public class Transition {

	private State _source;
	private State _target;
	private String _action;
	
	public Transition(State source, State target, String action) {
		_source = source;
		_source.addOutgoing(this);
		
		_target = target;
		_target.addIncoming(this);
		
		_action = action;
	}
	
	public State getTarget() {
		return _target;
	}
	
	public State getSource() {
		return _source;
	}
	
	public String getAction() {
		return _action;
	}
	
	public void setTarget(State source) {
		_source = source;
	}
	
	public void setSource(State target) {
		_target = target;
	}
	
	
	
	
}
