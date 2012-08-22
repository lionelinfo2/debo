package ER2FSM.fsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ER2FSM.rules.Condition;
import ER2FSM.rules.Conjunction;
import ER2FSM.rules.DNFEventRule;

public class State {

	private List<Transition> _incoming = new ArrayList<Transition>();
	private List<Transition> _outgoing = new ArrayList<Transition>();
	
	// Collection of event indicating the completed tasks on this moment in the process state
	private LinkedList<String> _trace = new LinkedList<String>();
	
	// Keep a set of event rules
	private List<DNFEventRule> _rules = new ArrayList<DNFEventRule>();
	
	// Collection of conditional expressions which are evaluated in this state
	// Each condition is linked to a trace, which indicates the time when this condition
	// is evaluated
	// The condition is reset if a trace T exists where T\trace(condition) evaluates on ER(condition) 
	public HashMap<Conjunction,LinkedList<String>> _conditionsEvaluated = new LinkedHashMap<Conjunction, LinkedList<String>>();
	
	public boolean endState;
	public boolean startState;
	
	
	/**
	 * @pre Transition has to contain this.state as target
	 * @param trans
	 */
	public void addIncoming(Transition trans) {
		if (trans.getTarget() != this) return;
		
		_incoming.add(trans);
		
	}
	
	public List<DNFEventRule> getRules() {
		return _rules;
	}
	
	public HashMap<Conjunction, LinkedList<String>> getAllValidatedConditions() {
		return _conditionsEvaluated;
	}
	
	public void addAllConditions(HashMap<Conjunction,LinkedList<String>> validatedConditions) {
		_conditionsEvaluated.putAll(validatedConditions);
	}
	
	/**
	 * Copy event rules and clear a specific conjunction
	 * This is needed to trigger the conjunction i.e. every event is disabled again
	 * Required to know which conjunction is triggered in this new state!
	 * 
	 * @param rules
	 * @param toClear
	 */
	public void setEventRulesCopyAndClear(List<DNFEventRule> rules, Conjunction toClear) {
		for (DNFEventRule rule: rules) {
			// Copy the rule into this state
			DNFEventRule copyRule = new DNFEventRule();
			for (Conjunction conj: rule.getConjunctions()) {
				Conjunction copyConjunction = new Conjunction(conj);
				if (conj == toClear) {
					copyConjunction.clearAll();
				}
				copyRule.addConjunction(copyConjunction);
			}
			
			copyRule.fragment = rule.fragment;			
			_rules.add(copyRule);
		}
	}
	
	public void clear(Conjunction enabledConjunction) {
		for (DNFEventRule rule: this._rules) {
			for (Conjunction conj: rule.getConjunctions()) {
				if (conj.equals(enabledConjunction)) {
					conj.clearAll();
				}
			}
		}
	}
	
	public void setEventRulesCopy(List<DNFEventRule> rules) {
		for (DNFEventRule rule: rules) {
			_rules.add(new DNFEventRule(rule));
		}
	}
	
	public void setEventFired(String action) {
		for (DNFEventRule rule : _rules) {
			rule.setEventFired(action);
		}
	}
		
	/**
	 * @pre Transition has to contain this.state as source
	 * @param trans
	 */
	public void addOutgoing(Transition trans) {
		if (trans.getSource() != this) return;
		
		_outgoing.add(trans);
	}
	
	public LinkedList<String> getTrace() {
		return _trace;
	}
	
	public List<Transition> getOutgoing() {
		return _outgoing;
	}
	
	public Collection<Transition> getIncoming() {
		return _incoming;
	}
	
//	public Collection<String> getTotalState() {
//		Collection<String> completeState = new ArrayList<String>();
//		
//		completeState.addAll(_events);
//		
//		return completeState;
//	}
	
	public void addToTrace(String event) {
		if (!event.equals("tau")) getTrace().add(event);
	}
	
	public void addAllEvents(List<String> trace) {
		getTrace().addAll(trace);
	}
	
	public String toString() {
		String toString = ""; 
		
		for (String e: getTrace()) {
			toString = toString + e + " ";
		}
		
		if (this._conditionsEvaluated.size()>0) {
			toString = toString + "\n";
			for (Conjunction c: this._conditionsEvaluated.keySet()) {
				toString = toString + c.get_conditions() + " ";
			}
		}
		
//		toString = toString + "\nRules:\n";
//		
//		for (DNFEventRule er: this.getRules()) {
//			toString = toString + er.toString() + "\n";
//		}
//		
		return toString;
	}

	public Conjunction getConditionalConjunctionValidating(Conjunction conj) {
		for (Conjunction c: _conditionsEvaluated.keySet()) {
			if (conj.isSubSetOf(c) || c.isSubSetOf(conj)) return c;
		}
		
		return null;		
	}

	public void addValidatedCondition(Conjunction conj,LinkedList<String> trace) {
		_conditionsEvaluated.put(conj,trace);
	}

	public Collection<String> getOutgoingActions() {
		LinkedList<String> outgoingActions = new LinkedList<String>();
		
		for (Transition t: getOutgoing()) {
			outgoingActions.add(t.getAction());
		}
		
		return outgoingActions;
	}

	public boolean isLastEventDuplicate() {
		int size = getTrace().size();
					
		return getTrace().get(size-1).equals(getTrace().get(size-2));
	}

	/**
	 * Create a set of the trace containing no duplicates and not ordered
	 * @return
	 */
	public Set<String> getTraceSet() {
		return new HashSet<String>(this.getTrace());
	}

	
	/**
	 * Two states are equal is their event rules state equals AND
	 * if their trace sets equal AND
	 * Conditions validated on this state should be equal
	 * 
	 * Assumption, the two state contain the same amount of rules
	 * 
	 * @param otherState
	 * @return
	 */
	public boolean equals(State otherState) {
		if (!this.getTraceSet().equals(otherState.getTraceSet())) return false;
		
		// Traces are equal
		for (DNFEventRule rule: this.getRules()) {
			if (!rule.equals(otherState.getRule(rule.fragment))) return false;
		}
		
//		if (!this.getAllValidatedConditions().keySet().equals(otherState.getAllValidatedConditions().keySet())) return false;

		return true;
	}

	private DNFEventRule getRule(String name) {
		for (DNFEventRule rule: this.getRules()) {
			if (rule.fragment.equals(name)) return rule;
		}
		
		return null;
	}

	/**
	 * Re-evaluate all conditions on the state
	 * If a conjunction is true on the remainder state -> the condition is removed from the stack
	 */
	public void resetCondition() {
		HashMap<Conjunction,LinkedList<String>> tempList = new HashMap<Conjunction, LinkedList<String>>();
		tempList.putAll(_conditionsEvaluated);
		
		for (Conjunction condition: _conditionsEvaluated.keySet()) {
			List<String> subList = getTrace().subList(_conditionsEvaluated.get(condition).size(), getTrace().size());
			if (condition.evaluateOn(subList)) {
				tempList.remove(condition);
			}
		}
		
		_conditionsEvaluated.clear();
		_conditionsEvaluated.putAll(tempList);
	}

	public void setMax(State otherState) {
		// Set the event tokens as the max between the two states
		// Assumption: rules are always in correct order
		for (int i=0;i<this.getRules().size();i++) {
			DNFEventRule thisRule = this.getRules().get(i); 
			DNFEventRule otherRule = otherState.getRules().get(i);
			
			thisRule.setMax(otherRule);
		}
		
	}

	public LinkedList<Condition> getConditionsWithOrigin(
			String _originatorGateway) {
		LinkedList<Condition> conditions = new LinkedList<Condition>();
		
		for (Conjunction conj : this.getAllValidatedConditions().keySet()) {
			for (Condition c: conj.get_conditions()) {
				if (c._originatorGateway.equals(_originatorGateway)) {
					conditions.add(c);
				}
			}
		}
		
		return conditions;
	}

	
	

	
	
	
	
	
	
}
