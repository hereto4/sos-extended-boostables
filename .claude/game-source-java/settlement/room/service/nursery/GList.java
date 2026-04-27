package settlement.room.service.nursery;

import init.sprite.UI.UI;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.child.AIModule_Child;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.text.Dic;
import view.main.VIEW;

class GList {

	private final Entry[] entries = new Entry[256];
	private final ArrayList<Entry> all = new ArrayList<Entry>(entries.length);
	private int ssx,ssy;
	private int ei;
	private int vi;
	private final static GList self = new GList();
	
	private GList() {
		for (int i = 0; i < entries.length; i++)
			entries[i] = new Entry();
	}
	
	public static LIST<Entry> get(NurseryInstance ins){
		return self.pget(ins);
	}
	
	public LIST<Entry> pget(NurseryInstance ins){
		
		if (VIEW.RI() == vi && ins.mX() == ssx && ins.mY() == ssy)
			return all;
		
		vi = VIEW.RI();
		ssx = ins.mX();
		ssy = ins.mY();
		
		ei = 0;
		all.clearSloppy();
		
		
		for (COORDINATE c : ins.body()) {
			if (ins.is(c) && ins.blueprintI().ss.init(c.x(), c.y())) {
				int age = ins.blueprintI().ss.age.get();
				if (age > 0) {
					Entry e = entries[ei];
					all.add(e);
					e.icon = UI.icons().s.bed;
					e.name.clear().add(Dic.¤¤Baby);
					e.age = age;
					ei++;
					if (!all.hasRoom())
						return all;
				}
				
			}
		}
		
		
		iter.iterate();
		
		
		
		return all;
		
	}
	
	private final EntityIterator.Humans iter = new EntityIterator.Humans() {
		
		@Override
		protected boolean processAndShouldBreakH(Humanoid h, int ie) {
			if (AIModule_Child.isResident(h, ssx, ssy)) {
				Entry e = entries[ei];
				all.add(e);
				e.icon = UI.icons().s.human;
				e.name.clear().add(STATS.APPEARANCE().name(h.indu()));
				e.age = STATS.POP().age.DAYS.get(h.indu());
				ei++;
				if (!all.hasRoom())
					return true;
			}
			return false;
		}
	};
	
	public final  static class Entry {
	
		public SPRITE icon;
		public final Str name = new Str(24);
		public int age;
		
	}
	
}
