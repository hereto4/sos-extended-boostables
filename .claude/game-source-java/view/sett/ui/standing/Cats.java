package view.sett.ui.standing;

import game.GAME;
import init.race.RACES;
import init.race.Race;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LinkedList;
import util.gui.table.GScrollRows;
import view.interrupter.ISidePanel;

final class Cats {

	public final ArrayList<Cat> all = new ArrayList<>(STATS.COLLECTIONS().size());
	
	private ISidePanel[] panels = new ISidePanel[STATS.COLLECTIONS().size()];
	final Cat access;
	
	private final HCLASS cl;
	
	Cats(HCLASS cl){
		
		this.cl = cl;
		access = new CatAccess(cl);
		add(new CatPopulation(cl));
		add(access);
		add(new CatServices(cl));
		add(new CatEnv(cl));
		add(new CatReligion(cl));
		add(new CatOccupation(cl));
		add(new CatGovern(cl));
		for (StatCollection c : STATS.COLLECTIONS()) {
			if (panels[c.index()] == null && hasStanding(c))
				add(new CatDummy(cl, c));
		}
		
	}
	
	private void add(Cat p) {
		
		all.add(p);
		for (StatCollection c : p.cs)
			panels[c.index()] = p;
	}
	
	private int updateI = -1;
	private double biggest = 0;
	
	double getBiggest() {
		if (updateI == GAME.updateI())
			return biggest;
		
		updateI = GAME.updateI();
		biggest = 0;
		
		for (Cat ca : all) {
			double m = 0;
			for (StatCollection c : ca.cs) {
				for (STAT s : c.all()) {
					m += s.standing().max(cl, CitizenMain.current);
				}
			}
			if (m > biggest)
				biggest = m;
		}
		return biggest;
		
	}
	
	private boolean hasStanding(StatCollection c) {
		for (STAT s : c.all()) {
			for (HCLASS cl : HCLASSES.ALL()) {
				for (Race r : RACES.all()) {
					if (s.standing().max(cl, r) > 0) {
						return true;
					}
				}
			}
			
		}
		return false;
	}
	
	static class Cat extends ISidePanel {
		
		final StatCollection[] cs;
		
		Cat(StatCollection[] cs){
			this.cs = cs;
			titleSet(cs[0].info.name);	
		}

	}
	
	private static class CatDummy extends Cat {
		
		CatDummy(HCLASS cl, StatCollection... cs){
			super(cs);
			titleSet(cs[0].info.name);
			
				
			LinkedList<RENDEROBJ> rens = new LinkedList<>();

			for (StatCollection c : cs) {
				rens.add(new StatRow.Title(c.info));
				for (STAT s : c.all()) {
					rens.add(new StatRow(s, cl));
					
				}
			}
			
			section.add(new GScrollRows(rens, HEIGHT, 0).view());
			
		}

	}
	
	


}
