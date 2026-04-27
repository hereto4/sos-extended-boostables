package world.army;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayListShort;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.entity.army.WArmy;

public final class ADArmies {

	final ArrayListShort armies;
	private final int factionI;
	private final List list = new List();
	final long[] data;
	
	ADArmies(int factionI, int max) {
		this.factionI = factionI;
		armies = new ArrayListShort(max);
		data = new long[AD.iinit().dataT.longCount()];
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save (FilePutter f) {
			armies.save(f);
			AD.iinit().dataT.saver().save(faction(), f);
		}
		
		@Override	
		public void load (FileGetter f) throws IOException {
			armies.load(f);
			AD.iinit().dataT.loader().load(faction(), f);
		}
		
		@Override	
		public void clear(){
			armies.clear();
			Arrays.fill(data, 0);
		}
	};
	
	public Faction faction() {
		if (factionI == -1)
			return null;
		return FACTIONS.getByIndex(factionI);
	}
	
	public LIST<WArmy> all(){
		return list;
	}
	
	public boolean canCreate() {
		return WORLD.ENTITIES().armies.canCreate() && armies.hasRoom();
	}

	
	public void disbandAll() {
		while (all().size() > 0) {
			all().get(0).disband();
		}
	}
	
	private final class List implements LIST<WArmy>, Iterator<WArmy>{


		private int ii;
		
		@Override
		public Iterator<WArmy> iterator() {
			ii = 0;
			return this;
		}

		@Override
		public WArmy get(int index) {
			WArmy a = WORLD.ENTITIES().armies.get(armies.get(index));
			if (a == null)
				throw new RuntimeException(index + " " + armies.get(index) + " " + faction());
			return a;
		}

		@Override
		public boolean contains(int i) {
			return i >= 0 && i < armies.size();
		}

		@Override
		public boolean contains(WArmy object) {
			for (int i = 0; i < armies.size(); i++) {
				if (get(i) == object)
					return true;
			}
			return false;
		}

		@Override
		public int size() {
			return armies.size();
		}

		@Override
		public boolean isEmpty() {
			return armies.isEmpty();
		}

		@Override
		public boolean hasNext() {
			return ii < armies.size();
		}

		@Override
		public WArmy next() {
			WArmy r = get(ii);
			ii++;
			return r;
		}
		
	}


	
}
