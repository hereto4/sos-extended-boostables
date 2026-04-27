package util.text;

import game.battle.div.Div;
import game.boosting.BOOSTABLES;
import init.race.Race;
import init.religion.Religion;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.type.TRAIT;
import init.type.TRAITS;
import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.MATH;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.data.GETTER_TRANS;
import world.WORLD;
import world.map.regions.Region;

final class InsertIndu extends Inserter<Induvidual> {

	private static CharSequence ¤¤poor = "¤poor";
	private static CharSequence ¤¤good = "¤good";
	private static CharSequence ¤¤excellent = "¤excellent";
	private static CharSequence ¤¤amongOthers = "¤among others.";
	
	static {
		D.ts(InsertIndu.class);
	}
	
	InsertIndu(){
		new II("NAME") {
			@Override
			public void set(Induvidual t, Str str) {
				str.add(STATS.APPEARANCE().name(t));
			}
		}; 
		new II("CLASS") {
			@Override
			public void set(Induvidual t, Str str) {
				str.add(t.clas().name);
			}
		}; 
		new II("WORKPLACE") {
			@Override
			public void set(Induvidual t, Str str) {
				if (STATS.WORK().EMPLOYED.get(t) != null)
					str.add(STATS.WORK().EMPLOYED.get(t).name());
			}
		}; 
		new II("AGE") {
			@Override
			public void set(Induvidual a, Str str) {
				int i = (int)Math.ceil(STATS.POP().age.years.getD(a));
				str.add(i);
			}
		}; 
		new II("HEALTH") {
			@Override
			public void set(Induvidual a, Str str) {
				double def = BOOSTABLES.PHYSICS().HEALTH.baseValue;
				if (BOOSTABLES.PHYSICS().HEALTH.get(a) < def)
					str.add(¤¤poor);
				else if (BOOSTABLES.PHYSICS().HEALTH.get(a) < 1.5)
					str.add(¤¤good);
				else
					str.add(¤¤excellent);
			}
		}; 
		new II("TRAITS") {
			@Override
			public void set(Induvidual a, Str str) {
				
				LIST<TRAIT> tt = TRAITS.tmp(a, 4);
				int am = tt.size();
				
				if (am > 1) {
					for (TRAIT i : tt) {
						str.add(i.info.name);
						am --;
						if (am == 1)
							str.s().add(Dic.¤¤and).s();
						else if (am > 1)
							str.add(',').s();
					}
				}else {
					for (TRAIT i : tt) {
						str.add(i.info.name);
					}
				}
			}
		}; 
		new II("RELIGION") {
			@Override
			public void set(Induvidual a, Str str) {
				str.add(STATS.RELIGION().getter.get(a).info.name);
			}
		};
		
		
		
		new II("RND_REGION") {
			@Override
			public void set(Induvidual a, Str str) {
				int ran = (int) STATS.RAN().get(a, 128, 31);
				ran = MATH.mod(ran, WORLD.TAREA());
				int x = ran%WORLD.TWIDTH();
				int y = ran/WORLD.THEIGHT();
				
				Region r = WORLD.REGIONS().map.get(x, y);
				if (r == null) {
					outer:
					for (int i = 0; i < WORLD.TWIDTH(); i++) {
						for (DIR d : DIR.ALL) {
							r = WORLD.REGIONS().map.get(x+d.x()*i, y+d.y()*i);
							if (r != null) {
								break outer;
							}
						}
					}
				}
				if (r != null)
					str.add(r.info.name());
				else {
					str.add('?');
				}
			}
		};
		new II("HOME_TYPE") {
			@Override
			public void set(Induvidual a, Str str) {
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h != null) {
					str.add(h.typeName(h.serviceX(), h.serviceY()));
				}
			}
		}; 
		new II("HOME_LOCATION") {
			@Override
			public void set(Induvidual a, Str str) {
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h != null) {
					DIR d = DIR.get(SETT.TWIDTH/2, SETT.THEIGHT/2, h.serviceX(), h.serviceY());
					if (COORDINATE.tileDistance(SETT.TWIDTH/2, SETT.THEIGHT/2, h.serviceX(), h.serviceY()) < 150)
						d = DIR.C;
					str.add(Dic.get(d));
				}
			}
		}; 
		new II("HOME_MATES") {
			@Override
			public void set(Induvidual a, Str str) {
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h != null) {
					int am = 0;
					
					int occ = Math.min(h.occupants(), 3);
					
					for (int i = 0; i < occ; i++) {
						if (h.occupant(i).indu() != a) {
							if (am == 0)
								;
							else if (am == occ-1) {
								str.s().add(Dic.¤¤and).s();
							}else
								str.s().add(',').s();
							am++;
							str.add(STATS.APPEARANCE().name(h.occupant(i).indu()));
						}
					}
					
					if (h.occupants() > 3)
						str.s().add(¤¤amongOthers);
				}
			}
		}; 
		new II("DIVISION") {
			@Override
			public void set(Induvidual a, Str str) {
				Div d = STATS.BATTLE().DIV.get(a);
				if (d == null)
					d = STATS.BATTLE().RECRUIT.get(a);
				if (d != null)
					str.add(d.info.name());
			}
		}; 

		new II("ANIMAL_FRIEND_NAME") {
			@Override
			public void set(Induvidual a, Str str) {
				
				ENTITY b = STATS.POP().FRIEND.get(a);
				if (b instanceof Animal)
					str.add(((Animal)b).species().name);
			}
		}; 


		
		new II("RELIGION_GOD") {
			@Override
			public void set(Induvidual a, Str str) {
				Religion r = STATS.RELIGION().getter.get(a).religion;
				str.add(r.diety);
				
			}
		}; 
		new II("HE") {
			@Override
			public void set(Induvidual a, Str str) {
				
				str.add(a.race().info.pHE.get(a, false));
				
			}
		}; 
		new II("HEC") {
			@Override
			public void set(Induvidual a, Str str) {
				str.add(a.race().info.pHE.get(a, true));
				
			}
		}; 
		new II("HIM") {
			@Override
			public void set(Induvidual a, Str str) {
				
				str.add(a.race().info.pHIM.get(a, false));
				
			}
		}; 
		new II("HIMC") {
			@Override
			public void set(Induvidual a, Str str) {
				str.add(a.race().info.pHIM.get(a, true));
				
			}
		}; 
		new II("HIS") {
			@Override
			public void set(Induvidual a, Str str) {
				
				str.add(a.race().info.pHIS.get(a, false));
				
			}
		}; 
		new II("HISC") {
			@Override
			public void set(Induvidual a, Str str) {
				str.add(a.race().info.pHIS.get(a, true));
				
			}
		}; 
		new II("HIMSELF") {
			@Override
			public void set(Induvidual a, Str str) {
				
				str.add(a.race().info.pHIMSELF.get(a, false));
				
			}
		}; 
		new II("HIMSELFC") {
			@Override
			public void set(Induvidual a, Str str) {
				str.add(a.race().info.pHIMSELF.get(a, true));
				
			}
		}; 
		new II("RND_PROFESSION") {
			@Override
			public void set(Induvidual a, Str str) {
				RoomEmploymentSimple r = null;
				double m = 0;
				for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
					if (a.race().pref().getWork(e) > m) {
						r = e;
						m = a.race().pref().getWork(e);
					}
				}
				int i = (int) STATS.RAN().get(a, 80, 4) & 0b1111;
				if (i > 0)
					for (RoomEmploymentSimple e : SETT.ROOMS().employment.ALLS()) {
						
						if (Math.abs(m-a.race().pref().getWork(e)) < 0.1) {
							r = e;
							i--;
							if (i <= 0)
								break;
						}
					}
				str.add(r.title);
			}
		}; 

		new II("RND_FURNITURE") {
			@Override
			public void set(Induvidual a, Str str) {
				LIST<RES_AMOUNT> ll = a.race().home().clas(a.clas()).resources();
				RESOURCE res = null;
				if (ll != null && ll.size() > 0) {
					res = ll.getC(STATS.RAN().get(a, 11)).resource();
				}else
					res = RESOURCES.ALL().getC(STATS.RAN().get(a, 11));
				str.add(res.names);
			}
		};
		
		join(new InsertRace(), new GETTER_TRANS<Induvidual, Race>() {

			@Override
			public Race get(Induvidual f) {
				return f.race();
			}
		
		});
		
	}
	
}