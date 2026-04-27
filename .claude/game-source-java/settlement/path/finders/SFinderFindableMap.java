package settlement.path.finders;

import static settlement.main.SETT.IN_BOUNDS;

import java.io.IOException;

import game.time.TIME;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.Bitmap2D;

public final class SFinderFindableMap{

	public final static int quad = 16;
	final static int W = SETT.TWIDTH/quad;
	final static int H = SETT.THEIGHT/quad;;
	
	final Bitmap2D  day;
	final Bitmap2D  tries;
	final Bitmap2D  fail;
	
	public SFinderFindableMap() {
		day = new Bitmap2D(W, H, false);
		tries = new Bitmap2D(W, H, false);
		fail = new Bitmap2D(W, H, false);
	}
	
	public void report(COORDINATE c, boolean success) {
		report(c.x(), c.y(), success);
	}
	
	public void report(int tx, int ty, boolean success) {
		if (!IN_BOUNDS(tx, ty))
			return;
		
		tx = tx >> 4;
		ty = ty >> 4;
		
		tries.set(tx, ty, true);
		day.set(tx, ty, (TIME.days().bitsSinceStart()&1) == 1);
		if (!success) {
			fail.set(tx, ty, true);
		}
	}
	
	public boolean has(int tx, int ty) {
		tx = tx >> 4;
		ty = ty >> 4;
		return tries.is(tx, ty);
	}
	
	public boolean fail(int tx, int ty) {
		tx = tx >> 4;
		ty = ty >> 4;
		return fail.is(tx, ty);
	}
	
	public boolean is(int tx, int ty) {
		tx = tx >> 4;
		ty = ty >> 4;
		return !fail.is(tx, ty);
	}

	void update(int i, boolean d) {
		if (tries.is(i) && day.is(i) == d) {
			tries.set(i, false);
			fail.set(i, false);
		}
	}

	void save(FilePutter file) {
		day.save(file);
		fail.save(file);
		tries.save(file);
		
	}
	
	void load(FileGetter file) throws IOException {
		day.load(file);
		fail.load(file);
		tries.load(file);
	}
	
	void clear() {
		day.clear();
		fail.clear();
		tries.clear();
	}
	

	
}
