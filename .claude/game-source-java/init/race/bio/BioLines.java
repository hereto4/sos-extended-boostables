package init.race.bio;

import init.type.CAUSE_ARRIVE;
import init.type.CAUSE_ARRIVES;
import init.type.HCLASSES;
import init.type.HTYPES;
import init.type.TRAIT;
import init.type.TRAITS;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.Humanoid;
import settlement.room.home.HOME;
import settlement.stats.STATS;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;

final class BioLines {

	final LinkedList<BioLine> descs = new LinkedList<>();
	final LinkedList<BioLine> houseP = new LinkedList<>();

	BioLines(BioLines org, Json json){
		this(json);
		
		for (int i = 0; i < descs.size(); i++) {
			BioLine l = descs.get(i);
			if (!json.has(l.key)) {
				l.strings = org.descs.get(i).strings;
			}
		}
		if (json.has("TRAIT")) {
			Json tj = json.json("TRAIT");
			
			for (TRAIT t : TRAITS.ALL()) {
				if (!tj.has(t.key())) {
					CharSequence[] dd1 = descs.get(4+t.index()).strings;
					CharSequence[] dd2 = org.descs.get(4+t.index()).strings;
					
					CharSequence[] dd = new CharSequence[dd1.length + dd2.length];
					for (int i = 0; i < dd1.length; i++)
						dd[i] = dd1[i];
					for (int i = 0; i < dd2.length; i++)
						dd[i+dd1.length] = dd2[i];
					descs.get(4+t.index()).strings = dd;
				}
			}
		}
		
	}
	
	BioLines(Json json){
		
		new BioLine(descs, json, "INFO_OTHER") {
			@Override
			protected boolean use(Humanoid a) {
				if (!a.indu().hType().player || a.indu().hType() == HTYPES.CHILD())
					return true;
				return false;
			}
		};

		
		new BioLine(descs, json, "INFO_GENERAL");;
		new BioLine(descs, json, "INFO_TITLE");
		new BioLine(descs, json, "INFO_GENERAL2");
		new BioLine(descs, json, "INFO_GENERAL3").nlSet();
		
		houseP.add(new BioLine(descs, json, "HOME_NONE_WORK") {
			@Override
			protected boolean use(Humanoid a) {
				if (!a.indu().hType().player || a.indu().hType() == HTYPES.CHILD())
					return false;
				return STATS.HOME().GETTER.hasSearched.indu().isMax(a.indu()) && !STATS.HOME().GETTER.has(a) && STATS.WORK().EMPLOYED.get(a) != null;
			};
		}.nlSet());
		
		houseP.add(new BioLine(descs, json, "HOME_NONE") {
			@Override
			protected boolean use(Humanoid a) {
				if (!a.indu().hType().player || a.indu().hType() == HTYPES.CHILD())
					return false;
				return STATS.HOME().GETTER.hasSearched.indu().isMax(a.indu()) && !STATS.HOME().GETTER.has(a) && STATS.WORK().EMPLOYED.get(a) == null;
			};
		}.nlSet());
		
		houseP.add(new BioLine(descs, json, "HOME_NONE_SEARCH") {
			@Override
			protected boolean use(Humanoid a) {
				if (!a.indu().hType().player || a.indu().hType() == HTYPES.CHILD())
					return false;
				return !STATS.HOME().GETTER.hasSearched.indu().isMax(a.indu()) && !STATS.HOME().GETTER.has(a);
			};
		}.nlSet());
		
		{
			Json tj = json.json("TRAIT");
			
			for (TRAIT t : TRAITS.ALL()) {
				new BioLine(descs, tj, t.key(), t.bios) {
					
					@Override
					protected boolean use(Humanoid a) {
						if (a.indu().hType() == HTYPES.CHILD())
							return false;
						return STATS.TRAITS().stat(t).getD(a.indu()) > 0.35;
					}
				};
			}
		}
		
		new Friend(descs, json, "FRIEND") {
			@Override
			protected boolean use(Humanoid a) {
				if (super.use(a)) {
					Humanoid b = (Humanoid) STATS.POP().FRIEND.get(a.indu());
					return a.race().pref().race(b.indu().race()) >= 0.5;
				}
				return false;
			};
		}.nlSet();
		
		new Friend(descs, json, "FRIEND_ENEMY") {
			@Override
			protected boolean use(Humanoid a) {
				if (super.use(a)) {
					Humanoid b = (Humanoid) STATS.POP().FRIEND.get(a.indu());
					return a.race().pref().race(b.indu().race()) < 0.5;
				}
				return false;
			};
		}.nlSet();
		
		new BioLine(descs, json, "FRIEND_OTHER") {
			@Override
			protected boolean use(Humanoid a) {
				if (!a.indu().hType().player || a.indu().hType() == HTYPES.CHILD())
					return false;
				return STATS.POP().FRIEND.get(a.indu()) != null && (STATS.POP().FRIEND.get(a.indu()) instanceof Animal);
			};
		}.nlSet();
		
		new Origin(descs, json, "ORIGIN_NATIVE", CAUSE_ARRIVES.BORN());
		new Origin(descs, json, "ORIGIN_IMMI", CAUSE_ARRIVES.IMMIGRATED());
		new Origin(descs, json, "ORIGIN_FREED", CAUSE_ARRIVES.EMANCIPATED());
		new Origin(descs, json, "ORIGIN_PAROLE", CAUSE_ARRIVES.PAROLE());
		new Origin(descs, json, "ORIGIN_SOLDIER", CAUSE_ARRIVES.SOLDIER_RETURN());
		new Origin(descs, json, "ORIGIN_INSANE", CAUSE_ARRIVES.CURED());
		
		houseP.add(new BioLine(descs, json, "HOME") {
			@Override
			protected boolean use(Humanoid a) {
				if (a.indu().clas() == HCLASSES.NOBLE())
					return false;
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h == null)
					return false;
				
				return h.occupants() > 1;
			};
		}.nlSet());
		
		houseP.add(new BioLine(descs, json, "HOME_ALONE") {
			@Override
			protected boolean use(Humanoid a) {
				if (!a.indu().hType().player || a.indu().hType() == HTYPES.CHILD())
					return false;
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h == null)
					return false;
				return h.occupants() == 1;
			};
		}.nlSet());
		
//		houseP.add(new BioLine(descs, json, "HOME_NOBLE") {
//			@Override
//			protected boolean use(Humanoid a) {
//				return a.indu().clas() == HCLASS.NOBLE && STATS.HOME().GETTER.has(a);
//			};
//		});
		

		
		new BioLine(descs, json, "DIVISION") {
			@Override
			protected boolean use(Humanoid a) {
				return super.use(a) && STATS.BATTLE().DIV.get(a) != null;
			};
		}.nlSet();
		
		new BioLine(descs, json, "DIVISION_RECRUIT") {
			@Override
			protected boolean use(Humanoid a) {
				return super.use(a) && STATS.BATTLE().DIV.get(a) == null && STATS.BATTLE().RECRUIT.get(a) != null;
			};
		}.nlSet();
		
		new BioLine(descs, json, "DIVISION_NONE") {
			@Override
			protected boolean use(Humanoid a) {
				return super.use(a) && STATS.BATTLE().DIV.get(a) == null && STATS.BATTLE().RECRUIT.get(a) == null;
			};
		}.nlSet();
		
		new BioLine(descs, json, "WORK_NOBLE") {
			@Override
			protected boolean use(Humanoid a) {
				return super.use(a) && !STATS.WORK().WORK_TIME.indu().isMax(a.indu()) && a.indu().clas() == HCLASSES.NOBLE();
			};
		};
		
		new BioLine(descs, json, "WORK_EMPLOYED") {
			@Override
			protected boolean use(Humanoid a) {
				return super.use(a) && !STATS.WORK().WORK_TIME.indu().isMax(a.indu()) && a.indu().clas() != HCLASSES.NOBLE() && STATS.WORK().EMPLOYED.get(a) != null;
			};
		}.nlSet();
		
		new BioLine(descs, json, "WORK_UNEMPLOYED") {
			@Override
			protected boolean use(Humanoid a) {
				return super.use(a) && !STATS.WORK().WORK_TIME.indu().isMax(a.indu()) && a.indu().clas() != HCLASSES.NOBLE() && STATS.WORK().EMPLOYED.get(a) == null;
			};
		}.nlSet();
		
		new BioLine(descs, json, "WORK_LEISURE") {
			@Override
			protected boolean use(Humanoid a) {
				return super.use(a) && STATS.WORK().WORK_TIME.indu().isMax(a.indu());
			};
		}.nlSet();
		

		
		new BioLine(descs, json, "DREAMS");
		
		new BioLine(descs, json, "DREAMS_CHILD") {
			@Override
			protected boolean use(Humanoid a) {
				return a.indu().hType() == HTYPES.CHILD();
			}
		};

	}
	
	
	
	private static class Origin extends BioLine {

		private final CAUSE_ARRIVE ca;
		
		Origin(LISTE<BioLine> all, Json json, String key, CAUSE_ARRIVE ca) {
			super(all, json, key);
			this.ca = ca;
		}
		
		@Override
		protected boolean use(Humanoid a) {
			if (a.indu().clas() != HCLASSES.CITIZEN())
				return false;
			if (STATS.POP().COUNT.arrive.get(a.indu()) != ca)
				return false;
			return true;
		}
		
		
	}
	
	private static class Friend extends BioLine {
		
		Friend(LISTE<BioLine> all, Json json, String key) {
			super(all, json, key);
		}
		
		@Override
		protected boolean use(Humanoid a) {
			return super.use(a) && STATS.POP().FRIEND.get(a.indu()) != null && STATS.POP().FRIEND.get(a.indu()) instanceof Humanoid;
		}
	}
	

	
}
