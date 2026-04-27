package game.battle.formation;

import java.io.IOException;

import game.battle.div.Div;
import game.battle.util.Copyable;
import game.battle.util.DIV_SPEC;
import init.constant.Config;
import settlement.main.SETT;
import snake2d.PathUtilOnline.Filler;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class DivFormationImp extends DivPositionImp implements DivFormation, Copyable<DivFormationImp>{

	private DIR faceDir = DIR.N;
	private final Rec bounds = new Rec();
	final Coo start = new Coo();
	private double dx;
	private double dy;
	private int width;

	private DIV_FORMATION ts = DIV_FORMATION.TIGHT;
	private int centreI = -1;
	private boolean hasExtraRoom = false;
	private boolean isNotCoherent = false;
	private final byte[] dirMasks;
	
	public DivFormationImp() {
		this(Config.battle().MEN_PER_DIVISION);
	}
	
	private DivFormationImp(int maxMen) {
		super(maxMen);
		dirMasks = new byte[maxMen];
	}


	@Override
	public DIR dir() {
		return faceDir;
	}
	
	@Override
	public void save(FilePutter file) {
		super.save(file);
		file.i(faceDir.id());
		bounds.save(file);
		start.save(file);
		file.i(centreI);
		file.d(dx);
		file.d(dy);
		file.i(ts.ordinal());
		file.bs(dirMasks);
		file.bool(hasExtraRoom);
		file.bool(isNotCoherent);
		file.i(width);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		super.load(file);
		faceDir = DIR.ALL.get(file.i());
		bounds.load(file);
		start.load(file);
		centreI = file.i();
		dx = file.d();
		dy = file.d();
		ts = DIV_FORMATION.all.get(file.i());
		file.bs(dirMasks);
		hasExtraRoom = file.bool();
		isNotCoherent = file.bool();
		width = file.i();
	}
	
	@Override
	public void clear() {
		super.clear();
		bounds.set(SETT.PWIDTH+1, -1, SETT.PHEIGHT+1, -1);
		ts = DIV_FORMATION.TIGHT;
		hasExtraRoom = false;
		isNotCoherent = false; 
		centreI = -1;
	}


	public void deployInit(DIR face, int x1, int y1, double dx, double dy, DIV_FORMATION ts, int width) {
		clear();
		this.start.set(x1, y1);
		this.dx = dx;
		this.dy = dy;
		this.faceDir = face;
		this.ts = ts;
		this.width = width;
	}
	
	public void move(int dx, int dy) {
		
		this.start.increment(dx, dy);
		bounds.incr(dx, dy);
		
		for (int i = 0; i < deployed(); i++) {
			int x = px(i)+dx;
			int y = py(i)+dy;
			set(i, x, y);
		}
		
	}
	
	public void deploy(int x, int y, DIV_SPEC spec) {
		int t = ts.sizeH(spec);
		bounds.unify(x-t, y-t);
		bounds.unify(x+t, y-t);
		bounds.unify(x-t, y+t);
		bounds.unify(x+t, y+t);
		
		set(deployed(), x, y);
		init(deployed()+1);
	}
	
	public void deploy(int x, int y, int pi, DIV_SPEC spec) {
		int t = ts.sizeH(spec);
		bounds.unify(x-t, y-t);
		bounds.unify(x+t, y-t);
		bounds.unify(x-t, y+t);
		bounds.unify(x+t, y+t);
		
		set(pi, x, y);
		init(deployed()+1);
	}
	
	
	public void deployFinish(Filler f, DIV_SPEC spec) {
		isNotCoherent = false;
		if (deployed() == 0)
			return;
		
		int xx = 0;
		int yy = 0;
		for (int i = 0; i < deployed(); i++) {
			COORDINATE p = pixel(i);
			xx+=p.x();
			yy+=p.y();
		}
		
		xx/=deployed();
		yy/=deployed();
		
		int cx = xx;
		int cy = yy;
		
		double dist = Double.MAX_VALUE;
		int distI = -1;
		for (int i = 0; i < deployed(); i++) {
			COORDINATE p = pixel(i);
			int dx = p.x()-cx;
			int dy = p.y()-cy;
			double d = Math.sqrt(dx*dx+dy*dy);
			if (d < dist) {
				dist = d;
				distI = i;
			}
		}
		
		if (distI == -1)
			throw new RuntimeException();
		
		this.centreI = distI;
		
		setDirs(f, spec);
	}
	
	public void setHasExtraRoom() {
		hasExtraRoom = true;
	}
	
	@Override
	public void copy(DivFormationImp o) {
		super.copyposition(o);
		faceDir = o.faceDir;
		bounds.set(o.bounds);
		start.set(o.start);
		dx = o.dx;
		dy = o.dy;
		ts = o.ts;
		width = o.width;
		for (int i = 0; i < dirMasks.length; i++) {
			dirMasks[i] = o.dirMasks[i];
		}
		hasExtraRoom = o.hasExtraRoom;
		isNotCoherent = o.isNotCoherent;
		this.centreI = o.centreI;
	}

	@Override
	public RECTANGLE body() {
		return bounds;
	}
	
	@Override
	public DIV_FORMATION formation() {
		return ts;
	}
	
	@Override
	public COORDINATE start() {
		return start;
	}
	
	@Override
	public double dx() {
		return dx;
	}
	
	@Override
	public double dy() {
		return dy;
	}
	
	@Override
	public int width() {
		return width;
	}
	
	@Override
	public int height(DIV_SPEC spec) {
		double dep = deployed();
		if (dep == 0)
			return 0;
		
		double size = formation().size(spec);
		
		double menW = width()/size;
		return (int) Math.ceil(size*dep/menW);
	}
	
	@Override
	public int dirMaskOrtho(int i) {
		return dirMasks[i] & 0x0F;
	}
	
	@Override
	public DIR dir(int i) {
		int di = (dirMasks[i]>>4) & 0x0F;
		if (di < 2 || di-2 >= DIR.ALL.size())
			return null;
		return DIR.ALL.get(di-2);
	}
	
	public void setDir(int i, DIR d) {
		
		int m = 2 + d.id();
		dirMasks[i] &= 0x0F;
		dirMasks[i] |= m << 4;
		
		
	}
	
	@Override
	public boolean isEdge(int i) {
		int di = (dirMasks[i]>>4) & 0x0F;
		return di != 0;
	}
	
	@Override
	public boolean hasExtraRoom() {
		return hasExtraRoom;
	}
	
	private void setDirs(Filler f, DIV_SPEC spec) {
		int o = faceDir.id();
		proj.init(this);
		f.init(this);
		
		for (int i = 0; i < deployed(); i++) {
			COORDINATE t = proj.projectTile(this, i, spec);
			f.fill(t);
		}
		
		for (int i = 0; i < deployed(); i++) {
			
			int mo = 0;
			int mn = 0;
			COORDINATE t = proj.projectTile(this, i, spec); 
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (f.isser.is(t, d)) {
					mo |= d.next(o).mask();
				}
			}

			if (mo != 0x0F) {
				mn  = 1;
				double dx = 0;
				double dy = 0;
				double dd = 0;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					if (!f.isser.is(t, d)) {
						d = d.next(o);
						dx += d.x();
						dy += d.y();
						dd ++;									
					}
				}
				dx /= dd;
				dy /= dd;
				if (dd > 0 && dx != 0 || dy != 0)
					mn += DIR.get(dx, dy).id()+1;
			}
			
			mo = mo | (mn<<4);
			dirMasks[i] = (byte) mo;
			
		}
		
		
		f.done();
	}
	
	private final DivPosProjector proj = new DivPosProjector();
	
	private final class DivPosProjector {

		private double angle;
		
		private final VectorImp vec = new VectorImp();
		private final Coo coo = new Coo();
		
		private DivPosProjector() {
			
		}
		
		public int init(DivFormationImp pos){
			vec.set(pos.dx, pos.dy);
			angle = Math.atan2(vec.nX(), vec.nY())-Math.PI/2;
			return -(int)(angle / Math.PI/2);
		}
		
		public COORDINATE projectTile(DivFormationImp pos, int i, DIV_SPEC spec) {


			COORDINATE p = pos.pixel(i);
			double x = p.x();
			double y = p.y();
			
			
			
			double length = vec.set(pos.start().x(), pos.start().y(), x,y);
			double ang = Math.atan2(vec.nX(), vec.nY());
			if (pos.start.isSameAs(p)) {
				ang = angle;
				length = 0;
			}
			
			ang -= angle;
			

			x = pos.ts.sizeH(spec) + Math.sin(ang)*length;
			y = pos.ts.sizeH(spec) + Math.cos(ang)*length;
			
			int ty = (int) (y/pos.ts.size(spec));
			int tx = (int) (x/pos.ts.size(spec));
		
			tx += SETT.TWIDTH/2;
			ty += SETT.THEIGHT/2;
			
			coo.set(tx, ty);
			return coo;
			
		}

		
	}

	public boolean isSameAs(DivFormationImp o) {
		return start.isSameAs(o.start) && dx == o.dx && dy == o.dy && deployed() == o.deployed() && width == o.width && ts == o.ts && centrePixel().isSameAs(o.centrePixel());
	}

	public boolean isSameAs(DivFormationImp o, Div div) {
		if (!start.isSameAs(o.start))
			return false;
		if (dx != o.dx || dy != o.dy)
			return false;
		if (ts != o.ts || !centrePixel().isSameAs(o.centrePixel()))
			return false;
		if (deployed() != o.deployed())
			return false;
		int w1 = width/ts.size(div);
		int w2 = o.width/ts.size(div);
		
		if (w1 != w2)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return start.x() + " " + start.y() + " | > " + dx + " " + dy + " | " + width + " " + deployed() + " | " + ts + " | " + centrePixel();
	}

	

	@Override
	public COORDINATE centreTile() {
		if (deployed() == 0)
			return null;
		return tile(centreI);
	}
	
	@Override
	public COORDINATE centrePixel() {
		if (deployed() == 0)
			return null;
		return pixel(centreI);
	}
	
	public int centreI() {
		return centreI;
	}
	
	public void swap(int a, int b) {
		
		if (a == centreI)
			centreI = b;
		else if (b == centreI)
			centreI = a;
		
		byte dm = dirMasks[a];
		int x = px(a);
		int y = py(a);
		set(a, px(b), py(b));
		set(b, x, y);
		dirMasks[a] = dirMasks[b];
		dirMasks[b] = dm;
		
		
	}

	@Override
	public boolean isCoherent() {
		return !isNotCoherent;
	}
	
	public void coherentSetNot() {
		isNotCoherent = true;
	}


	
}
