package settlement.stats.law;

import java.io.IOException;

import game.battle.div.Div;
import game.boosting.BOOSTABLES;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.Boostable;
import game.boosting.BoosterValue;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.time.TIME;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.law.Processing.PunishmentImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.statistics.HISTORY;
import util.statistics.HistoryInt;
import util.text.D;
import world.map.regions.Region;

public final class LawRate {
	
	private static CharSequence ¤¤popSize = "Population Size";
	
	static {
		D.ts(LawRate.class);
	}
	
	private final HistoryInt rate = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
	
	public static CharSequence ¤¤desc = "Law prevents crime and makes your subjects feel safe. The most important aspect is to catch the criminals. You can then process them as you see fit, with harsher punishments decreasing crime further. Law is determined by the effective rates of your arrests and punishments. The effective rate is the average of an 8-day period."; 
	
	public final Boostable blawfull = BOOSTABLES.BEHAVIOUR().LAWFULNESS;
	public final Boostable blaw = BOOSTABLES.CIVICS().LAW;
	
	static {
		D.ts(LawRate.class);
	}
	
	LawRate(){
		
		
		Value v = new Value() {
			
			@Override
			protected double value(int daysBack) {
				double d = 2000.0/STATS.POP().POP.data().get(null, daysBack);
				d = Math.pow(d, 1.5);
				d = 1.0-d;
				d = CLAMP.d(d, 0, 1);
				return d;
			}
		};
		new Boost(v, ¤¤popSize, UI.icons().s.citizen, 10, 0, true).add(BOOSTABLES.BEHAVIOUR().LAWFULNESS);

		v = new Value() {
			
			@Override
			protected double value(int daysBack) {
				return blaw.get(POP_CL.clP(), daysBack);
			}
		};
		new Boost(v, blaw.name, blaw.icon, 1, 10, true).add(BOOSTABLES.BEHAVIOUR().LAWFULNESS);
		
		v = new Value() {
			
			@Override
			protected double value(int daysBack) {
				return LAW.process().arrests.rate(null).getD(daysBack);
			}
		};
		new Boost(v, LAW.process().arrests.name, LAW.process().arrests.icon, 0, 1, false).add(blaw);
		
		for (PunishmentImp p : LAW.process().punishments) {
			if (p.multiplier == 0)
				continue;
			v = new Value() {
				
				@Override
				protected double value(int daysBack) {
					return p.rate(null).getD(daysBack);
				}
			};
			new Boost(v, p.name, p.icon, 1, 1+p.multiplier, true).add(blaw);
		}
		
		for (PunishmentImp p : LAW.process().extras) {
			if (p.multiplier == 0)
				continue;
			v = new Value() {
				
				@Override
				protected double value(int daysBack) {
					return p.rate(null).getD(daysBack);
				}
			};
			new Boost(v, p.name, p.icon, 1, 1+p.multiplier, true).add(blaw);
		}
		
		v = new Value() {
			
			@Override
			protected double value(int daysBack) {
				return LAW.process().escape.rate(null).getD(daysBack);
			}
		};
		new Boost(v, LAW.process().escape.name, LAW.process().escape.icon, 1, 0.25, true).add(blaw);

		
		
	}
	
	public HISTORY rate() {
		return rate;
	}
	
	public double increase() {
		return rate.getD(0)-rate.getD(1);
	}
	
	public double today() {
		return rate.getD();
	}
	
	SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			rate.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			rate.load(file);
			
		}
		
		@Override
		public void clear() {
			rate.clear();
		}
	};

	void update(double ds) {
		
		
		
		rate.setD(CLAMP.d(blaw.get(POP_CL.clP()), 0, 1));
	}
	
	private abstract static class Value implements BValue {

		@Override
		public double vGet(Induvidual indu) {
			return value(0);
		}
		
		@Override
		public double vGet(POP_CL indu) {
			return value(0);
		}
		
		
		@Override
		public double vGet(PopTime popTime) {
			return value(popTime.daysBack);
		}
		
		
		
		protected abstract double value(int daysBack);

		@Override
		public double vGet(Region reg) {
			return 0;
		}

		@Override
		public double vGet(Div div) {
			return 0;
		}

		@Override
		public double vGet(Player f) {

			return value(0);
		}

		@Override
		public double vGet(FactionNPC f) {
			return 0;
		}


		
		
	}
	
	private static class Boost extends BoosterValue {

		public Boost(Value v, CharSequence name, SPRITE icon, double from, double to, boolean isMul) {
			super(v, new BSourceInfo(name, icon), from, to, isMul);
		}
	}
	
}
