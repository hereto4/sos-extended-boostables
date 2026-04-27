package util.text;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;
import world.map.regions.Region;

final class InsertRegion extends Inserter<Region> {
	

	
	public InsertRegion() {

		new II("NAME") {

			@Override
			public void set(Region t, Str str) {
				if (t == null)
					return;
				str.add(t.info.name());
			}
			
		};
		
		join(new Inserter<Faction>(new InsertFaction(), "FACTION_"), new GETTER_TRANS<Region, Faction>() {



			@Override
			public Faction get(Region f) {
				if (f.faction() == null)
					return FACTIONS.player();
				return f.faction();
				
						
			}
		});
	}
	
}
