package util.text;

import game.faction.Faction;
import game.faction.royalty.Royalty;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import world.map.regions.Region;

public final class INSERT {

	public static final Inserter<Induvidual> indu = new InsertIndu();
	public static final Inserter<Race> race = new InsertRace();
	public static final Inserter<Humanoid> human = new InsertHuman();
	public static final Inserter<Faction> faction = new InsertFaction();
	public static final Inserter<Integer> player = new InsertPlayer();
	public static final Inserter<Region> reg = new InsertRegion();
	public static final Inserter<Royalty> royalty = new InsertRoyalty();
}
