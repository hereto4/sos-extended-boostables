package game.battle.thread.general.offence;

import game.GAME;
import game.battle.thread.general.StrategosUtil;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.thing.projectiles.Trajectory;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.Bitmap2D;

class StepArtilleryBombard {
	
	private final int absSize = 8;
	private final Bitmap2D mapArt;
	private final Bitmap2D mapBreak;
	{
		int wi = (int) Math.ceil((double)SETT.TWIDTH/absSize);
		int hi = (int) Math.ceil((double)SETT.THEIGHT/absSize);
		mapArt = new Bitmap2D(wi, hi, false);
		mapBreak = new Bitmap2D(wi, hi, false);
	}
	
	
	private final StrategosUtil c;
	
	private final ArrayListResize<ArtilleryInstance> ins = new ArrayListResize<>(256);
	private final ArrayList<ArtilleryInstance> tmp = new ArrayList<ArtilleryInstance>(32);
	private final Trajectory traj = new Trajectory();
	
	StepArtilleryBombard(StrategosUtil context){
		this.c = context;
	}
	
	void bombard() {
		ins.clearSoft();
		
		for (int ai = 0; ai < SETT.ROOMS().ARTILLERY.size(); ai++) {
			SETT.ROOMS().ARTILLERY.get(ai).threadInstances(ins);
		}
		for (int ii = 0; ii < ins.size(); ii++) {
			ArtilleryInstance i = ins.get(ii);
			if (i.menMustering() == 0 || i.isFiring() || i.army() != c.getArmy()) {
				ins.remove(ii);
				ii--;
			}
		}
		
		if (ins.size() > 0) {

			mapBreak.clear();
			for (int y = 0; y < SETT.THEIGHT; y++) {
				for (int x = 0; x < SETT.TWIDTH; x++) {
					if (SETT.PATH().availability.get(x, y).isSolid(c.getArmy()) || GAME.ARMIES().map.attackable.is(x, y, c.getArmy())) {
						int ax = x/absSize;
						int ay = y/absSize;
						mapBreak.set(ax, ay, true);
					}
				}
			}
			
		
			for (ArtilleryInstance i : ins) {
				int ax = i.mX()/absSize;
				int ay = i.mY()/absSize;
				
				mapArt.set(ax, ay, true);
				
			}
			
			Flooder f = c.flooder.getFlooder();
			f.init(this);
			{
				
				int ax = c.getDestCoo().x()/absSize;
				int ay = c.getDestCoo().y()/absSize;
				f.pushSloppy(ax, ay, 0);
			}
			

			
			while(f.hasMore() && ins.size() > 0) {
				PathTile t = f.pollSmallest();
				if (mapArt.is(t)) {
					pushTarget(t, ins);
					continue;
				}
				
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					int dx = t.x()+d.x();
					int dy = t.y()+d.y();
					if (mapBreak.body().holdsPoint(dx, dy)) {
						double v = mapBreak.is(dx, dy) ? 8 : 1;
						f.pushSmaller(dx, dy, t.getValue()+v*d.tileDistance(), t);
					}
				}
				
			}
			
			f.done();
		}
		
	}
	
	private void pushTarget(PathTile t, ArrayListResize<ArtilleryInstance> ins) {
		
		tmp.clearSloppy();
		double min = Double.MAX_VALUE;
		double max = 0;
		
		
		for (ArtilleryInstance i: ins) {
			int ax = i.mX()/absSize;
			int ay = i.mY()/absSize;
			if (ax == t.x() && ay == t.y()) {
				if (tmp.hasRoom()) {
					min = Math.min(min, i.rangeMin());
					max = Math.max(max, i.rangeMax());
					tmp.add(i);
				}
			}
		}
		
		min *= min;
		max *= max;
		
		
		
		if (tmp.size() == 0)
			return;
		
		int ox = t.x();
		int oy = t.y();
		while(t != null && tmp.size() > 0) {
			PathTile p = t;
			t =  t.getParent();
			double dx = (ox-p.x())*absSize;
			double dy = (oy-p.y())*absSize;
			double d = dx*dx+dy*dy;
			if (d < min) {
				continue;
			}
			if (d > max)
				continue;
			if (mapBreak.is(p)) {
				push(p, tmp);
			}
			
		}

	}
	
	private void push(PathTile t, ArrayList<ArtilleryInstance> ins) {
		
		int x1 = t.x()*absSize;
		int y1 = t.y()*absSize;
		
		for (int dy = 0; dy < absSize; dy++) {
			for (int dx = 0; dx < absSize && tmp.size() > 0; dx++) {
				int x = x1+dx;
				int y = y1+dy;
				
				if (SETT.PATH().availability.get(x, y).isSolid(c.getArmy()) || GAME.ARMIES().map.attackable.is(x, y, c.getArmy())) {
					for (ArtilleryInstance i : tmp) {
						if (i.testTarget(x, y, traj, false) == null) {
							i.targetCooSet(x,y, false, false);
							tmp.remove(i);
							ins.remove(i);
							break;
						}
						
					}
				}
				
			}
		}
		
		
	}

	

	
}
