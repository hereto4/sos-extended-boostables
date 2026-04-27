package settlement.path.components;

import static settlement.main.SETT.PATH;

import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Filler;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import util.GUTIL;

final class SCompN extends SComponent{

	boolean retired = true;
	private final int index;
	private short cx,cy;
	byte edgeMask;
	private final byte level;
	
	
	SCompN(int index, byte level){
		this.index = index;
		this.level = level;
	}

	@Override
	public int index() {
		return index;
	}

	@Override
	public int centreX() {
		return cx;
	}

	@Override
	public int centreY() {
		return cy;
	}

	@Override
	public boolean hasEdge() {
		return (edgeMask & 1) != 0;
	}
	
	@Override
	public boolean hasEntry() {
		return (edgeMask & 2) != 0;
	}
	
	@Override
	protected void retire(){
		super.retire();
		this.retired = true;
	}
	
	@Override
	public boolean retired() {
		return retired;
	}

	@Override
	public SCompNLevel level() {
		return SETT.PATH().comps.levels.get(level-1);
	}

	void init(SComponent underling, RECTANGLE boundsC, SComponentLevel lower, SComponentChecker checker) {
		
		edgeMask = 0;
		
		Filler f = GUTIL.filler();
		f.init(this);
		f.fill(underling.centreX(),underling.centreY());
		
		double low = Double.MAX_VALUE;
		SComponent bestCentre = null;
		
		while(f.hasMore()) {
			COORDINATE coo = f.poll();
			SComponent c = lower.get(coo);
			add(c);
			
			SComponentEdge e = c.edgefirst();
			
			double dist = Math.abs(boundsC.cX()-coo.x()) + Math.abs(boundsC.cY()-coo.y())/(double)level().size();
			double cost = 1;
			while(e != null) {
				if (e.to().superComp() == this && boundsC.holdsPoint(e.to().centreX(), e.to().centreY())) {
					f.fill(e.to().centreX(), e.to().centreY());
					if (e.cost2() > cost)
						cost = e.cost2();
				}
				e = e.next();
			}
			
			if (dist + cost < low) {
				low = dist + cost;
				bestCentre = c;
			}
			
		}
		cx = (short) bestCentre.centreX();
		cy = (short) bestCentre.centreY();
		
		f.done();
		if (level() == PATH().comps.last) {
			return;
		}
		
		setEdges(checker, boundsC);
		
	}
	
	
	private void add(SComponent underling) {
		edgeMask |= underling.hasEdge() ? 1 : 0;
		edgeMask |= underling.hasEntry() ? 2 : 0;
	}
	
	private void setEdges(SComponentChecker neighbours, RECTANGLE bounds) {
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(centreX(), centreY(), 0);
		GUTIL.flooder().setValue2(centreX(), centreY(), 0);
		
		neighbours.init();


		
		while (GUTIL.flooder().hasMore()) {

			PathTile t = GUTIL.flooder().pollSmallest();
			int x = t.x();
			int y = t.y();

			SComponent underling = SETT.PATH().comps.all.get(level-1).get(x, y);
			if (underling == null)
				continue;
			SCompN s = (SCompN) underling.superComp();
			if (s != null && s != this && s.centreX() == t.x() && s.centreY() == t.y()) {
				pushEdge(s, t.getValue2(), t.getValue());
				s.pushEdge(this, t.getValue2(), t.getValue());
				neighbours.isSetAndSet(s);
				continue;
			}

			SComponentEdge e = underling.edgefirst();
			while(e != null) {
				SComponent o = e.to();
				double cost = e.cost2();
				double dist = e.distance();
				e = e.next();
				if (o.superComp() == null)
					continue;
				SCompN so = (SCompN) o.superComp();
				if (so.retired || neighbours.is(so))
					continue;
				if (so == this && !bounds.holdsPoint(this.centreX(), this.centreY()))
					continue;
				if (so != this && s != this && so != s)
					continue;
				if (GUTIL.flooder().pushSmaller(o.centreX(), o.centreY(), cost+t.getValue(), t) != null){
					GUTIL.flooder().setValue2(o.centreX(), o.centreY(),dist+t.getValue2());
				}
			}
		}

		GUTIL.flooder().done();
		
		
		
		pruneEdges();
	}


	
}
