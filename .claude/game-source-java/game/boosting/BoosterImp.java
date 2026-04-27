package game.boosting;

import game.battle.div.Div;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import settlement.stats.Induvidual;
import snake2d.util.misc.CLAMP;
import world.map.regions.Region;

public class BoosterImp extends Booster implements BValue{

	private final double from;
	private final double to;

	public BoosterImp(BSourceInfo info, double from, double to, boolean isMul) {
		super(info, isMul);
		this.from = from;
		this.to = to;
	}
	
	public BoosterImp(BSourceInfo info, double to, boolean isMul) {
		super(info, isMul);
		this.to = to;
		
		if (isMul) {
			from = 1;
		}else {
			from = 0;
		}
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
	public double vGet(PopTime popTime) {
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
	public double getValue(double input) {
		input = CLAMP.d(input, 0, 1);
		return from() + input*(to()-from());
	}

	@Override
	protected double pget(BOOSTABLE_O o) {
		return o.boostableValue(this);
	}
	

}