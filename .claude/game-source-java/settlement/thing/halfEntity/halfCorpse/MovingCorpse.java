package settlement.thing.halfEntity.halfCorpse;

import java.io.IOException;

import init.constant.C;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.spirte.HCorpseRenderer;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.thing.halfEntity.HalfEntity;
import snake2d.Renderer;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;

public final class MovingCorpse extends HalfEntity {
	
	private double z;
	private double dx;
	private double dy;
	private double mag;
	
	private static int rsize = 24*C.SCALE;
	
	private double x;
	private double y;
	private double dirD;
	private Induvidual indu;
	private boolean gore;
	private byte cl;
	
	
	public MovingCorpse() {
		super(rsize, rsize);
	}
	
	@Override
	protected void save(FilePutter f) {
		f.d(x);
		f.d(y);
		f.d(z);
		f.d(mag);
		f.d(dx);
		f.d(dy);
		f.d(dirD);
		f.b(cl);
		indu.save(f);
	}


	@Override
	protected HalfEntity load(FileGetter f) throws IOException {
		x = f.d();
		y = f.d();
		z = f.d();
		mag = f.d();
		dx = f.d();
		dy = f.d();
		dirD = f.d();
		cl = f.b();
		indu = new Induvidual(f);
		return this;
	}
	
	
	protected void init(Humanoid h, boolean gore, CAUSE_LEAVE l) {
		x = h.body().cX();
		y = h.body().cY();
		dirD = h.speed.dir().id();
		mag = h.speed.magnitude();
		dx = h.speed.nX();
		dy = h.speed.nY();
		z = h.physics.getZ();
		indu = h.indu();
		this.gore = gore;
		cl = (byte) l.index();
		add();
	}


	
	@Override
	protected void update(double ds) {
		
		z -= ds*C.TILE_SIZE;
		
		if (z <= 0) {
			z = 0;
			mag -= ds*(4*C.TILE_SIZE+mag*0.1);
		}else {
			mag -= ds*(8*C.TILE_SIZE+mag*0.1);
		}
		if (mag <= 0) {
			remove();
			SETT.THINGS().corpses.create(indu, (int)x, (int)y, DIR.ALL.getC((int)dirD), !gore, CAUSE_LEAVES.ALL().get(cl));
			return;
		}
		dirD += C.ITILE_SIZE*mag*ds;
		if (dirD >= DIR.ALL.size())
			dirD -= DIR.ALL.size();
		
		double nx = x + ds*mag*dx;
		double ny = y + ds*mag*dy;
		
		if (SETT.PATH().solidity.is( ((int) nx)>>C.T_SCROLL, ((int) ny)>>C.T_SCROLL)) {
			remove();
			if (SETT.PATH().solidity.is( ((int) x)>>C.T_SCROLL, ((int) x)>>C.T_SCROLL)) {
				for (DIR d : DIR.ALL) {
					if (SETT.PATH().solidity.is( ((int) (x+d.xN()*C.TILE_SIZE))>>C.T_SCROLL, ((int) (y+d.yN()*C.TILE_SIZE))>>C.T_SCROLL)) {
						SETT.THINGS().corpses.create(indu, (int) (x+d.xN()*C.TILE_SIZE), (int) (y+d.yN()*C.TILE_SIZE), DIR.ALL.getC((int)dirD), !gore, CAUSE_LEAVES.ALL().get(cl));
						return;
					}
				}
				return;
			}
			
			SETT.THINGS().corpses.create(indu, (int)x, (int)y, DIR.ALL.getC((int)dirD), !gore, CAUSE_LEAVES.ALL().get(cl));
			return;
		}
		
		x = nx;
		y = ny;
		body().moveC(x, y);
	}
	
	@Override
	protected void removeAction() {

	}
	
	@Override
	protected MovingCorpseFactory constructor() {
		return SETT.HALFENTS().corpses;
	}

	
	
	@Override
	public void hoverInfo(GBox box) {
		
	}

	@Override
	protected void render(Renderer r, ShadowBatch s, float ds, int x, int y) {
		boolean inWater = SETT.ENTITIES().submerged.is(ctx(), cty());
		
		DIR d = DIR.ALL.getC((int)dirD);
		
		if (!gore)
			HCorpseRenderer.renderCorpse(indu, d.id(), inWater, 0, r, s, x, y, (int) z);
		else
			HCorpseRenderer.renderGore(indu, d.id(), inWater, 0, r, s, x, y);
	}



	
}
