package settlement.stats.law;

import init.paths.PATHS;
import init.race.Race;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;

public class PRISONER_TYPE implements INDEXED{

	public static final PRISONER_TYPE WAR;
	public static final CRIME THEFT;
	public static final CRIME MURDER;
	public static final CRIME VANDALISM;
	public static final CRIME FLASHING;
	public static final CRIME DISRESPECT;
	public static final PRISONER_TYPE PLEASURE;
	public final static LIST<PRISONER_TYPE> ALL;
	public final static LIST<CRIME> CRIMES;
	static {
		LinkedList<PRISONER_TYPE> all = new LinkedList<>();
		LinkedList<CRIME> crime = new LinkedList<>();
		
		
		WAR = new PRISONER_TYPE(all, "WAR", false);
		
		FLASHING = new CRIME(all, crime, "FLASHING", 0.2);
		THEFT = new CRIME(all, crime, "THEFT", 1.0);
		VANDALISM = new CRIME(all, crime, "VANDALISM", 1.0);
		MURDER = new CRIME(all, crime, "MURDER", 0.05);
		DISRESPECT = new CRIME(all, crime, "DISRESPECT", 0.02);
		PLEASURE = new PRISONER_TYPE(all, "PLEASURE", false);
		ALL = new ArrayList<>(all);
		CRIMES = crime;
		
		double t = 0;
		for (CRIME c : CRIMES) {
			t += c.occurence;
		}
		for (CRIME c : CRIMES) {
			c.occurence/= t;
		}
		
		
	}
	
	public static CRIME RND(Race race) {
		double f = RND.rFloat();
		for (int i = 0; i < CRIMES.size();i++) {
			f -= CRIMES.get(i).occurence;
			if (f <= 0)
				return CRIMES.get(i);
		}
		return CRIMES.get(0);
	}
	
	
	public CharSequence name,names,title,titles;
	private final int index;
	public final String key;
	public boolean isJudged;
	
	PRISONER_TYPE(LISTE<PRISONER_TYPE> all, String key, boolean isJudged){
		this.index = all.add(this);
		
		this.key = key;
		this.isJudged = isJudged;
	}

	static void init() {
		
		Json json = new Json(PATHS.TEXT_MISC().get("Law")).json("CRIMES");
		for (PRISONER_TYPE t : ALL) {
			Json j = json.json(t.key);
			t.name = j.text("NAME");
			t.names = j.text("NAMES");
			t.title = j.text("TITLE");
			t.titles = j.text("TITLES");
		}
	}
	
	@Override
	public int index() {
		return index;
	}
	
	public static class CRIME extends PRISONER_TYPE {
		public final int crimeI;
		private double occurence;
		CRIME(LISTE<PRISONER_TYPE> all, LISTE<CRIME> crime, String key,
				double occurence) {
			super(all, key, true);
			crimeI = crime.add(this);
			this.occurence = occurence;
		}
		
		public double rarty() {
			return occurence;
		}
		
	}
	
}
