package ER2FSM.utils;

import java.util.List;

public class Utils {

	
	public static boolean equalsListsUnordered(List<?> list1, List<?> list2) {
		if (list1.size() != list2.size()) return false;
		
		return list1.containsAll(list2);
	}
}
