package game.battle.thread.general.offence;

import game.battle.div.Div;
import game.battle.thread.general.StrategosUtil;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.iterators.RECedgeIter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LIST;

/**
 * A thing that takes all enemy divs and flood fills them in a fancy way to create blobs surrounding them.
 * @author Jake
 *
 */
class StepBlob {

	private final StrategosUtil context;
	private final double tileRange = 32;
	private final Node[] nodes = new Node[Config.battle().DIVISIONS_PER_ARMY];
	private final ArrayList<Node> anodes = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private final Node[][] nmap = new Node[(int) Math.ceil(SETT.TWIDTH/tileRange)][(int) Math.ceil(SETT.THEIGHT/tileRange)];
	private final Rec rec = new Rec();

	private final Coo[] coos = new Coo[Config.battle().DIVISIONS_PER_ARMY];
	
	public StepBlob(StrategosUtil context) {
		for (int i = 0; i < nodes.length; i++)
			nodes[i] = new Node();
		this.context = context;
		for (int c = 0; c < coos.length; c++)
			coos[c] = new Coo(-16, -16);
	}
	
	void update(Bitmap2D blob, int range) {
	
		blob.clear();
		
		anodes.clearSloppy();
		rec.setDim(1, 1).moveX1Y1(0, 0);

		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			
			
			Div d = context.getArmy().enemy().divisions().get(di);
			if (d.active()) {
				
				int tx = d.centre().cUnitX()>>C.T_SCROLL;
				int ty = d.centre().cUnitY()>>C.T_SCROLL;
				Coo c = coos[d.indexArmy()];
				if (c.tileDistanceTo(tx, ty) > 10) {
					c.set(tx, ty);
				}
				
				
				Node n = nodes[di];
				n.next = null;
				n.coo.set(c.x(), c.y());
				anodes.add(n);
			}else {
				coos[d.indexArmy()].set(-C.TILE_SIZE*16, -C.TILE_SIZE*16);
			}
		}
		
		
		
		if (anodes.size() > 0) {
			fill(blob, anodes, range);
		}
	}
	
	public RECTANGLE area() {
		return rec;
	}
	
	RECedgeIter iter = new RECedgeIter();
	
	private void fill(Bitmap2D blob, LIST<Node> nodes, int range) {
		
		
		if (context.getArmy().men() == 0)
			return;
		
		
		
		
		
		for (int y = 0; y < nmap.length; y++) {
			for (int x = 0; x < nmap.length; x++) {
				nmap[y][x] = null;
			}
		}
		
		Flooder f = context.flooder.getFlooder();
		f.init(this);
		
		rec.setDim(1, 1).moveX1Y1(nodes.get(0).coo);
		
		for (Node n : nodes) {
			add(n, f);
		}
	
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			if (t.getValue() > range) {
				f.pushSloppy(t.x(), t.y(), t.getValue());
				break;
			}
			blob.set(t, true);
			rec.unify(t.x(), t.y());
			
			if (t.getValue() < 12) {
				for (int i = 0; i < DIR.ALL.size(); i++) {
					int dx = t.x()+DIR.ALL.get(i).x();
					int dy = t.y()+DIR.ALL.get(i).y();
					if (blob.body().holdsPoint(dx, dy) && !SETT.PATH().solidity.is(dx, dy))
						if (f.pushSmaller(dx, dy, t.getValue()+1)!=null)
							f.setValue2(dx, dy, t.getValue2());
				}
			}else {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					int dx = t.x()+DIR.ORTHO.get(i).x();
					int dy = t.y()+DIR.ORTHO.get(i).y();
					if (blob.body().holdsPoint(dx, dy) && !SETT.PATH().solidity.is(dx, dy))
						if (f.pushSmaller(dx, dy, t.getValue()+1)!=null)
							f.setValue2(dx, dy, t.getValue2());
				}
			}
			
			
			
		}
	
		iter.init(SETT.TILE_BOUNDS);
		
		for (COORDINATE c : iter) {
			if (!f.hasBeenPushed(c))
				f.pushSloppy(c, 0);
		}

		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				int dx = t.x()+DIR.ORTHO.get(i).x();
				int dy = t.y()+DIR.ORTHO.get(i).y();
				if (blob.body().holdsPoint(dx, dy) && !f.hasBeenPushed(dx, dy))
					f.pushSmaller(dx, dy, t.getValue()+1);
			}			
		}
		
		for (int ty = 0; ty < SETT.THEIGHT; ty++) {
			for (int tx = 0; tx < SETT.TWIDTH; tx++) {
				if (!f.hasBeenPushed(tx, ty))
					blob.set(tx, ty, true);
			}
		}
		
		
		f.done();
	}
	
	private void add(Node n, Flooder f) {
		
		if (!SETT.IN_BOUNDS(n.coo))
			return;
		
		f.pushSloppy(n.coo, 0);
		for (int di = 0; di < DIR.ALLC.size(); di++) {
			DIR d = DIR.ALLC.get(di);
			int x = (int) (n.coo.x() + d.xN()*tileRange);
			int y = (int) (n.coo.y() + d.yN()*tileRange);
			if (SETT.IN_BOUNDS(x, y)) {
				int nx = (int) (x/tileRange);
				int ny = (int) (y/tileRange);
				Node other = nmap[ny][nx];
				add(n, other, f);
			}
		}
		
		int x = (int) (n.coo.x()/tileRange);
		int y = (int) (n.coo.y()/tileRange);
		
		n.next = nmap[y][x];
		nmap[y][x] = n;
		
		
		
	}
	
	private final double tileRange2 = tileRange*tileRange;
	
	private void add(Node n, Node other, Flooder f){
		
		while(other != null) {
			
			Node o = other;
			other = other.next;
			double dx = o.coo.x()-n.coo.x();
			double dy = o.coo.y()-n.coo.y();
			if (dx*dx+dy*dy > tileRange2) {
				continue;
			}
			double step = Math.max(Math.abs(dx), Math.abs(dy));
			if (step <= 0)
				continue;
			
			dx /= step;
			dy /= step;
			
			if (testLine(n, step, dx, dy))
				addLine(n, f, step, dx, dy);
				
				
			
		}
	}
	
	private boolean testLine(Node n, double step, double dx, double dy) {
		double x = n.coo.x()+0.5;
		double y = n.coo.y()+0.5;
		for (double d = 0; d < step; d++) {
			
			int fx = (int) x;
			int fy = (int) y;
			x += dx;
			y += dy;
			int tx = (int) x;
			int ty = (int) y;
			if (fx == tx && fy == ty)
				continue;
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			if (SETT.PATH().solidity.is(tx, ty) || SETT.PATH().solidity.is(fx, ty) || SETT.PATH().solidity.is(tx, fy))
				return false;
		}
		return true;
	}
	
	private void addLine(Node n, Flooder f, double step, double dx, double dy) {
		double x = n.coo.x()+0.5;
		double y = n.coo.y()+0.5;
		
		
		
		for (double d = 0; d < step; d++) {
			x += dx;
			y += dy;
			int tx = (int) x;
			int ty = (int) y;
			f.pushSloppy(tx, ty, 0);
		}
	}
	
	private static class Node {
		
		Node next;
		final Coo coo = new Coo();
		
		
		Node(){

		}
		
		
	}

}
