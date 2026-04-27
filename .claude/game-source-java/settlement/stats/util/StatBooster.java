package settlement.stats.util;

import game.battle.div.Div;
import game.boosting.BValue;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import settlement.stats.Induvidual;
import settlement.stats.stat.STAT;
import snake2d.util.misc.CLAMP;
import world.map.regions.Region;

public abstract class StatBooster implements BValue {

//	private final int index;
	
	public StatBooster() {
//		this.index = index;
	}
	
	@Override
	public double vGet(Player f) {
		return 0;
	}

	@Override
	public double vGet(FactionNPC f) {
		return 0;
	}

	@Override
	public double vGet(Region reg) {
		return vGet(reg.faction());
	}
	
	


	public static StatBooster make(STAT stat) {
		return new StatBooster() {
			
			@Override
			public double vGet(Div div) {
				return CLAMP.d(stat.div().getD(div), 0, 1);
			}
			
			@Override
			public double vGet(Induvidual indu) {
				return CLAMP.d(stat.indu().getD(indu), 0, 1);
			}

			@Override
			public double vGet(PopTime t) {
				return CLAMP.d(stat.data(t.pop.cl).getD(t.pop.race, t.daysBack), 0, 1);
			}
		};
	}
	

	

//	public static class StatBoosterStat implements StatBooster{
//		
//		private final STAT stat;
//		private final boolean npc;
//		
//		public StatBoosterStat(STAT stat, boolean hasNPC){
//			this.stat = stat;
//			this.npc = hasNPC;
//		}
//		
//		@Override
//		public double vGet(Div div) {
//			return CLAMP.d(stat.div().getD(div), 0, 1);
//		}
//		
//		@Override
//		public double vGet(Induvidual indu) {
//			return CLAMP.d(stat.indu().getD(indu), 0, 1);
//		}
//
//		@Override
//		public double vGet(POP_CL reg) {
//			return CLAMP.d(stat.data(reg.cl).getD(reg.race), 0, 1);
//		}
//		
//		@Override
//		public double vGet(POP_CL reg, int daysBack) {
//			return CLAMP.d(stat.data(reg.cl).getD(reg.race, daysBack), 0, 1);
//		}
//
//		@Override
//		public double vGet(NPCBonus bonus) {
//			if (npc)
//				return bonus.get(stat.index());
//			return 0;
//		}
//		
//		@Override
//		public boolean has(Class<?> b) {
//			if (!npc && b == NPCBonus.class)
//				return false;
//			return StatBooster.super.has(b);
//		}
//		
//	}




	
	

}