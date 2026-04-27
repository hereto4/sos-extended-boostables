package init.type;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.keymap.RMAP;

public class DISEASES {

	private final LIST<DISEASE> all;
	private static DISEASES s;
	private final RMAP<DISEASE> map;
	private final double regularDays;
	
	DISEASES() {
		s = this;
		PATH pd = PATHS.INIT().getFolder("disease");
		PATH ps = PATHS.TEXT().getFolder("disease");
		regularDays = new Json(pd.get("_CONFIG")).d("REGULAR_SICKNESS_DAY_INVERVAL", 1, 10000000);
		LinkedList<DISEASE> all = new LinkedList<>();
		
		for (String k : pd.getFiles(1, 120)) {
			new DISEASE(all, k, new Json(pd.get(k)), new Json(ps.get(k)));
		}
		
		this.all = new ArrayList<>(all);
		
		map = new RMAP<>("DISEASE", all);
		
		
	}
	
	public static LIST<DISEASE> all(){
		return s.all;
	}
	
	public static RMAP<DISEASE> map(){
		return s.map;
	}
	
	public static double regularDays() {
		return s.regularDays;
	}
	
	public static DISEASE randomEpidemic(double ran) {
		double lim = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			if (!dd.epidemic)
				continue;
			lim += dd.occurence();
		}
		lim *= ran;
		double d = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			if (!dd.epidemic)
				continue;
			d += dd.occurence();
			if (d >= lim)
				return dd;
		}
		return s.all.get(s.all.size()-1);
	}
	
	public static DISEASE randomRegular() {
		double lim = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			if (!dd.regular)
				continue;
			lim += dd.occurence();
		}
		lim *= RND.rFloat();
		double d = 0;
		for (int i = 0; i < s.all.size(); i++) {
			DISEASE dd = s.all.get(i);
			if (!dd.regular)
				continue;
			d += dd.occurence();
			if (d >= lim)
				return dd;
		}
		return s.all.get(s.all.size()-1);
	}
	
}
