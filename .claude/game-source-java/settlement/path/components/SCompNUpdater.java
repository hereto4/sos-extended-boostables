package settlement.path.components;

import static settlement.main.SETT.PATH;

import settlement.main.SETT;
import snake2d.PathUtilOnline.Filler;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayListResize;
import util.GUTIL;

final class SCompNUpdater {

	private final SCompNFactory factory;
	private final Rec bounds;
	private final Rec boundsC = new Rec();
	private final SComponentChecker checkerUnderlings;
	private final SComponentChecker checkerSelf;
	private final SComponentLevel lower;
	private final ArrayListResize<SComponent> upUnderlings;
	private final int size;
	private final SCompNLevel map;
	
	SCompNUpdater(SCompNLevel map, SCompNFactory f, SComponentLevel prev, int size){
		this.lower = prev;
		this.factory = f;
		checkerUnderlings = new SComponentChecker(prev); 
		checkerSelf = new SComponentChecker(map); 
		upUnderlings = new ArrayListResize<>(SComp0Level.startSize >> map.level(), Integer.MAX_VALUE);
		bounds = new Rec(size, size);
		this.size = size;
		this.map = map;
	}

	void update() {
		
		checkerUnderlings.init();
		for (int i = 0; i < upUnderlings.size(); i++) {
			SComponent c = upUnderlings.get(i);
			if (!checkerUnderlings.isSetAndSet(c)) {
				update(c);
			}
		}
		
		upUnderlings.clearSoft();
	}
	
	void remove(SComponent toBeRemoved) {
		if (toBeRemoved == null)
			return;
		SCompN o = (SCompN) toBeRemoved;
		if (o.superComp() != null) {
			PATH().comps.levels.get(map.level()).remove(o.superComp());
		}
		GUTIL.filler().init(this);
		GUTIL.filler().fill(toBeRemoved.centreX(), toBeRemoved.centreY());
		while(GUTIL.filler().hasMore()) {
			COORDINATE c = GUTIL.filler().poll();
			SComponent ss = PATH().comps.all.get(map.level()-1).get(c.x(), c.y());
			ss.superCompSet(null);
			SComponentEdge e = ss.edgefirst();
			while(e != null) {
				if (e.to().superComp() == toBeRemoved) {
					GUTIL.filler().fill(e.to().centreX(), e.to().centreY());
				}
				e = e.next();
			}
		}
		GUTIL.filler().done();
		
		factory.retire(o);

	}
	
	void add(SComponent c) {
		upUnderlings.add(c);
	}
	
	private void update(SComponent underling) {
		{
			int qx1 = size*(underling.centreX()/size);
			int qy1 = size*(underling.centreY()/size);
			bounds.moveX1Y1(qx1, qy1);
			boundsC.clear();
		}
		
		if (underling.superComp() != null) {
			throw new RuntimeException(size + " " + underling.superComp() + " " + underling.superComp().retired());
		}
		
		if (lower.get(underling.centreX(),underling.centreY()) != underling) {
			throw new RuntimeException(size + " " + underling.centreX() + " " + underling.centreY());
		}
		
		final SCompN comp = factory.create();
		
		Filler f = GUTIL.filler();
		f.init(this);
		f.fill(underling.centreX(),underling.centreY());
		
		
		while(f.hasMore()) {
			COORDINATE coo = f.poll();
			SComponent c = lower.get(coo);
			if (c == null)
				throw new RuntimeException();
			checkerUnderlings.isSetAndSet(c);
			c.superCompSet(comp);
			boundsC.unify(coo.x(), coo.y());
			SComponentEdge e = c.edgefirst();
			
			while(e != null) {
				if (bounds.holdsPoint(e.to().centreX(), e.to().centreY()) && !checkerUnderlings.is(e.to())) {
					f.fill(e.to().centreX(), e.to().centreY());
				}
				e = e.next();
			}
		}
		f.done();
		
		comp.init(underling, boundsC, lower, checkerSelf);
		
		SETT.PATH().comps.data.initComponentN(comp);
		
		if (map.level() < SETT.PATH().comps.levels.size()) {
			SETT.PATH().comps.levels.get(map.level()).addNew(comp);
		}
		
		
	}

}
