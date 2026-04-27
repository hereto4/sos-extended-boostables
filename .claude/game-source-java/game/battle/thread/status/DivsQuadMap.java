package game.battle.thread.status;

import java.util.Arrays;

import game.GAME;
import game.battle.Army;
import game.battle.div.Div;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.CircleCooIterator;
import snake2d.util.map.MAP_OBJECT_ISSER;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Tree;
import util.GUTIL;

public final class DivsQuadMap {

	
	
	public final static int size = 32;
	private final QDiv[][] map = new QDiv[SETT.TWIDTH/size][SETT.THEIGHT/size];
	private final Bitsmap1D artillery = new Bitsmap1D(0, 2, map.length*map[0].length);
	private final QDiv[] free = new QDiv[Config.battle().DIVISIONS_PER_BATTLE];
	private final CircleCooIterator iter = new CircleCooIterator((int)Math.ceil(Math.sqrt(map[0].length*map[0].length + map.length*map.length)), GUTIL.flooder());
	private int freeI = 0;
	private final static int add_scroll = Integer.numberOfTrailingZeros(size*C.TILE_SIZE);
	private final static int a_scroll = Integer.numberOfTrailingZeros(size);
	private final Tree<QDiv> sort = new Tree<DivsQuadMap.QDiv>(free.length) {

		@Override
		protected boolean isGreaterThan(QDiv current, QDiv cmp) {
			return current.dist > cmp.dist;
		}
		
	};
	
	public DivsQuadMap() {
		
		for (int i = 0; i < free.length; i++)
			free[i] = new QDiv();
		
	}
	
	void add(Div div, int pcx, int pcy) {
		
		
		pcx = pcx >> add_scroll;
		if (pcx < 0 || pcx >= map[0].length)
			return;
		
		pcy = pcy >> add_scroll;
		if (pcy < 0 || pcy >= map.length)
			return;
		
		QDiv old = map[pcy][pcx];
		QDiv n = free[freeI];
		freeI++;
		n.next = old;
		n.divI = div.index();
		map[pcy][pcx] = n;
	}
	
	void clear(){
		for (QDiv[] qq : map)
			Arrays.fill(qq, null);
		artillery.clear();
		freeI = 0;
	}
	
	public void getNearest(LISTE<Div> res, int px, int py, int pixelDistance, Army target, Div self) {
		
		if (!res.hasRoom())
			return;
		
		int fx = px;
		int fy = py;
		
		px = px >> add_scroll;
		py = py >> add_scroll;
		
		
		int ra = (int) Math.ceil((double)pixelDistance/(C.TILE_SIZE*size));
		ra = CLAMP.i(ra, 0, iter.radius(iter.length()-1));
		sort.clear();
		
		int rac = 0;
		int i = 0;
		while(iter.radius(i) <= ra) {
			if (rac != iter.radius(i)) {
				while(sort.hasMore() && res.hasRoom()) {
					res.add(GAME.ARMIES().divisions().get(sort.pollSmallest().divI));
				}
				if (!res.hasRoom())
					return;
				rac = iter.radius(i);
			}
			int dx = iter.get(i).x();
			int dy = iter.get(i).y();
			i++;
			int pcx = px + dx;
			if (pcx < 0 || pcx >= map[0].length)
				continue;
		
			int pcy = py + dy;
			if (pcy < 0 || pcy >= map.length)
				continue;
			
			QDiv f = map[pcy][pcx];
			while(f != null) {
				Div d = GAME.ARMIES().divisions().get(f.divI);
				if (d != self && d.army() == target) {
					int xx = d.centre().cUnitX()-fx;
					int yy = d.centre().cUnitY()-fy;
					int dist = (int)Math.sqrt(xx*xx+yy*yy);
					if (dist < pixelDistance) {
						f.dist = dist;
						sort.add(f);
					}
				}
				f = f.next;
			}
		}
		
		while(sort.hasMore() && res.hasRoom()) {
			res.add(GAME.ARMIES().divisions().get(sort.pollSmallest().divI));
		}

	}
	
	public void getInQuad(LISTE<Div> res, int tx, int ty, Army target) {
		tx = tx >> a_scroll;
		ty = ty >> a_scroll;
		QDiv f = map[ty][tx];
		while(f != null && res.hasRoom()) {
			Div d = GAME.ARMIES().divisions().get(f.divI);
			if (d.army() == target) {
				res.add(d);
			}
			f = f.next;
		}
	}
	
	private static final class QDiv {
		private int dist;
		private QDiv next;
		private short divI;
	}
	
	public MAP_OBJECT_ISSER<Army> ART = new MAP_OBJECT_ISSER<Army>() {

		@Override
		public boolean is(int tile, Army value) {
			return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH, value);
		}

		@Override
		public boolean is(int tx, int ty, Army value) {
			tx = tx >> a_scroll;
			ty = ty >> a_scroll;
			return (artillery.get(tx+ty*map[0].length) & value.bit) != 0;
		}
		
	};
	

	
	void addArtillery(ArtilleryInstance ins) {
		
		int tx = ins.body().cX();
		int ty = ins.body().cY();
		tx = tx >> a_scroll;
		ty = ty >> a_scroll;
		int i = artillery.get(tx+ty*map[0].length);
		i |= ins.army().bit;
		artillery.set(tx+ty*map[0].length, i);
		
	}
	
}
