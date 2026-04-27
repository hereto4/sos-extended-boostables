package world.map.pathing;

import java.io.IOException;

import init.constant.C;
import snake2d.PathGame;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLEE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.entity.WEntity;
import world.map.pathing.WRegFinder.Treaty;
import world.map.road.WTRAV;

public abstract class WPath implements SAVABLE {

	private short destX, destY = -1;
	private byte dir;
	private float movement = 0;
	final PathGame.PathSimple tilePath = new PathGame.PathSimple(256);
	private static final double sqrt = Math.sqrt(2);



	public WPath() {
		clear();
		dir = (byte) RND.rInt(DIR.ALL.size());
	}

	@Override
	public void save(FilePutter file) {
		tilePath.save(file);
		file.s(destX);
		file.s(destY);
		file.b(dir);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		tilePath.load(file);
		destX = file.s();
		destY = file.s();
		dir = file.b();
	}

	@Override
	public void clear() {
		tilePath.clear();
		destX = -1;
		destY = -1;
	}
	
	public void copyTo(WPath tmp) {
		tmp.destX = destX;
		tmp.destY = destY;
		tmp.dir = dir;
		tilePath.copyTo(tmp.tilePath);
	}

	public int destX() {
		return destX;
	}

	public int destY() {
		return destY;
	}

	public boolean isValid() {
		return destX >= 0 && destY >= 0;
	}

	public DIR dir() {
		return DIR.ALL.get(dir);
	}

	public boolean arrived() {
		return destX == tilePath.x() && destY == tilePath.y();
	}

	public int x() {
		return tilePath.x();
	}

	public int y() {
		return tilePath.y();
	}

	
	public boolean find(int sx, int sy, int destX, int destY) {
		
		clear();
		if (sx == destX && sy == destY) {
			this.destX = (short) destX;
			this.destY = (short) destY;
			tilePath.setOne(destX, destY);
			return true;
		}
		
		if (!WORLD.PATH().map.is.is(destX, destY)) {
			return false;
		}
		
		if (!WORLD.PATH().map.is.is(sx, sy)) {
			
			DIR d = DIR.get(sx, sy, destX, destY);
			if (WORLD.PATH().map.is.is(sx, sy, d))
				;
			else if (WORLD.PATH().map.is.is(sx, sy, d.next(1)))
				d = d.next(1);
			else if (WORLD.PATH().map.is.is(sx, sy, d.next(-1)))
				d = d.next(-1);
			else
				return false;
			sx += d.x();
			sy += d.y();
		}

		PathTile t = WORLD.PATH().path(sx,  sy,  destX, destY, treaty());

		if (t != null) {
			tilePath.set(t);
			this.destX = (short) destX;
			this.destY = (short) destY;
			
			return true;
		}
		
		return false;
	}
	
	public boolean move(WEntity ent, double speed) {
		
		RECTANGLEE e = ent.body();
		
		speed *= WPATHING.movementSpeed(ent.ctx(), ent.cty());
		if (!WORLD.WATER().is(ent.ctx(), ent.cty()) && WORLD.WATER().isBig.is(x(), y())) {
			speed /= WTRAV.PORT_PENALTY*2;
		}
		
		return move(e, speed);
		
	}
	
	public boolean move(RECTANGLEE e, double speed) {
		
		if (!moving(e))
			return false;
		
		
		movement += speed;
		
		while(movement > sqrt && isValid())
			if (!move(e)) {
				movement = 0;
				return false;
			}
		
		return true;
	}
	
	private boolean move(RECTANGLEE e) {
		
		int cx = e.cX();
		int cy = e.cY();
		
		int destx = x()*C.TILE_SIZE + C.TILE_SIZEH;
		int desty = y()*C.TILE_SIZE + C.TILE_SIZEH;
		
		int x = 0;
		int y = 0;
		while (movement > sqrt) {
			if (cx == destx && cy == desty) {
				return setNext();
			}
			x = destx-cx;
			y = desty-cy;
			x = CLAMP.i(x, -1, 1);
			y = CLAMP.i(y, -1, 1);
			
			if (Math.abs(x) != Math.abs(y)) {
				movement -= 1;
			}else {
				movement -= sqrt;
			}
		
			
			cx += x;
			cy += y;
			
		}
		
		dir = (byte) DIR.get(x,y).id();
		e.moveC(cx, cy);
		return true;
		

	}
	
	public boolean moving(RECTANGLEE e) {
		
		if (destX == -1 || destY == -1) {
			movement = 0;
			return false;
		}
		
		if (x() == destX && y() == destY) {
			int cx = e.cX();
			int cy = e.cY();
			int dx = x()*C.TILE_SIZE + C.TILE_SIZEH;
			int dy = y()*C.TILE_SIZE + C.TILE_SIZEH;
			if (cx == dx && cy == dy)
				return false;
		}
		return true;
	}

	public int remaining() {
		return tilePath.length()-tilePath.getCurrentI();
	}

	public boolean setNext() {
		

		if (tilePath.setNext()) {
			return true;
		}
		
		if (destX == tilePath.x() && destY == tilePath.y())
			return false;

		if (find(tilePath.x(), tilePath.y(), destX, destY)) {
			
			tilePath.setNext();
			return true;
		}

		return false;

	}
	
	public abstract Treaty treaty();

}
