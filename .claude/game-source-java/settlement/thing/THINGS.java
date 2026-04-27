package settlement.thing;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.THINGS;
import static settlement.main.SETT.TILE_BOUNDS;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;
import java.io.ObjectStreamException;

import game.debug.Profiler;
import game.time.TIME;
import init.constant.C;
import init.resources.RBIT.RBITImp;
import settlement.entity.ESpeed;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.Renderer;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.RECTANGLEE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.ShortStack;
import util.rendering.ShadowBatch;
import util.statistics.HistoryInt;
import view.sett.SETT_HOVERABLE;

public class THINGS extends SETT.SettResource{

	private final ArrayList<ThingFactory<?>> all = new ArrayList<>(16);
	private final Thing[][] grid = new Thing[THEIGHT][TWIDTH];
	private final ArrayList<Thing> tmp = new ArrayList<Thing>(16000);
	private final static int MAX_OUT_TILES = 2; 
	
	public final Sprites sprites = new Sprites();
	public final ThingsGore gore = new ThingsGore(all, 1.0f, sprites);
	public final ThingsResources resources = new ThingsResources(all);
	public final ThingsCorpses corpses = new ThingsCorpses(all);
	public final ThingsCadavers cadavers = new ThingsCadavers(all);
	public final ThingsRubbish rubbish = new ThingsRubbish(all);
	
	public THINGS() throws IOException{
		super("THINGS", true);
		ThingPlacer.init();
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		if (ds > 0)
			for (ThingFactory<?> t : all)
				t.update(ds);	
	}
	

	@Override
	protected void clear() {
		for (COORDINATE c : TILE_BOUNDS)
			grid[c.y()][c.x()] = null;
	
		for (ThingFactory<?> t : all)
			t.clear();
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		for (ThingFactory<?> t : all)
			t.save(saveFile);
	}
	
	@Override
	protected void load(FileGetter saveFile) throws IOException {
		for (ThingFactory<?> t : all)
			t.load(saveFile);
		
		super.load(saveFile);
		
//		for (COORDINATE c : Settlement.TILE_BOUNDS) {
//			if (Settlement.TILE_BOUNDS.isOnEdge(c.x(), c.y())) {
//				outer: while(true) {
//					for (Thing t : get(c.x(), c.y())) {
//						if (t instanceof ScatteredResource) {
//							((ScatteredResource) t).removeUnreserved(((ScatteredResource) t).amount());
//							continue outer;
//						}
//					}
//					break;
//				}
//			}
//		}
		
	}

	

	
	
	private void addTile(int tx, int ty) {
		if (!tmp.hasRoom())
			return;
		if (!IN_BOUNDS(tx, ty))
			return;
		Thing t = grid[ty][tx];
		if (t == null)
			return;
		
		while (t != null && tmp.hasRoom()) {
			tmp.add(t);
			t = t.next;
		}
		
		
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return all things that have their centre in this tile.
	 */
	public LISTE<Thing> get(int tx, int ty) {
		tmp.clear();
		addTile(tx, ty);
		return tmp;
	}
	
	public SETT_HOVERABLE getArroundCoo(int x, int y) {
		int tx1 = (x >> C.T_SCROLL) -1;
		int tx2 = (x >> C.T_SCROLL) + 1;
		int ty1 = (y >> C.T_SCROLL) -1;
		int ty2 = (y >> C.T_SCROLL) + 1;
		double maxDistance = 100000;
		SETT_HOVERABLE h = null;
		for (Thing t : get(tx1, tx2, ty1, ty2)) {
			if (!(t instanceof SETT_HOVERABLE))
				continue;
			if (!t.body().holdsPoint(x, y))
				continue;
			double d = COORDINATE.properDistance(x, y, t.body().cX(), t.body().cY());
			if (d < maxDistance) {
				maxDistance = d;
				h = (SETT_HOVERABLE) t;
			}
			
		}
		return h;
	}
	
	
	public Thing getFirst(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return grid[ty][tx];
		return null;
	}
	
	/**
	 * area version of get
	 * @param tx1
	 * @param tx2
	 * @param ty1
	 * @param ty2
	 * @return
	 */
	public LISTE<Thing> get(int tx1, int tx2, int ty1, int ty2){
		tmp.clear();
		for (int y = ty1; y < ty2; y++) {
			for (int x = tx1; x < tx2; x++) {
				addTile(x, y);
			}
		}
		return tmp;
	}

	/**
	 *  * area version of get
	 * @param tiles
	 * @return
	 */
	public LISTE<Thing> get(RECTANGLE tiles){
		return get(tiles.x1(), tiles.x2(), tiles.y1(), tiles.y2());
	}
	
	
	public void render(Renderer r, ShadowBatch shadowBatch, float ds, RECTANGLE renWin, int offX, int offY) {

		int tx1 = (renWin.x1() >> C.T_SCROLL) - MAX_OUT_TILES;
		int tx2 = (renWin.x2() >> C.T_SCROLL) + MAX_OUT_TILES;
		int ty1 = (renWin.y1() >> C.T_SCROLL) - MAX_OUT_TILES;
		int ty2 = (renWin.y2() >> C.T_SCROLL) + MAX_OUT_TILES;
		tmp.clear();
		get(tx1, tx2, ty1, ty2);
		
		int offXs = (int) (offX - renWin.x1());
		int offYs = (int) (offY - renWin.y1());
		
		for (int i = 0; i < tmp.size(); i++){
			tmp.get(i).render(r, shadowBatch, ds, offXs, offYs);
		}
		
	}
	
	public void renderZoomed(Renderer r, RECTANGLE renWin, int offX, int offY) {
		
		resources.renderZoomed(r, renWin, offX, offY);
		
		
	}

	public static abstract class ThingFactory<T extends Thing> implements INDEXED{

		private final int index;
		private short firstAdded = -1;
		private short lastAdded = -1;
		private ShortStack free;
		public final HistoryInt addedHistory = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
		
		ThingFactory(LISTE<ThingFactory<?>> all, int size) {
			index = all.add(this);
			free = new ShortStack(size);
			for (int i = size-1; i >= 0; i--)
				free.push((short)i);
		}
		
		@Override
		public int index() {
			return index;
		}
		
		protected abstract T[] all();
		
		protected void clear() {
			firstAdded = -1;
			lastAdded = -1;
			addedHistory.clear();
			free.clear();
			for (int i = free.capacity()-1; i >= 0; i--)
				free.push((short)i);
			for (T t : all()) {
				t.clear();
			}
		}
		
		protected void save(FilePutter file) {
			file.mark(this);
			
			
			int am = 0;
			short f = firstAdded;
			while (f != -1) {
				f = all()[f].addedNext;
				am++;
			}
			
			file.i(am);
			f = firstAdded;
			while (f != -1) {
				file.s(f);
				all()[f].save(file);
				all()[f].saveP(file);
				f = all()[f].addedNext;
			}
			addedHistory.save(file);
		}
		
		protected void load(FileGetter file) throws IOException {
			file.check(this);
			free.clear();
			
			firstAdded = -1;
			lastAdded = -1;
			int am = file.i();
			for (int i = 0; i < am; i++) {
				T t = all()[file.s()];
				t.load(file);
				free.push(t.index());
				t.loadP(file);
			}
			if (all().length != free.capacity())
				throw new RuntimeException();
			
			for (T t : all()) {
				if (t == null || t.isRemoved()) {
					free.push(t.index());
				}
			}
			
			addedHistory.load(file);
			addedHistory.set(added());
		}
		
		public final MAP_OBJECT<T> tGet = new MAP_OBJECT<T>() {

			@Override
			public T get(int tile) {
				throw new RuntimeException();
			}

			@SuppressWarnings("unchecked")
			@Override
			public T get(int tx, int ty) {
				if (!IN_BOUNDS(tx, ty))
					return null;
				Thing t = THINGS().grid[ty][tx];
				if (t == null)
					return null;
				
				while (t != null) {
					if (t.factory() == ThingFactory.this)
						return (T) t;
					t = t.next;
				}
				return null;
			}
		};

		void update(double ds) {
			
		}
		
		private final void remove(Thing res) {
			
			short next = res.addedNext;
			short prev = res.addedPrev;
			
			if (next != -1) {
				all()[next].addedPrev = prev;
			}
			
			if (prev != -1) {
				all()[prev].addedNext = next;
			}
			
			if (res.index() == firstAdded) {
				firstAdded = next;
			}
			
			if (res.index() == lastAdded) {
				lastAdded = prev;
			}
			
			
			res.addedPrev = -1;
			res.addedNext = -1;
			free.push(res.index());
			addedHistory.set(added());
			
		}
		
		private final void add(Thing res) {
			
			if (res.addedNext != -1)
				throw new RuntimeException();
			if (res.addedPrev != -1)
				throw new RuntimeException(); 
			
			int i = free.pop();
			addedHistory.set(added());
			if (i != res.index())
				throw new RuntimeException(i + " " + res.index());
			
			if (firstAdded == -1) {
				firstAdded = res.index();
				lastAdded = res.index();
				return;
			}
				
			all()[lastAdded].addedNext = res.index();
			res.addedPrev = lastAdded;
			lastAdded = res.index();
			
			
			
		}
		
//		protected void checkForLoop() {
//			T t = first();
//			if (t == null)
//				return;
//			T o = null;
//			while(t != null) {
//				
//				if (o == null)
//					o = t;
//				else if (o == t)
//					throw new RuntimeException(""+added());
//				t = next(t);
//			}
//		}
		
		T nextInLine() {
			if (free.isEmpty()) {
				all()[firstAdded].remove();
			}
			short i = free.pop();
			free.push(i);
			T res = all()[i];
			
			if (!res.isRemoved())
				throw new RuntimeException(free.size() + " " + i);
			
			return res;
		}
		
		public int added() {
			return all().length - free.size();
		}
		
		public int remainingToAdd() {
			return free.size();
		}
		
		final T first() {
			if (firstAdded >= 0)
				return all()[firstAdded];
			return null;
		}
		
		final T next(T t) {
			if (t.addedNext != -1)
				return all()[t.addedNext];
			return null;
		}
		
	}
	
	
	public static abstract class Thing implements BODY_HOLDER{

		private short ix = -1;
		private short iy = -1;
		short addedNext = -1;
		short addedPrev = -1;
		private final short index; 
		private Thing next;
		private Thing prev;
		protected final RBITImp resourcemask = new RBITImp();
		
		/**
		 * renders this entity at its current position with the adjustment of the offsets.
		 * @param ds seconds passed
		 * @param offsetX
		 * @param offsetY
		 */
		public abstract void render(Renderer r, ShadowBatch shadows, float ds, int offsetX, int offsetY);
		
		Thing(int index){
			this.index = (short) index;
		}
		
		public final boolean isRemoved(){
			return ix == -1;
		}
		
		public final void remove() {
			if (ix == -1)
				throw new RuntimeException();
			THINGS m = THINGS();
			resourcemask.clear();
			if (next != null) {
				next.prev = prev;
			}
			if (prev != null) {
				prev.next = next;
			}
			
			if (m.grid[iy][ix] == this) {
				m.grid[iy][ix] = next;
			}
			
			next = null;
			prev = null;
			ix = -1;
			factory().remove(this);
			removeAction();
		}
		
		Object readResolve() throws ObjectStreamException {
			if (!isRemoved())
		    	addColdAsHell();
			return this;
		}
		
		protected void addAction() {
			
		}
		
		protected void removeAction() {
			
		}
		
		final void addColdAsHell() {
			
			THINGS m = THINGS();
			next = null;
			if (m.grid[iy][ix] == null) {
				m.grid[iy][ix] = this;
				return;
			}
			
			resourcemask.or(m.grid[iy][ix].resourcemask);
			
			if (m.grid[iy][ix].z() >= z()) {
				m.grid[iy][ix].prev = this;
				next = m.grid[iy][ix];
				m.grid[iy][ix] = this;
				return;
			}
			
			Thing parent = m.grid[iy][ix];
			while(parent.next != null && parent.next.z() < z())
				parent = parent.next;
			
			if (parent.next != null) {
				parent.next.prev = this;
				next = parent.next;
			}
			
			parent.next = this;
			prev = parent;
			
		}
		
		
		
		protected final void add(){
			
			if (ix != -1)
				throw new RuntimeException();
			ix = (short) ctx();
			iy = (short) cty();
			if (!TILE_BOUNDS.holdsPoint(ix, iy)) {
				ix = -1;
				return;
			}
			
			addColdAsHell();
			if (factory() != null)
				factory().add(this);
			addAction();
			
		}
		
		protected void move(ESpeed speed, double ds, float restituion, RECTANGLEE body, boolean tileCollide) {
			
			body.incrX(speed.x()*ds);
			body.incrY(speed.y()*ds);
			
			if (ix != ctx() || iy != cty()) {
				if (!isRemoved())
					remove();
				add();
			}
			
		}
		
		protected void move() {
			if (ix != ctx() || iy != cty()) {
				remove();
				add();
			}
			
		}
		
		public int ctx() {
			return body().cX() >> C.T_SCROLL;
		}
		
		public int cty() {
			return body().cY() >> C.T_SCROLL;
		}
		
		protected abstract int z();
		
		final void saveP(FilePutter f) {
			f.bool(!isRemoved());
			resourcemask.save(f);
		}
		
		final void loadP(FileGetter f) throws IOException {
			clear();

			if (f.bool()) {
				resourcemask.load(f);
				ix = (short) ctx();
				iy = (short) cty();
				addColdAsHell();
				factory().add(this);
			}
		}
		
		final void clear() {
			addedNext = -1;
			addedPrev = -1;
			next = null;
			prev = null;
			ix = -1;
			iy = -1;
		}
		
		protected abstract void save(FilePutter f);
		
		protected abstract void load(FileGetter f) throws IOException;
		
		public short index() {
			return index;
		}
		
		public abstract ThingFactory<?> factory();
		
		public Thing tileNext() {
			return next;
		}
		
	}
	
}
