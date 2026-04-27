package game.battle;

import java.io.IOException;
import java.util.Iterator;

import game.battle.div.Div;
import init.constant.Config;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LIST;

public class ArmyDivOrder implements LIST<Div>{

	private final Army army;
	private final int [] order = new int[Config.battle().DIVISIONS_PER_ARMY];
	private int ii = 0;
	
	
	ArmyDivOrder(Army army) {
		this.army = army;
		for (int i = 0; i < order.length; i++)
			order[i] = i;
	}
	
	@Override
	public Iterator<Div> iterator() {
		ii = 0;
		return iterer;
	}
	
	private final Iterator<Div> iterer = new Iterator<Div>() {

		@Override
		public boolean hasNext() {
			return ii < order.length-1;
		}

		@Override
		public Div next() {
			int i = order[ii];
			ii++;
			return army.divisions().get(i);
		}
	
	};
	
	public void swap(Div d1, Div d2) {
		if (d1.army() != army || d2.army() != army)
			throw new RuntimeException();
		
		int i1 = -1;
		int i2 = -1;
		for (int i : order) {
			if (i == d1.index())
				i1 = i;
			if (i == d2.index())
				i2 = i;
		}
		
		if (i1 == -1 || i2 == -1)
			throw new RuntimeException();
		
		int i = order[i1];
		order[i1] = order[i2];
		order[i2] = i;
		
	}
	
	public void shoveIn(Div div, Div before) {
		if (div.army() != army || before.army() != army)
			throw new RuntimeException();
		
		boolean f = false;
		for (int i = 0; i < order.length-1; i++) {
			f |= get(i) == div;
			if (f) {
				order[i] = order[i+1];
			}
		}
		
		for (int i = order.length-1; i > 0; i--) {
			order[i] = order[i-1];
			if (get(i-1) == before) {
				order[i-1] = div.index();
				break;
			}
			
		}
	}

	void save(FilePutter file) {
		file.is(order);
		
	}

	void load(FileGetter file) throws IOException {
		file.is(order);
		
	}

	void clear() {
		for (int i = 0; i < order.length; i++)
			order[i] = i;
	}

	@Override
	public Div get(int index) {
		return army.divisions().get(order[index]);
	}

	@Override
	public boolean contains(int i) {
		return true;
	}

	@Override
	public boolean contains(Div object) {
		return object.army() == army;
	}

	@Override
	public int size() {
		return order.length;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
	
	
}
