package world.region.updating;

import init.race.Race;
import init.religion.Religion;
import init.resources.RESOURCE;
import world.map.regions.Region;

public interface RealmBuilder {
	
	public double policy(Race race, Region reg);
	public double priority(RESOURCE res, Region reg);
	public double priority(Religion religion, Region reg);
	public double military(Region reg);
	public double size();
}
