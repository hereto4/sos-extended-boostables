package world.region;


import game.boosting.BSourceInfo;
import game.boosting.BoosterImp;
import game.faction.Faction;
import world.map.regions.Region;

public abstract class RBooster extends BoosterImp {

	public RBooster(BSourceInfo info, double from, double to, boolean isMul) {
		super(info, from, to, isMul);
	}

	@Override
	public double vGet(Faction f) {
		return 0;
	}
	
	
	protected abstract double get(Region reg);
	
	@Override
	public double vGet(Region reg) {
		return get(reg);
	}

}
