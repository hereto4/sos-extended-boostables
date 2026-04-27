package init.type;

import game.boosting.BOOSTABLE_O;
import game.boosting.BValue;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.race.RACES;
import init.race.Race;
import settlement.stats.Induvidual;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.keymap.MAPPED;
import util.keymap.RMAPS;

public final class POP_CL implements BOOSTABLE_O, MAPPED{

	public final int index;
	public final HCLASS cl;
	public final Race race;
	private final int fi;
	private final String key;
	
	POP_CL(int index, HCLASS cl, Race race) {
		this.index = index;
		this.cl = cl;
		this.race = race;
		fi = -1;
		key = (cl == null ? "NULL" : cl.key) + "_" + (race == null ? "NULL" : race.key);
	}
	
	POP_CL(int index, int fi) {
		this.index = index;
		this.cl = null;
		this.race = null;
		this.fi = fi;
		key = "FACTION_" + fi;
	}

	public FactionNPC f() {
		if (fi == -1)
			return null;
		return ((FactionNPC)FACTIONS.getByIndex(fi));
	}
	
	@Override
	public double boostableValue(BValue v) {
		return v.vGet(this);
	}
	
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String key() {
		return key;
	}
	
	@Override
	public String toString() {
		return "POP_CL : " + cl + " " + race;
	}
	
	public static POP_CL clP() {
		return  all.classes[0][0];
	}

	public static POP_CL clP(Induvidual i) {
		return clP(i.race(), i.clas());
	}
	
	public static POP_CL clP(Race race) {
		int ci = 0;
		int ri = race == null ? 0 : race.index+1;
		return  all.classes[ci][ri];
	}

	public static POP_CL clP(HCLASS clas) {
	
		int ci = clas == null ? 0 : clas.index()+1;
		int ri = 0;
		return  all.classes[ci][ri];
	}

	public static POP_CL clP(Race race, HCLASS clas) {
		
		
		int ci = clas == null ? 0 : clas.index()+1;
		int ri = race == null ? 0 : race.index+1;
		return  all.classes[ci][ri];
	}

	public static RMAPS<POP_CL> MAP(){
		return MAP;
	}
	
	public static LIST<POP_CL> ALL() {
		return all.all;
	}

	private static RClasses all;
	private static RMAPS<POP_CL> MAP;
	
	static void init(HCLASSES cl, RACES races) {
		all = new RClasses(RACES.all());
		ArrayListGrower<POP_CL> pps = new ArrayListGrower<POP_CL>();
		for (POP_CL p : all.all)
			if (p.cl != null && p.race != null)
				pps.add(p);;
		MAP = new RMAPS<>("POPCL", pps);
	}
	
	private final static class RClasses {
		
		private final POP_CL[][] classes;
		private final ArrayList<POP_CL> all;
		
		RClasses(LIST<Race> all) {
			
			this.all = new ArrayList<POP_CL>((all.size()+1)*(all.size()+1));
			classes = new POP_CL[HCLASSES.ALL().size()+1][all.size()+1];
			
			
			classes[0][0] = this.all.addReturn(new POP_CL(this.all.size(), null, null));
			for (Race r : all) {
				classes[0][r.index+1] = this.all.addReturn(new POP_CL(this.all.size(), null, r));
			}
			
			for (HCLASS cl : HCLASSES.ALL()) {
				classes[cl.index()+1][0] = this.all.addReturn(new POP_CL(this.all.size(), cl, null));
				for (Race r : all) {
					classes[cl.index()+1][r.index+1] = this.all.addReturn(new POP_CL(this.all.size(), cl, r));
				}
			}
		}
		
	}
	
}