package world;

import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.sets.Bitmap2D;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public class WRenContext {

	public SPRITE_RENDERER r;
	public ShadowBatch s;
	public float ds;
	public final RenderData data;
	
	public final Bitmap2D fow;
	public final Bitmap2D hiBuildings;
	
	WRenContext(int width, int height) {
		data = new RenderData(width, height);
		fow = new Bitmap2D(width, height, false);
		hiBuildings = new Bitmap2D(width, height, false);
	}
	
	void init(SPRITE_RENDERER r, ShadowBatch s, RECTANGLE renWindow, int offX, int offY, float ds) {
		this.r = r;
		this.s = s;
		data.init(renWindow, offX, offY);
		this.ds = ds;
		
		
		for (int y = data.ty1()-1; y <= data.ty2()+1; y++){
			for (int x = data.tx1()-1; x <= data.tx2()+1; x++){
				fow.set(x, y, false);
				hiBuildings.set(x, y, false);
			}
		}
	}
	
}
