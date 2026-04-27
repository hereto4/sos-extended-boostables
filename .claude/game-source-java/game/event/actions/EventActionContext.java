package game.event.actions;

import game.event.actions.EventAction.CInt;
import game.event.engine.EContext;
import game.event.engine.Event;
import snake2d.Errors;
import snake2d.util.sets.LIST;

public class EventActionContext {

	private EventActionContext() {
		
	}
	
	public static int[] makeData(Event a, int[] old) {
		
		int ii = make(a, 0);
		
		if (old == null || old.length < ii) {
			old = new int[ii+16];
		}
		return old;
	}
	
	private static int make(Event event, int ii) {
		
		if (ii < 0) {
			throw new Errors.DataError("Something is wrong with event " + event.key + ". Either the event chain is too long, or the event has a cyclic behaviour.");
		}
		
		for (int ai = 0; ai < event.actions().size(); ai++) {
			EventAction a = event.actions().get(ai);
			for (CInt i : a.ints) {
				i.di = ii++;
			}
		}
		
		for (int ai = 0; ai < event.actions().size(); ai++) {
			EventAction a = event.actions().get(ai);
			if (a instanceof _EVENT.Imp) {
				ii = make(((_EVENT.Imp)a).other, ii);
			}
		}
		
		return ii;
		
	}
	
	public static void setData(Event event, EContext con) {

		
		for (int ai = 0; ai < event.actions().size(); ai++) {
			EventAction a = event.actions().get(ai);
			a.setContext(event, con);
		}
		
//		for (int ai = 0; ai < event.actions().size(); ai++) {
//			EventAction a = event.actions().get(ai);
//			if (a instanceof _EVENT.Imp) {
//				setData(((_EVENT.Imp)a).other, con);
//			}
//		}
		
	}
	

	static void check(LIST<Event> events) {

		
		for (Event o : events) {
			for (int ai = 0; ai < o.actions().size(); ai++) {
				EventAction a = o.actions().get(ai);
				if (a instanceof _EVENT.Imp && ((_EVENT.Imp)a).other == o) {
					throw new Errors.DataError("Event: " + o.key + " Has a cyclic nature and this is bad!");
				}
			}
		}
		
	}
	

}
