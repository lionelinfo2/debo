package ER2FSM;

import java.util.HashMap;

import ER2FSM.rules.Conjunction;

public class Test {
	
	
	public static void main(String[] args) {
		HashMap<Conjunction,String> testy = new HashMap<Conjunction, String>();
		
		Conjunction conj = new Conjunction();
		testy.put(conj, "tralala");
	}
}
