package util.text;

import game.faction.Faction;
import game.faction.royalty.Royalty;
import settlement.stats.Induvidual;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;

final class InsertRoyalty extends Inserter<Royalty> {
	

	
	public InsertRoyalty() {
		
		new II("NAME") {
			
			@Override
			public void set(Royalty t, Str str) {
				str.add(t.name());
			}
		};
		
		new II("NAME_FULL") {
			
			@Override
			public void set(Royalty t, Str str) {
				t.nameFull(str);
			}
		};
		
		new II("RANK") {
			
			@Override
			public void set(Royalty t, Str str) {
				t.nameSucc(str);
			}
		};
		
		join(new Inserter<Induvidual>(new InsertIndu(), "INDUVIDUAL_"), new GETTER_TRANS<Royalty, Induvidual>() {

			@Override
			public Induvidual get(Royalty f) {
				if (f == null)
					return null;
				return f.induvidual;
			}
		});
		
		join(new Inserter<Faction>(new InsertFaction(), "FACTION_"), new GETTER_TRANS<Royalty, Faction>() {

			@Override
			public Faction get(Royalty f) {
				if (f == null)
					return null;
				return f.court.faction;
			}
		});
	}
	
}
