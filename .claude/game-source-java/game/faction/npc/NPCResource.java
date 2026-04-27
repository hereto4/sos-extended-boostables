package game.faction.npc;

import snake2d.util.file.SAVABLE;
import snake2d.util.sets.LISTE;
import world.region.pop.RDRace;

public abstract class NPCResource {

	protected NPCResource(LISTE<NPCResource> all) {
		all.add(this);
	}
	
	protected abstract SAVABLE saver();
	protected abstract void update(FactionNPC faction, double seconds);
	protected abstract void generate(RDRace pref, FactionNPC faction, boolean fromScratch);
	
}
