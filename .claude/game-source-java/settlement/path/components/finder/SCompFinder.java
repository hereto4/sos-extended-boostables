package settlement.path.components.finder;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;

import game.GAME;
import settlement.main.SETT;
import settlement.path.components.SCOMPONENTS;
import settlement.path.components.SComp0Level;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentChecker;
import settlement.path.components.SComponentEdge;
import settlement.path.components.SComponentLevel;
import settlement.room.main.RoomInstance;
import snake2d.PathTile;
import snake2d.PathUtilOnline;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.ArrayCooShort;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;
import util.data.BOOLEANO;

public final class SCompFinder {

	private final SCOMPONENTS comps;
	private final SComponentChecker checker;
	private final SComponentChecker[] checkers;
	private final BOOLEANO<SComponent> checkerDummy = new BOOLEANO<SComponent>() {

		@Override
		public boolean is(SComponent t) {
			return true;
		}
		
	};
	
	private final SCompReal res = new SCompReal();
	private final SCompDummy dummy = new SCompDummy();
	private final SCompPatherFinder fDummy = new SCompPatherFinder() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return true;
		}
	};
	private final PathUtilOnline p;
	
	public SCompFinder(PathUtilOnline p){
		this(SETT.PATH().comps, p);
	}
	
	public SCompFinder(SCOMPONENTS comps, PathUtilOnline p){
		this.comps = comps;
		this.checker = new SComponentChecker(comps.zero);
		this.checkers = new SComponentChecker[] {
			checker,
			new SComponentChecker(comps.zero)
		};
		this.p = p;
	}

	private ArrayCooShort coosTmp = new ArrayCooShort(2);
	public SCompPath fill(int startX, int startY, SCompPatherFinder fi, int maxDistance) {
		
		coosTmp.set(0).set(startX, startY);
		coosTmp.inc();
		return fill(coosTmp, fi, maxDistance);
	}
	
	/**
	 * Will return all components within the distance that the {@link SCompPatherFinder} isInComponent answers true
	 * will also set these components to true.
	 * @param startX
	 * @param startY
	 * @param f
	 * @param maxDistance
	 * @return
	 */
	public SCompPath fill(ArrayCooShort coos, SCompPatherFinder fi, int maxDistance) {
		
		res.path.clearSoft();
		res.distance = maxDistance;
		res.checker = checker;
		checker.init();
		
		SComponentLevel l = comps.zero;
		checker.init();
		Flooder f = p.getFlooder();
		
		f.init(this);
		
		{
			int am = 0;
			int k = coos.getI();
			
			for (int i = 0; i < k; i++) {
				coos.set(i);
				SComponent ss = l.get(coos.get().x(), coos.get().y());
				if (ss == null) {
					for (int di = 0; di < DIR.ORTHO.size(); di++) {
						ss = l.get(coos.get().x(), coos.get().y(), DIR.ORTHO.get(di));
						if (ss != null)
							break;
					}
				}
				if (ss != null && !checker.isSetAndSet(ss)){
					am++;
					f.pushSloppy(ss.centreX(), ss.centreY(), 0);
				}
			}
		
			if (am == 0) {
				f.done();
				return res;
			}
		}

		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			SComponent c = l.get(t);

			if (fi.isInComponent(c, t.getValue2())) {
				res.path.add(c);
				checker.isSetAndSet(c);
			}
			if (t.getValue() > maxDistance) {
				continue;
			}
			
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				if (!fi.canCross(e.to()))
					continue;
				f.pushSmaller(e.to().centreX(), e.to().centreY(), t.getValue()+e.distance(), t);
				e = e.next();
			}
		}
		
		f.done();
		return res;
	}
	
	public SCompPath fill(int startX, int startY, int maxDistance) {
		return fill(startX, startY, fDummy, maxDistance);
	}
	
	public SCompPath fill(ArrayCooShort starts, int maxDistance) {
		return fill(starts, fDummy, maxDistance);
	}

	
	/**
	 * Will search with {@link SCompPatherFinder} and return true if a level 0 component was found
	 * @param startX
	 * @param startY
	 * @param f
	 * @param maxDistance
	 * @return
	 */
	public boolean exists(int startX, int startY, SCompPatherExister f, int maxDistance) {
		
		SComponent ss = comps.zero.get(startX, startY);
		if (ss == null) {
			return true;
		}
		
		
		
		while(ss.superComp() != null)
			ss = ss.superComp();
		
		f.init(ss.level());
		
		if (!f.isInComponent(ss, 0)) {
			return false;
		}
		
		for (int i = ss.level().level()-1; i >= 0; i--) {
			f.init(comps.all.get(i));
			if (!exists(startX, startY, f, maxDistance, comps.all.get(i)))
				return false;
		}
		return true;
	}
	
	private boolean exists(int startX, int startY, SCompPatherExister fi, int maxDistance, SComponentLevel l) {
		SComponent start = l.get(startX, startY);
		if (start == null)
			return false;
		
		Flooder f = p.getFlooder();
		
		f.init(this);
		f.pushSloppy(start.centreX(), start.centreY(), 0);
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			SComponent c = l.get(t);

			if (fi.isInComponent(c, t.getValue2())) {
				f.done();
				return true;
			}
			
			if (t.getValue() > maxDistance) {
				continue;
			}
			
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				if (!fi.canCross(e.to()))
					continue;
				f.pushSmaller(e.to().centreX(), e.to().centreY(), t.getValue()+e.distance(), t);
				
				e = e.next();
			}
		}
		
		f.done();
		return false;
	}
	
	public SCompPath find(int startX, int startY, SCompPatherFinder f, int maxDistance) {
		
		PathTile t = null;
		
		SComponent ss = comps.zero.get(startX, startY);
		
		if (ss == null) {
			return null;
		}
		
		if (ss.superComp() == null) {
			t = find(startX, startY, f, maxDistance, comps.zero, checkers[1], checkers[0]);
			if (t == null) {
				return null;
			}
			res.set(t, comps.zero, checker);
			return res;
		}
		
		while(ss.superComp() != null)
			ss = ss.superComp();
		
		if (!f.isInComponent(ss, 0)) {
			return null;
		}
		
		checkers[(ss.level().level())&1].init();
		checkers[(ss.level().level())&1].isSetAndSet(ss);
		
		for (int i = ss.level().level()-1; i >= 0; i--) {
			t = find(startX, startY, f, maxDistance, comps.all.get(i), checkers[(i+1)&1], checkers[i&1]);
			if (t == null) {
				
				return null;
			}
		}
		res.set(t, comps.zero, checker);
		return res;
	}
	
	public SCompPath find(RoomInstance startRoom, SCompPatherFinder f, int maxDistance) {
		
		PathTile t = null;
		
		SComponent ss = comps.zero.get(startRoom.mX(), startRoom.mY());
		
		if (ss == null) {
			return null;
		}
		
		if (ss.superComp() == null) {
			t = find(startRoom, f, maxDistance, comps.zero, checkers[1], checkers[0]);
			if (t == null) {
				return null;
			}
			res.set(t, comps.zero, checker);
			return res;
		}
		
		while(ss.superComp() != null)
			ss = ss.superComp();
		
		if (!f.isInComponent(ss, 0)) {
			return null;
		}
		
		checkers[(ss.level().level())&1].init();
		checkers[(ss.level().level())&1].isSetAndSet(ss);
		
		for (int i = ss.level().level()-1; i >= 0; i--) {
			t = find(startRoom, f, maxDistance, comps.all.get(i), checkers[(i+1)&1], checkers[i&1]);
			if (t == null) {

				return null;
			}
		}
		res.set(t, comps.zero, checker);
		return res;
	}
	
	public SComponent get(int startX, int startY, SCompPatherFinder f, int maxDistance) {
		
		PathTile t = null;
		
		SComponent ss = comps.zero.get(startX, startY);
		
		if (ss == null) {
			return null;
		}
		
		if (ss.superComp() == null) {
			t = find(startX, startY, f, maxDistance, comps.zero, checkers[1], checkers[0]);
			if (t == null) {
				return null;
			}
			return comps.zero.get(t);
		}
		
		while(ss.superComp() != null)
			ss = ss.superComp();
		
		if (!f.isInComponent(ss, 0)) {
			return null;
		}
		
		checkers[(ss.level().level())&1].init();
		checkers[(ss.level().level())&1].isSetAndSet(ss);
		
		for (int i = ss.level().level()-1; i >= 0; i--) {
			t = find(startX, startY, f, maxDistance, comps.all.get(i), checkers[(i+1)&1], checkers[i&1]);
			if (t == null) {
				
				return null;
			}
		}
		return comps.zero.get(t);
	}
	
	public SCompPath find(int startX, int startY, SCompPatherFinder f, int maxDistance, int gridSize) {
		
		PathTile t = null;
		
		SComponent ss = comps.zero.get(startX, startY);
		if (ss == null) {
			return dummy;
		}
		
		if (ss.superComp() == null) {
			t = find(startX, startY, f, maxDistance, comps.zero, checkers[1], checkers[0]);
			if (t == null) {
				return null;
			}
			res.set(t, comps.zero, checker);
			return res;
		}
		
		
		while(ss.superComp() != null && ss.superComp().level().size() <= gridSize)
			ss = ss.superComp();
		
		
		t = find(startX, startY, f, maxDistance, ss.level(), checkerDummy, checkers[ss.level().level()&1]);
		if (t == null) {
			return null;
		}
		
		for (int i = ss.level().level()-1; i >= 0; i--) {
			t = find(startX, startY, f, maxDistance, comps.all.get(i), checkers[(i+1)&1], checkers[i&1]);
			if (t == null) {
				return null;
			}
		}
		res.set(t, comps.zero, checker);
		return res;
	}
	
	private final Rec rBounds = new Rec();
	
	private PathTile find(RoomInstance startRoom, SCompPatherFinder fi, int maxDistance, SComponentLevel l, BOOLEANO<SComponent> checker, SComponentChecker marker) {
		
		final SComponent start = l.get(startRoom.mX(), startRoom.mY());
		if (start == null)
			return null;
		
		maxDistance += l.size()*2;
		if (maxDistance < 0)
			maxDistance = Integer.MAX_VALUE;
		
		
		rBounds.setDim(l.size());
		marker.init();
		Flooder f = p.getFlooder();

		f.init(this);
		f.pushSloppy(start.centreX(), start.centreY(), 0);
		f.setValue2(start.centreX(), start.centreY(), 0);
		
		
		double v = 0;
		boolean canCross = false;
		PathTile best = null;
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			final SComponent c = l.get(t);
			if (c == null)
				throw new RuntimeException(l + " " + l.level());

			boolean inRoom = false;
			
			if (t.getValue2() == 0) {
				rBounds.moveX1Y1(c.centreX()&~(l.size()-1), c.centreY()&~(l.size()-1));
				if (c == start || rBounds.touches(startRoom.body()))
					inRoom = true;
			}
			
			if (fi.isInComponent(c, t.getValue2())) {
				mark(t, l, marker);
				if (best == null)
					best = t;
				if (l.level() == 0) {
					f.done();
					return best;
				}
			}
			if (t.getValue2() > maxDistance) {
				v = t.getValue2();
				continue;
			}
			
			if (c.superComp() != null && !checker.is(c.superComp()))
				continue;
			
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				SComponent to = e.to();
				double cost = e.cost2();
				double dist = e.distance();
				if (inRoom) {
					rBounds.moveX1Y1(to.centreX()&~(l.size()-1), to.centreY()&~(l.size()-1));
					if (rBounds.touches(startRoom)) {
						cost = 0;
						dist = 0;
					}
						
				}
				e = e.next();
				if (!fi.canCross(to)) {
					canCross = true;
					continue;
				}
				if (f.pushSmaller(to.centreX(), to.centreY(), t.getValue()+cost, t) != null)
					f.setValue2(to.centreX(), to.centreY(), t.getValue2()+dist);
			}
		}
		
		f.done();
		
		if (best != null)
			return best;
		
		if (!canCross && !SETT.PATH().willUpdate() && maxDistance > v)
			GAME.Notify("nono " + maxDistance + " " + v + " " + startRoom.mX() + " " + startRoom.mY() + " " + l.level() + " " + fi + " " + checker.is(start) + " " + fi.canCross(start));
		return null;
		
	}
	
	private PathTile find(int startX, int startY, SCompPatherFinder fi, int maxDistance, SComponentLevel l, BOOLEANO<SComponent> checker, SComponentChecker marker) {
		
		final SComponent start = l.get(startX, startY);
		if (start == null)
			return null;
		
		maxDistance += l.size()*2;
		if (maxDistance < 0)
			maxDistance = Integer.MAX_VALUE;
		
		
		
		marker.init();
		Flooder f = p.getFlooder();

		f.init(this);
		f.pushSloppy(start.centreX(), start.centreY(), 0);
		f.setValue2(start.centreX(), start.centreY(), 0);
		double v = 0;
		boolean canCross = false;
		PathTile best = null;
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			SComponent c = l.get(t);
			
			if (fi.isInComponent(c, t.getValue2())) {
				mark(t, l, marker);
				if (best == null)
					best = t;
				if (l.level() == 0) {
					f.done();
					return best;
				}
			}
			if (t.getValue2() > maxDistance) {
				v = t.getValue2();
				continue;
			}
			
			if (c.superComp() != null && !checker.is(c.superComp()))
				continue;
			
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				SComponent to = e.to();
				double cost = e.cost2();
				double dist = e.distance();
				e = e.next();
				if (!fi.canCross(to)) {
					canCross = true;
					continue;
				}
				if (to != l.get(to.centreX(), to.centreY())) {
					debug(c, to);
					continue;
				}
				if (f.pushSmaller(to.centreX(), to.centreY(), t.getValue()+cost, t) != null)
					f.setValue2(to.centreX(), to.centreY(), t.getValue2()+dist);
			}
		}
		
		f.done();
		
		if (best != null)
			return best;
		
		if (!canCross && !SETT.PATH().willUpdate() && maxDistance > v)
			GAME.Notify("nono " + maxDistance + " " + v + " " + startX + " " + startY + " " + l.level() + " " + fi + " " + checker.is(start) + " " + fi.canCross(start));
		return null;
		
	}
	
	private void debug(SComponent from, SComponent to) {
		System.err.println(to.level().level());
		System.err.println(to.centreX() + " " + to.retired() + " " + to.centreY() + " " + (to == to.level().get(to.centreX(), to.centreY())));
		
		SComponent u = SETT.PATH().comps.all.get(0).get(to.centreX(), to.centreY());
		
		while (u != null) {
			
			System.err.println("level " + u.level().level()  + " " + u.retired());
			System.err.println(u.superComp());
			System.err.println(u.centreX() + " " + u.centreY());
			if (u.superComp() != null) {
				System.err.println("supC " + u.superComp().centreX() + " " + u.superComp().centreY());
			}
			u = u.superComp();
		}
		System.err.println();
		
		
	}
	
	public SCompPath findDest(int startX, int startY, int destX, int destY) {
	
		SComponent s = PATH().comps.zero.get(startX, startY);
		if (s == null)
			return null;
		
		while(s.superComp() != null)
			s = s.superComp();
		
		if (!testDest(s, destX, destY))
			return null;
		
		
		PathTile t = null;
		
		SComponent ss = comps.zero.get(startX, startY);
		if (ss == null) {
			return null;
		}
		
		if (ss.superComp() == null) {
			t = findDest(startX, startY, destX, destY, comps.zero, checker);
			if (t == null) {
				return null;
			}
			res.set(t, comps.zero, checker);
			return res;
		}
		
		while(ss.superComp() != null)
			ss = ss.superComp();
		
		checker.init();
		checker.isSetAndSet(ss);
		
		for (int i = ss.level().level()-1; i >= 0; i--) {
			t = findDest(startX, startY, destX, destY, comps.all.get(i), checker);
			if (t == null) {
				return null;
			}
		}
		res.set(t, comps.zero, checker);
		
		
		return res;
	}
	
	private final DIR[] dirs = new DIR[] {
		DIR.C,
		DIR.N,
		DIR.E,
		DIR.S,
		DIR.W
	};
	
	private boolean testDest(SComponent s, int destX, int destY) {
		for (DIR d : dirs) {
			SComponent de = PATH().comps.zero.get(destX, destY, d);
			if (de != null) {
				while(de.superComp() != null)
					de = de.superComp();
				if (de == s)
					return true;
			}
		}
		return false;
	}
	
	private PathTile findDest(int startX, int startY, int destX, int destY, SComponentLevel l, BOOLEANO<SComponent> checker) {
		
		final SComponent start = l.get(startX, startY);
		if (start == null)
			return null;
		
		
		
		Flooder f = p.getFlooder();
		
		f.init(this);
		f.pushSloppy(start.centreX(), start.centreY(), 0);
		int v = 0;
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			SComponent c = l.get(t);
			//for filthy threads
			if (c == null) {
				f.done();
				return null;
			}
				
			v++;
			for (DIR d : dirs) {
				if (c.is(destX, destY, d)) {
					mark(t, l);
					PathTile res = t;
					f.done();
					return res;
				}
			}
			
			if (c.superComp() != null && !checker.is(c.superComp()))
				continue;
			
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				SComponent to = e.to();
				double cost = e.cost2();
				e = e.next();
				f.pushSmaller(to.centreX(), to.centreY(), t.getValue()+cost, t);
			}
		}
		
		f.done();
		
		if (!SETT.PATH().willUpdate())
			GAME.Notify("nono " + v + " " + startX + " " + startY + " " + l.level() + " " + checker.is(start.superComp()) + " " + (l.get(destX, destY) != null ? checker.is(l.get(destX, destY).superComp()) : false) + " " + destX + " " + destY);
		return null;
		
	}
	
	private void mark(PathTile t, SComponentLevel l) {
		checker.init();
		while(t != null) {
			SComponent c = l.get(t.x(), t.y());
			mark(c, checker);
			t = t.getParent();
		}
		
	}
	
	private void mark(PathTile t, SComponentLevel l, SComponentChecker checker) {
		while(t != null) {
			SComponent c = l.get(t.x(), t.y());
			mark(c, checker);
			t = t.getParent();
		}
		
	}
	
	private void mark(SComponent c, SComponentChecker checker) {
		checker.isSetAndSet(c);
		SComponentEdge e = c.edgefirst();
		while(e != null) {
			checker.isSetAndSet(e.to());
			e = e.next();
		}
		
	}
	
	public interface SCompPath extends MAP_BOOLEAN {
		
		public LIST<SComponent> path();
		public double distance();
	}
	
	public interface SCompPatherFinder {
		
		/**
		 * If the component is valid o cross
		 * @param c
		 * @return
		 */
		public default boolean canCross(SComponent c) {
			return true;
		}
		
		public abstract boolean isInComponent(SComponent c, double distance);
		
	}
	
	public interface SCompPatherExister extends SCompPatherFinder{
		
		void init(SComponentLevel l);
		
	}

	
	private final class SCompReal implements SCompPath{

		private final ArrayListResize<SComponent> path = new ArrayListResize<>(SComp0Level.startSize, Integer.MAX_VALUE);
		private double distance;
		private SComponentChecker checker;
		
		@Override
		public boolean is(int tile) {
			SComponent s = comps.zero.get(tile);
			if (s != null && checker.isSet(s.index()))
				return true;
			return false;
		}

		@Override
		public boolean is(int tx, int ty) {
			if (IN_BOUNDS(tx, ty)) {
				SComponent s = comps.zero.get(tx, ty);
				if (s != null && checker.isSet(s.index()))
					return true;
			}
			return false;
		}
		
		private void set(PathTile t, SComponentLevel l, SComponentChecker checker) {
			distance = t.getValue();
			this.checker = checker;
			path.clearSoft();
			while(t != null) {
				SComponent c = l.get(t.x(), t.y());
				
				path.add(c);
				t = t.getParent();
			}
		}
		
		@Override
		public LIST<SComponent> path() {
			return path;
		}

		@Override
		public double distance() {
			return distance;
		}
		
		
		
	}
	
	private final class SCompDummy implements SCompPath{

		private ArrayList<SComponent> path = new ArrayList<>(0);
		
		@Override
		public boolean is(int tile) {
			return true;
		}

		@Override
		public boolean is(int tx, int ty) {
			if (IN_BOUNDS(tx, ty)) {
				return true;
			}
			return false;
		}

		@Override
		public LIST<SComponent> path() {
			return path;
		}

		@Override
		public double distance() {
			return 0;
		}
		
	}

	
	
}
