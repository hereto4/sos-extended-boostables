package init.type;

import game.boosting.BSourceInfo;
import game.boosting.BoostableCat;
import game.boosting.BoosterImp;
import game.faction.Faction;
import game.time.TIME;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.sprite.UI.UI;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.keymap.RMAP;
import util.text.D;

public class NEEDS {

	private static NEEDS self;

	{
		D.gInit(this);
	}

	private final BoostableCat bCat = new BoostableCat("RATES_", D.g("Service",  "Service Needs"), "", BoostableCat.TYPE_SETT, UI.icons().s.house);
	private final BoostableCat bCatE = new BoostableCat("RATES_", D.g("Basic Needs"), "", BoostableCat.TYPE_SETT, UI.icons().s.house);
	
	private final ArrayListGrower<NEED> ALL = new ArrayListGrower<>();
	private final ArrayListGrower<NEED> ALLNE = new ArrayListGrower<>();
	private final ArrayListGrower<NEED_E> ALLE = new ArrayListGrower<>();
	private final RMAP<NEED> coll;
	private final Types types;
	private final ResFolder f = PATHS.STATS().folder("need");

	NEEDS() {
		self = this;


		ResFolder f = PATHS.STATS().folder("need");

		for (String k : f.init.getFiles()) {
			new NEED(k, f, ALL, bCat, null, false);

		}
		types = new Types();

		final ArrayListGrower<NEED> events = new ArrayListGrower<>();
		
		for (NEED n : ALL) {
			if (n.event > 1)
				events.add(n);
			if (n instanceof NEED_E)
				continue;
			ALLNE.add(n);
			
		}
		
		{
			int days = events.size()*2+1;
			int day = 0;
			for (NEED n : events) {
				new Event(day, days, n);
				day+= 2;
			}
		}
		
		
		
		coll = new RMAP<NEED>("NEED", ALL);
	}

	private static CharSequence ¤¤event = "Small Event";
	static {
		D.ts(NEEDS.class);
	}
	
	private static class Event extends BoosterImp {

		private final int day;
		private final int days;
		
		public Event(int day, int days, NEED need) {
			super(new BSourceInfo(¤¤event, UI.icons().s.arrowUp), 1, need.event, true);
			this.day = day;
			this.days = days;
			add(need.rate);
		}

		@Override
		public double vGet(Faction f) {
			return (TIME.days().bitsSinceStart() % days) == day ? 1 : 0;
		}
		
		
	}
	
	public static LIST<NEED> ALL() {
		return self.ALL;
	}
	
	public static LIST<NEED> ALLSIMPLE() {
		return self.ALLNE;
	}

	public static LIST<NEED_E> ALLE() {
		return self.ALLE;
	}


	public static BoostableCat bCat() {
		return self.bCat;
	}
	
	public static BoostableCat bCatE() {
		return self.bCatE;
	}

	public static Types TYPES() {
		return self.types;
	}

	public static RMAP<NEED> MAP() {
		return self.coll;
	}

	public final class Types {
		public final NEED_E HUNGER = new NEED_E("_HUNGER", f, ALL, ALLE, bCatE);
		public final NEED_E THIRST = new NEED_E("_THIRST", f, ALL, ALLE, bCatE);
		public final NEED_E SHOPPING = new NEED_E("_SHOPPING", f, ALL, ALLE, bCatE);
		public final NEED SKINNYDIP = new NEED("_SKINNYDIP", f, ALL, bCat, UI.icons().s.drop, false);
		public final NEED TEMPLE = new NEED("_TEMPLE", f, ALL, bCat, UI.icons().s.temple, false);
		public final NEED SHRINE = new NEED("_SHRINE", f, ALL, bCat, UI.icons().s.shrine, false);

	}

}
