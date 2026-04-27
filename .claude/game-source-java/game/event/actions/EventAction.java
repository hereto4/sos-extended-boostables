package game.event.actions;

import game.event.engine.EContext;
import game.event.engine.Event;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;

public abstract class EventAction {

	public final ArrayListGrower<CInt> ints = new ArrayListGrower<>();
	public final String key;
	public boolean hideUI = false;
	
	EventAction(String key, LISTE<EventAction> all){
		this.key = key;
		all.add(this);
	}
	
	public void exe(Event e, EContext data) {
		
	}
	void setContext(Event e, EContext data) {
		
	}
	public void hover(GBox b, Event e, EContext context) {
		
	}
	public CharSequence problem(Event e, EContext context) {
		return null;
	}
	
	public void addToMessageBody(LISTE<RENDEROBJ> rows, Event e, EContext context, RECTANGLE messBody) {
		
	}
	
	public void update(Event e, EContext context, double ds, double second) {
		
	}
	
	private static String levent = null;
	
	public class CInt {
		
		int di = -1;
		
		
		CInt(String key){
			ints.add(this);
		}

		public int get(Event e, EContext t) {
			if (levent != e.key) {
				t.actionContext = EventActionContext.makeData(e, t.actionContext);
				levent = e.key;
			}
			if (t.actionContext != null && di >= 0 && di < t.actionContext.length)
				return t.actionContext[di];
			return 0;
		}

		public void set(Event e, EContext t, int i) {
			if (levent != e.key) {
				t.actionContext = EventActionContext.makeData(e, t.actionContext);
				levent = e.key;
			}
			if (t.actionContext != null && di >= 0 && di < t.actionContext.length)
				t.actionContext[di] = i;
		}
		
	}
	
}
