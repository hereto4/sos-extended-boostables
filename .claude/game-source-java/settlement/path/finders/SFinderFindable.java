package settlement.path.finders;

import game.GameDisposable;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.path.components.FindableDataSingle;
import settlement.path.components.SComponent;
import settlement.path.path.SPath;
import settlement.thing.ThingFindable;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public abstract class SFinderFindable implements SFINDER{

	public static ArrayList<SFinderFindable> all = new ArrayList<SFinderFindable>(256);
	static{new GameDisposable() {
		@Override
		protected void dispose() {
			all.clear();
		}
	};}

	public final short index = (short) (all.add(this));
	protected FINDABLE result;
	private double distance;
	public final CharSequence name;
	public final SFinderFindableMap map = new SFinderFindableMap();
	
	public SFinderFindable(CharSequence name) {
		this.name = name;
	}

	public double getDistance() {
		return distance;
	}
	
	public static LIST<SFinderFindable> all(){
		return all;
	}
	
	public static SFinderFindable get(short index) {
		return all.get(index & 0x0FF);
	}
	
	@Override
	public boolean isInComponent(SComponent c, double distance) {
		if (fin().get(c) > 0) {
			this.distance = distance;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isTile(int tx, int ty, int tileNr) {
		result = getReservable(tx, ty);
		if (result != null) {
			return true;
		}
		return false;
	}
	
	public final boolean has(int sx, int sy) {
		return fin().has(sx, sy);
	}
	
	public final boolean has(COORDINATE c) {
		return fin().has(c.x(), c.y());
	}
	
	private FindableDataSingle fin() {
		return SETT.PATH().comps.data.SINGLES.get(index);
	}
	
	public abstract FINDABLE getReservable(int tx, int ty);
	public abstract FINDABLE getReserved(int tx, int ty);
	
	/**
	 * Finds and reserves and sets path, returns false if nothing was found.
	 * @param start
	 * @param r
	 * @param path
	 * @return true and a set path if found
	 */
	public boolean reserve(COORDINATE start, SPath path, int maxdistance) {
		
		if (path.request(start.x(), start.y(), this, maxdistance)) {
			result.findableReserve();
			map.report(start, true);
			return true;
		}
		map.report(start, false);
		return false;
	}
	
	/**
	 * Finds and reserves, returns null if nothing was found.
	 * @param start
	 * @param maxdistance
	 * @return
	 */
	public COORDINATE reserve(COORDINATE start, int maxdistance) {
		if (SETT.PATH().finders.finder().findDest(start.x(), start.y(), this, maxdistance) != null) {
			result.findableReserve();
			map.report(start, true);
			return result;
		}
		map.report(start, false);
		return null;
	}
	
	public COORDINATE reserve(int sx, int sy, int maxdistance) {
		if (has(sx, sy) && SETT.PATH().finders.finder().findDest(sx, sy, this, maxdistance) != null) {
			
			result.findableReserve();
			map.report(sx, sy, true);
			return result;
		}
		map.report(sx, sy, false);
		return null;
	}
	
	public final void report(FINDABLE coo, int delta) {
		report(coo.x(), coo.y(), delta);
	}
	
	public final void report(int x, int y, int delta) {
		if (delta == 1)
			fin().reportPresence(x, y);
		else if(delta == -1)
			fin().reportAbsence(x, y);
		else
			throw new RuntimeException("" + delta);
	}
	
	public static abstract class FinderThing<T extends ThingFindable> extends SFinderFindable{

		FinderThing(String name) {
			super(name);
		}
		@Override
		public abstract T getReservable(int tx, int ty);
		@Override
		public abstract T getReserved(int tx, int ty);
		
		@SuppressWarnings("unchecked")
		public T getResult() {
			return (T) result;
		}
	}

}
