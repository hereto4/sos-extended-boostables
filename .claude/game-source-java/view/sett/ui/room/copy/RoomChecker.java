package view.sett.ui.room.copy;

import settlement.main.SETT;
import settlement.room.main.ROOMA;
import settlement.room.main.Room;
import settlement.room.main.util.RoomAreaWrapper;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.iterators.RECIter;
import snake2d.util.sets.Bitmap1D;

final class RoomChecker {

	private final Dest dest;
	private final Bitmap1D placableBits = new Bitmap1D(SETT.TAREA, false);
	private Rec box = new Rec();
	private final RECIter iter = new RECIter(box);
	private static final RoomAreaWrapper wrap = new RoomAreaWrapper();
	
	RoomChecker(Dest dest){
		this.dest = dest;
	}
	
	void init() {
		placableBits.clear();
		for (COORDINATE c : dest.body()) {
			if (!placableBits.get(c.x() + c.y()*SETT.TWIDTH) && isBlocked(c.x(), c.y())) {
				markRoom(c);
			}
		}
	}
	
	private void markRoom(COORDINATE coo) {
		setBox(coo.x(), coo.y());
		COORDINATE source = dest.transform(coo.x(), coo.y());
		Room room = SETT.ROOMS().map.get(source);
		ROOMA r = wrap.init(room, source.x(), source.y());
		for (COORDINATE c : iter) {
			COORDINATE s = dest.transform(c.x(), c.y());
			if (r.is(s)) {
				placableBits.set(c.x()+c.y()*SETT.TWIDTH, true);
			}
		}
		wrap.done();
	}
	
	private void setBox(int newX, int newY) {
		COORDINATE s = dest.transform(newX, newY);
		
		Room room = SETT.ROOMS().map.get(s.x(), s.y());
		ROOMA r = wrap.init(room, s.x(), s.y());
		
		int dx = s.x()-r.body().cX();
		int dy = s.y()-r.body().cY();
		int w = r.body().width();
		int h = r.body().height();
		for (int i = 0; i < 4-dest.rot(); i++) {
			int k = dx;
			dx = dy;
			dy = -k;
			k = w;
			w = h;
			h = k;
		}
		
		int x1 = newX-dx-w/2;
		int y1 = newY-dy-h/2;
		box.setDim(w, h);
		box.moveX1Y1(x1, y1);
		wrap.done();
	}
	
	public boolean place(int tx, int ty) {
		COORDINATE s = dest.transform(tx, ty);
		
		Room room = SETT.ROOMS().map.get(s.x(), s.y());
		if (room == null)
			return false;
		
		if (!isBlocked(tx, ty) && !isPartOfBlocked(tx, ty)) {

			ROOMA r = wrap.init(room, s.x(), s.y());
			wrap.done();
			if (r == null) {
				return true;
			}
			if (r.mX() == s.x() && r.mY() == s.y()) {
				
				for (COORDINATE c : r.body()) {
					
					

					if (r.is(c)) {
						int x = c.x()-r.mX()+tx;
						int y = c.y()-r.mY()+ty;
						
						if (dest.sourceIs(x, y)) {
							
							return true;
						}
//						COORDINATE ss = dest.transform(c.x(), c.y());
//						if (!SETT.ROOMS().copy.copier.isPlacable(c.x(), c.y(), x, y))
//							return true;
					
					}
				}
				s = dest.transform(tx, ty);
				
				int nx = s.x()-r.body().cX();
				int ny = s.y()-r.body().cY();
				for (int i = 0; i < 4-dest.rot(); i++) {
					int k = nx;
					nx = ny;
					ny = -k;
				}
				tx -= nx;
				ty -= ny;
				SETT.ROOMS().copy.copier.copy(r.mX(), r.mY(), tx, ty, dest.rot());
			}
			
			
			return true;
			
		}
		
		
		
		return true;
	}
	
	public boolean isBlocked(int dx, int dy) {
		COORDINATE s = dest.transform(dx, dy);
		return SETT.ROOMS().copy.copier.canCopy(s.x(), s.y()) && !SETT.ROOMS().copy.copier.isPlacable(s.x(), s.y(), dx, dy);
	}
	
	public boolean isPartOfBlocked(int dx, int dy) {
		return placableBits.get(dx+dy*SETT.TWIDTH);
	}
	
}
