package game.battle;

import java.io.IOException;

import game.GAME;
import game.battle.div.Div;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.constant.Config;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class Army {
	
	private final int index;
	private final LIST<Div> divisions;
	private final ArrayList<Div> ordered;
	public final Cache men = new Cache() {
		
		@Override
		protected int count() {
			int a = 0;
			for (int di = 0; di < divisions.size(); di++) {
				Div d = divisions.get(di);
				a += d.menNrOf();
			}
			return a;
		}
	};
	private final int menMax;
	
	public final int bit;
	
	Army(ArrayList<Army> armies, ArrayList<Div> divisions){
		this.index = armies.add(this);
		
		ArrayList<Div> divs = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
		
		
		for (int i = 0; i < Config.battle().DIVISIONS_PER_ARMY; i++)
			new Div(divisions, divs, this);
		this.divisions = divs;
		ordered = new ArrayList<Div>(divs);
		
		menMax = Config.battle().MEN_PER_DIVISION*Config.battle().DIVISIONS_PER_ARMY;
		bit = 1 << index;
		
	}
	
	public int index() {
		return index;
	}
	
	public LIST<Div> divisions(){
		return divisions;
	}
	
	public LIST<Div> ordered(){
		return ordered;
	}
	
	public void setDivAtOrderedIndex(Div toBeReplaced, Div replacer) {
		if (ordered.removeOrdered(replacer) == -1)
			throw new RuntimeException();
		int oi = ordered.indexOf(toBeReplaced);
		if (oi < 0)
			throw new RuntimeException();
		ordered.insert(oi, replacer);
	}
	
	public Div getNextEmptyOrdered() {
		for (int di = 0; di < ordered.size(); di++) {
			Div d = ordered.get(di);
			if (d.info.men() == 0 && d.menNrOf() == 0) {
				return d;
			}
		}
		return null;
	}
	
	public int men() {
		return men.i();
	}
	
	public int menMax() {
		return menMax;
	}
	
	public Army enemy() {
		return GAME.ARMIES().armies().get((index+1)%2);
	}
	
	public double morale() {
		return GAME.ARMIES().factors.morale(this);
	}
	
	public Faction faction() {
		if (this == GAME.ARMIES().player())
			return FACTIONS.player();
		return FACTIONS.otherFaction();
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (Div d : ordered) {
				file.i(d.indexArmy());
			}
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
		
			men.recount();
			
			ordered.clearSloppy();
			for (int i = 0; i < divisions.size(); i++) {
				ordered.add(divisions.get(file.i()));
			}
		}
		
		@Override
		public void clear() {
			men.recount();
			ordered.clearSloppy();
			ordered.add(divisions);
		}
	};
	
	public boolean defender() {
		return true;
	}
	
	public static abstract class Cache {
		
		private boolean dirty = true;
		private int i = 0;
		
		public int i() {
			if (dirty) {
				i = count();
				dirty = false;
			}
			return i;
		}
		
		public void recount() {
			dirty = true;
		}
		
		protected abstract int count();
		
		
		
	}
	
	public boolean player() {
		return GAME.ARMIES().player() == this;
	}
	
}
