package world.army;

import game.faction.Faction;
import util.data.INT_O;
import world.entity.army.WArmy;

public interface ADInt extends INT_O<WArmy>{

	public default int faction(WArmy a) {
		return faction(a.faction());
	}
	
	public int faction(Faction f);
	
	public interface ADIntE extends ADInt, INT_OE<WArmy> {
		
	}
	
	static class ADIntImp implements ADIntE, ADInit.Countable{
		
		private final INT_OE<WArmy> a;
		private final INT_OE<Faction> f;
		
		public ADIntImp(ADInit init, String key, CharSequence name, CharSequence desc) {
			a = init.dataA. new DataInt(key);
			f = init.dataT. new DataInt(key);
			init.countable.add(this);
		}

		@Override
		public void set(WArmy t, int i) {
			count(t, -1);
			a.set(t, i);
			count(t, 1);
		}
		
		@Override
		public int get(WArmy t) {
			return a.get(t);
		}

		@Override
		public int min(WArmy t) {
			return 0;
		}

		@Override
		public int max(WArmy t) {
			return Integer.MAX_VALUE;
		}

		@Override
		public int faction(Faction f) {
			return this.f.get(f);
		}

		@Override
		public void count(WArmy t, int delta) {
			f.inc(t.faction(), a.get(t)*delta);
		}

		
	}
	
}
