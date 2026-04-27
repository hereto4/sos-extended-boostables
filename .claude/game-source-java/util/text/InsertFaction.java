package util.text;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.PTitles.PTitle;
import init.race.Race;
import init.type.TRAIT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;
import world.region.RD;

final class InsertFaction extends Inserter<Faction> {
	

	
	public InsertFaction() {

		new II("FACTION") {

			@Override
			public void set(Faction t, Str str) {
				str.add(t.name);
					
			}
			
		};
		new II("FACTION_RULER") {

			@Override
			public void set(Faction t, Str str) {
				if (t == FACTIONS.player())
					str.add(FACTIONS.player().rulerName());
				else {
					str.add(((FactionNPC)t).court().king().name);
				}
			}
			
		};
		new II("FACTION_RULER_TITLE") {

			@Override
			public void set(Faction t, Str str) {
				if (t == FACTIONS.player())
					str.add(FACTIONS.player().level().current().name());
				else {
					double d = t.realm().regions()/20.0;
					int i = (int) (d*FACTIONS.player().level().all().size());
					i = CLAMP.i(i, 0, FACTIONS.player().level().all().size()-1);
					str.add(FACTIONS.player().level().all().get(i).male);
				}
					
			}
			
		};
		new II("FACTION_RULER_INTRO") {

			@Override
			public void set(Faction f, Str str) {
				if (f == FACTIONS.player()) {
					str.add(RD.RACE(f.race()).names.rIntro.get(0));
				}else {
					str.add(((FactionNPC)f).nameIntro);
				}
				
					
			}
			
		};
		new II("FACTION_RULER_TITLES") {

			@Override
			public void set(Faction ff, Str str) {
				if (ff == FACTIONS.player()) {
					for (PTitle t : FACTIONS.player().titles.all()) {
						if (t.selected()) {
							str.add(t.name);
							str.add(',').s();
						}

					}
				}else {
					FactionNPC t = (FactionNPC) ff;
					LIST<TRAIT> tt = t.court().king().roy().traits;
					for (int i = 0; i < tt.size(); i++) {
						str.add(tt.get(i).rTitle);
						if (i < tt.size()-1)
							str.add(',').s();
					}
				}
				
			}
		};
		
		
		
		join(new Inserter<Race>(new InsertRace(), "FACTION_"), new GETTER_TRANS<Faction, Race>() {

			@Override
			public Race get(Faction f) {
				return f.race();
			}
		});
	}
	
}
