package util.text;

import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.type.BUILDING_PREF;
import init.type.BUILDING_PREFS;
import settlement.main.SETT;
import settlement.tilemap.floor.Floors.Floor;
import snake2d.util.sprite.text.Str;

final class InsertRace extends Inserter<Race>{
	
	InsertRace(){
		
		new II("RACE") {
			@Override
			public void set(Race t, Str str) {
				str.add(t.info.name);
			}
		}; 
		new II("RACES") {
			@Override
			public void set(Race t, Str str) {
				str.add(t.info.names);
			}
		}; 
		new II("RACIAN") {
			@Override
			public void set(Race t, Str str) {
				str.add(t.info.namePosessive);
			}
		};
		new II("RACIANS") {
			@Override
			public void set(Race t, Str str) {
				str.add(t.info.namePosessives);
			}
		};
		new II("RACE_HELLO") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sHello));
			}
		};
		new II("RACE_GOODBYE") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sGoodbye));
			}
		};
		new II("RACE_CURSE") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sCurse));
			}
		};
		new II("RACE_INSULT") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sInsult));
			}
		};
		new II("RACE_INSULTING") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sInsulting));
			}
		};
		new II("RACE_LORD") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sLord));
				
			}
		};
		new II("RACE_CITY") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sCity));
				
			}
		};
		new II("RACE_OTHERS") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sOthers));
				
			}
		};
		new II("RACE_SELVES") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sSelves));
			}
		};
		new II("RACE_SELF") {
			@Override
			public void set(Race a, Str str) {
				str.add(s(a.info.sSelf));
				
			}
		};
		new II("RACE_NAME_RND") {
			@Override
			public void set(Race a, Str str) {
				int g = ran();
				str.add(a.appearance().types.getC(g).names.firstNames.getC(ran()));
				str.s();
				str.add(a.appearance().types.getC(g).names.lastNames.getC(ran()));
			}
		};
		new II("RACE_LIKED_BUILDING") {
			@Override
			public void set(Race a, Str str) {
				BUILDING_PREF p = null;
				for (BUILDING_PREF pp: BUILDING_PREFS.ALL()) {
					if (p == null || a.pref().structure(pp) > a.pref().structure(p))
						p = pp;
				}
				str.add(p.name);
			}
		};
		new II("RACE_LIKED_ROAD") {
			@Override
			public void set(Race a, Str str) {
				Floor p = null;
				for (Floor pp: SETT.FLOOR().roads) {
					if (p == null || pp.pref(a) > p.pref(a))
						p = pp;
				}
				str.add(p.name);
			}
		};
		new II("RACE_LIKED_FOOD") {
			@Override
			public void set(Race a, Str str) {
				
				RESOURCE r = a.pref().food.getC(ran()).resource;
				str.add(r.name);
				
			}
		};
		new II("RACE_LIKED_DRINK") {
			@Override
			public void set(Race a, Str str) {
				
				RESOURCE r = a.pref().drink.getC(ran()).resource;
				str.add(r.name);
				
			}
		};
		new II("RACE_LIKED_FOODS") {
			@Override
			public void set(Race a, Str str) {
				
				for (int i = a.pref().food.size()-1; i > 0; i--) {
					str.add(a.pref().food.get(i).resource.names).add(',').s();
				}
				if (a.pref().food.size() > 1)
					str.add(Dic.¤¤and).s();
				str.add(a.pref().food.get(0).resource.names);
				
			}
		};
		
		new II("HATED_RACE") {
			@Override
			public void set(Race a, Str str) {
				
				Race hh = RACES.all().get(0);
				for (Race r : RACES.all()) {
					if (a.pref().race(r) < a.pref().race(hh))
						hh = r;
				}
				
				str.add(hh.info.name);
				
			}
		};
		
	}
	
	private CharSequence s(CharSequence[] ss) {
		return ss[ran()%ss.length];
	}
	
}