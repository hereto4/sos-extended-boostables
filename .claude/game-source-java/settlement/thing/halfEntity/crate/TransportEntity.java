package settlement.thing.halfEntity.crate;

import java.io.IOException;

import init.constant.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.work.AIModule_Work;
import settlement.main.SETT;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.thing.halfEntity.HalfEntity;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import util.gui.misc.GBox;
import util.rendering.ShadowBatch;

public final class TransportEntity extends HalfEntity {
	
	private static int length = 3;
	
	private final int[] oo = new int[2*2*length];
	
	private static VectorImp vec = new VectorImp();
	
	private int hi = -1;
	private int ri = -1;
	private byte ran;
	private double mov;
	private int ox,oy;
	private boolean mil;
	private final int random = RND.rInt();
	private static final double moveMax = C.TILE_SIZE + C.TILE_SIZEH;
	
	private static final int[] bumpOff = new int[128];
	
	static {
		for (int i = 0; i < bumpOff.length; i+=2) {
			bumpOff[i] = (int) (RND.rSign()*RND.rFloat()*2);
			bumpOff[i+1] = (int) (RND.rSign()*RND.rFloat()*2);
		}
	}
	
	
	public TransportEntity() {
		super(C.TILE_SIZE, C.TILE_SIZE);
	}
	
	@Override
	protected void save(FilePutter file) {
		file.i(hi);
		RESOURCES.map().saver().save(res(), file);
		file.b(ran);
		file.d(mov);
		file.i(ox);
		file.i(oy);
		file.bool(mil);
		file.isE(oo);
	}
	
	@Override
	protected HalfEntity load(FileGetter file) throws IOException {
		hi = file.i();
		RESOURCE res = RESOURCES.map().loader().loadB(file, null);
		ri = res == null ? -1 : res.index();
		ran = file.b();
		mov = file.d();
		ox = file.i();
		oy = file.i();
		mil = file.bool();
		file.isE(oo);
		return this;
	}
	
	public RESOURCE res() {
		return ri == -1 ? null : RESOURCES.ALL().get(ri);
	}
	
	public Humanoid host() {
		ENTITY e = SETT.ENTITIES().getByID(hi);
		if (e != null && e instanceof Humanoid)
			return (Humanoid) e;
		return null;
	}

	public double carryAmount() {
		Humanoid h = host();
		if (h == null)
			return -1;
		return AIModule_Work.getTransportAmount(h);
	}


	boolean init(Humanoid h, int tx, int ty, RESOURCE res, byte ran, boolean mil) {
		body().moveC(tx*C.TILE_SIZE+C.TILE_SIZEH, ty*C.TILE_SIZE+C.TILE_SIZEH);
		hi = h.id();
		this.ran = ran;
		ri = res.bIndex();
		ox = (short) body().cX();
		oy = (short) body().cY();
		mov = 0;
		vec.set(body(), h.body());
		
		DIR d = DIR.N;
		
		FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
		if (it != null)
			d = DIR.ORTHO.get(it.rotation).perpendicular();

		for (int i = 0; i < oo.length/2; i++) {
			
			
			
			oo[i*2] = (int) (body().cX()+d.xN()*(i)*moveMax);
			oo[i*2+1] = (int) (body().cY()+d.yN()*(i)*moveMax);
			
		}
		
		this.mil = mil;
		add();
		return true;
	}
	
	

	
	@Override
	protected void update(double ds) {
		
		Humanoid a = host();
		if (a == null) {
			remove();
			return;
		}
		
		double am = carryAmount();
		if (am <= 0) {
			remove();
			return;
		}
		
		if (ox == a.body().cX() && oy == a.body().cY()) {
			return;
		}
		
		mov = vec.set(ox, oy, a.body().cX(), a.body().cY());

		if (mov >= moveMax) {
			mov = 0;
			body().moveC(ox, oy);
			ox = a.body().cX();
			oy = a.body().cY();
			
			for (int i = oo.length/2-1; i > 0; i--) {
				oo[i*2] = oo[(i-1)*2];
				oo[i*2+1] = oo[(i-1)*2+1];
			}
			oo[0] = a.body().cX();
			oo[1] = a.body().cY();
		}
	}
	
	@Override
	public void render(Renderer r, ShadowBatch s, float ds, int x1, int y1) {
		
		double am = carryAmount();
		if (am < 0)
			return;
		x1 += C.TILE_SIZEH;
		y1 += C.TILE_SIZEH;
		
		if (mil){
			
			vec.set(body().cX(), body().cY(), ox, oy);
			DIR dir = vec.dir();
			int dx = (int) (vec.nX()*mov);
			int dy = (int) (vec.nY()*mov);
			int x = x1 + dx;
			int y = y1 + dy;
			
			int bi = (int) (mov*2);
			bi %= bumpOff.length;
			bi &= ~1;


			int cx = (int) (x);
			int cy = (int) (y);
			SETT.ANIMALS().renderCaravan(r, s, mov/C.TILE_SIZE, cx, cy, null, 0, false, dir.id(), ran);
			cx = (int) (x-dir.xN()*C.TILE_SIZE) + bumpOff[bi];
			cy = (int) (y-dir.yN()*C.TILE_SIZE) + bumpOff[bi+1];
			renderCart(r, s, dir.id(), cx, cy, ran, res(), am, mov/C.TILE_SIZE);
		}else {
			for (int i = 2; i < oo.length/2; i++) {
				int xx = oo[i*2]-(body().x1()+C.TILE_SIZEH);
				int yy = oo[i*2+1]-(body().y1()+C.TILE_SIZEH);
				vec.set(oo[i*2], oo[i*2+1], oo[(i-1)*2], oo[(i-1)*2+1]);
				
				DIR dir = vec.dir();
				int dx = (int) (vec.nX()*mov);
				int dy = (int) (vec.nY()*mov);
				int x = x1 + dx + xx;
				int y = y1 + dy + yy;
				
				int bi = (int) (mov*2) + (random >> i*2)&0x0F;
				bi %= bumpOff.length;
				bi &= ~1;


				
				int cx = (int) (x) + bumpOff[bi];
				int cy = (int) (y) + bumpOff[bi+1];
				if (i == 2) {
					int ax = (int) (x+dir.xN()*C.TILE_SIZE);
					int ay = (int) (y+dir.yN()*C.TILE_SIZE);
					SETT.ANIMALS().renderCaravan(r, s, mov/C.TILE_SIZE, ax, ay, null, 0, false, dir.id(), ran);
					constructor().sprite.renderHarness(r, s, dir.id(), cx, cy);
				}
				
				
				
				renderCart(r, s, dir.id(), cx, cy,ran, res(), am, mov/C.TILE_SIZE);
				//UI.icons().s.crossheir.renderScaled(r, x-C.TILE_SIZEH, y-C.TILE_SIZEH, 4);
			}
		}
		
		
		
		
		
	}

	public void renderCart(SPRITE_RENDERER r, ShadowBatch s, int rot, int cx, int cy, int ran, RESOURCE res, double resamount, double mov) {
		constructor().sprite.renderBelow(r, s, rot, cx, cy, mov, ran, 0, res, resamount);
		constructor().sprite.render(r, s, rot, cx, cy, 0, mil);
	}
	

	@Override
	protected void removeAction() {

	}
	
	@Override
	protected TransportFactory constructor() {
		return SETT.HALFENTS().transports;
	}

	
	
	@Override
	public void hoverInfo(GBox box) {
		
	}



	
}
