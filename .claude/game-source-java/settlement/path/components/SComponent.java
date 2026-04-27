package settlement.path.components;

import static settlement.main.SETT.TWIDTH;

import init.resources.RBIT.RBITImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.INDEXED;

public abstract class SComponent implements INDEXED, MAP_BOOLEAN {

	private SComponentEdge edgeFirst;
	private SComponent superComp = null;
	boolean checked;
	
	final RBITImp[] ress = new RBITImp[FindableDataRes.all.size()];
	{
		for (int i = 0; i < ress.length; i++)
			ress[i] = new RBITImp();
	}
	
	final long[] fdata = new long[FindableData.datao.longCount()];
	
	public abstract int centreX();
	public abstract int centreY();
	public abstract boolean hasEdge();
	public abstract boolean hasEntry();
	
	protected final void pushEdge(SComponent to, double cost, double distance) {
		edgeFirst = SComponentEdge.create(to, cost, distance, edgeFirst);
	}
	
	@Override
	public boolean is(int tile) {
		return level().get(tile) == this;
	}

	@Override
	public boolean is(int tx, int ty) {
		return level().get(tx, ty) == this;
	}
	
	protected void retire() {
		while (edgeFirst != null) {
			edgeFirst.to().removeEdge(this);
			removeEdge(edgeFirst.to());
		}
		superComp = null;
		check();
	}
	
	public abstract boolean retired();

	
	private void removeEdge(SComponent to) {
		if (edgeFirst.to() == to) {
			SComponentEdge e = edgeFirst;
			edgeFirst = e.next();
			e.retire();
			return;
		}
		
		SComponentEdge e = edgeFirst;
		while(e.next() != null) {
			if (e.next().to() == to) {
				SComponentEdge ret = e.next();
				e.setNext(e.next().next());
				ret.retire();
				return;
			}
			e = e.next();
		}
	}
	
	private void check() {
		SComponentEdge e = edgeFirst;
		while(e != null) {
			if (e.to() != e.to().level().get(e.to().centreX(), e.to().centreY()))
				new RuntimeException().printStackTrace();
			e = e.next();
		}
	}
	
	protected void pruneEdges() {
		check();
//		SComponentEdge e = edgefirst();
//		while(e != null) {
//			SComponentEdge ee = e;
//			e = e.next();
//			if (!needsEdge(this, ee)) {
//				ee.to().removeEdge(this);
//				removeEdge(ee.to());
//			}
//			
//		}
	}
	
//	private static boolean needsEdge(SComponent c, SComponentEdge e) {
//		SComponent to = e.to();
//		double dist = e.cost2();
//		e = c.edgefirst();
//		while(e != null) {
//			if (e.to() != to && e.cost2() + distanceTo(e.to(), to, dist) < dist) {
//				return false;
//			}
//			
//			e = e.next();
//		}
//		return true;
//	}
//	
//	private static double distanceTo(SComponent from, SComponent to, double max) {
//		SComponentEdge e = from.edgefirst();
//		while(e != null) {
//			if (e.to() == from) {
//				return e.cost2();
//			}
//			e = e.next();
//		}
//		return max;
//	}
	
	public SComponent superComp() {
		return superComp;
	}
	
	public SComponent superCompTop() {
		SComponent s = superComp;
		while(s != null && s.superComp() != null)
			s = s.superComp();
		return s;
	}
	
	void superCompSet(SComponent sComp) {
		if (this.superComp != null && sComp != null) {
			System.err.println(level().level());
			System.err.println(this.superComp + " " + sComp);
			throw new RuntimeException();
		}
		this.superComp = sComp;
	}
	
	public SComponentEdge edgefirst() {
		return edgeFirst;
	}
	
	void clearData() {
		for (int i = 0; i < fdata.length; i++) {
			fdata[i] = 0;
		}
		for (RBITImp b : ress)
			b.clear();
	}
	
	public abstract SComponentLevel level();
	
	private static Coo rndCoo = new Coo();
	
	public COORDINATE rndCoo() {
		
		int size = level().size();
		int x1 = CLAMP.i(centreX()-size, 0, TWIDTH);
		int x2 = CLAMP.i(centreX()+size, 0, TWIDTH);
		int y1 = CLAMP.i(centreY()-size, 0, TWIDTH);
		int y2 = CLAMP.i(centreY()+size, 0, TWIDTH);
		int w = x2-x1;
		int h = y2-y1;
		int area = (x2-x1)*(y2-y1);
		
		for (int i = 0; i < area; i++) {
			int x = x1 + RND.rInt(w);
			int y = y1 + RND.rInt(h);
			if (is(x, y)) {
				rndCoo.set(x, y);
				return rndCoo;
			}
		}
		
		rndCoo.set(centreX(), centreY());
		return rndCoo;
		
		
	}
}
