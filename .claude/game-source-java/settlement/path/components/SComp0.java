package settlement.path.components;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import settlement.entry.EntryPoints.EntryPoint;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import util.GUTIL;

final class SComp0 extends SComponent{

	private final int index;
	private short cx,cy;
	private byte edgeMask;
	boolean checked;
	
	SComp0(int index){
		this.index = index;
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
	
//	@Override
//	public byte edgeMask() {
//		return (byte) (edgeMask & 0x0F);
//	}
	
	@Override
	protected void retire(){
		retire(true);
		cx = -1;
		cy = -1;
		super.retire();
	}
	
	@Override
	public boolean retired() {
		return (edgeMask & 0b0001_0000) != 0;
	}
	
	void retire(boolean b) {
		if (b)
			edgeMask |= 0b0001_0000;
		else
			edgeMask &= ~0b0001_0000;
	}
	
	boolean checked() {
		return (edgeMask & 0b0010_0000) != 0;
	}
	
	void checked(boolean b) {
		if (b)
			edgeMask |= 0b0010_0000;
		else
			edgeMask &= ~0b0010_0000;
	}
	
	void init(RECTANGLE bounds, int size, SComponentChecker neighbours){
		
		edgeMask = 0;
		if (bounds.x1() == 0) {
			edgeMask |= 1;
		}else if(bounds.x2() == TWIDTH) {
			edgeMask |= 1;
		}
		if (bounds.y1() == 0) {
			edgeMask |= 1;
		}else if(bounds.y2() == THEIGHT) {
			edgeMask |= 1;
		}
		
		for (EntryPoint p : SETT.ENTRY().points.active()) {
			if (is(p.coo()))
				edgeMask |= 2;
		}
		
		
		
		int smallest = -1;
		double smallestValue = Double.MAX_VALUE;
		for (COORDINATE c : bounds) {
			int x = c.x();
			int y = c.y();
			if (is(x, y)) {
				AVAILABILITY a = PATH().availability.get(x, y);
				double rx = (bounds.cX()-x);
				double ry = (bounds.cY()-y);
				double r = rx*rx+ry*ry + 1;
				double v = (a.player + a.from);
				v = v*v*r;
				if ( v < smallestValue) {
					smallest = 1;
					smallestValue = v;
					this.cx = (short) x;
					this.cy = (short) y;
				}
			}
		}
		
		if (smallest == -1)
			throw new RuntimeException("shitty component");	
		
		
		
		
		setEdges(neighbours);
		
		return;
		
	}
	
	
	
	void setEdges(SComponentChecker neighbours) {
		
		super.retire();
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(centreX(), centreY(), 0);
		GUTIL.flooder().setValue2(centreX(), centreY(), 0);
		
		neighbours.init();

		while (GUTIL.flooder().hasMore()) {

			PathTile t = GUTIL.flooder().pollSmallest();
			int x = t.x();
			int y = t.y();

			SComp0 n = PATH().comps.zero.get(x, y);
			if (neighbours.is(n))
				continue;

			if (n != this) {
				if (SETT.PATH().comps.zero.updating().is(x, y)) {
					neighbours.isSetAndSet(n);
					continue;
				}

				if ((n.centreX() == x && n.centreY() == y)) {
					
					pushEdge(n, t.getValue2(), t.getValue());
					n.pushEdge(this, t.getValue2(), t.getValue());
					neighbours.isSetAndSet(n);
					
				}
			}
			
			

			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				double v2 = PATH().coster.player.getCost(x, y, x + d.x(), y + d.y())*d.tileDistance();
				if (v2 <= 0)
					continue;
				SComponent next =  PATH().comps.zero.get(x, y, d);
				if (next == null)
					continue;
				if (next != this && n != this && next != n)
					continue;
				if (neighbours.is(next))
					continue;
				double v = PATH().availability.get(x + d.x(), y + d.y()).movementSpeedI;
				if (GUTIL.flooder().pushSloppy(x, y, d, t.getValue() + v * d.tileDistance(), t) != null) {
					GUTIL.flooder().setValue2(x, y,d,v2+t.getValue2());
				}
			}
		}

		GUTIL.flooder().done();
		
		pruneEdges();
	}

	@Override
	public SComponentLevel level() {
		return PATH().comps.zero;
	}

	
}
