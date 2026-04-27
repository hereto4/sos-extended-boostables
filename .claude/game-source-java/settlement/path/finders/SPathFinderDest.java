package settlement.path.finders;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import game.GAME;
import settlement.main.SETT;
import settlement.path.components.SComp0Level;
import settlement.path.components.SComponent;
import settlement.path.components.finder.SCompFinder.SCompPath;
import snake2d.LOG;
import snake2d.PathGame.COST;
import snake2d.PathTile;
import snake2d.PathUtilOnline;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;

final class SPathFinderDest {

	private final PathUtilOnline p;
	
	SPathFinderDest(PathUtilOnline p){
		this.p = p;
	}
	
	/**
	 * Finds the destination in the destination component in the comp path.
	 * If the comppath is smaller than 2, then startX and startY will be used and the pathtile is start -> destination 
	 * @param startX
	 * @param startY
	 * @param comp
	 * @param finder
	 * @return
	 */
	PathTile findDest(int startX, int startY, SCompPath comp, SFINDER finder) {
		
		
		SComponent parent = comp.path().get(0);
		final SComponent target = parent;
		

		if (!finder.isInComponent(target, comp.distance())) {
			LOG.ln("nay");
			LOG.ln(startX + " " + startY + " " + comp.path().size());
			LOG.ln(target.centreX() + " " + target.centreY());
			LOG.ln();
			return null;
		}
		
		Flooder f = p.getFlooder();
		f.init(SPathFinder.class);
		
		if (comp.path().size() >= 2) {
			parent = comp.path().get(1);
			markNeigh(parent, target);
		}else
			f.pushSloppy(startX, startY, 0);
		
		
		
		int tiles = 0;
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			tiles++;
			
			if (finder.isTile(t.x(), t.y(), 0)) {
				f.done();
				return t;
			}
			
			if (!target.is(t.x(), t.y()))
				continue;
			
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				int tx = t.x()+d.x();
				int ty = t.y()+d.y();
				if (!IN_BOUNDS(tx, ty))
					continue;
				if (finder.isTile(tx, ty, 0)) {
					t = f.force((short)tx, (short)ty, t.getValue2(), t);
					f.done();
					return t;
				}
				
				
				
				double cost = SETT.PATH().huristics.getCost(t.x(), t.y(),tx,ty);
				if (cost > 0) {
					cost*= d.tileDistance();
					f.pushSmaller(tx, ty, t.getValue()+cost, t);
					
				}else if(cost == COST.BLOCKED) {
					f.close(tx, ty, 0);
				}
				
			}

		}
		
		if (!SETT.PATH().willUpdate())
			GAME.Notify("nono " + startX + " " + startY + " " + parent.centreX() + " " + parent.centreY() + " " + target.centreX() + " " + target.centreY() + " " + tiles + " " + (target.level().get(target.centreX(), target.centreY()) + " " + comp.path().size() + " " + target));
		
		
		f.done();
		return null;
		
	}
	
	final void markNeigh(SComponent current, SComponent parent) {
		int x1 = (current.centreX() & ~(SComp0Level.SIZE-1));
		int y1 = (current.centreY() & ~(SComp0Level.SIZE-1));
		
		if (x1 - 1 >= 0) {
			int x = x1-1;
			boolean hit = false;
			for (int y = -1; y < SComp0Level.SIZE; y++) {
				if ( parent.is(x, y+y1)) {
					p.getFlooder().pushSloppy(x, y+y1, 0);
					hit |= true;
				}
			}
			if (hit)
				return;
		}
		
		if (x1 + SComp0Level.SIZE <= TWIDTH) {
			int x = x1 +  SComp0Level.SIZE;
			boolean hit = false;
			for (int y = 0; y <=  SComp0Level.SIZE; y++) {
				if (parent.is(x, y+y1)) {
					p.getFlooder().pushSloppy(x, y+y1, 0);
					hit |= true;
				}
			}
			if (hit)
				return;
		}
		
		if (y1 - 1 >= 0) {
			int y = y1-1;
			boolean hit = false;
			for (int x = 0; x <= SComp0Level.SIZE; x++) {
				if (parent.is(x+x1, y)) {
					p.getFlooder().pushSloppy(x+x1, y, 0);
					hit |= true;
				}
			}
			if (hit)
				return;
		}
		
		if (y1 + SComp0Level.SIZE <= THEIGHT) {
			int y = y1 + SComp0Level.SIZE;
			boolean hit = false;
			for (int x = -1; x < SComp0Level.SIZE; x++) {
				if (parent.is(x+x1, y)) {
					p.getFlooder().pushSloppy(x+x1, y, 0);
					hit |= true;
				}
			}
			if (hit)
				return;
		}
		GAME.Error(parent.index() + " " + current.index());
	}
	
}
