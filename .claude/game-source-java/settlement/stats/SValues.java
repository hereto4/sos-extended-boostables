package settlement.stats;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HTYPE;
import init.type.HTYPES;
import init.value.GVALUES;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.main.throne.THRONE;
import settlement.stats.colls.StatsReligion.StatReligion;
import settlement.stats.standing.STANDINGS;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEANO;
import util.data.DOUBLE_O;
import util.text.D;
import util.text.Dic;

final class SValues {

	private static CharSequence ¤¤player = "Chosen Race";
	private static CharSequence ¤¤playerIs = "Faction is:";
	
	private static CharSequence ¤¤oddjobs = "Odd jobs exist";
	static {
		D.ts(SValues.class);
	}
	
	SValues(){
		for (STAT s : STATS.all()) {
			if (s.key() != null && s.indu() != null) {
				GVALUES.INDU.push(s.key() + "_F", s.info().name, s.info().icon == null ? UI.icons().s.question : s.info().icon, s.indu());
				GVALUES.INDU.pushI(s.key() + "_I", s.info().name, s.info().icon == null ? UI.icons().s.question : s.info().icon, s.indu());
			}
			if (s.key() != null) {

				String k = s.key();
				if (s.info().isInt()) {
					GVALUES.FACTION.push(k, s.info().name, s.info().icon == null ? UI.icons().s.question : s.info().icon, new DOUBLE_O<Faction>() {

						@Override
						public double getD(Faction t) {
							return s.data(HCLASSES.CITIZEN()).getD(null) * s.dataDivider();
						}

					}, false);
				} else {
					GVALUES.FACTION.push(k, s.info().name, s.info().icon == null ? UI.icons().s.question : s.info().icon, new DOUBLE_O<Faction>() {

						@Override
						public double getD(Faction t) {
							return s.data(HCLASSES.CITIZEN()).getD(null);
						}

					}, true);

				}

			}
		}
		
		SPRITE[] ri = new SPRITE[RACES.all().size()];
		for (Race race : RACES.all()) {
			SPRITE s = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					 race.appearance().iconBig.render(r, X1, X2, Y1, Y2);
					 
					
				}
			};
			ri[race.index] = s;
		}
		
		GVALUES.INDU.push("RACE_IS_PLAYER", ¤¤player, UI.icons().s.human, new BOOLEANO<Induvidual>() {

			@Override
			public boolean is(Induvidual t) {
				return t.race() == FACTIONS.player().race();
			}
			
			
		});
		for (Race race : RACES.all()) {
			GVALUES.INDU.push("RACE_" + race.key, race.info.name, ri[race.index], new BOOLEANO<Induvidual>() {

				@Override
				public boolean is(Induvidual t) {
					return t.race() == race;
				}
				
				
			});
		}
		for (HTYPE t : HTYPES.ALL()) {
			GVALUES.INDU.push("TYPE_" + t.key, t.name, UI.icons().s.human, new BOOLEANO<Induvidual>() {

				@Override
				public boolean is(Induvidual i) {
					return i.hType() == t;
				}
				
				
			});
		}
		for (HCLASS t : HCLASSES.ALL()) {
			GVALUES.INDU.push("CLASS_" + t.key, t.name, UI.icons().s.human, new BOOLEANO<Induvidual>() {

				@Override
				public boolean is(Induvidual i) {
					return i.clas() == t;
				}
				
				
			});
		}
		

		GVALUES.FACTION.push("POPULATION", Dic.¤¤Population, UI.icons().s.human, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction o) {
				return STATS.POP().POP.data(null).get(null);
			}

		}, false);
		
		GVALUES.FACTION.push("CREDITS", Dic.¤¤Currs, UI.icons().s.money, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction o) {
				return (int)o.credits().getD();
			}

		}, false);
		
		for (HCLASS cl : HCLASSES.ALL()) {
			String k = "POPULTAION_" + cl.key;
			GVALUES.FACTION.push(k, cl.names, cl.icon(), new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction o) {
					return STATS.POP().POP.data(cl).get(null);
				}

			}, false);
		}
		
		GVALUES.FACTION.push("WORKFORCE", Dic.¤¤Employees, UI.icons().s.hammer, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction o) {
				return STATS.WORK().workforce();
			}

		}, false);
		
		for (Race r : RACES.all()) {
			String k = "POPULATION_" + r.key + "_";
			GVALUES.FACTION.push(k + "_F", r.info.names, ri[r.index], new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction o) {
					double div = STATS.POP().POP.data(null).get(null);
					if (div == 0)
						return 0;
					return STATS.POP().POP.data(null).get(r)/div;
					
				}

			}, true);
			GVALUES.FACTION.push(k + "_I", r.info.names, ri[r.index], new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction o) {
					return STATS.POP().POP.data(null).get(r);
				}

			}, false);
			GVALUES.FACTION.push("FACTION_IS_" + r.key , ¤¤playerIs + " " + r.info.names, ri[r.index], new BOOLEANO<Faction>() {

				@Override
				public boolean is(Faction t) {
					return t.race() == r;
				}

			});
			for (HCLASS cl : HCLASSES.ALL()) {
				if (!cl.player)
					continue;
				GVALUES.FACTION.push(k + cl.key + "_F", cl.names + ": " + r.info.names, ri[r.index], new DOUBLE_O<Faction>() {

					@Override
					public double getD(Faction o) {
						double div = STATS.POP().POP.data(cl).get(null);
						if (div == 0)
							return 0;
						return STATS.POP().POP.data(cl).get(r)/div;
						
					}

				}, true);
				GVALUES.FACTION.push(k + cl.key + "_I", cl.names + ": " + r.info.names, ri[r.index], new DOUBLE_O<Faction>() {

					@Override
					public double getD(Faction o) {
						return STATS.POP().POP.data(cl).get(r);
					}

				}, false);
			}
		}
		
		for (HTYPE t : HTYPES.ALL()) {
			String k = "POPULTAION_" + t.key;
			GVALUES.FACTION.push(k + "_I", t.names, UI.icons().s.human, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction o) {
					return STATS.POP().pop(t);
				}

			}, false);
			GVALUES.FACTION.push(k + "_F", t.names, UI.icons().s.human, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction o) {
					double div = STATS.POP().POP.data(null).get(null);
					if (div == 0)
						return 0;
					return STATS.POP().pop(t) / div;
				}

			}, true);
		}
		
		for (StatReligion r : STATS.RELIGION().ALL) {
			GVALUES.FACTION.push(STATS.RELIGION().key + "_" + r.religion.key + "_F", r.religion.info.name, r.religion.icon, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction t) {
					return r.followers.data(HCLASSES.CITIZEN()).getD(null);
				}
				
			}, true);
			GVALUES.FACTION.push(STATS.RELIGION().key + "_" + r.religion.key + "_I", r.religion.info.name, r.religion.icon, new DOUBLE_O<Faction>() {

				@Override
				public double getD(Faction t) {
					return r.followers.data(HCLASSES.CITIZEN()).get(null);
				}
				
			}, false);
			GVALUES.INDU.push(STATS.RELIGION().key + "_" + r.religion.key, r.religion.info.name, r.religion.icon, new DOUBLE_O<Induvidual>() {

				@Override
				public double getD(Induvidual t) {
					return STATS.RELIGION().getter.get(t) == r ? 1 : 0;
				}
				
			}, false, true);
		}
		
		for (HCLASS cl : HCLASSES.ALL()) {
			if (cl.player) {
				GVALUES.FACTION.push("LOYALTY_" + cl.key, Dic.¤¤Happiness + ": " + cl.names, UI.icons().s.heart, new DOUBLE_O<Faction>() {

					@Override
					public double getD(Faction t) {
						return STANDINGS.get(cl).current();
					}
					
				}, true);
				
				
			}
		}
		
		GVALUES.FACTION.push("PLAYER_HAS_ODDJOBS", ¤¤oddjobs, UI.icons().s.hammer, new BOOLEANO<Faction>() {

			@Override
			public boolean is(Faction t) {
				return SETT.PATH().finders.job.hasAnyJobs(THRONE.coo().x(), THRONE.coo().y());
			}
		
		});
		
		for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
			GVALUES.INDU.push("WORK_" + e.blueprint().key, e.title, e.blueprint().icon, new BOOLEANO<Induvidual>() {

				@Override
				public boolean is(Induvidual i) {
					RoomInstance ins = STATS.WORK().EMPLOYED.get(i);
					return ins != null && ins.blueprint().employment() == e;
				}
				
				
			});
		}
	}
	
}
