package settlement.thing.halfEntity.caravan;


import game.GameDisposable;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.ArrayList;
import util.gui.misc.GBox;

abstract class Type {
	

	
	static final ArrayList<Type> all = new ArrayList<>(10);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				all.clear();
			}
		};
	}
	
	final int index;
	
	final CharSequence name;
	
	Type(CharSequence name){
		this.name = name;
		index = all.add(this);
	}
	
	final static Coo coo = new Coo();
	abstract boolean init(Caravan c, int amount);
	abstract boolean update(Caravan c, double ds);
	abstract void cancel(Caravan c, boolean dump);

	int carryCap(Caravan c) {
		return c.reservedGlobally;
	}
	
	public abstract void hoverInfo(GBox box, Caravan c);
}