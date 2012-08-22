package ER2FSM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ui.FSMViewer;
import ER2FSM.fsm.FSM;
import ER2FSM.fsm.State;
import ER2FSM.fsm.Transition;
import ER2FSM.rules.Condition;
import ER2FSM.rules.Conjunction;
import ER2FSM.rules.DNFEventRule;
import ER2FSM.rules.Event;
import ER2FSM.utils.Utils;

public class ER2FSM {

	private List<DNFEventRule> _fragmentERcollection = new ArrayList<DNFEventRule>();
	private FSM fsm = new FSM();

	public ER2FSM(HashMap<String, String> fragmentERcollection) {
		for (String key : fragmentERcollection.keySet()) {
			DNFEventRule er = parseER(fragmentERcollection.get(key));
			er.fragment = key;
			_fragmentERcollection.add(er);
		}
	}

	// public FSM getFSMfromRules(HashMap<String,String> fragmentERcollection) {
	// HashMap<String, DNFEventRule> tempFragmentERcollection = new
	// HashMap<String, DNFEventRule>();
	//
	// for (String key: fragmentERcollection.keySet()) {
	// DNFEventRule er = parseER(fragmentERcollection.get(key));
	// tempFragmentERcollection.put(key, er);
	// }
	//
	// return getFSMfromDNFRules(tempFragmentERcollection);
	// }

	public FSM getFSM() {
		if (_fragmentERcollection.size() == 0)
			return null;

		// Init
		// create start state
		State startState = new State();
		startState.startState = true;
		fsm.addState(startState);
		startState.setEventRulesCopy(_fragmentERcollection);
		startState.setEventFired("StartProcess");
		startState.addToTrace("StartProcess");

		List<String> visitedFragments = new ArrayList<String>();

		// Add states
		List<State> leaves = fsm.getLeavesNotEnd();

		while (leaves.size() != 0) {

			// For each leave
			for (State s : leaves) {
				// loop over each rule
				// if rule fits on the state, add a transition + a new state

				HashMap<Conjunction, String> validTransitions = this
						.getAllSucceedingTransitions(s);

				// add states for all valid transitions
				for (Conjunction enabledConjunction : validTransitions.keySet()) {
					visitedFragments.add(validTransitions
							.get(enabledConjunction));

					// Create and link a new (or similar existing) state
					State newState = this.retrieveState(s, enabledConjunction,
							validTransitions);
					Transition t = new Transition(s, newState,
							validTransitions.get(enabledConjunction));
					fsm.addState(newState);
				}

				// if no event rule fits, then the leave is an endstate
				if (validTransitions.size() == 0) {
					s.endState = true;
				}
			}

			// Search for new leaves
			leaves = fsm.getLeavesNotEnd();
		}

		// Add non visited fragments
		for (DNFEventRule fragment : _fragmentERcollection) {
			if (!visitedFragments.contains(fragment.fragment)) {
				// there are non visited fragments
				State s = new State();
				s.addToTrace(fragment.fragment);
				fsm.addState(s);
			}
		}

		return fsm;
	}

	/**
	 * 
	 * @param s
	 * @return a HashMap linking the name of the validated fragment and the
	 *         conjunction that evaluated to true in the event rule
	 */
	private LinkedHashMap<Conjunction, String> getAllSucceedingTransitions(
			State s) {
		LinkedHashMap<Conjunction, String> validTransitions = new LinkedHashMap<Conjunction, String>();

		for (DNFEventRule rule : s.getRules()) {
			// Retrieve all conjunctions in the rule that validate (all events
			// enabled)
			List<Conjunction> validConjunctions = rule
					.getAllValidConjunctions();

			for (Conjunction conj : validConjunctions) {
				// The event rule validates on the trace
				// check if there are conditions present
				if (conj.get_conditions().size() == 0) {
					validTransitions.put(conj, rule.fragment);
				} else {
					// Conditions are present in the conjunction
					// The conjunction only evaluates if
					// 1. No conditions are already validated on the state for
					// this conjunction
					// 2. Their is a condition already validated for this
					// conjunction
					// AND this condition is a subset of the validated condition

					// (1) Search for a conjunction included the state (A1)
					// where A1 \subset conj

					// For all conditions in the conjunction
					// each condition needs to validate
					boolean condValidated = true;

					for (Condition c : conj.get_conditions()) {
						// get conditions on the state which originate from the
						// same XOR split
						LinkedList<Condition> condOnState = s
								.getConditionsWithOrigin(c._originatorGateway);

						if (condOnState.size() != 0 && !condOnState.contains(c))
							condValidated = false;
					}

					if (condValidated) {
						validTransitions.put(conj, rule.fragment);
					}
				}
			}
		}

		// // Add negative data condition states
		// // retrieve all conditions that are being added
		// HashMap<String,LinkedList<Condition>> addedConditions = new
		// HashMap<String, LinkedList<Condition>>();
		// for (Conjunction validRule : validTransitions.keySet()) {
		// for (Condition c : validRule.get_conditions()) {
		// LinkedList<Condition> condList =
		// addedConditions.get(c._originatorGateway);
		// if (condList != null) {
		// condList.add(c);
		// } else{
		// condList = new LinkedList<Condition>();
		// condList.add(c);
		// addedConditions.put(c._originatorGateway, condList);
		// }
		// }
		// }
		//
		// // for all conditions originating from a unique gateway, create a
		// // transition with a negative condition
		// for (String originater: addedConditions.keySet()) {
		// if (addedConditions.get(originater).size() == 1) {
		// // An XOR-Split with only one outgoing sequence flow
		// // add another state with negative condition
		// Conjunction conjunction = new Conjunction();
		// Condition ncondition =
		// addedConditions.get(originater).getFirst().createNegativeCondition();
		// conjunction.get_conditions().add(ncondition);
		//
		// validTransitions.put(conjunction, "tau");
		// }
		// }

		return validTransitions;
		//
		// for (String fragment: _fragmentERcollection.keySet()) {
		// Conjunction conj =
		// evaluateRuleOnState(s,_fragmentERcollection.get(fragment),fragment);
		// if (conj != null) {
		// validTransitions.put(fragment,conj);
		// }
		// }

	}

	private State retrieveState(State currentState,
			Conjunction enabledConjunction,
			HashMap<Conjunction, String> validTransitions) {
		// idea : disable events from concurring XOR -> no need to keep
		// conditions?

		// Create a temporary state representing the new state if a new one has
		// to be
		// created
		State tempState = new State();
		tempState.addAllEvents(currentState.getTrace());
		tempState.addAllConditions(currentState.getAllValidatedConditions());
		tempState.setEventRulesCopyAndClear(currentState.getRules(),
				enabledConjunction);
		tempState.addToTrace(validTransitions.get(enabledConjunction));
		tempState.setEventFired(validTransitions.get(enabledConjunction));

		// Under unique task assumption, reset is only needed when fragment ==
		// last event on trace
		// Condition is not added if the last event in the trace is duplicated
		// -> i.e. a new completion event is triggered
		// Add condition to the newly created state
		if (enabledConjunction.get_conditions().size() > 0) {
			tempState.addValidatedCondition(
					new Conjunction(enabledConjunction),
					currentState.getTrace());
		}

		// A condition can be reset if it's event rule is triggered again (e.g.
		// new data value)
		tempState.resetCondition();

		// check is a similar state already exists in the upwards flow!
		// 2 states are similar if the trace set is similar AND
		// the event rule state is similar (e.g. enabled transitions)
		State s = this.findSimilarState(tempState);

		if (s != null) {
			// Update s to contain unbounded information
			// if (tempState.isLastEventDuplicate()) {
			// s.clear(enabledConjunction);
			// s.setEventFiredAgain(validTransitions.get(enabledConjunction));
			// }

			s.setMax(tempState);

			return s;
		}

		return tempState;
	}

	/**
	 * Find a similar state Similar if trace set equals (unordered, duplicates
	 * removed) AND event rule state equals AND condition state equals
	 */
	private State findSimilarState(State newState) {
		// for (State s: fsm.getState()) {
		// if (s.equals(newState)) return s;
		// }
		//
		// return null;

		//
		//
		
		// TODO compare event states!
		Set<String> enabledTransitions = new HashSet<String>(this
				.getAllSucceedingTransitions(newState).values());
		List<Conjunction> newStateConditions = new LinkedList<Conjunction>(
				newState.getAllValidatedConditions().keySet());

		for (State s : fsm.getState()) {
			boolean erStatesEqual = true;
			
			if (newState.getRules().size() != s.getRules().size()) {
				erStatesEqual = false;
			} else {
				for (DNFEventRule er : newState.getRules()) {
					if (!s.getRules().contains(er)) {
						erStatesEqual = false;
						break;
					}
				}
			}
			
			
//			Set<String> outGoing = new HashSet<String>(this
//					.getAllSucceedingTransitions(s).values());
			List<Conjunction> sConditions = new LinkedList<Conjunction>(s
					.getAllValidatedConditions().keySet());

			// Outgoing transitions are equal & trace is equal & contained
			// conditions are equal
//			if (outGoing.equals(enabledTransitions)
//					&& s.getTraceSet().equals(newState.getTraceSet())
//					&& Utils.equalsListsUnordered(newStateConditions,
//							sConditions))
			
			if (erStatesEqual
						&& s.getTraceSet().equals(newState.getTraceSet())
						&& Utils.equalsListsUnordered(newStateConditions,
								sConditions))
				return s;
		}

		return null;
	}

	/*********************************/
	/************ UTILS **************/
	/*********************************/

	public DNFEventRule parseER(String eventRule) {
		DNFEventRule newRule = new DNFEventRule();

		// Empty Event Rule
		if (eventRule.length() == 2)
			return newRule;

		// Retrieve all conjunctions
		String[] conjunctions = eventRule.substring(1, eventRule.length() - 1)
				.split(" OR ");

		for (String conjunction : conjunctions) {
			Conjunction newConj = new Conjunction();

			// Retrieve all events within one conjunction
			String[] events = conjunction
					.substring(1, conjunction.length() - 1).split(" AND ");

			for (String event : events) {
				if (event.substring(0, 5).equals("Cond(")) {
					// Condition found
					// Add it to the conjunction
					newConj.addCondition(event.substring(5, event.length() - 1));
				} else {
					newConj.addEvent(new Event(event.substring(6)));
				}
			}

			newRule.addConjunction(newConj);
		}

		return newRule;
	}

	public boolean deadlock(FSM fsm, String endFragment) {
		return this.deadlock(fsm.getStartState(), endFragment,
				new ArrayList<State>());
	}

	private boolean deadlock(State s, String endFragment,
			ArrayList<State> visited) {
		if (!findEndState(s, new ArrayList<State>(), endFragment))
			return true;

		visited.add(s);
		boolean deadlock = false;
		for (Transition outgoing : s.getOutgoing()) {
			if (!visited.contains(outgoing.getTarget()))
				deadlock = deadlock
						|| deadlock(outgoing.getTarget(), endFragment, visited);
		}

		return deadlock;
	}

	private boolean findEndState(State state, ArrayList<State> visited,
			String endFragment) {
		if (state.getTrace().contains(endFragment))
			return true;

		visited.add(state);
		for (Transition outgoing : state.getOutgoing()) {
			if (!visited.contains(outgoing.getTarget())) {
				if (this.findEndState(outgoing.getTarget(), visited,
						endFragment))
					return true;
			}
		}

		return false;
	}

	public boolean multipleEndExecution(FSM fsm, String endFragment) {

		for (State s : fsm.getState()) {
			if (s.getTrace().contains(endFragment)
					&& s.getOutgoingActions().contains(endFragment))
				return true;
		}

		return false;
	}

	public Set<Condition> deadlockWhenNegativeCondition(FSM fsm, String endFragment) {
		// Collect all end states
		LinkedList<State> endStates = new LinkedList<State>();
		
		for (State s: fsm.getState()) {
			if (s.getTrace().getLast().equals(endFragment)) {
				endStates.add(s);
			}
		}
		
		
		Set<Condition> deadlockConditions = new HashSet<Condition>();
		
		// Loop over endstates
		for (State s1: endStates) {
			// Loop over conditions
			for (Conjunction conj1: s1.getAllValidatedConditions().keySet()) {
				for (Condition c: conj1.get_conditions()) {
					// Loop again over endStates
					boolean conditionContainedInAll = true;
					for (State s2: endStates) {
						boolean foundTheCondition = false;
						for (Conjunction conj2: s2.getAllValidatedConditions().keySet()) {
							if (conj2.get_conditions().contains(c)) foundTheCondition = true;
						}
						
						conditionContainedInAll = conditionContainedInAll && foundTheCondition;		
					}
					
					if (conditionContainedInAll) deadlockConditions.add(c); 
				}
			}
		}
		
		return deadlockConditions;
	}

	public static void main(String args[]) {

		LinkedHashMap<String, String> fragmentERcollection = new LinkedHashMap<String, String>();

		/** Pizza Delivery **/
		 String ER1 = "[(SignalStartProcess)]";
		 String ER2 = "[(SignalCalculatePrice)]";
		 String ER3 =
		 "[(SignalCreateSideDish AND SignalBakePizza) OR (SignalStartProcess AND SignalBakePizza AND Cond(NoSideDish,1))]";
		 String ER4 = "[(SignalStartProcess AND Cond(SideDish,1))]";
		 String ER5 = "[(SignalStartProcess)]";
		  String ER6 = "[(SignalArrangePayment AND SignalPackageOrder)]";
//		 String ER6 = "[(SignalPackageOrder)]";
		
		 fragmentERcollection.put("CalculatePrice", ER1);
		 fragmentERcollection.put("ArrangePayment", ER2);
		 fragmentERcollection.put("PackageOrder", ER3);
		 fragmentERcollection.put("CreateSideDish", ER4);
		 fragmentERcollection.put("BakePizza", ER5);
		 fragmentERcollection.put("DeliverPizza", ER6);
		 fragmentERcollection.put("END","[(SignalDeliverPizza)]");

		/** Insurance Claim handling **/
//		fragmentERcollection.put("AcceptClaim","[(SignalStartProcess)]");
//		fragmentERcollection.put("PrepareClaim","[(SignalAcceptClaim)]");
//		fragmentERcollection.put("CalculateInsuranceSum","[(SignalPrepareClaim)]");
//		fragmentERcollection.put("CheckClientHistory","[(SignalAcceptClaim)]");
//		fragmentERcollection.put("AssessClaim","[(SignalCheckClientHistory) OR (SignalCalculateInsuranceSum)]");
//		fragmentERcollection.put("SchedulePayment","[(SignalAssessClaim AND Cond(accept,XOR1))]");
//		fragmentERcollection.put("InformClient","[(SignalAssessClaim AND Cond(reject,XOR1))]");
//		fragmentERcollection.put("END","[(SignalInformClient) OR (SignalSchedulePayment)]");

		/** XOR **/
		// String ER1 = "[(SignalStartProcess)]";
		// String ER2 = "[(SignalA AND Cond(0--30,1))]";
		// String ER3 = "[(SignalA AND Cond(31--70,1))]";
		// String ER4 = "[(SignalB) OR (SignalC)]";
		//
		// fragmentERcollection.put("A",ER1);
		// fragmentERcollection.put("B",ER2);
		// fragmentERcollection.put("C",ER3);
		// // fragmentERcollection.put("D",ER4);
		//
		// /** Change XOR join to AND join **/
		// fragmentERcollection.put("D","[(SignalB AND SignalC)]");

		/** AND **/
//		 String ER1 = "[(SignalStartProcess)]";
//		 String ER2 = "[(SignalA)]";
//		 String ER3 = "[(SignalA)]";
//		 String ER4 = "[(SignalB AND SignalC)]";
////		  String ER4 = "[(SignalB) OR (SignalC)]";
//		
//		 fragmentERcollection.put("A",ER1);
//		 fragmentERcollection.put("B",ER2);
//		 fragmentERcollection.put("C",ER3);
//		 fragmentERcollection.put("D",ER4);

		// fragmentERcollection.put("StartProcess","[(SignalD)]");

		/** Dead Fragment **/
		// String ER100 = "[(SignalDeadAction)]";
		// fragmentERcollection.put("DeadTask", ER100);

		/** SEQUENCE **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B","[(SignalA)]");
		// // fragmentERcollection.put("C","[(SignalB)]");
		// fragmentERcollection.put("C","[(SignalA)]");
		// fragmentERcollection.put("D","[(SignalC)]");
		// fragmentERcollection.put("END","[(SignalD)]");

		/** LOOP **/
		// String ER1 = "[(SignalStartProcess)]";
		// String ER2 = "[(SignalA) OR (SignalB AND Cond(loopTrue,1))]";
		// String ER3 = "[(SignalB AND Cond(loopFalse,1))]";
		//
		// fragmentERcollection.put("A",ER1);
		// fragmentERcollection.put("B",ER2);
		// fragmentERcollection.put("C",ER3);

		/** DOUBLE LOOP **/
		// String ER1 = "[(SignalStartProcess) OR (SignalB AND Cond(loopTrue,1))]";
		// String ER2 = "[(SignalA)]";
		// String ER3 = "[(SignalB AND Cond(loopFalse,1))]";
		//
		// fragmentERcollection.put("A",ER1);
		// fragmentERcollection.put("B",ER2);
		// fragmentERcollection.put("C",ER3);

		/** UNBOUNDED PROCESS **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B","[(SignalA) OR (SignalC AND Cond(loopTrue))]");
		// fragmentERcollection.put("C","[(SignalB)]");
		// fragmentERcollection.put("Z","[(SignalC AND Cond(loopFalse))]");
		// fragmentERcollection.put("W","[(SignalB)]");
		// fragmentERcollection.put("V","[(SignalW)]");
		// fragmentERcollection.put("END","[(SignalV AND SignalZ)]");

		/** UNBOUNDED PROCESS 2 **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B","[(SignalA) OR (SignalB AND Cond(loopTrue))]");
		// fragmentERcollection.put("Z","[(SignalB AND Cond(loopFalse))]");
		// fragmentERcollection.put("END","[(SignalZ AND SignalB)]");

		/** UNBOUNDED PROCESS 3 **/
		// fragmentERcollection.put("END","[(SignalZ AND SignalB)]");
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("C","[(SignalB)]");
		// fragmentERcollection.put("Z","[(SignalC AND Cond(loopFalse))]");
		// fragmentERcollection.put("B","[(SignalA) OR (SignalC AND Cond(loopTrue))]");

		/** UNBOUNDED PROCESS 4 **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B","[(SignalA) OR (SignalB)]");
		// fragmentERcollection.put("END","[(SignalB)]");

		/** DEADLOCK **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("t1","[(SignalA AND Cond(c1,1))]");
		// fragmentERcollection.put("t2","[(SignalA AND Cond(c2,1))]");
		// fragmentERcollection.put("t3","[(Signalt1 AND Signalt4)]");
		// fragmentERcollection.put("t4","[(SignalStartProcess AND Signalt2) OR (Signalt2 AND Signalt3)]");
		// fragmentERcollection.put("t5","[(Signalt4)]");
		// fragmentERcollection.put("End","[(Signalt5)]");

		/** MULTIPLE CONDITIONS **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B","[(SignalA AND Cond(1,1) AND Cond(2,2))]");
		// fragmentERcollection.put("C","[(SignalA AND Cond(1,1))]");
		// fragmentERcollection.put("D","[(SignalA AND Cond(3,1))]");

		/** CP 1 - Parallel Insert **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B", "[(SignalA)]");
		//
		// fragmentERcollection.put("New","[(SignalA)]");

		// Partially possible, next fragment does not wait for the completion of
		// New fragment

		/** CP 1 - Conditional Insert **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B", "[(SignalA)]");
		//
		// fragmentERcollection.put("New","[(SignalA AND Cond(T,1))]");

		// Partially possible, as B needs to change its event rule to start
		// after New, or immediatly

		/** CP 2 - Delete Process Fragment **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// // fragmentERcollection.put("B", "[(SignalA)]");
		// fragmentERcollection.put("C","[(SignalB)]");

		/** EXAMPLE IN PAPER **/
		// fragmentERcollection.put("ReceiveOrder","[(SignalStartProcess)]");
		// fragmentERcollection.put("BakePizza","[(SignalReceiveOrder) OR (SignalBakePizza AND Cond(AdditionalPizza))]");
		// fragmentERcollection.put("ArrangePayment","[(SignalReceiveOrder)]");
		// //
		// fragmentERcollection.put("DeliverPizza","[(SignalBakePizza AND SignalArrangePayment AND Cond(NoAdditionalPizza))]");
		// fragmentERcollection.put("DeliverPizza","[(SignalBakePizza AND Cond(NoAdditionalPizza))]");

		/** Newly added data condition **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B","[(SignalA AND Cond(x,1))]");
		// fragmentERcollection.put("C","[(SignalA AND Cond(y,1))]");
		// fragmentERcollection.put("D","[(SignalB) OR (SignalC)]");
		// fragmentERcollection.put("X","[(SignalA AND Cond(z,2))]");

		/** Multiple XOR gateways **/
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B","[(SignalA AND Cond(x,1))]");
		// fragmentERcollection.put("C","[(SignalA AND Cond(y,1))]");
		// fragmentERcollection.put("D","[(SignalA AND Cond(z,2))]");
		// fragmentERcollection.put("E","[(SignalA AND Cond(w,2))]");

		/** Reachability Test Model **/
//		fragmentERcollection.put("Start", "[(SignalStartProcess)]");
//		fragmentERcollection.put("A", "[(SignalStart)]");
//		fragmentERcollection.put("B", "[(SignalStart)]");
//		fragmentERcollection.put("C", "[(SignalA AND SignalB)]");
//		fragmentERcollection.put("D", "[(SignalC)]");
////		 fragmentERcollection.put("D","[(SignalC AND Cond(cond3,2))]");
//		fragmentERcollection.put("E", "[(SignalD AND Cond(cond1,1))]");
//		fragmentERcollection.put("F", "[(SignalD AND Cond(cond2,1))]");
//		fragmentERcollection.put("G", "[(SignalE) OR (SignalF)]");
//		fragmentERcollection.put("END", "[(SignalG)]");

		/** MISC **/
		// fragmentERcollection.put("Start","[(SignalStartProcess)]");

		//
		// fragmentERcollection.put("D","[(SignalC) OR (SignalB) OR (SignalY)]");
		//

		// fragmentERcollection.put("New","[(SignalB AND Cond(T))]");
		// fragmentERcollection.put("X","[(SignalD)]");
		// // fragmentERcollection.put("END","[(SignalX)]");
		//
		// // fragmentERcollection.put("New","[(SignalB) OR (SignalC)]");
		//
		// fragmentERcollection.put("A","[(SignalStartProcess)]");
		// fragmentERcollection.put("B","[(SignalA) OR (SignalC AND Cond(T))]");
		// fragmentERcollection.put("C","[(SignalB)]");
		// fragmentERcollection.put("Y","[(SignalA AND Cond(3))]");
		//
		// fragmentERcollection.put("D","[(SignalC)]");

		ER2FSM er2FSM = new ER2FSM(fragmentERcollection);

		FSM fsm = er2FSM.getFSM();
		System.out.println("Deadlock? : " + er2FSM.deadlock(fsm, "END"));
		System.out.println("Mutliple end executions? : "
				+ er2FSM.multipleEndExecution(fsm, "END"));
		System.out.println("Deadlock when a condition does not validate? : " +
				er2FSM.deadlockWhenNegativeCondition(fsm, "END"));
		FSMViewer viewer = new FSMViewer(fsm);

	}
}
