package settlement.path.components;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TWIDTH;

import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.IntegerStack;

final class SComp0Quads {

	private final int QuadrantSize;
	private final int QScroll;
	private final int quadsDim;
	private final Bitmap1D up;
	private final Bitmap1D change;
	private final IntegerStack needsUpdate;
	private final Rec grid;
	
	public SComp0Quads(int size) {
		QuadrantSize = size;
		QScroll = Integer.numberOfTrailingZeros(QuadrantSize);
		quadsDim = SETT.TWIDTH / QuadrantSize;
		up = new Bitmap1D(quadsDim*quadsDim, false);
		change = new Bitmap1D(quadsDim*quadsDim, false);
		needsUpdate = new IntegerStack(quadsDim*quadsDim);
		grid = new Rec(QuadrantSize, QuadrantSize);
	}

	public void setChangedAvailability(int tx, int ty) {
		if (!IN_BOUNDS(tx, ty))
			return;
		
		for (int i = 0; i < DIR.ALLC.size(); i++) {
			int x = (tx + DIR.ALLC.get(i).x()) >> QScroll;
			int y = (ty + DIR.ALLC.get(i).y()) >> QScroll;
			
			if (x >= 0 && y >= 0 && x < quadsDim && y < quadsDim) {
				int qi = x + y*quadsDim;
				if (!change.get(qi)) {
					needsUpdate.push(qi);
					change.set(qi, true);
				}
				up.set(qi, true);
			}
		}
	}
	
	public void setChangedServices(int tx, int ty) {
		if (!IN_BOUNDS(tx, ty))
			return;
		
		for (int i = 0; i < DIR.ALLC.size(); i++) {
			int x = (tx + DIR.ALLC.get(i).x()) >> QScroll;
			int y = (ty + DIR.ALLC.get(i).y()) >> QScroll;
			int qi = x + y*quadsDim;
			if (x >= 0 && y >= 0 && x < quadsDim && y < quadsDim && !up.get(qi)) {
				if (!change.get(qi)) {
					needsUpdate.push(qi);
					change.set(qi, true);
				}
			}
			
		}
	}
	
	public void changeAll() {
		change.setAll(true);
		up.setAll(true);
		needsUpdate.clear();
		for (int i = 0; !needsUpdate.isFull(); i++)
			needsUpdate.push(i);
	}
	
	public boolean updating() {
		return needsUpdate.size() > 0;
	}

	public void clear() {
		needsUpdate.clear();
		up.clear();
		change.clear();
	}
	
	void update(SComp0Updater updater) {
		
		if (needsUpdate.size() > 0)
			SETT.ENTRY().points.updateAvailability();
		
		for (int ii = 0; ii < needsUpdate.size(); ii++) {
			int i = needsUpdate.get(ii);
			int tx = i % quadsDim;
			int ty = i / quadsDim;
			grid.moveX1Y1(tx << QScroll, ty << QScroll);
			if (up.get(i)) {
				updater.removeSuperComp(grid, this);
			}
		}
		
		for (int ii = 0; ii < needsUpdate.size(); ii++) {
			int i = needsUpdate.get(ii);
			int tx = i % quadsDim;
			int ty = i / quadsDim;
			grid.moveX1Y1(tx << QScroll, ty << QScroll);
			if (up.get(i)) {
				updater.remove(grid, this);
			}
		}
		
		while(!needsUpdate.isEmpty()) {
			int i = needsUpdate.pop();
			int tx = i % quadsDim;
			int ty = i / quadsDim;
			grid.moveX1Y1(tx << QScroll, ty << QScroll);
			boolean bup = up.get(i);
			up.set(i, false);
			change.set(i, false);
			
			if (bup) {
				updater.assign(grid, this);
			}else {
				updater.initData(grid);
			}
			
		}
	}
	
	public RECTANGLE popNext() {
		if (needsUpdate.isEmpty())
			return null;
		int i = needsUpdate.pop();
		int tx = i % quadsDim;
		int ty = i / quadsDim;
		up.set(i, false);
		grid.moveX1Y1(tx << QScroll, ty << QScroll);
		
		return grid;
	}
	
	public RECTANGLE peekpNext(int ii) {
		int i = needsUpdate.get(ii);
		int tx = i % quadsDim;
		int ty = i / quadsDim;
		grid.moveX1Y1(tx << QScroll, ty << QScroll);
		
		return grid;
	}
	
	public int updatable() {
		return needsUpdate.size();
	}
	
	public boolean upping() {
		return needsUpdate.size() > 0;
	}
	
	public MAP_BOOLEAN updating = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			int x = (tx) >> QScroll;
			int y = (ty) >> QScroll;
			int qi = x + y*quadsDim;
			if (x > 0 && y > 0 && x < quadsDim && y < quadsDim && up.get(qi)) {
				return true;
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%TWIDTH, tile/TWIDTH);
		}
	};

}
