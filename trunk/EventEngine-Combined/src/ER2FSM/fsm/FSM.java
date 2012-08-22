package ER2FSM.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FSM {

	public Collection<State> _states = new ArrayList<State>();
	
	public void addState(State state) {
		if (_states.contains(state)) return;
		_states.add(state);
	}
	
	public void removeState(State state) {
		_states.remove(state);
	}
	
	public List<State> getLeavesNotEnd() {
		List<State> leaves = new ArrayList<State>();
		
		for (State s: _states) {
			if (s.getOutgoing().size() <= 0 && !s.endState) {
				leaves.add(s);
			}
		}
		
		return leaves;	
	}

	public Collection<State> getState() {
		return _states;
	}
	
	public State getStartState() {
		for (State s: getState()) {
			if (s.startState) return s;
		}
		
		return null;
	}
	
	
	
	
	
}
