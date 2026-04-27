package world.army;

import game.GAME;
import game.faction.Faction;
import snake2d.util.misc.CLAMP;
import util.data.INT_O.INT_OE;
import world.entity.army.WArmy;

public final class ADPower {

	private final INT_OE<WArmy> carmy;
	private final INT_OE<Faction> cfaction;
	
	private final INT_OE<WArmy> army;
	private final INT_OE<Faction> faction;
	
	ADPower(ADInit init){
		
		carmy = init.dataA.new DataBit("CPOWER");
		cfaction = init.dataT.new DataBit("CPOWER");
		
		army = init.dataA.new DataInt("POWER");
		faction = init.dataT.new DataInt("POWER");
		
		init.countable.add(new ADInit.Countable() {
			
			@Override
			public void count(WArmy a, int delta) {
				mor(a);
			}
		});
		
		init.registers.add(new ADInit.Register() {
			
			@Override
			public void register(ADDiv div, int d) {
				mor(div.army());
			}
		});
		
	}
	
	void mor(WArmy a) {
		cfaction.set(a.faction(), 0);
		carmy.set(a, 0);
	}
	
	public int get(WArmy a) {
		
		if (carmy.get(a) == 0) {
			army.set(a,  CLAMP.i((int)GAME.battle().power.get(a), 0, Integer.MAX_VALUE));
		}
		carmy.inc(a, 1);
		return (int) Math.ceil(army.get(a)*morale(a));
	}
	
	public int get(Faction f) {
		if (cfaction.get(f) == 0) {
			
			int p = 0;
			for (int ai = 0; ai < f.armies().all().size(); ai++) {
				WArmy a = f.armies().all().get(ai);
				p += get(a);
			}
			cfaction.set(f, 1);
			faction.set(f, p);
			if (p < 0) {
				for (int ai = 0; ai < f.armies().all().size(); ai++) {
					WArmy a = f.armies().all().get(ai);
					System.err.println(get(a) + " " + f);
				}
			}
		}
		return faction.get(f);
	}
	
	public double morale(WArmy a) {
		return CLAMP.d(AD.supplies().health(a)*(0.5 + 0.5*AD.supplies().morale(a)), 0, 1);
	}
	
}
