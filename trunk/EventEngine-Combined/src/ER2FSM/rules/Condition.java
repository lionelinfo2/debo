package ER2FSM.rules;

public class Condition {

	
	public String _name;
	public String _originatorGateway;
	public boolean _not = false;
	
	public Condition(String name, String originator) {
		_name = name;
		_originatorGateway = originator;
	}
	
	public String toString() {
		String pre = "";
		if (_not) {
			pre = "Not";
		}
		
		return pre + " " + _name + "," + _originatorGateway;
	}
	
	public boolean equals(Object cond) {
		if (!(cond instanceof Condition)) return false;
		
		return this._name.equals(((Condition)cond)._name) 
			&& this._originatorGateway.equals(((Condition)cond)._originatorGateway)
			&& this._not == ((Condition)cond)._not;
		
	}

	public Condition createNegativeCondition() {
		Condition ncondition = new Condition(this._name,this._originatorGateway);
		ncondition._not = true;
		
		return ncondition;
	}		
}
