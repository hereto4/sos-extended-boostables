package settlement.thing;

import java.io.IOException;

import init.constant.C;
import settlement.entity.ESpeed;
import settlement.main.SETT;
import settlement.thing.THINGS.ThingFactory;
import settlement.thing.ThingsGore.Gore;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DEG;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;

public class Gore_Flesh extends Gore{

	private static COLOR[] cols = new COLOR[64];
	static {
		for (int i = 0; i < cols.length; i++) {
			double shade = 0.4+0.6*RND.rFloat();
			int r = (int) (127*(shade - RND.rFloat()*0.05));
			int g = (int) (127*(shade - RND.rFloat()*0.05));
			int b = (int) (127*(shade - RND.rFloat()*0.05));
			cols[i] = new ColorImp(r,g,b);
		}
	}
	
	private final Rec body = new Rec();
	private final ESpeed.Imp speed = new ESpeed.Imp();
	private final ColorImp col = new ColorImp();
	
	private int ran;
	float timer;
	boolean debris = false;
	static boolean debr = false;
	
	public Gore_Flesh(int index) {
		super(index);
		speed.magnitudeTargetSet(0);
		speed.accelerationInit(C.TILE_SIZE*5);
		
	}
	
	@Override
	protected void save(FilePutter f) {
		body.save(f);
		speed.save(f);
		f.i(ran);
		f.f(timer);
		f.bool(debris);
		col.save(f);
	}
	
	@Override
	protected void load(FileGetter f) throws IOException {
		body.load(f);
		speed.load(f);
		ran = f.i();
		timer = f.f();
		debris = f.bool();
		col.load(f);
	}
	
	@Override
	protected void  init(int cx, int cy, double sx, double sy, COLOR col) {
		body.setDim(sprite().size(), sprite().size());
		body.moveC(cx, cy); 
		this.col.set(col);
		ran = RND.rInt();
		double m = C.TILE_SIZE*2 + RND.rFloatP(2)*(C.TILE_SIZE*15);
		DEG.setRandom();
		speed.setRaw(sx + DEG.getCurrentX()*m, sy + DEG.getCurrentY()*m);
		timer = 120 + RND.rFloat(100);
		this.debris = debr;

	}
	
	void setDebris() {
		this.debris = true;
	}
	
	@Override
	protected boolean update(double ds) {
		
		if (speed.isZero()) {
			timer -= ds;
			if (timer < 0) {
				return false;
			}
			return true;
		}
		
		speed.magnitudeAdjust(ds, 2.0, 1);
		move(speed, ds, 0.5f, body, true);
		return true;
	}

	@Override
	public void render(Renderer r, ShadowBatch shadows,
			float ds, int offsetX, int offsetY) {
		int spriteI = ran&(sprite().tiles()-1);
		if (!debris)
			bindCol(col, ran>>8);
		else
			cols[(ran>>8)&63].bind();
		sprite().render(r, spriteI, body().x1()+offsetX, body().y1() + offsetY);
		if (spriteI < 32) {
			shadows.setDistance2Ground(2).setHeight(0);
			sprite().render(shadows, spriteI, body().x1()+offsetX, body().y1() + offsetY);
		}
		COLOR.unbind();
	}

	private final TILE_SHEET sprite() {
		if (debris)
			return SETT.THINGS().sprites.debris;
		return SETT.THINGS().sprites.flesh;
	}
	
	@Override
	public RECTANGLE body() {
		return body;
	}

	@Override
	protected int z() {
		return 100;
	}
	
	@Override
	public ThingFactory<?> factory() {
		return SETT.THINGS().gore.flesh;
	}
	
}
