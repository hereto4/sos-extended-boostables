package world.army;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.text.Dic;
import view.world.panel.IDebugPanelWorld;
import world.army.ADInit.Register;
import world.army.ADInit.Updater;
import world.entity.army.WArmy;
import world.region.RD;
import world.region.pop.RDRace;

public final class ADConscripts {

	final ArrayList<INT_OE<Faction>> total = new ArrayList<INT_OE<Faction>>(RACES.all().size());
	private final INT_O<Faction> totalAll;
	private final ArrayList<INT_O<Faction>> available = new ArrayList<INT_O<Faction>>(RACES.all().size());
	private final INT_O<Faction> availableAll;
	final ArrayList<INT_OE<Faction>> used = new ArrayList<INT_OE<Faction>>(RACES.all().size());
	private final INT_O<Faction> usedAll;
	
	public INT_O<Faction> total(Race race) {
		if (race == null)
			return totalAll;
		return total.get(race.index);
	}
	
	public INT_O<Faction> available(Race race) {
		if (race == null)
			return availableAll;
		return available.get(race.index);
	}
	
	public INT_O<Faction> used(Race race) {
		if (race == null)
			return usedAll;
		return used.get(race.index);
	}
	
	public boolean canTrain(Race race, Faction f) {
		if (f == null)
			return true;
		return AD.men(race).faction(f) - AD.cityDivs().total(race) < total(race).get(f);
	}
	
	public int canTrainI(Race race, Faction f) {
		if (f == null)
			return 100000;
		return  total(race).get(f)-AD.men(race).faction(f)+ AD.cityDivs().total(race);
	}
	
	public void kill(Race race, Faction f, int men) {
		if (f != null)
		total.get(race.index).inc(f, -men);
	}
	
	ADConscripts(ADInit init){
		for (Race r : RACES.all()) {
			total.add(init.dataT. new DataInt("CONSCRIPTABLE_" + r.key, Dic.¤¤Conscriptable, Dic.¤¤ConscriptsD) {
				
				@Override
				public int get(Faction t) {
					if (r.population().max <= 0) {
						if (t == FACTIONS.player()) {
							return 0;
						}
					}
					return super.get(t);
				}
				
			});
			
			
			available.add(new INT_O<Faction>() {

				@Override
				public int get(Faction t) {
					return total.get(r.index).get(t)-used.get(r.index).get(t);
				}

				@Override
				public int min(Faction t) {
					return 0;
				}

				@Override
				public int max(Faction t) {
					return Integer.MAX_VALUE;
				}
				
			});
			
			used.add(init.dataT. new DataInt("CONSCRIPTABLE_USED_" + r.key, Dic.¤¤Conscriptable, Dic.¤¤ConscriptsD));
		}
		
		totalAll = tot(total);
		availableAll = tot(available);
		usedAll = tot(used);
		IDebugPanelWorld.add("Conscripts 1000", new ACTION() {
			
			@Override
			public void exe() {
				for (RDRace rr : RD.RACES().all)
					total.get(rr.race.index).inc(FACTIONS.player(), 1000);
			}
		});
		
		init.inits.add(new ACTION_O<Faction>() {
			
			@Override
			public void exe(Faction t) {
				for (Race r : RACES.all()) {
					total.get(r.index()).set(t, RD.MILITARY().conscripts(r, t));
				}
			}
		});
		
		init.registers.add(new Register() {
			
			@Override
			public void register(ADDiv div, int d) {
				if (div.needConscripts()) {
					used.get(div.race().index).inc(div.faction(), d*div.menTarget());
				}
					
			}
		});
		
		init.updaters.add(new Updater() {
			
			@Override
			public void update(Faction f, double timeSinceLast) {
				if (f == null || !f.isActive())
					return;
				
				
				for (Race r : RACES.all()) {
					int n = total(r).get(f);
					int t = RD.MILITARY().conscripts(r, f);
					if (t < n) {
						AD.conscripts().total.get(r.index()).set(f, t);
					
					}else {
						double d = t-n;
						
						if (d > 0) {
							
							d *= TIME.secondsPerDayI()*timeSinceLast/8.0;
							n += (int) d;
							if (RND.rFloat() < (d - (int) d))
								n++;
						}else if (d < 0) {
							n = t;
						}
						
						n = CLAMP.i(n, 0, t);
						
						AD.conscripts().total.get(r.index()).set(f, n);
					}
					
					
				}
			}
			
			@Override
			public void update(WArmy a, double timeSinceLast) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	private static INT_O<Faction> tot(LIST<? extends INT_O<Faction>> li){
		return new INT_O<Faction>() {

			@Override
			public int get(Faction t) {
				int am = 0;
				for (INT_O<Faction> f : li)
					am += f.get(t);
				return am;
			}

			@Override
			public int min(Faction t) {
				return 0;
			}

			@Override
			public int max(Faction t) {
				return Integer.MAX_VALUE;
			}
			
		};
	}
}
