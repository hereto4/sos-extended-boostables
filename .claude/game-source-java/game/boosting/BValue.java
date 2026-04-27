package game.boosting;

import game.battle.div.Div;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.faction.royalty.Royalty;
import init.type.POP_CL;
import settlement.stats.Induvidual;
import world.map.regions.Region;

public interface BValue {

	public default double vGet(Royalty roy) {
		return vGet(roy.induvidual);
	}
	
	public double vGet(Region reg);
	
	public double vGet(Induvidual indu);
	
	public double vGet(Div div);
	
	public default double vGet(POP_CL reg) {
		return vGet(PopTime.tmp(reg, 0)); 
	}
	
	public double vGet(PopTime t);

	public double vGet(Player f);
	
	public double vGet(FactionNPC f);
	
	public default double vGet(Faction f) {
		if (f == null)
			return 0;
		if (f instanceof FactionNPC) {
			return vGet((FactionNPC) f);
		}
		return vGet(FACTIONS.player());
	}
	
	public class BValueNone implements BValue {
		
		@Override
		public double vGet(Region reg) {
			return 0;
		}

		@Override
		public double vGet(Induvidual indu) {
			return 0;
		}

		@Override
		public double vGet(Div div) {
			return 0;
		}

		@Override
		public double vGet(Faction f) {
			return 0;
		}

		@Override
		public double vGet(POP_CL reg) {
			return 0;
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
		public double vGet(PopTime t) {
			return 0;
		}
	};
	
	public class BValueSome implements  BValue {
		
		private final double v;
		
		public BValueSome(double v){
			this.v = v;
		}
		
		@Override
		public double vGet(Region reg) {
			return v;
		}

		@Override
		public double vGet(Induvidual indu) {
			return v;
		}

		@Override
		public double vGet(Div div) {
			return v;
		}

		@Override
		public double vGet(Faction f) {
			return v;
		}

		@Override
		public double vGet(POP_CL reg) {
			return v;
		}

		@Override
		public double vGet(Player f) {
			return v;
		}

		@Override
		public double vGet(FactionNPC f) {
			return v;
		}

		@Override
		public double vGet(PopTime t) {
			return v;
		}
	};
	
	public static final BValue VALUE1 = new BValueSome(1.0);
	
	public static final BValue VALUE0 = new BValueNone();
	
	public abstract static class BValueAll implements BValue {

		@Override
		public double vGet(Region reg) {
			return get();
		}

		@Override
		public double vGet(Induvidual indu) {
			return get();
		}

		@Override
		public double vGet(Div div) {
			return get();
		}

		@Override
		public double vGet(Faction f) {
			return get();
		}

		@Override
		public double vGet(Player f) {
			return get();
		}

		@Override
		public double vGet(FactionNPC f) {
			return get();
		}

		public abstract double get();

		@Override
		public double vGet(PopTime t) {
			return get();
		}
		
	}
	
	public abstract static class BValueFaction implements BValue {

		private final Boostable bb;
		
		public BValueFaction(Boostable bo){
			this.bb = bo;
		}
		
		@Override
		public double vGet(Region reg) {
			return vGet(reg.faction());
		}

		@Override
		public double vGet(Induvidual indu) {
			return vGet(indu.faction());
		}

		@Override
		public double vGet(Div div) {
			return vGet(div.faction());
		}

		@Override
		public double vGet(POP_CL reg) {
			return vGet(FACTIONS.player());
		}
		
		@Override
		public double vGet(PopTime t) {
			return vGet(FACTIONS.player());
		}

		@Override
		public double vGet(FactionNPC f) {
			return f.bonus.getD(bb);
		}


	}
	
	public abstract static interface BValuePlayerOnly extends BValue {


		
		@Override
		public default double vGet(Region reg) {
			return vGet(reg.faction());
		}

		@Override
		public default double vGet(Induvidual indu) {
			return vGet(indu.faction());
		}

		@Override
		public default double vGet(Div div) {
			return vGet(div.faction());
		}

		@Override
		public default double vGet(POP_CL reg) {
			return vGet(FACTIONS.player());
		}
		
		@Override
		public default double vGet(PopTime t) {
			return vGet(FACTIONS.player());
		}


	}
	
	public abstract static class BValueInduOnly implements BValue {


		public BValueInduOnly(){

		}
		
		@Override
		public double vGet(Region reg) {
			return 0;
		}

		@Override
		public double vGet(FactionNPC f) {
			return 0;
		}

		@Override
		public double vGet(Player f) {
			return 0;
		}

		@Override
		public double vGet(PopTime t) {
			return 0;
		}
		

	}
	

	
	public static class PopTime implements BOOSTABLE_O{
		
		private static PopTime tmp = new PopTime();
		
		public POP_CL pop;
		public int daysBack;
		
		public PopTime get(POP_CL pop, int daysBack) {
			this.pop = pop;
			this.daysBack = daysBack;
			return this;
		}

		@Override
		public double boostableValue(BValue v) {
			return v.vGet(this);
		}
		
		private static PopTime tmp(POP_CL pop, int daysBack) {
			return tmp.get(pop, daysBack);
		}
	}
	
}
