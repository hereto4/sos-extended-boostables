package settlement.thing.halfEntity.dingy;

import java.io.IOException;

import game.time.TIME;
import init.constant.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.spirte.HSprite;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.thing.halfEntity.HalfEntity;
import snake2d.Renderer;
import snake2d.util.MATH;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;
import util.GUTIL;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;

final class Dingy extends HalfEntity {
	
	private static VectorImp vec = new VectorImp();
	private final static double SPEED = 2.0;
	
	private int hi = -1;
	private byte up = 0;
	private byte pointI;
	private byte pointM;
	private byte rCatch;
	private double mov;
	
	private final WayPoint[] points = new WayPoint[4];

	
	private static final int[] bumpOff = new int[128];
	
	static {
		for (int i = 0; i < bumpOff.length; i+=2) {
			bumpOff[i] = (int) (RND.rSign()*RND.rFloat()*2);
			bumpOff[i+1] = (int) (RND.rSign()*RND.rFloat()*2);
		}
	}
	
	
	public Dingy() {
		super(C.TILE_SIZE*2, C.TILE_SIZE*2);
		for (int i = 0; i < points.length; i++)
			points[i] = new WayPoint();
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(hi);
		file.b(pointI);
		file.b(pointM);
		RESOURCES.map().saver().save(catc(), file);
		file.b(rCatch);
		file.d(mov);
		file.b(up);
		for (WayPoint p : points)
			p.save(file);
	}
	
	@Override
	protected HalfEntity load(FileGetter file) throws IOException {
		hi = file.i();
		pointI = file.b();
		pointM = file.b();
		rCatch = RESOURCES.map().loader().loadB(file, null).bIndex();
		file.b();
		mov = file.d();
		up = file.b();
		for (WayPoint p : points)
			p.load(file);
		return this;
	}
	
	public Humanoid host() {
		ENTITY e = SETT.ENTITIES().getByID(hi);
		if (e != null && e instanceof Humanoid)
			return (Humanoid) e;
		return null;
	}
	
	private RESOURCE catc() {
		return RESOURCES.ALL().get(rCatch);
	}



	boolean init(Humanoid h, int tx, int ty, RESOURCE cat, int upgrade, DIR dir) {
		hi = h.id();
		mov = 0;
		RoomInstance ins = SETT.ROOMS().map.instance.get(tx, ty);
		if (!setWaypoint(tx+0.5, ty+0.5, 0, ins, dir.id()))
			return false;
		pointI = 0;
		pointM = 1;
		up = (byte) (upgrade&1);
		body().moveC(points[0].tx*C.TILE_SIZE, points[0].ty*C.TILE_SIZE);
		
		for (int i = 1; i < points.length; i++) {
			WayPoint prev = points[i-1];
			double dx = prev.tx + prev.dx*prev.distance;
			double dy = prev.ty + prev.dy*prev.distance;
			if (setWaypoint(dx, dy, i, ins, prev.di))
				pointM++;
			else
				break;
			
		}
		rCatch = cat.bIndex();
		add();
		return true;
	}
	
	private boolean setWaypoint(double tx, double ty, int pi, RoomInstance ins, int ri) {
		
		WayPoint p = points[pi];
		
		

		
		p.tx = (float) tx;
		p.ty = (float) ty;
		
		int mLength = 16 + RND.rInt(32);
		
		ri += RND.rInt0(1);
		
		for (int di = 0; di < DIR.ALL.size(); di++) {
			DIR d = DIR.ALL.getC(ri+di);
			
			if (passable(tx, ty, tx+d.xN(), ty+d.yN(), ins)) {
				vec.set(d.xN(), d.yN());
				p.dx = (float) vec.nX();
				p.dy = (float) vec.nY();
				p.di = (byte) d.id();
				p.distance = 0;
				for (int i = 0; i < mLength; i++) {
					double nx = tx + p.dx;
					double ny = ty + p.dy;
					if (passable(tx, ty, nx, ny, ins)) {
						p.distance ++;
						tx += p.dx;
						ty += p.dy;
					}else if (i > 5){
						return true;
					}
				}
				
				return true;
			}
			
		}
		
		return false;
		
		
	}
	
	private boolean passable(double fromX, double fromY, double toX, double toY, RoomInstance ins) {
		int tx = (int) toX;
		int ty = (int) toY;
		if (!SETT.IN_BOUNDS(tx, ty))
			return false;
		if (ins != null && ins.is(tx, ty)) {
			if (!SETT.TERRAIN().WATER.is.is(tx, ty))
				return false;
		}else if (!SETT.TERRAIN().WATER.DEEP.is(tx, ty) && !SETT.TERRAIN().WATER.BRIDGE.is(tx, ty))
			return false;
		int x = (int) fromX;
		int y = (int) fromY;
		return SETT.TERRAIN().WATER.is.is((int) x, ty) && SETT.TERRAIN().WATER.is.is(tx, (int) y);
	}
	
	@Override
	protected void update(double ds) {
		
		Humanoid a = host();
		if (a == null) {
			remove();
			return;
		}
		
		mov += ds*SPEED;
		
		
	
		int pi = pointI;
		
		if (pi == pointM) {
			if (mov > 20) {
				pointI ++;
				mov = 0;
			}
		}else if(pointI > pointM) {
			pi = pointM - (pointI-pointM);
			
			if (pi < 0) {
				remove();
				return;
			}
			
			WayPoint p = points[pi];
			
			if (mov >= p.distance) {
				mov = 0;
				int cx = (int) ((p.tx)*C.TILE_SIZE);
				int cy = (int) ((p.ty)*C.TILE_SIZE);
				pointI++;
				body().moveC(cx, cy);
			}else {
				int cx = (int) ((p.tx + (p.distance-mov)*p.dx)*C.TILE_SIZE);
				int cy = (int) ((p.ty + (p.distance-mov)*p.dy)*C.TILE_SIZE);
				body().moveC(cx, cy);
			}
			
		}else {
			WayPoint p =  points[pointI];
			if (mov >= p.distance) {
				mov = 0;
				int cx = (int) ((p.tx + p.distance*p.dx)*C.TILE_SIZE);
				int cy = (int) ((p.ty + p.distance*p.dy)*C.TILE_SIZE);
				pointI++;
				body().moveC(cx, cy);
			}else {
				int cx = (int) ((p.tx + mov*p.dx)*C.TILE_SIZE);
				int cy = (int) ((p.ty + mov*p.dy)*C.TILE_SIZE);
				body().moveC(cx, cy);
			}
		}
				
		
		
	}
	
	@Override
	public void renderBelow(Renderer r, ShadowBatch s, float ds, int x1, int y1) {


		DIR dir = DIR.N;
		int pi = pointI;
		RESOURCE res = null;
		int frame = ((int)((TIME.currentSecond()*4)) & 0b1);
		if (pi == pointM) {
			dir = DIR.ALL.get(points[pi-1].di);
			frame = 0;
		}else if(pointI > pointM) {
			pi = pointM - (pointI-pointM);
			if (pi < 0)
				return;
			dir = DIR.ALL.get(points[pi].di).perpendicular();
			res = RESOURCES.ALL().get(rCatch&0x0FF);
		}else {
			dir = DIR.ALL.get(points[pi].di);
		}
		
		{
			int ran = GUTIL.ran2().get(GUTIL.ran2().get(index()));
			double sp = 10.0/(1+(ran&0b1111));
			ran = ran >> 4;
			int f = (ran & 0b1111) + (int) (sp*TIME.currentSecond());
			ran = ran >> 4;
			int df = MATH.distanceC(8, f, 16);
			x1 += df;
			
			sp = 10.0/(1+(ran&0b1111));
			ran = ran >> 4;
			f = (ran & 0b1111) + (int) (sp*TIME.currentSecond());
			ran = ran >> 4;
			df = MATH.distanceC(8, f, 16);
			y1 += df;
			
		}
		
		constructor().sprite.render(r, s, dir.id(), x1, y1, frame, up);
		
		Humanoid host = host();
		
		if (res != null) {
			int x = x1 + body().width()/2;
			int y = y1 + body().height()/2;
			
			x += dir.xN()*2*C.SCALE;
			y += dir.yN()*2*C.SCALE;
			
			res.renderOneC(r, x, y, 0);
		}
		
		if (host != null) {
			DIR d = dir.perpendicular();
			
			HSprite sp = HSprites.STAND;
			double ani = 0;
			if (pointI == pointM) {
				d = d.next(2);
				sp = HSprites.GRAB;
				ani = TIME.currentSecond()*3.0;
			}else {
				sp = frame == 0 ? HSprites.STAND : HSprites.CARRY;
			}
			
			int x = x1 + body().width()/2 - 12*C.SCALE;
			int y = y1 + body().height()/2 - 12*C.SCALE;
			
			x += d.xN()*2*C.SCALE;
			y += d.yN()*2*C.SCALE;
			
			sp.render(host.indu(), ani, 0, false, d, null, 0, r, s, ds, x, y);
			
		}
		
		
		
		
	}
	

	@Override
	protected void removeAction() {
		if (host() != null) {
			double time = 0;
			for (int i = 0; i < pointM; i++) {
				time += points[i].distance;
			}
			time /= SPEED;
			time *= 2;
			time += 20;
			HEvent.Handler.fishingTripOver(host(), time);
			
		}
	}
	
	@Override
	protected DingyFactory constructor() {
		return SETT.HALFENTS().dingy;
	}
	
	@Override
	public void hoverInfo(GBox box) {
		
	}
	
	private static final class WayPoint implements SAVABLE{
		
		byte di;
		float tx,ty;
		float dx,dy;
		float distance;
		
		WayPoint(){
			
		}

		@Override
		public void save(FilePutter file) {
			file.b(di);
			file.f(tx);
			file.f(ty);
			file.f(dx);
			file.f(dy);
			file.f(distance);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			di = file.b();
			tx = file.f();
			ty = file.f();
			dx = file.f();
			dy = file.f();
			distance = file.f();
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			
		}
	}

	@Override
	protected void render(Renderer r, ShadowBatch s, float ds, int x, int y) {
		// TODO Auto-generated method stub
		
	}
	
}
