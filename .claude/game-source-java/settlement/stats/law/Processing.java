package settlement.stats.law;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import game.time.TIMECYCLE;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.law.PUNISHMENT_SERVICE;
import settlement.room.main.RoomBlueprintImp;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEANO.BOOLEAN_OE;
import util.info.INFO;
import util.keymap.RMapInt;
import util.statistics.HISTORY;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryRace;

public final class Processing {

	final HistoryRace punishAll = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false) {
		@Override
		public int min(Race t) {
			return Integer.MIN_VALUE;
		};
	};
	
	private Json json = new Json(PATHS.TEXT_MISC().get("Law")).json("PUNISHMENT");
	private final LinkedList<SAVABLE> savers = new LinkedList<>();
	{savers.add(punishAll);}
	private LinkedList<Punishment> al = new LinkedList<>();
	
	public final PunishmentDec none = new PunishmentDec(al, json, "NONE", SETT.ROOMS().STOCKADE, 0);

	public final Punishment pardoned = new Punishment(al, json, "PARDONED", null, SPRITES.icons().m.admin, -0.5);
	
	public final PunishmentDec exile = new PunishmentDec(al, json, "EXILE", null, UI.icons().s.arrow_left, -0.25);
	public final PunishmentDec prison = new PunishmentDec(al, json, "PRISON", SETT.ROOMS().PRISON, 0);
	public final PunishmentDec execution = new PunishmentDec(al, json, "EXECUTION", SETT.ROOMS().EXECUTION, 2.0);
	public final PunishmentDec enslaved = new PunishmentDec(al, json, "ENSLAVED", SETT.ROOMS().SLAVER, 1.0);
	public final PunishmentDec arena = new PunishmentDec(al, json, "ARENA", SETT.ROOMS().FIGHTPITS.get(0), 2.0);
	
	public final Extra stocks = new ExtraImp(json, "STOCKS", SETT.ROOMS().STOCKS);
	public final Extra judgement = new ExtraImp(json, "JUDGEMENT", SETT.ROOMS().COURT);
	public final Extra arrests = new ExtraImp(json, "ARRESTS", SETT.ROOMS().GUARD.iconBig()) {
		
		@Override
		public void inc(Race race, boolean success) {
			if (success)
				this.history.inc(race, 1);
			this.total.inc(race, 1);
		};
		
	};
	public final Extra prosecute = new Extra(json, "PROSECUTE", SPRITES.icons().m.descrimination) {

		@Override
		public int total(Race race, int daysBack) {
			return STATS.POP().POP.data(HCLASSES.CITIZEN()).get(race, daysBack)+1;
		}
		
		
	};
	
	public final Extra escape = new Extra(json, "ESCAPE", SPRITES.icons().m.chainsFree) {

		@Override
		public int total(Race race, int daysBack) {
			return 1 + (int) (STATS.POP().POP.data(HCLASSES.CITIZEN()).get(race, daysBack)/100.0);
		}
		
		
	};

	public final LIST<Punishment> punishments = new ArrayList<Punishment>(al);
	{al = null;};
	public final LIST<PunishmentDec> punishmentsdec = new ArrayList<PunishmentDec>(exile,prison,execution,enslaved,arena);
	public final LIST<Extra> extras = new ArrayList<Extra>(judgement,stocks);
	public final LIST<Extra> other = new ArrayList<Extra>(arrests,prosecute);
	
	
	private double upD = 0;
	
	
	public Processing() {
		saver.clear();
		json = null;
		
		if (false) {
			//the rates do not work as prisoners are in the stockade. They should go to the dungeon instead where they can be punished further.
		}
	}
	
	public HISTORY_INT punishTotal(Race race) {
		return punishAll.history(race);
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (SAVABLE s : savers)
				s.save(file);
			file.d(upD);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			for (SAVABLE s : savers)
				s.load(file);
			upD = file.d();
			upD = 0;
		}
		
		@Override
		public void clear() {
			for (SAVABLE s : savers)
				s.clear();
			
			for (Extra e : extras) {
				e.allowedd.setAll(1);
			}
			arrests.allowedd.setAll(1);
			
			Arrays.fill(prison.dec, 0.5f);
			Arrays.fill(enslaved.dec, 0.25f);
			Arrays.fill(execution.dec, 0.25f);
			
			
			upD = 0;
		}
	};
	
	private final double dd = 0.025*RACES.all().size()/(0.25*TIME.secondsPerDay());
	
	void update(double ds) {
		
		
		int ri = (int) upD;
		upD+=ds*0.25;
	
		int am = (int)upD - ri;
		while(am > 0) {
			am--;
			Race r = RACES.all().getC(ri);
			ri++;
			
			{
				double ra = prate(prosecute, r);
				double cu = prosecute.rate.getD(r);
				if (ra <= cu) {
					cu -= dd;
				}
				
				cu = CLAMP.d(cu, 0, 1);
				prosecute.rate.setD(r, cu);
				prosecute.rate.total().setD(prate(prosecute, null));
			}
			{
				double ra = prate(escape, r);
				double cu = prosecute.rate.getD(r);
				if (ra <= cu) {
					cu -= dd;
				}
				cu = CLAMP.d(cu, 0, 1);
				escape.rate.setD(r, cu);
				escape.rate.total().setD(prate(escape, null));
			}
			
			{
				double next = prate(arrests, r);
				double now = arrests.rate.getD(r);
				double d = next-now;
				if (d >= 0) {
					d = dd;
					next = CLAMP.d(now+d, 0, next);
				}else {
					d = -dd;
					next = CLAMP.d(now+d, next, now);
				}
				next = CLAMP.d(next, 0, 1);
				arrests.rate.setD(r, next);
			}
			
			
			double tot = 0;
			double ttot = 0;
			for (int i = 0; i < 8; i++) {
				tot += punishAll.history(r).get(i);
				ttot +=  punishAll.history(null).get(i);
			}
			
			if (tot == 0)
				continue;
			
			for (PunishmentImp p : punishments) {
				p.rate.setD(r, prate(p, r, tot));
				p.rate.total().setD(prate(p, null, ttot));
			}
		}

		if (upD > 100000)
			upD -= 100000;
		
	}
	
	private static double prate(PunishmentImp current, Race race) {
		double tot = 0;
		double am = 0;
		for (int i = 0; i < 16; i++) {
			am += current.history(race).get(i);
			tot += current.total(race, i);
		}
		if (tot == 0)
			return CLAMP.d(current.rate.getD(race), 0 ,1);
		else
			am = CLAMP.d(am/tot, 0, 1);
		return am;
	}
	
	private static double prate(PunishmentImp current, Race race, double tot) {
		double am = 0;
		for (int i = 0; i < 16; i++) {
			am += current.history(race).get(i);
		}
		if (tot == 0)
			am = CLAMP.d(am, 0, 1);
		else
			am = CLAMP.d(am/tot, 0, 1);
		return am;
	}
	
	public Punishment getPunishment(Humanoid h) {
		double r = (int)(STATS.RAN().getD(h.indu(), 12)*100);
		for (PunishmentDec p : punishmentsdec) {
			r -= Math.ceil(p.limit(h.race())*100);
			if (r <= 0)
				return p;
		}
		return none;
	}
	
	public abstract class PunishmentImp extends INFO{

		final HistoryRace history = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false) {
			@Override
			public int min(Race t) {
				return Integer.MIN_VALUE;
			};
		};
		final HistoryRace rate = new LRate(); 
		public final CharSequence action;
		public final CharSequence verb;
		
		public final RoomBlueprintImp room;
		public final PUNISHMENT_SERVICE ser;
		public final SPRITE icon;
		public final double multiplier;
		public final String key;
		private PunishmentImp(Json json, String key, PUNISHMENT_SERVICE room, SPRITE icon, double mul) {
			super(json.json(key));
			this.key = key;
			this.multiplier = mul;
			verb = json.json(key).text("VERB");
			action = json.json(key).text("ACTION");
			if (room != null) {
				this.room = (RoomBlueprintImp) room;
				this.ser = room;
			}else {
				this.room = null;
				this.ser = null;
			}
			this.icon = icon;
			savers.add(history);
			savers.add(rate);
		}
		
		public HISTORY_INT history(Race race) {
			return history.history(race);
		}
		
		public HISTORY rate(Race race) {
//			if (race == null)
//				return tot;
			return rate.history(race);
		}

		public abstract int total(Race race, int daysBack);

	
		
	}
	
	public abstract class Extra extends PunishmentImp{

		final RMapInt<Race> allowedd = new RMapInt<Race>(RACES.map());
		
		private Extra(Json json, String key , PUNISHMENT_SERVICE room) {
			super(json, key, room, ((RoomBlueprintImp) room).iconBig(), 0);
			savers.add(allowedd);
		}
		
		private Extra(Json json, String key , SPRITE icon) {
			super(json, key, null, icon, 0);
			savers.add(allowedd);
		}
		
		public final BOOLEAN_OE<Race> allowed = new BOOLEAN_OE<Race>() {

			@Override
			public boolean is(Race t) {
				if (t == null) {
					for (int i = 0; i < RACES.all().size(); i++)
						if (allowedd.get(RACES.all().get(i)) == 1)
							return true;
					return false;
				}
				return allowedd.get(t) == 1;
			}

			@Override
			public BOOLEAN_OE<Race> set(Race t, boolean b) {
				if (t == null) {
					for (int i = 0; i < RACES.all().size(); i++)
						allowedd.set(RACES.all().get(i), b ? 1 : 0);
				}else {
					allowedd.set(t, b ? 1 : 0);
				}
				return this;
			}
			
			
			
		};
		
		public void inc(Race race, boolean success) {
			if (success)
				this.history.inc(race, 1);
			rate.setD(race, prate(this, race));
			rate.total().setD(prate(this, null));
		}
		
	}
	

	
	class ExtraImp extends Extra{

		final HistoryRace total = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
		
		private ExtraImp(Json json, String key , PUNISHMENT_SERVICE room) {
			super(json, key, room);
			savers.add(total);
		}
		
		private ExtraImp(Json json, String key , SPRITE icon) {
			super(json, key, icon);
			savers.add(total);
		}

		@Override
		public int total(Race race, int daysBack) {
			return total.history(race).get(daysBack);
		}
		
		@Override
		public void inc(Race race, boolean success) {
			total.inc(race, 1);
			super.inc(race, success);
		}
		
	}
	
	public class Punishment extends PunishmentImp implements INDEXED{

		private final int index;
		
		private Punishment(LISTE<Punishment> all, Json json, String key, PUNISHMENT_SERVICE room, SPRITE icon, double value) {
			super(json, key, room, icon, value);
			this.index = all.add(this);
		}
		
		
		public final void inc(Race race) {
			
			history.inc(race, 1);
			punishAll.inc(race, 1);
			upD = RACES.all().size()-1;
				
		}
		
		public final void dec(Race race) {
			
			punishAll.inc(race, -1);
			history.inc(race, -1);
			
			upD = RACES.all().size()-1;
		}

		@Override
		public int total(Race race, int daysBack) {
			return punishTotal(race).get(daysBack);
		}


		@Override
		public int index() {
			return index;
		}
		
	}
	
	public final class PunishmentDec extends Punishment {
		
		private final double[] dec = new double[RACES.all().size()];
		
		private PunishmentDec(LISTE<Punishment> all, Json json, String key, PUNISHMENT_SERVICE room, SPRITE icon, double mul) {
			super(all, json,key, room, icon, mul);
			savers.add(new SAVABLE() {
				
				@Override
				public void save(FilePutter f) {
					RACES.map().saver().save(dec, f);
				}
				
				@Override
				public void load(FileGetter f) throws IOException {
					RACES.map().loader().load(dec, f, 0);
				}
				
				@Override
				public void clear() {
					Arrays.fill(dec, 0.0);
				}
			});
		}
		
		private PunishmentDec(LISTE<Punishment> all, Json json, String key, PUNISHMENT_SERVICE room, double mul) {
			super(all, json,key, room, ((RoomBlueprintImp) room).iconBig(), mul);
			savers.add(new SAVABLE() {
				
				@Override
				public void save(FilePutter f) {
					RACES.map().saver().save(dec, f);
				}
				
				@Override
				public void load(FileGetter f) throws IOException {
					RACES.map().loader().load(dec, f, 0);
				}
				
				@Override
				public void clear() {
					Arrays.fill(dec, 0.0);
				}
			});
		}
		
		public double limit(Race race) {
			if (race == null) {
				double rr = 0;
				for (int i = 0; i < dec.length; i++)
					rr += dec[i];
				return rr / dec.length;
			}
			
			return dec[race.index];
		}
		
		public void limitSet(Race race, double lim) {
			if (race == null) {
				for (int i = 0; i < dec.length; i++)
					limitSet(RACES.all().get(i), lim);
				return;
			}
			
			lim = CLAMP.d(lim, 0, 1);
			dec[race.index] = (float) lim;
			double total = 0;
			for (PunishmentDec p : punishmentsdec) {
				total += p.dec[race.index];
			}
			
			
			total -= 1.0;
			if (total <= 0) {
				return;
			}
			
			int k = punishmentsdec.size();
			while(total > 0 && k > 0) {
				k--;
				double am = total/(punishmentsdec.size()-1);
				for (PunishmentDec p : punishmentsdec) {
					if (p != PunishmentDec.this) {
						double a = Math.min(am, p.dec[race.index]);
						p.dec[race.index] -= a;
						total -= a;
					}
				}
			}
			

		}
		
	}
	
	private static class LRate extends HistoryRace implements HISTORY_INT.HISTORY_INTE{

		public LRate() {
			super(STATS.DAYS_SAVED, TIME.days(), true);
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return Integer.MAX_VALUE;
		}

		@Override
		public TIMECYCLE time() {
			return TIME.days();
		}

		@Override
		public int historyRecords() {
			return STATS.DAYS_SAVED;
		}

		@Override
		public void set(int t) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public double getD(int fromZero) {
			return get(fromZero)/(double)(max());
		}

		@Override
		public int get(int fromZero) {
			double pop = 0;
			double tot = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				double p = STATS.POP().POP.data().get(RACES.all().get(ri), fromZero);
				pop += p;
				tot += p*get(RACES.all().get(ri));
			}
			if (pop == 0)
				return 0;
			double d = tot/pop;
			d = CLAMP.d(d, 0, Integer.MAX_VALUE);
			return (int) (d);
			
		}
		
		@Override
		public HISTORY_INT.HISTORY_INTE history(Race r) {
			if (r == null)
				return this;
			return super.history(r);
		}

		@Override
		public HISTORY_INT.HISTORY_INTE total() {
			return this;
		}

		@Override
		public int get(Race t) {
			if (t == null)
				return this.get();
			return history(t).get();
		}

		
		
		
	}
	
}
