package world.region;

import java.io.IOException;
import java.util.Iterator;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayListShortResize;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;

public final class Realm{

	final ArrayListShortResize regions = new ArrayListShortResize(32, WREGIONS.MAX);
	private final List list = new List();
	short capitolI = -1;
	private final short index;
	
	double ferArea = 0;
	
	Realm(int index){
		this.index = (short) index;
	}

	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			regions.save(file);
			file.s(capitolI);
			file.d(ferArea);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			regions.load(file);
			capitolI = file.s();
			ferArea = file.d();
		}

		@Override
		public void clear() {
			regions.clear();
			capitolI = -1;
			ferArea = 0;
		}
	};
	
	public double ferArea() {
		return ferArea;
	}
	
	public LIST<Region> all(){
		return list;
	}
	
	public int regions() {
		return regions.size();
	}
	
	public Region region(int i) {
		return WORLD.REGIONS().all().get(regions.get(i));
	}
	
	public Region capitol() {
		if (capitolI == -1)
			return null;
		return WORLD.REGIONS().getByIndex(capitolI);
	}
	
	public Faction faction() {
		return FACTIONS.getByIndex(index);
	}
	
	private class List implements LIST<Region>, Iterator<Region>{

		private int ii;
		
		@Override
		public Iterator<Region> iterator() {
			ii = 0;
			return this;
		}

		@Override
		public Region get(int index) {
			return WORLD.REGIONS().getByIndex(regions.get(index));
		}

		@Override
		public boolean contains(int i) {
			return i >= 0 && i < regions.size();
		}

		@Override
		public boolean contains(Region object) {
			for (int i = 0; i < regions.size(); i++) {
				if (get(i) == object)
					return true;
			}
			return false;
		}

		@Override
		public int size() {
			return regions.size();
		}

		@Override
		public boolean isEmpty() {
			return regions.isEmpty();
		}

		@Override
		public boolean hasNext() {
			return ii < regions.size();
		}

		@Override
		public Region next() {
			Region r = get(ii);
			ii++;
			return r;
		}
		
	}



	
}