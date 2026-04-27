package settlement.thing;

import java.io.IOException;

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

class Gore_BloodDrop extends Gore{
	
	private Rec body = new Rec();
	private int ran;
	private float timerLife;
	private final ColorImp color = new ColorImp();
	
	Gore_BloodDrop(int index) {
		super(index);
	}
	
	@Override
	protected void save(FilePutter f) {
		body.save(f);
		f.i(ran);
		f.f(timerLife);
		color.save(f);
	}
	
	@Override
	protected void load(FileGetter f) throws IOException {
		body.load(f);
		ran = f.i();
		timerLife = f.f();
		color.load(f);
	}

	@Override
	protected void  init(int cx, int cy, double sx, double sy, COLOR c) {
		int x = cx;
		int y = cy;
		int w = RND.rInt(16);
		color.set(c);
		init(x,y,w);
		
	}
	
	private void init(int x, int y, int dim){

		DEG.setRandom();
		x += dim*DEG.getCurrentX();
		y += dim*DEG.getCurrentY();
		
		timerLife = 120;
		ran = RND.rInt();
		
		body.setDim(sprite().size());
		body.moveX1Y1(x, y);
	}
	
	private TILE_SHEET sprite(){
		return SETT.THINGS().sprites.bloodPool;
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
		
		bindCol(color, ran >> 8);
		sprite().render(r, ran%sprite().tiles(), body().x1()+offsetX, body().y1()+offsetY);
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
		return SETT.THINGS().gore.drop;
	}



}
