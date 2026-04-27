package init.type;

import java.util.Arrays;

import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.race.Race;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tree;
import util.keymap.RMAP;
import util.keymap.RMAPS;

public final class TRAITS {

	private static TRAITS s;
	private final RMAPS<TRAIT> map;
	private int[] iorder;
	private boolean[] idis;
	private final ArrayList<TRAIT> tmp;
	
	private Induvidual sortI;
	private final Tree<TRAIT> sort;
	
	TRAITS(){
		
		s = this;
		ResFolder f = PATHS.RACE().folder("trait");
		String[] keys = f.init.getFiles();
		Json[] js = new Json[keys.length];
		
		ArrayList<TRAIT> all = new ArrayList<>(keys.length);
		
		for (int i = 0; i < keys.length; i++) {
			js[i] = new Json(f.init.get(keys[i]));
			new TRAIT(all, keys[i], js[i], new Json(f.text.get(keys[i])));
		}
		
		map = new RMAPS<>("TRAIT", all);
		
		for (int i = 0; i < keys.length; i++) {
			final TRAIT tt = map.all().get(i);
			for (TRAIT o : map.readMany("DISABLES_OTHERS", js[i])) {
				if (o != tt)
					tt.disables.add(o);
			}
		}
		
		iorder = new int[map.all().size()];
		idis = new boolean[map.all().size()];
		tmp = new ArrayList<TRAIT>(map.all().size());
		sort = new Tree<TRAIT>(map.all().size()) {
			
			@Override
			protected boolean isGreaterThan(TRAIT current, TRAIT cmp) {
				return current.get(sortI) > cmp.get(sortI);
			}
		};
		
	}
	
	public static RMAP<TRAIT> MAP(){
		return s.map;
	}
	
	public static LIST<TRAIT> ALL(){
		return s.map.all();
	}
	
	public static void serRaceData(Race race, Json json) {
		s.map.new KJson(json) {
			
			@Override
			protected void process(TRAIT s, Json j, String key, boolean isWeak) {
				s.occRaces[race.index] = j.d(key, 0, 1);
			}
		};
	}
	
	public static void init(Induvidual in) {
		for (int i = 0; i < s.iorder.length; i++) {
			s.iorder[i] = i;
		}
		for (int i = 0; i < s.iorder.length; i++) {
			int o = s.iorder[i];
			int ii = RND.rInt(s.iorder.length);
			s.iorder[i] = s.iorder[ii];
			s.iorder[ii] = o;
		}
		Arrays.fill(s.idis, false);
		
		for (int i : s.iorder) {
			TRAIT t = s.map.all().get(i);
			if (s.idis[i] || RND.rFloat() > t.occRaces[in.race().index()]) {
				STATS.TRAITS().stat(t).setD(in, 0);
				continue;
			}
			double v = RND.rFloat();
		
			STATS.TRAITS().stat(t).setD(in, v);
			for (TRAIT o : t.disables)
				s.idis[o.index()] = true;
		}
		
		
	}
	
	public static LIST<TRAIT> tmp(Induvidual i, int max){
		s.sort.clear();
		s.sortI = i;
		for (TRAIT t : s.map.all()) {
			if (t.get(i) > 0)
				s.sort.add(t);
		}
		s.tmp.clearSloppy();
		while(s.sort.hasMore()) {
			s.tmp.add(s.sort.pollGreatest());
		}
		return s.tmp;
		
	}
}
