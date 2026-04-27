package settlement.thing;

import java.io.IOException;

import init.constant.C;
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

final class Gore_BloodDrops extends Gore{
	
	private final Rec body = new Rec(C.TILE_SIZE*3);
	private final byte[] data = new byte[16*3];
	private int lim;
	private float timerLife;
	private final ColorImp col = new ColorImp();
	
	Gore_BloodDrops(int index, Sprites s) {
		super(index);
		for (int i = 0; i < data.length; i+= 3) {
			DEG.setRandom();
			int d = RND.rInt(C.TILE_SIZE);
			data[i] = (byte) (d*DEG.getCurrentX() + C.TILE_SIZE);
			data[i+1] = (byte) (d*DEG.getCurrentY() + C.TILE_SIZE);
			data[i+2] = (byte) RND.rInt(s.bloodPool.tiles());
		}
		
	}
	
	@Override
	protected void save(FilePutter f) {
		body.save(f);
		f.bs(data);
		f.i(lim);
		f.f(timerLife);
		col.save(f);
	}
	
	@Override
	protected void load(FileGetter f) throws IOException {
		body.load(f);
		f.bs(data);
		lim = f.i();
		timerLife = f.f();
		col.load(f);
	}


	@Override
	protected void init(int cx, int cy, double sx, double sy, COLOR col){
		this.col.set(col);
		
		
		init(cx, cy, 1.0);
		
	}
	
	private void init(int x, int y, double amount){

		lim = (int) (16*amount);
		if (lim == 0)
			lim = 1;
		if (lim > 16)
			lim = 16;
		lim*= 3;
		
		timerLife = 250;
		body.moveC(x, y);
	}
	
	@Override
	protected boolean update(double ds) {
		timerLife -= ds;
		if (timerLife < 0)
			return false;
		return true;
	}

	@Override
	public void render(Renderer r, ShadowBatch shadows,
			float ds, int offsetX, int offsetY) {
		
		for (int i = 0; i < lim; i+= 3) {
			int x = body().x1()+offsetX + data[i];
			int y = body().y1()+offsetY + data[i+1];
			bindCol(col, i);
			SETT.THINGS().sprites.bloodPool.render(r, data[i+2], x, y);
		}
		COLOR.unbind();
	}

	@Override
	public RECTANGLE body() {
		return body;
	}

	@Override
	protected int z() {
		return 0;
	}
	
	@Override
	public ThingFactory<?> factory() {
		return SETT.THINGS().gore.drops;
	}



}
