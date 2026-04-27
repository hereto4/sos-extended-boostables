package world.map.pathing;

import java.io.IOException;

import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitsmap2D;
import snake2d.util.sets.LIST;
import util.GUTIL;
import world.WORLD;
import world.map.road.WTRAV;

public final class WDirMap {

	private final Bitsmap2D m = new Bitsmap2D(0, 8, WORLD.TBOUNDS());
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			m.save(file);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			m.load(file);
			
		}
		
		@Override
		public void clear() {
			m.clear();
		}
	};
	
	public final MAP_BOOLEAN is = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return m.get(tx, ty) != 0;
		}
		
		@Override
		public boolean is(int tile) {
			return m.get(tile) != 0;
		}
	};
	
	public boolean can(int fromX, int fromY, DIR d) {
		return (m.get(fromX, fromY) & d.bit) != 0;
	}
	
	public boolean can(int tile, DIR d) {
		return (m.get(tile) & d.bit) != 0;
	}
	
	public boolean can(COORDINATE from, DIR d) {
		return can(from.x(), from.y(), d);
	}
	
	public boolean can(int fromX, int fromY, int di) {
		return can(fromX, fromY, DIR.ALL.get(di));
	}
	
	public boolean can(COORDINATE c, int di) {
		return can(c.x(), c.y(), DIR.ALL.get(di));
	}
	
	public boolean isOnly(COORDINATE c, DIR d) {
		int mm = d.bit | d.perpendicular().bit;
		return m.get(c) == mm;
	}
	
	public boolean isOnly(int tx, int ty, DIR d) {
		int mm = d.bit | d.perpendicular().bit;
		return m.get(tx, ty) == mm;
	}

	int get(PathTile t) {
		return m.get(t);
	}
	
	void add(int tx, int ty, DIR d) {
		int s = m.get(tx, ty);
		s |= d.bit;
		m.set(tx, ty, s);
		if (WORLD.IN_BOUNDS(tx, ty, d)) {
			s = m.get(tx, ty, d);
			s |= d.perpendicular().bit;
			m.set(tx, ty, d, s);
		}
		
	}
	
	void remove(int tx, int ty) {
		int s = m.get(tx, ty);
		for (int di = 0; di < DIR.ALL.size(); di++) {
			DIR d = DIR.ALL.get(di);
			if ((s & d.bit) != 0){
				s &= ~d.bit;
				
				if (WORLD.IN_BOUNDS(tx,ty, d)) {
					int sd = m.get(tx, ty, d);
					sd &= ~d.perpendicular().bit;
					m.set(tx, ty, d, sd);
				}
			}
		}
		m.set(tx, ty, s);
		
	}
	
	void add(COORDINATE c, DIR d) {
		add(c.x(), c.y(), d);
	}
	
	private static final LIST<DIR> dirs = new ArrayList<>(DIR.ALL);
	
	public void push(PathTile t, double v) {
		int md = m.get(t);
		for (DIR d : dirs) {
			if ((md & (d.bit)) != 0)
				GUTIL.flooder().pushSmaller(t, d, v+d.tileDistance()*cost(t.x(), t.y(), d), t);
		}
	}
	
	public void pushSimple(PathTile t) {
		int md = m.get(t);
		for (DIR d : dirs) {
			if ((md & (d.bit)) != 0)
				GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance(), t);
		}
	}
	
	public static int cost(int fromX, int fromY, DIR d) {
		if (WORLD.WATER().isBig.is(fromX, fromY)) {
			return 1;
		}
		int toX = fromX + d.x();
		int toY = fromY + d.y();
		if (WORLD.WATER().isBig.is(toX, toY))
			return WTRAV.PORT_PENALTY;
		
		if (WORLD.MOUNTAIN().coversTile(fromX, fromY))
			return 6;
		if (WORLD.FOREST().amount.get(fromX, fromY) == 1.0)
			return 4;
		return 3;
	}
	
}
