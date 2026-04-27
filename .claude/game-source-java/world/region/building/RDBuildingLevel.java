package world.region.building;

import game.boosting.BoostSpecs;
import init.sprite.UI.Icon;
import init.value.Lockable;
import snake2d.util.sets.INDEXED;
import world.map.regions.Region;

public final class RDBuildingLevel implements INDEXED{
	
	public final BoostSpecs local;
	BoostSpecs global;
	public final Lockable<Region> reqs;
	public final Icon icon;
	public final CharSequence name;
	int index;
	public int cost = 0;
	
	RDBuildingLevel(CharSequence name, Icon icon, Lockable<Region> needs) {
		local = new BoostSpecs(name, icon, false);
		global = new BoostSpecs(name, icon, false);

		this.name = name;
		this.icon = icon;
		this.reqs = needs;
	}

	@Override
	public int index() {
		return index;
	}
	
}