package game.battle.factors;

import game.battle.div.Div;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.BoosterValue;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import world.map.regions.Region;

public abstract class DivFactor {
	
	public final BSourceInfo info;
	public final CharSequence message;
	public final BoostSpecs specs;
	public final double midValue;
	
	DivFactor(CharSequence name, CharSequence desc, SPRITE icon, CharSequence message, double mid){
		info = new BSourceInfo(name, desc, null, icon);
		this.message = message;
		specs = new BoostSpecs(info, true);
		midValue = mid;
		DivFactors.all.add(this);
	}
	
	public abstract double getD(Div div);
	
	public final void hover(Div div, GUI_BOX box) {
		GBox b = (GBox) box;
		b.title(info.name);
		b.text(info.desc);
		b.NL(4);
		phover(div, b);
		b.NL(8);
		specs.hoverDetailed(b, v.vGet(div), null, null, -1);
		b.NL();
	}
	
	protected void phover(Div div, GBox b) {
		
	}
	
	public DivFactor boost(Boostable b, double from, double to, boolean isMul) {
		specs.push(new BoosterValue(v, info, from, to, isMul), b);
		return this;
	}
	
	protected double induValue(Induvidual indu) {
		return midValue;
	}
	
	
	private final BValue v = new BValue() {

		@Override
		public double vGet(Div div) {
			return getD(div);
		}
		
		@Override
		public double vGet(Faction f) {
			return midValue;
		}

		@Override
		public double vGet(Region reg) {
			return midValue;
		}

		@Override
		public double vGet(Induvidual indu) {
			Div d = STATS.BATTLE().DIV.get(indu);
			if (d == null || !d.active())
				return induValue(indu);
			return getD(d);
		}

		@Override
		public double vGet(PopTime popTime) {
			return midValue;
		}

		@Override
		public double vGet(Player f) {
			return midValue;
		}

		@Override
		public double vGet(FactionNPC f) {
			return midValue;
		}
		
	};
}
