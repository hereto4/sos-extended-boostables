package game.battle.thread.general.offence;

import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DivPlacability;
import game.battle.thread.general.StrategosUtil;
import game.battle.thread.general.offence.ContextLines.Line;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.Bitmap2D;
import util.data.INT.IntImp;

class StepLinesBacker {

	private final StrategosUtil u;
	private final ContextLines lines;
	private final Bitmap2D blob;
	private final Bitmap2D block;
	private final IntImp lineI;
	private final Context c;
	private final double[] dists = new double[Config.battle().DIVISIONS_PER_ARMY];
	
	
	private final Rec bounds = new Rec(SETT.TWIDTH-80, SETT.THEIGHT-80);
	{
		bounds.centerIn(SETT.TILE_BOUNDS);
	}
	
	private final VectorImp vec = new VectorImp();
	
	
	StepLinesBacker(StrategosUtil u, Context c){
		
		this.u = u;
		this.lines = c.lines;
		this.blob = c.blob;
		lineI = c.checkI;
		block = c.block;
		this.c = c;
	}
	
	public void init() {
		lineI.set(0);
		block.clear();
		Flooder f = u.flooder.getFlooder();
		f.init(this);
		
		for (int ty = 0; ty < SETT.THEIGHT; ty++) {
			for (int tx = 0; tx < SETT.TWIDTH; tx++) {
				f.setValue2(tx, ty, 0);
			}
		}
		
		c.map.clear();
		
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			
			Div d = u.getArmy().divisions().get(di);
			if (d.active()) {
				c.map.add(d);
			}
			
			
		}
		for (int l = 0; l < lines.lines(); l++) {
			
			Line li = lines.get(l);
			
			int tx = li.cx()/C.TILE_SIZE;
			int ty = li.cy()/C.TILE_SIZE;
			
			f.pushSloppy(tx, ty, 0);
			f.setValue2(tx,  ty, li.blobID);
		}
		
		Arrays.fill(dists, Double.MAX_VALUE);
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			
			if (c.map.get(t.x(), t.y()).size() > 0) {
				double v = t.getValue();
				int bi = (int) t.getValue2();
				if (v < dists[bi]) {
					dists[bi] = v;
				}
			}
			
			boolean b = blob.is(t);
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (!SETT.IN_BOUNDS(dx, dy))
					continue;
				if (!DivPlacability.tileIsOK(dx, dy, u.getArmy()))
					continue;
				double v = d.tileDistance()*(!b && blob.is(dx, dy) ? 5 : 1);
				f.pushSmaller(dx, dy, v+t.getValue(), t);
				
			}
			
		}
		
		f.done();
		
		boolean shouldShill = shouldChill();
		for (int i = 0; i < dists.length; i++) {
			double closest = dists[i];
			if (closest == Double.MAX_VALUE) {
				dists[i] = 0;
				continue;
			}
			
			
			if (!shouldShill)
				closest-=32;
			
			closest = (int)closest/48;
			closest *= 48;
			
			
			
			if (closest < 2)
				closest = 2;
			if (closest > 256)
				closest = 256;
			closest *= C.TILE_SIZE;
			dists[i] = closest;
		}
		for (int l = 0; l < lines.lines(); l++) {
			
			Line li = lines.get(l);
			li.blobID = (int) dists[li.blobID];
		
		}
		
		
	}
	
	private final ArrayListResize<ArtilleryInstance> ins = new ArrayListResize<>(256);
	
	private boolean shouldChill() {
		
		double aa = GAME.ARMIES().factors.casulties(GAME.ARMIES().enemy());
		double ab = GAME.ARMIES().factors.casulties(GAME.ARMIES().player());

		if (aa / (GAME.ARMIES().enemy().men() + 1.0) > 0.05 && aa > ab * 0.75)
			return false;
		
		double pow = 0;
		double epow = 0;
		
		for (int i = 0; i < Config.battle().DIVISIONS_PER_ARMY; i++) {
			pow += u.getArmy().divisions().get(i).settings().getPower();
		}
		for (int i = 0; i < Config.battle().DIVISIONS_PER_ARMY; i++) {
			epow += u.getArmy().enemy().divisions().get(i).settings().getPower();
		}
		
		if (pow > epow*1.5)
			return false;
		
		int ally = 0;
		int enemy = 0;

		ins.clear();
		for (int ai = 0; ai < SETT.ROOMS().ARTILLERY.size(); ai++) {
			SETT.ROOMS().ARTILLERY.get(ai).threadInstances(ins);
		}
		for (int ii = 0; ii < ins.size(); ii++) {
			ArtilleryInstance i = ins.get(ii);
			if (i.isFiring()) {
				if (i.army() == u.getArmy())
					ally++;
				else
					enemy++;
			}
		}
		
		if (enemy > ally)
			return false;
		
		return ally > 0;
		
		
	}

	public boolean retreatThroneLine() {
		
		if (lineI.get() >= lines.lines())
			return false;
		
		Line l = lines.get(lineI.get());
		
		double length = getRetreat(l, l.blobID);
		
		
		if (length < 0) {
			lines.remove(lineI.get());
			return true;
		}
		
		block(l, length);
		
		lineI.inc(1);
		
		vec.set(l.dx, l.dy);
		vec.rotate90();

		l.sx += vec.nX()*length;
		l.sy += vec.nY()*length;
		
		
		
		return true;
	}
	
	
	private double getRetreat(Line n, double target) {
		
		vec.set(n.dx, n.dy);
		vec.rotate90();

		
		for (int in = C.TILE_SIZE; in <= target; in+= C.TILE_SIZE) {
			for (int r = 0; r <= n.length; r += C.TILE_SIZE) {
				
				int x = (int) (n.sx + vec.nX()*in + r*n.dx);
				int y = (int) (n.sy + vec.nY()*in + r*n.dy);
				x /= C.TILE_SIZE;
				y /= C.TILE_SIZE;
				
				if (!SETT.IN_BOUNDS(x,y))
					return in-C.TILE_SIZE*8;
				
				if (this.block.is(x, y)) {
					return in-C.TILE_SIZE*8;
				}
				if (solid(x, y))
					return in-C.TILE_SIZE;
			}
			
		}
		
		double awayFromBlobDist = target;
		
		int cx = n.cx();
		int cy = n.cy();
		int death = 0;
		while(true) {
			int x = (int) (cx + vec.nX()*awayFromBlobDist)/C.TILE_SIZE; 
			int y = (int) (cy + vec.nY()*awayFromBlobDist)/C.TILE_SIZE; 
			if (!SETT.IN_BOUNDS(x,y))
				break;
			if (!blob.is(x,y))
				break;
			if (death++ > 1000) {
				LOG.err(cx + " " + cy + " " + n.dx + " " + n.dy + " " + awayFromBlobDist);
				break;
			}
			awayFromBlobDist += C.TILE_SIZE;
		}
		
		for (int in = C.TILE_SIZE; in <= awayFromBlobDist; in+= C.TILE_SIZE) {
			for (int r = 0; r <= n.length; r += C.TILE_SIZE) {
				
				int x = (int) (n.sx + vec.nX()*in + r*n.dx);
				int y = (int) (n.sy + vec.nY()*in + r*n.dy);
				x /= C.TILE_SIZE;
				y /= C.TILE_SIZE;
				this.block.set(x, y, true);
			}
			
		}
		
		
		
		return awayFromBlobDist;
		
		
		
		
	}
	
	private void block(Line n, double awayFromBlobDist) {
		
		vec.set(n.dx, n.dy);
		vec.rotate90();

		for (int in = C.TILE_SIZE; in <= awayFromBlobDist; in+= C.TILE_SIZE) {
			for (int r = 0; r <= n.length; r += C.TILE_SIZE) {
				
				int x = (int) (n.sx + vec.nX()*in + r*n.dx);
				int y = (int) (n.sy + vec.nY()*in + r*n.dy);
				x /= C.TILE_SIZE;
				y /= C.TILE_SIZE;
				this.block.set(x, y, true);
			}
			
		}
	}
	
	public boolean solid(double dx, double dy) {
		
		int tx = (int) dx;
		int ty = (int) dy;
		if (!bounds.holdsPoint(tx, ty))
			return true;
		if (!DivPlacability.tileIsOK(tx, ty, u.getArmy()))
			return true;
		return false;
	}
	
	
	
}
