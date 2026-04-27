package init.tech;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import init.paths.PATHS.ResFolder;
import init.tech.TECH.TechRequirement;
import init.tech.TechCurrency.TechCurrencies;
import snake2d.Errors;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.info.INFO;
import util.text.D;

public final class TECHS {

	private static LIST<TECH> ALL;
	private static LIST<TechCurrency> costs;
	public static CharSequence ¤¤name = "Technology";
	private static CharSequence ¤¤desc = "Technologies unlocks various boosts and rooms.";
	static {D.ts(TECHS.class);}
	private static final INFO info = new INFO(¤¤name, ¤¤desc);
	
	private static LIST<TechTree> trees;

	public static LIST<TechTree> TREES(){
		return trees;
	}
	
	public static LIST<TECH> ALL(){
		return ALL;
	}
	
	public static LIST<TechCurrency> COSTS(){
		return costs;
	}
	
	public static INFO INFO(){
		return info;
	}
	
	public TECHS() throws IOException{
		
		
		
		TechCurrencies cc = new TechCurrencies();

		
		final KeyMap<TECH> map = new KeyMap<>();
		LinkedList<TECH> all = new LinkedList<>();
		
		{
			
			//ArrayListGrower<TECH> all = new ArrayListGrower<>();
			
			ArrayListGrower<TechTree> trees = new ArrayListGrower<>();
			ResFolder data = new ResFolder("tech", false);
			
			for (String key : data.init.getFiles()) {

				Json dd = new Json(data.init.get(key));
				Json tt = new Json(data.text.get(key));
				trees.add(new TechTree(cc, key, dd, tt, all));
				
				
			}
			TECHS.trees = trees;
		}
		
		costs = cc.all;
		

		ALL = new ArrayList<>(all);
		
		for (TECH t : ALL) {
			if (map.containsKey(t.key)) {
				throw new Errors.DataError(t.key + " is more than once in the tree!");
			}
			map.put(t.key, t);
		}
		
		for (TECH tech : ALL) {
			
			Json j = tech.requiresTech;
			tech.requiresTech = null;
			LinkedList<TechRequirement> needs = new LinkedList<>();
			if (j.has("REQUIRES_TECH_LEVEL")) {
				Json jj = j.json("REQUIRES_TECH_LEVEL");
				for (String k : jj.keys()) {
					String kk = k;
					if (!map.containsKey(k)) {
						if (tech.tree != null)
							k = tech.tree.key + "_" + k;

						if (!map.containsKey(k)) {
							GAME.Warn(jj.errorGet(k, "REQUIRES_TECH_LEVEL"));
							continue;
						}
					}
					TechRequirement t = new TechRequirement(map.get(k), jj.i(kk, 0, map.get(k).levelMax));
					needs.add(t);
				}
			}
			tech.set(new ArrayList<>(needs));
			tech.prune(new ArrayList<>(needs));
		}
		
		detectCycles();
	
		{
			int[] reqed = new int[ALL.size()];
			
			for (int i = 0; i < ALL.size(); i++) {
				TECH t = ALL.get(i);
				Arrays.fill(reqed, 0);
				fillRequirements(reqed, t);
				
				LinkedList<TechRequirement> needs = new LinkedList<>();
				for (int ri = 0; ri < ALL.size(); ri++) {
					if (reqed[ri] > 0) {
						TechRequirement tt = new TechRequirement(ALL.get(ri), reqed[ri]-1);
						needs.add(tt);
					}
				}
				
				t.set(new ArrayList<>(needs));
			}
			
			
		}
		
		
	}
	
	private void fillRequirements(int[] reqed, TECH t) {
		
		for (int i = 0; i < t.requires().size(); i++) {
			TechRequirement r = t.requires().get(i);
			reqed[r.tech.index()] = Math.max(reqed[r.tech.index()], r.level+1);
			fillRequirements(reqed, r.tech);	
		}
	}
	

	
	private void detectCycles() {
		
		boolean[] checked = new boolean[ALL.size()];
		
		for (int i = 0; i < ALL.size(); i++) {
			if (ALL.get(i).requires().size() == 0)
				continue;
			Arrays.fill(checked, false);
			detectCycles(ALL.get(i), checked);
		}
		
	}
	
	private void detectCycles(TECH tech, boolean[] checked) {
		
		checked[tech.index()] = true;
		
		for (int i = 0; i < tech.requires().size(); i++) {
			TECH t = tech.requires().get(i).tech;
			if (checked[t.index()])
				throw new Errors.DataError("tech: " + t.key + " has a cyclic requirement", "");
		}
		
		for (int i = 0; i < tech.requires().size(); i++) {
			TECH t = tech.requires().get(i).tech;
			detectCycles(t, Arrays.copyOf(checked, checked.length));
		}
		
	}
	


	
	
}
