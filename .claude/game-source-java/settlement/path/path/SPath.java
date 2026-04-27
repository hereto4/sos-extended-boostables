package settlement.path.path;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;

import java.io.IOException;

import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.finders.SFINDER;
import settlement.path.finders.SPathFinder.SPathUtilResult;
import settlement.path.thread.FinderThread.ThreadPath;
import snake2d.LOG;
import snake2d.PathGame;
import snake2d.PathTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;

public class SPath extends PathGame.PathFancy {

	protected short destX, destY;
	protected boolean successful = false;
	protected boolean arrived = false;
	private boolean full = false;
	public static final int size = 256;
	private int sx, sy;

	public SPath() {
		super(size);
	}

	private static Text t = UI.FONT().M.getText(5).adjustWidth();
	public final ThreadPath thread = new ThreadPath();
	
	
	@Override
	public void save(FilePutter file) {
		file.i(destX).i(destY).i(sx).i(sy);
		file.bool(successful).bool(arrived).bool(full);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		destX = (short) file.i();
		destY = (short) file.i();
		sx = file.i();
		sy = file.i();
		successful = file.bool();
		arrived = file.bool();
		full = file.bool();
//		PathFancy f = new PathFancy(256);
//		f.load(file);
//		f.copyTo(this);
		super.load(file);
	}
	
	public void render(SPRITE_RENDERER r, int offsetX, int offsetY) {

		if (!successful)
			return;
		if (length() == 0)
			return;

		COLOR.YELLOW100.bind();
		
		int i = 0;
		t.clear();

		SPRITE s = SPRITES.cons().ICO.crosshair;
		
		int tileI = getCurrentI();

		int x = x() * C.TILE_SIZE + offsetX;
		int y = y() * C.TILE_SIZE + offsetY;
		int d = 0; //(C.TILE_SIZE - Icon.M) / 2;
		s.render(r, x + d, y + d);
		t.clear().add(i++);
		t.render(r, x, y);

		while (super.setNext()) {
			t.clear().add(i++);
			x = super.x() * C.TILE_SIZE + offsetX;
			y = super.y() * C.TILE_SIZE + offsetY;
			s.render(r, x + d, y + d);
			t.render(r, x, y);
		}
		
		COLOR.unbind();

		setCurrentI(tileI);
	}

	public boolean request(int startX, int startY, SFINDER f, int maxDistance) {
		this.full = false;
		this.sx = startX;
		this.sy = startY;
		this.destX = -1;
		this.destY = -1;
		this.successful = false;
		
		if (setNear(startX, startY, f, full)) {
			this.successful = true;
			return true;
		}
		
		SPathUtilResult r = SETT.PATH().finders.finder().find(startX, startY, f, maxDistance);
		if (r != null) {
			this.successful = true;
			this.destX = (short) r.destX;
			this.destY = (short) r.destY;
			PathTile t = r.t;
			sett(t);
			
		}
		
		return successful;

	}
	
	public boolean request(Humanoid start, int dx, int dy) {
		return request(start.tc().x(), start.tc().y(), dx, dy, false);
	}
	
	public boolean request(COORDINATE start, COORDINATE to) {
		return request(start.x(), start.y(), to.x(), to.y(), false);
	}
	
	public boolean request(COORDINATE start, int dx, int dy) {
		return request(start.x(), start.y(), dx, dy, false);
	}

	public boolean request(int sx, int sy, COORDINATE to) {
		return request(sx, sy, to.x(), to.y(), false);
	}
	
	public boolean requestFull(COORDINATE start, COORDINATE to) {
		return request(start.x(), start.y(), to.x(), to.y(), true);
	}
	
	public boolean requestFull(COORDINATE start, int dx, int dy) {
		return request(start.x(), start.y(),dx, dy, true);
	}
	
	public boolean requestFull(int sx, int sy, int dx, int dy) {
		return request(sx, sy,dx, dy, true);
	}
	
	public boolean request(int startX, int startY, int destX, int destY, boolean full) {
		this.full = full;
		this.sx = startX;
		this.sy = startY;
		this.destX = (short) destX;
		this.destY = (short) destY;
		this.successful = false;

		if (!SETT.TILE_BOUNDS.holdsPoint(sx, sy)) {
			clear();
			return false;
		}
		
		if (full && !SETT.IN_BOUNDS(destX, destY)) {
			if (!SETT.IN_BOUNDS(destX, destY)) {
				clear();
				return false;
			}
		}else {
			if (!SETT.TILE_BOUNDS.touches(destX, destY)){
				clear();
				return false;
			}
		}
		
		successful = false;
		
		if (!SETT.TILE_BOUNDS.holdsPoint(startX, startY)) {
			return false;
		}
		
		if (full && SETT.PATH().availability.get(destX, destY).player < 0)
			return false;
		
		
		
		if (setNear(startX, startY, destX, destY, full)) {
			this.successful = true;
			return true;
		}
		
		PathTile t = SETT.PATH().finders.finder().find(startX, startY, destX, destY, full);
		
		if (t != null) {
			sett(t);
			
			successful = true;
		}
		
		return successful;

	}
	

	
	private boolean resumeThreaded(int startX, int startY) {
		
		
		
		successful = false;
		
		if (!SETT.TILE_BOUNDS.holdsPoint(startX, startY)) {
			return false;
		}
		
		if (full && SETT.PATH().availability.get(destX, destY).player < 0)
			return false;
		
		
		
		if (setNear(startX, startY, destX, destY, full)) {
			this.successful = true;
			return true;
		}
		
//		count++;
//		
//		if (count > 1000) {
//			LOG.ln(processed + " " + count);
//			count = 0;
//			processed = 0;
//		}
		

		if (thread.isProcessed(startX, startY, destX, destY)) {
			if (thread.isSuccess()) {
				arrived = thread.path.isCompleate();
				thread.path.copyTo(this);
				thread.path.setCurrentI(0);
				while(thread.path.hasNext()) {
					PATH().huristics.set(thread.path.x(), thread.path.y());
					thread.path.setNext();
				}
				
				super.setCurrentI(0);
				successful = true;
				if (full && (thread.destX != destX || thread.destY != destY)) {
					SETT.PATH().thread.prep(this, thread.destX, thread.destY, destX, destY, full);
				}else if ((Math.abs(thread.destX - destX) + Math.abs(thread.destY - destY) > 1)){
					SETT.PATH().thread.prep(this, thread.destX, thread.destY, destX, destY, full);
				}
				
			}
			return successful;
		}
		
		PathTile t = SETT.PATH().finders.finder().find(startX, startY, destX, destY, full);
		
		
		if (t != null) {
			sett(t);
			
			successful = true;
		}
		
		
		
		
		return successful;

	}
	
	private boolean setNear(int startX, int startY, SFINDER f, boolean full) {
		
		if (f.isTile(startX, startY, 0)) {
			destX = (short) startX;
			destY = (short) startY;
			return setNear(startX, startY, startX, startY, full);
		}

		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			int destX = startX + DIR.ORTHO.get(i).x();
			int destY = startY + DIR.ORTHO.get(i).y();
			if (f.isTile(destX, destY, 0)){
				if (setNear(startX, startY, destX, destY, full)) {
					this.destX = (short) destX;
					this.destY = (short) destY;
				}
			}
		}
		return false;
	}
	
	private boolean setNear(int startX, int startY, int destX, int destY, boolean full) {
		if (startX == destX && startY == destY) {
			if (full) {
				setOne(startX, startY);
				return true;
			}
				
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (IN_BOUNDS(destX, destY, d) && SETT.PATH().availability.get(destX, destY, d).player > 0) {
					setTwo(startX, startY, startX + d.x(), startY + d.y());
					return true;
				}
			}
			return false;
		}

		if (Math.abs(startX - destX) + Math.abs(startY - destY) == 1) {
			if (full) {
				setTwo(startX, startY, destX, destY);
				return true;
			}
			setOne(startX, startY);
			return true;
		}
		return false;
	}

	public boolean resume(COORDINATE start, RECTANGLE body) {
		
		if (!successful)
			return false;
		
		if (start.isSameAs(this))
			return !PATH().solidity.is(this);
		
		if (PATH().solidity.is(this)) {
			return request(start.x(), start.y(), destX, destY, full);
		}
		
		int x = x();
		int y = y();
		if (setPrev() && start.isSameAs(this)) {
			if (PATH().coster.player.getCost(start.x(),  start.y(), x, y) >= 0)
				return true;
		}
		
		return request(start.x(), start.y(), destX, destY, full);
			
	}
	
	@Override
	public boolean setNext() {

		if (!successful)
			throw new RuntimeException();

		if (isDest())
			return false;
		
		if (!super.hasNext()) {
			resumeThreaded(x(), y());
			super.setNext();
		}else {
			super.setNext();
		}
		
		return successful;		

	}
	/**
	 * 
	 * @return centre of tile in game coordiantes
	 */
	public int getSettCX() {
		return (x() << C.T_SCROLL) + C.TILE_SIZEH;
	}

	/**
	 * 
	 * @return centre of tile in game coordiantes
	 */
	public int getSettCY() {
		return (y() << C.T_SCROLL) + C.TILE_SIZEH;
	}



	@Override
	public boolean isDest() {
		if (!successful)
			return false;
		if (super.hasNext())
			return false;
		if (full)
			return x() == destX && y() == destY;
		
		return (Math.abs(x() - destX) + Math.abs(y() - destY) == 1);
	}

	public boolean isSuccessful() {
		return successful;
	}

	public short destX() {
		return destX;
	}

	public short destY() {
		return destY;
	}
	
	public boolean isFull() {
		return full;
	}
	
	public String toDebugString() {
		return "(" + "("+sx+","+sy+") --> (" + destX + "," + destY + ") succ:" + isSuccessful() + ", dest:" + isDest() + "\n "
				+ x() + " " + y() + " " + length() + " " + getCurrentI(); 
	}
	
	public static double LAST_DISTANCE() {
		return SETT.PATH().finders.finder().lastDistance;
	}
	
	@Override
	public void clear() {
		successful = false;
		full = false;
		super.clear();
	}
	
	private void sett(PathTile t) {

		set(t);
		if ((full && !t.isSameAs(destX, destY)) || (!full && (Math.abs(t.x() - destX) + Math.abs(t.y() - destY) > 1))){
			
			SETT.PATH().thread.prep(this, t.x(), t.y(), destX, destY, full);
			
		}
		while(t != null) {
			PATH().huristics.set(t.x(), t.y());
			t = t.getParent();
		}
	}
	
	public void setDirect(int sx, int sy, int destX, int destY, PathTile t, boolean full) {
		
		
		successful = true;
		this.destX = (short) destX;
		this.destY = (short) destY;
		this.sx = sx;
		this.sy = sy;
		this.full = full;
		sett(t);
		arrived = isCompleate();
	}
	
	public void copy(PathFancy other, int destX, int destY, boolean full) {
		
		
		other.setCurrentI(0);
		other.copyTo(this);
		successful = true;
		
		this.full = full;
		sx = other.x();
		sy = other.y();
		while(other.hasNext()) {
			PATH().huristics.set(other.x(), other.y());
			other.setNext();
		}
		
		this.destX = (short) destX;
		this.destY = (short) destY;
		arrived = other.isCompleate();

	}
	
	@Override
	public void setLength(int ll) {
		super.setLength(ll);
		int iold = getCurrentI();
		while(super.setNext()) {
			destX = (short) x();
			destY = (short) y();
		}
		setCurrentI(iold);
	}
	
	@Override
	public void debug() {
		int iold = getCurrentI();
		setCurrentI(0);
		LOG.ln();
		LOG.ln("l:" + length());
		for (int i = 0; i < length(); i++) {
			setCurrentI(i);
			LOG.ln("\t" + "("+x() + " " + y()+")");
		}
		setCurrentI(iold);
		LOG.ln(destX + " " + destY);
	}

}
