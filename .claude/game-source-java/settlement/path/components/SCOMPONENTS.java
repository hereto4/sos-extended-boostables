package settlement.path.components;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TWIDTH;

import settlement.main.SETT;
import settlement.path.components.finder.SCompFinder;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.GUTIL;
import view.sett.IDebugPanelSett;

public final class SCOMPONENTS {

	public final FindableDatas data = new FindableDatas();
	public final SComp0Level zero = new SComp0Level();
	public final LIST<SCompNLevel> levels;
	public final SCompNLevel last;
	public final LIST<SComponentLevel> all;
	public final SCompFinder pather;
	
	private boolean debug = false;
	
	public SCOMPONENTS(){
		
		final int increase = 8;
		
		int ls = 0;
		int ss = SComp0Level.SIZE*increase;
		while(ss <= SETT.TWIDTH) {
			ss *= increase;
			ls++;
		}
		ls++;
		ArrayList<SComponentLevel> all = new ArrayList<>(ls+1);
		all.add(zero);
		ArrayList<SCompNLevel> levels = new ArrayList<>(ls);
		ss = SComp0Level.SIZE*increase;
		ls = 1;
		while(levels.hasRoom()) {
			SCompNLevel l = new SCompNLevel(all.get(all.size()-1), ls, ss);
			levels.add(l);
			all.add(l);
			ss *= increase;
			if (ss > SETT.TWIDTH)
				ss = SETT.TWIDTH;
			ls++;
		}
		this.levels = levels;
		last = levels.get(levels.size()-1);
		this.all = all;

		
		pather = new SCompFinder(this, GUTIL.pathTools());
		
		new SCompTests(this);
		new SCompUI(this);
		
		IDebugPanelSett.add("comp debug", new ACTION() {
			
			@Override
			public void exe() {
				debug = !debug;
			}
		});
		
	}
	
	public void clear() {
		for (SComponentLevel l : all)
			l.init();
	}
	
	public void init() {
		for (SComponentLevel l : all)
			l.init();
		update();
		
//		check();
	}
	
	public void update() {
	
		if (debug) {
			for (int k = 0; k < 10; k++) {
				int x = RND.rInt(SETT.TWIDTH);
				int y = RND.rInt(SETT.THEIGHT);
				DIR d = DIR.ALL.rnd();
				TerrainTile t = RND.rBoolean() ? SETT.TERRAIN().NADA : SETT.TERRAIN().BUILDINGS.all().get(0).wall;
				
				for (int i = 0; i < 8; i++) {
					int tx = x + d.x()*i;
					int ty = y + d.y()*i;
					if (SETT.IN_BOUNDS(tx, ty))
						t.placeFixed(tx, ty);
				}
			}
			
			for (int k = 0; k < 10; k++) {
				int x = RND.rInt(SETT.TWIDTH);
				int y = RND.rInt(SETT.THEIGHT);
				DIR d = DIR.ALL.rnd();
				
				for (int i = 0; i < 8; i++) {
					int tx = x + d.x()*i;
					int ty = y + d.y()*i;
					if (SETT.IN_BOUNDS(tx, ty))
						updateService(tx, ty);
				}
			}
			
			for (SComponentLevel l : all)
				l.update();
			
			for (COORDINATE c : SETT.TILE_BOUNDS) {
				SComponent co = zero.get(c);
				while(co != null) {
					SComponentEdge e = co.edgefirst();
					while(e != null) {
						
						if (e.to() != e.to().level().get(e.to().centreX(), e.to().centreY())) {
							LOG.ln("eye!");
						}
						e = e.next();
					}
					co = co.superComp();
				}
				
			}
			
		}
		
		for (SComponentLevel l : all)
			l.update();
	}
	
//	public void check() {
//		SComponentChecker check = new SComponentChecker(zero);
//		check.init();
//		for (COORDINATE c : SETT.TILE_BOUNDS) {
//			SComponent cc = zero.get(c);
//			if (cc != null && !check.isSetAndSet(cc)) {
//				while(cc != null) {
//					if (cc.level().get(cc.centreX(), cc.centreY()) != cc) {
//						System.err.println(cc.centreX() + " " + cc.centreY() + " " + cc.level().level());
//					}
//					cc = cc.superComp();
//				}
//				
//				
//			}
//			
//			
//		}
//		
//	}
	
	public void updateAvailability(int tx, int ty) {
		zero.update(tx, ty);
	}
	
	public void updateService(int tx, int ty) {
		zero.changeSerives(tx, ty);
	}
	
	public final MAP_OBJECT<SComponent> superComp = new MAP_OBJECT<SComponent>() {

		@Override
		public SComponent get(int tile) {
			SComponent s = zero.get(tile);
			while(s != null && s.superComp() != null)
				s = s.superComp();
			return s;
		}

		@Override
		public SComponent get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return null;
		}
	
	};
	
}
