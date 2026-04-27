package settlement.path.finders;

import static settlement.main.SETT.PATH;

import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.components.FindableDataSingle;
import settlement.path.components.FindableDatas;
import settlement.path.components.SComponent;
import settlement.path.components.finder.SCompFinder.SCompPath;
import settlement.path.finders.SPathFinder.SPathUtilResult;
import settlement.path.path.SPath;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import view.sett.IDebugPanelSett;

public final class SFinderEntity{
	
	SFinderEntity() {
		IDebugPanelSett.add("find safety", new ACTION() {
			
			@Override
			public void exe() {
				long n = System.currentTimeMillis();
				int sx = RND.rInt(SETT.TWIDTH);
				int sy = RND.rInt(SETT.THEIGHT);
				int max = 100;
				enemies = s().people(true);
				SComponent s = PATH().comps.superComp.get(sx, sy);
				if (s != null) {
					SCompPath p = PATH().comps.pather.find(sx, sy, findSafety, max, 16);
					if (p != null) {
						SPathUtilResult r = SETT.PATH().finders.finder().find(sx, sy, findSafety, max, p);
						if (r != null)
							LOG.ln("yay " + sx + " " + sy + " " + r.destX + " " + r.destY);
							
					}
				}
				LOG.ln("" + (System.currentTimeMillis()-n));
			}
		});
	}
	
	private FindableDataSingle enemies;
	
	public void report(ENTITY e, int delta) {
		if (e instanceof Humanoid) {			
			FindableDataSingle s = s().people(!((Humanoid)e).indu().hostile());
			if (delta > 0)
				s.reportPresence(e.ssx(), e.ssy());
			else
				s.reportAbsence(e.ssx(), e.ssy());
		}else if (e instanceof Animal) {
			
			if (((Animal)e).huntReservable()) {

				if (delta == 1) {
					s().reservableAnimals.reportPresence(e.ssx(), e.ssy());
				}else
					s().reservableAnimals.reportAbsence(e.ssx(), e.ssy());
			}
		}
	}

	
	private final FindableDatas s() {
		return PATH().comps.data;
	}
	
	public int getEnemies(Humanoid asker, int tx, int ty) {
		SComponent c = SETT.PATH().comps.zero.get(tx, ty);
		if (c == null)
			return 0;
		return s().people(asker.indu().hostile()).get(c);
	}
	
	public int getFriendlies(Humanoid asker, int tx, int ty) {
		SComponent c = SETT.PATH().comps.zero.get(tx, ty);
		if (c == null)
			return 0;
		return s().people(!asker.indu().hostile()).get(c);
	}
	
	public int getAny(int tx, int ty) {
		SComponent c = SETT.PATH().comps.zero.get(tx, ty);
		if (c == null)
			return 0;
		return s().people(true).get(c) + s().people(false).get(c);
	}
	
	public boolean findExitNoEnemies(Humanoid asker, int sx, int sy, SPath path, int max) {
		
		enemies = s().people(asker.indu().hostile());
		SComponent s = PATH().comps.superComp.get(sx, sy);
		if (s != null && s.hasEdge()) {
			SCompPath p = PATH().comps.pather.find(sx, sy, rout, max, 16);
			if (p != null) {
				SPathUtilResult r = SETT.PATH().finders.finder().find(sx, sy, rout, max, p);
				if (r != null) {
					path.setDirect(sx, sy, r.destX, r.destY, r.t, false);
					return true;
				}
					
			}
		}
		return false;
		
	}
	
	public boolean findSafety(Humanoid asker, int sx, int sy, SPath path, int max) {
		
		enemies = s().people(asker.indu().hostile());
		SComponent s = PATH().comps.superComp.get(sx, sy);
		if (s != null) {
			SCompPath p = PATH().comps.pather.find(sx, sy, findSafety, max, 16);
			if (p != null) {
				SPathUtilResult r = SETT.PATH().finders.finder().find(sx, sy, findSafety, max, p);
				if (r != null) {
					path.setDirect(sx, sy, r.destX, r.destY, r.t, false);
					return true;
				}
					
			}
		}
		return false;
		
	}
		
	private final SFINDER findSafety = new SFINDER() {
		
		private int tx, ty;
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			if (enemies.get(c) == 0) {
//				SComponentEdge e = c.edgefirst();
//				while(e != null) {
//					if (enemies.get(e.to()) > 0)
//						return false;
//					e = e.next();
//				}
				COORDINATE coo = c.rndCoo();
				tx = coo.x();
				ty = coo.y();
				return true;
			}
			return false;
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			return tx == this.tx && ty == this.ty;
		}
		
		@Override
		public boolean canCross(SComponent c) {
			return enemies.get(c) == 0;
		};
		
	};
	
	private final SFINDER rout = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return c.hasEdge();
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			if (tx == 0 || tx == SETT.TWIDTH-1 || ty == 0 || ty == SETT.THEIGHT-1)
				return !SETT.PATH().solidity.is(tx, ty);
			return false;
		}
		
		@Override
		public boolean canCross(SComponent c) {
			return enemies.get(c) == 0;
		};
		
	};
	



}

