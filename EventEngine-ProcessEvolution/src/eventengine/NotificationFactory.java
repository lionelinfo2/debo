package eventengine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.bpmn2.SignalEventDefinition;

import siena.Filter;
import siena.Op;

public class NotificationFactory {
	
	public static List<Filter> getFilters(DNFEventRule eventRule) {
		return getFilters(eventRule.getAllEvents());
	}
	
	public static List<Filter> getFilters(DNFEventRule eventRule, Set<Integer> PIIDs) {
		return getFilters(eventRule.getAllEvents(), PIIDs);
	}
	
	public static List<Filter> getFilters(List<Event> events, Set<Integer> PIIDs) {
		ArrayList<Filter> filters = new ArrayList<Filter>();

		for (Event event: events) {
			for (int piid: PIIDs) {
				Filter f = new Filter();
				f.addConstraint("id", event.getSignal());
				f.addConstraint("processInstanceId", Integer.toString(piid));
				
				filters.add(f);
			}
		}

		return filters;
	}
	
	public static List<Filter> getFilters(List<Event> events) {
		ArrayList<Filter> filters = new ArrayList<Filter>();

		for (Event event: events) {
			Filter f = new Filter();
			f.addConstraint("id", event.getSignal());

			filters.add(f);
		}

		return filters;
	}
	
	public static List<Filter> getFiltersExcludingPIIDs(DNFEventRule rule, Set<Integer> PIIDs) {
		return getFiltersExcludingPIIDs(rule.getAllEvents(), PIIDs);
	}
	
	public static List<Filter> getFiltersExcludingPIIDs(List<Event> events, Set<Integer> PIIDs) {
		ArrayList<Filter> filters = new ArrayList<Filter>();

		for (Event event: events) {
				Filter f = new Filter();
				f.addConstraint("id", event.getSignal());
				
				for (int piid: PIIDs) {
					f.addConstraint("processInstanceId", Op.NE, Integer.toString(piid));
				}
				
				filters.add(f);	
		}

		return filters;
	}
}
