package game.boosting.tmp;

import game.GAME;
import game.battle.div.Div;
import game.boosting.BOOSTABLE_O;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.stats.Induvidual;
import util.text.D;
import world.map.regions.Region;

class TBooster extends Booster{

	private static CharSequence ¤¤name = "Other Effects";
	
	static {
		D.ts(TBooster.class);
	}
	
	private final double from;
	private final double to;
	private final BValue value;
	
	public TBooster(Boostable target, double min, double max, boolean isMul) {
		super(new BSourceInfo(¤¤name, UI.icons().s.question), isMul);
		this.from = min;
		this.to = max;
		value = isMul ? new VBMul(target) : new VBAdd(target);
		add(target);
	}

	@Override
	public double from() {
		return from;
	}

	@Override
	public double to() {
		return to;
	}

	@Override
	public double getValue(double input) {
		return input;
	}

	@Override
	protected double pget(BOOSTABLE_O o) {
		return o.boostableValue(value);
	}		
	
	
	private static class VBAdd implements BValue{

		private final Boostable target;
		
		VBAdd(Boostable target){
			this.target = target;
		}
		
		@Override
		public double vGet(FactionNPC f) {
			return GAME.BOOST().factions.add(f,target);
		}
		
		@Override
		public double vGet(Player f) {
			return GAME.BOOST().factions.add(f,target);
		}
		
		@Override
		public double vGet(PopTime t) {
			if (t.pop.f() == null) {
				return GAME.BOOST().popcl.add(t.pop,target) + GAME.BOOST().factions.add(FACTIONS.player(), target);
			}
			return GAME.BOOST().popcl.add(t.pop,target);
		}
		
		@Override
		public double vGet(Div div) {
			Faction f = div.faction();
			if (f == FACTIONS.player())
				return GAME.BOOST().popcl.add(POP_CL.clP(div.info.race()),target);
			else if (f != null)
				return GAME.BOOST().factions.add(f, target);
			return 0;
		}
		
		@Override
		public double vGet(Induvidual indu) {
			return GAME.BOOST().popcl.add(indu.popCL(), target) + GAME.BOOST().factions.add(FACTIONS.player(), target);
		}
		
		@Override
		public double vGet(Region reg) {
			if (reg.faction() != null)
				return GAME.BOOST().regions.add(reg,target)+ GAME.BOOST().factions.add(reg.faction(),target);
			return GAME.BOOST().regions.add(reg, target);
		}
		
	}
	
	private static class VBMul implements BValue{

		private final Boostable target;
		
		VBMul(Boostable target){
			this.target = target;
		}
		
		@Override
		public double vGet(FactionNPC f) {
			return GAME.BOOST().factions.mul(f,target);
		}
		
		@Override
		public double vGet(Player f) {
			return GAME.BOOST().factions.mul(f,target);
		}
		
		@Override
		public double vGet(PopTime t) {
			if (t.pop.f() == null) {
				return GAME.BOOST().popcl.mul(t.pop,target) * GAME.BOOST().factions.mul(FACTIONS.player(), target);
			}
			return GAME.BOOST().popcl.mul(t.pop,target);
		}
		
		@Override
		public double vGet(Div div) {
			Faction f = div.faction();
			if (f == FACTIONS.player())
				return GAME.BOOST().popcl.mul(POP_CL.clP(div.info.race()),target);
			else if (f != null)
				return GAME.BOOST().factions.mul(f,target);
			return 0;
		}
		
		@Override
		public double vGet(Induvidual indu) {
			return GAME.BOOST().popcl.mul(indu.popCL(),target) * GAME.BOOST().factions.mul(FACTIONS.player(),target);
		}
		
		@Override
		public double vGet(Region reg) {
			if (reg.faction() != null)
				return GAME.BOOST().regions.mul(reg,target)* GAME.BOOST().factions.mul(reg.faction(),target);
			return GAME.BOOST().regions.mul(reg,target);
		}
		
	}
	
	
}
