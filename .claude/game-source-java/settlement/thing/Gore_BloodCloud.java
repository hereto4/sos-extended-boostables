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
import util.rendering.ShadowBatch;

class Gore_BloodCloud extends Gore{
	
	private static final int time = 60*2;
	private static final int amount = 175*2;
	private final static int[][] pos = new int[time][amount];
	private ColorImp color = new ColorImp();
	
	static {
		
		for (int k = 0; k < amount; k+=2) {
			DEG.setRandom();
			double x = RND.rInt0(5) + C.TILE_SIZE + C.TILE_SIZEH;
			double y = RND.rInt0(5) + C.TILE_SIZE + C.TILE_SIZEH;
			double speed = (C.TILE_SIZE*2 + RND.rFloat()*C.TILE_SIZE*12)/60.0;
			for (int tick = 0; tick < time; tick++) {
				pos[tick][k] = (int) x;
				pos[tick][k+1] = (int) y;
				if (speed < 0)
					speed = 0;
				x += speed*DEG.getCurrentX();
				y += speed*DEG.getCurrentY();
				speed -= 0.4;
			}
		}


	}
	
	private final Rec body = new Rec(C.TILE_SIZE*3);
	private final ESpeed.Imp speed = new ESpeed.Imp();
	private float timer = -70;
	private int tick;
	private int am;
	
	Gore_BloodCloud(int index) {
		super(index);
		speed.magnitudeTargetSet(0);
	}
	
	@Override
	protected void save(FilePutter f) {
		body.save(f);
		speed.save(f);;
		f.f(timer);
		f.i(tick);
		f.i(am);
		color.save(f);
	}
	
	@Override
	protected void load(FileGetter f) throws IOException {
		body.load(f);
		speed.load(f);;
		timer = f.f();
		tick = f.i();
		am = f.i();
		color.load(f);
	}

	@Override
	protected void init(int cx, int cy, double sx, double sy, COLOR col){
		this.color.set(col);
		body.moveC(cx, cy);
		timer = 0;
		tick = 0;
		am = amount;
		speed.setRaw(sx, sy);


	}
	
	@Override
	protected boolean update(double ds) {
		
		if (speed.isZero()) {
			move(speed, ds, 0, body, false);
		}
		
		speed.magnitudeAdjust(ds, 10.0, 1);
		
		timer += ds;
		tick = (int) (timer*60);
		if (tick >= time) {
			am = (int) (amount - (timer-2)*10);
			if (am <= 0)
				return false;
			tick = time-1;
		}
		
		return true;
				
	}

	@Override
	public void render(Renderer r, ShadowBatch shadows,
			float ds, int offsetX, int offsetY) {
		int x = body().x1() + offsetX;
		int y = body().y1() + offsetY;
		
		
		
		for (int j = 0; j < am; j+=2){
			bindCol(color, j, 0.7f);
			r.renderParticle(pos[tick][j] + x, pos[tick][j+1] + y);
		}
		COLOR.unbind();
		
	}

	@Override
	public RECTANGLE body() {
		return body;
	}

	@Override
	protected int z() {
		return 1;
	}

	@Override
	public ThingFactory<?> factory() {
		return SETT.THINGS().gore.clouds;
	}

}
