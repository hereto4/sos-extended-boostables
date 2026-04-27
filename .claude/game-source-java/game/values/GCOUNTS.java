package game.values;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.GAME.GameResource;
import game.debug.Profiler;
import game.faction.Faction;
import game.time.TIME;
import init.paths.PATHS;
import init.sprite.UI.UI;
import init.value.GVALUES;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import util.data.DOUBLE_O;
import util.keymap.MAPPED;
import util.keymap.RMAPS;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;

public final class GCOUNTS extends GameResource{
	
	private LinkedList<SAccumilator> all = new LinkedList<>();
	{
		D.gInit(this);
	}
	public final SAccumilator ENSLAVED = new SAccumilator(all, "ENSLAVED", true,  D.g("ENSLAVED", "Enslaved Population"));
	public final SAccumilator FREED_SLAVES = new SAccumilator(all, "FREED_SLAVES", true, D.g("FREED_SLAVES", "Slaves Freed"));
	public final SAccumilator TIME_PLAYED = new SAccumilator(all, "TIME_PLAYED", true, D.g("TIME_PLAYED", "Time Played"));
	public final SAccumilator TRADE_SALES = new SAccumilator(all, "TRADE_SALES", true, D.g("TRADE_SALES", "Denari from sales"));
	public final SAccumilator TRADE_PURCHASES = new SAccumilator(all, "TRADE_PURCHASES", true, D.g("TRADE_PURCHASES", "Denari from purchases"));
	
	public final SAccumilator RIOTS = new SAccumilator(all, "RIOTS", true, D.g("RIOTS", "Riots"));
	public final SAccumilator CRAFTED = new SAccumilator(all, "CRAFTED", true, D.g("CRAFTED", "Goods Crafted"));
	public final SAccumilator INVASIONS = new SAccumilator(all, "INVASIONS", true, D.g("INVASIONS", "Invasions"));
	public final SAccumilator EXECUTIONS = new SAccumilator(all, "EXECUTIONS", true, D.g("EXECUTIONS", "Executions"));
	public final SAccumilator TUNNELS = new SAccumilator(all, "TUNNELS", true, D.g("TUNNELS", "Tunnels dug"));
	public final SAccumilator ROOMS_BUILT = new SAccumilator(all, "ROOMS_BUILT", false, D.g("ENSLAVED", "Rooms built"));
	public final SAccumilator SUBJECTS = new SAccumilator(all, "SUBJECTS", false, D.g("SUBJECTS", "Population"));
	
//	public final SAccumilator ENEMIES_KILLED = new SAccumilator(all, "ENEMIES_KILLED", true, D.g("ENEMIES_KILLED", "Enemies killed"));
//	public final SAccumilator BATTLES_WON = new SAccumilator(all, "BATTLES_WON", false, D.g("BATTLES_WON", "Battles Won"));
//	public final SAccumilator BATTLES_LOST = new SAccumilator(all, "BATTLES_LOST", false, D.g("BATTLES_LOST", "Battles Lost"));
	public final SAccumilator INVASIONS_WON = new SAccumilator(all, "INVASIONS_WON", false, D.g("INVASIONS_WON", "Invasions Won"));
	public final SAccumilator INVASIONS_LOST = new SAccumilator(all, "INVASIONS_LOST", false, D.g("INVASIONS_LOST", "Invasions Lost"));
	public final SAccumilator ROYALTIES_KILLED = new SAccumilator(all, "ROYALTIES_KILLED", false, D.g("ROYALTIES_KILLED", "Royalties assassinated"));
	public final SAccumilator CURED = new SAccumilator(all, "HOSPITAL_CURED", false, D.g("HOSPITAL_CURED", "Cured"));
	public final SAccumilator ACCIDENTS = new SAccumilator(all, "ACCIDENTS", false, D.g("ACCIDENTS", "Accidents"));
	public final SAccumilator UNITES = new SAccumilator(all, "UNITES", false, D.g("UNITES", "Kingdoms united"));
	
	
	public final LIST<SAccumilator> ALL = new ArrayList<>(all);;
	public final RMAPS<SAccumilator> MAP;
	
	private final static String filename = "StatsDoNotCheat";
	
	private static CharSequence ¤¤allTime = "¤all time";
	
	private final int[] trashold;
	private final int[] hi;
	private final int[] value;
	
	static {
		D.ts(GCOUNTS.class);
	}
	
	public GCOUNTS(){ 
		super("COUNTS", true);
		all = null;
		
		
		trashold = new int[ALL.size()];
		hi = new int[ALL.size()];
		value = new int[ALL.size()];
		
		MAP = new RMAPS<SAccumilator>("STATISTIC", ALL);		
		read();
		
		GVALUES.FACTION.push("WORLD_REGIONS", Dic.¤¤Regions, UI.icons().s.world, new DOUBLE_O<Faction>() {

			@Override
			public double getD(Faction t) {
				return t.realm().regions();
			}
			
		}, false);
		
		
	}

	
	private void read() {
		try {
			Arrays.fill(value, 0);
			Arrays.fill(trashold, 0);
			Arrays.fill(hi, 0);
			Json json = new Json(PATHS.local().PROFILE.get(filename));
			for (SAccumilator s : ALL) {
				trashold[s.index] = 0;
				hi[s.index] = 0;
				if (json.has(s.key)) {
					trashold[s.index] = json.i(s.key);	
				}
				if (json.has(s.key+"_HIGH"))
					hi[s.index] = json.i(s.key+"_HIGH");	
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			for (SAccumilator s : ALL) {
				trashold[s.index] = 0;
				hi[s.index] = 0;
			}
			try {
				JsonE j = new JsonE();
				for (SAccumilator s : ALL) {
					j.add(s.key, 0);
				}
				if (!PATHS.local().PROFILE.exists(filename))
					PATHS.local().PROFILE.create(filename);
				j.save(PATHS.local().PROFILE.get(filename));
			}catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}
	
	public void flush() {
		if (!GAME.achieving())
			return;
		
		try {
			JsonE j = new JsonE();
			for (SAccumilator s : ALL) {
				j.add(s.key, CLAMP.i(s.allTimeHigh(), 0, Integer.MAX_VALUE));
				j.add(s.key+"_HIGH", CLAMP.i(s.allTimeHigh(), 0, Integer.MAX_VALUE));
			}
			if (!PATHS.local().PROFILE.exists(filename))
				PATHS.local().PROFILE.create(filename);
			j.save(PATHS.local().PROFILE.get(filename));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Json getJson() {
		if (PATHS.local().PROFILE.exists(filename))
			return new Json(PATHS.local().PROFILE.get(filename));
		return null;
	}

	@Override
	protected void save(FilePutter file) {
		MAP.saver().save(hi, file);
		MAP.saver().save(trashold, file);
		MAP.saver().save(value, file);
		flush();
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		read();
		int[] hi = new int[ALL.size()];
		MAP.loader().load(hi, file, 0);
		MAP.loader().load(hi, file, 0);
		MAP.loader().load(value, file, 0);
		for (int i = 0; i < hi.length; i++) {
			this.hi[i] -= hi[i];
			if (this.hi[i] < 0)
				this.hi[i] = 0;
		}
	}

	@Override
	protected void update(double ds, Profiler prof) {
		TIME_PLAYED.set((int) (TIME.playedGame()/60.0));
	}

	public final class SAccumilator implements MAPPED{

		public final String key;
		private final boolean isBattle;
		private final int index;
		public final CharSequence name;
		
		SAccumilator(LISTE<SAccumilator> all, String key, boolean isBattle, CharSequence name){
			index = all.add(this);
			this.key = "COUNT_" + key;
			this.isBattle = isBattle;
			this.name = name;
			GVALUES.FACTION.push(this.key + "_GAME", name, UI.icons().s.pluses, new DOUBLE_O<Faction>() {
				
				@Override
				public double getD(Faction t) {
					return current();
				}
			}, false);
			GVALUES.FACTION.push(this.key + "_ALL_TIME", name + " (" + ¤¤allTime + ")", UI.icons().s.pluses, new DOUBLE_O<Faction>() {
				
				@Override
				public double getD(Faction t) {
					return allTimeHigh();
				}
			}, false);
		}
		

		
		public void inc(int delta) {
			if (isBattle || !VIEW.b().isActive()) {
				value[index] += delta;
				value[index] &= Integer.MAX_VALUE;
				hi[index] += delta;
				hi[index] &= Integer.MAX_VALUE;
			}
		}
		
		public void set(int a) {
			if (isBattle || !VIEW.b().isActive()) {
				value[index] = a;
				value[index] &= Integer.MAX_VALUE;
				if (value[index] > hi[index])
					hi[index] = value[index];
			}
		}

//
//		public int allTime() {
//			long l = old() + current();
//			if (l < 0)
//				return 0;
//			if (l > Integer.MAX_VALUE)
//				return Integer.MAX_VALUE;
//			return (int) l;
//		}
		
		public int allTimeHigh() {
			return Math.max(current(), hi());
		}

		public int current() {
			return value[index];
		}

//		void save(FilePutter file) {
//			file.i(value[index]);
//		}
//
//		void load(FileGetter file) throws IOException {
//			value[index] = file.i();
//		}

//		public int old() {
//			return old[index];
//		}
		
		public int hi() {
			return hi[index];
		}
		
//		public int value() {
//			return value[index];
//		}


		@Override
		public int index() {
			return index;
		}



		@Override
		public String key() {
			return key;
		}
		
	}
	
}
