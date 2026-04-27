package game.raiding;

import java.io.IOException;

import game.GAME;
import game.GAME.GameResource;
import game.boosting.BOOSTABLES;
import game.boosting.BSourceInfo;
import game.boosting.BValue.BValueAll;
import game.debug.Profiler;
import game.boosting.BoosterValue;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.raiding.RaidingMap.RaidRegion;
import game.time.TIME;
import init.constant.Config;
import init.sprite.UI.UI;
import settlement.entry.Immigration;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import view.interrupter.IDebugPanel;
import world.army.AD;
import world.map.regions.Region;

public final class RAIDING extends GameResource{
	
	public final int AMOUNT = 100;
	
	private final ArrayList<Raider> all = new ArrayList<Raider>(AMOUNT);
	public final RaidingCurrent current = new RaidingCurrent();
	public final RaidingUtil util = new RaidingUtil(AMOUNT);
	
	private final Updater updater = new Updater(this);
	private final UpdaterRegions updaterRegs = new UpdaterRegions();
	
	public final RaidingMap entry = new RaidingMap();

	private final ACTION init = new ACTION() {
		
		@Override
		public void exe() {
			all.clearSloppy();
			
			double w = Config.sett().POP_RAIDER_WORTH*Immigration.MAX_POPULATION + 25000000/Config.sett().POP_RAIDER_WORTH;
			
			for (int i = 0; i < all.max(); i++) {
				
				double d =  (double)(i)/all.max();
				double wealth = 200*Config.sett().POP_RAIDER_WORTH + Math.pow(d, 2.1)*w;
				double power = 5 + Config.battle().MEN_PER_ARMY*(1 + (GAME.battle().power.HIGH_POWER-1)/2)*Math.pow(d, 2.75);
				
				Raider r = new Raider(wealth, power, CLAMP.d(i/10.0, 0, 1));
				all.add(r);
			}
			current.clear();
			updater.clear();
		}
	};
	
	public RAIDING(){
		super("RAIDING", false);
		
		IDebugPanel.add("RAIDER event", new ACTION() {
			
			@Override
			public void exe() {
				if (util.active().size() > 0) {
					Raider r = util.active().rnd();
					current.appear(r);
				}else
					LOG.ln("nope");
			}
		});
		
		IDebugPanel.add("RAIDER spawn", new ACTION() {
			
			@Override
			public void exe() {
				LIST<RaidRegion> rr = entry.entryRegions();
				if (rr.size() == 0)
					return;
					
				Region reg = rr.rnd().r();
				double power = util.defences(reg);
				power *= (1+RND.rFloat(1));
				Raider raider = new Raider(FACTIONS.WORTH().raider(), power, RND.rFloat());
				raider.text.set(raider, true);
				current.raid(raider);
			}
		});
		
		IDebugPanel.add("RAIDER appear", new ACTION() {
			
			@Override
			public void exe() {
				
				LIST<RaidRegion> rr = entry.entryRegions();
				if (rr.size() == 0)
					return;
					
				Region reg = rr.rnd().r();
				double power = util.defences(reg);
				power *= (1+RND.rFloat(0.5));
				Raider raider = new Raider(FACTIONS.WORTH().raider(), power, RND.rFloat());
				raider.text.set(raider, true);
				if (!current.appear(raider))
					LOG.ln("nope");
			}
		});
		
		IDebugPanel.add("RAIDER appear cap", new ACTION() {
			
			@Override
			public void exe() {
				Raider raider = new Raider(FACTIONS.WORTH().raider(), util.defences(FACTIONS.player().capitolRegion())*(1+RND.rFloat(0.5)), RND.rFloat());
				raider.text.set(raider, true);
				current.appear(raider, FACTIONS.player().cx(), FACTIONS.player().cy());
			}
		});
		
		IDebugPanel.add("RAIDER clear", new ACTION() {
			
			@Override
			public void exe() {
				current.clear();
			}
		});
		
		IDebugPanel.add("RAIDER mess", new ACTION() {
			
			@Override
			public void exe() {
				Raider raider = new Raider(FACTIONS.WORTH().raider(), util.defences(FACTIONS.player().capitolRegion())*(1+RND.rFloat(0.5)), RND.rFloat());
				raider.text.set(raider, RND.rBoolean());
				new MessArmyAppear(raider, RND.rInt0(100), RND.rInt0(100)).send();
				new MessCustom(raider, "hello").send();
				new MessDefeated(raider).send();
				new MessDemand(raider).send();
				new MessDemandRejected(raider).send();
				new MessDemandTY(raider).send();
				new MessGoingAway(raider).send();
				new MessVictory(raider).send();
			}
		});
		
		
		
		
		IDebugPanel.add("RAIDERS reset", init);
		
		GAME.addOnInit(new ACTION() {
			
			@Override
			public void exe() {
				BValueAll vv = new BValueAll() {
					
					@Override
					public double get() {
						return CLAMP.d(AD.stats().repF().getD(FACTIONS.player()), 0, 1);
					}
				};
				BSourceInfo s = new BSourceInfo(AD.stats().repF().info().name, UI.icons().s.sword);
				
				new BoosterValue(vv, s, 1, 4, true).add(BOOSTABLES.CIVICS().RAID_SECURITY);
				
				vv = new BValueAll() {
					
					@Override
					public double get() {
						if (DIP.overlord(FACTIONS.player()) != null)
							return 1;
						return 0;
					}
				};
				s = new BSourceInfo( DIP.VASSAL().name, DIP.VASSAL().icon);
				new BoosterValue(vv, s, 0, 4, false).add(BOOSTABLES.CIVICS().RAID_SECURITY);
			}
		});
		GAME.addOnInit(init);
		
		
	}
	
	@Override
	protected void save(FilePutter file) {
		for (Raider r : all) {
			file.object(r);
		}
		current.save(file);
		updater.save(file);
		updaterRegs.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		all.clearSloppy();
		boolean fucked = false;
		for (int i = 0; i < AMOUNT; i++) {
			Raider r = (Raider)file.object(true);
			if (r == null) {
				fucked = true;
			}else
				all.add(r);
		}

		current.load(file);
		util.clear();
		updater.load(file);
		
		if (fucked)
			init.exe();

		
		updaterRegs.load(file);
		
	}

	void loadFix(){
		int powMax = 0;
		int pp = 0;
		for (Raider r : all) {
			if (r.defeated) {
				defeat(r);
				powMax = Math.max(powMax, r.army.power);
			}
			if (r.raids > 0)
				pp = Math.max(powMax, pp);
		}
		init.exe();
		for (Raider r : ALL()) {
			if (r.army.power <= powMax)
				r.defeated = true;
		}
	}
	
	@Override
	protected void update(double ds, Profiler prof) {
		prof.logStart(this);
		current.update(ds, prof);
		updater.update(ds);
		updaterRegs.update(ds);
		prof.logEnd(this);
	}
	
	public LIST<Raider> ALL(){
		return all;
	}

	void defeat(Raider raider) {
		raider.defeated = true;
		raider.secondDefeated = TIME.currentSecond();
	}
	
	public LIST<Raider> active(){
		return util.active();
	}
	
	public void reset() {
		init.exe();
	}
	
	public void raid() {
		if (active().size() > 0) {
			Raider r = active().rnd();
			r.text.set(r, r.raids == 0);
			GAME.raiders().current.raid(r);
		}
	}
	
}
