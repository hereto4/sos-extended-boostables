package settlement.thing.pointlight;

import java.io.IOException;

import game.debug.Profiler;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.rendering.ShadowBatch;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public class POINTLIGHTS extends SettResource {

	private final PointMap map = new PointMap(SETT.TWIDTH, SETT.THEIGHT);
	final Sprites sprites = new Sprites();
	private final LOS_MAP los;

	public POINTLIGHTS() throws IOException{
		super("LIGHTS", true);
		LightModel.flickerr(0);
		IDebugPanelSett.add(new PlacableMulti("torch") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				torch(tx, ty, 0);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
				return null;
			}
		
		});
		
		IDebugPanelSett.add(new PlacableMulti("torch remove") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				remove(tx, ty);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
				return null;
			}
		
		});
		los = new LOS_MAP();
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		map.load(file);
	}
	
	@Override
	public void save(FilePutter file) {
		map.save(file);
	}
	
	@Override
	protected void clear() {
		map.clear();
	}

	public void torch(int tx, int ty, int off) {
		map.add(tx, ty, off, off, LightModel.torch);
	}
	
	public void torchBig(int tx, int ty, int off) {
		map.add(tx, ty, off, off, LightModel.torch_big);
	}
	
	public void fire(int tx, int ty, int off) {
		map.add(tx, ty, off, off, LightModel.fire);
	}
	
	public void candle(int tx, int ty, int off) {
		map.add(tx, ty, off, off, LightModel.candle);
	}
	
	public void candle(int tx, int ty, int offx, int offy) {
		map.add(tx, ty, offx, offy, LightModel.candle);
	}

	public void remove(int tx, int ty) {
		map.remove(tx, ty);
	}
	
	public void hide(int tx, int ty, boolean hide) {
		map.hide(tx, ty, hide);
	}
	
	public boolean is(int tx, int ty) {
		return map.is(tx, ty);
	}

	@Override
	protected void update(double ds, Profiler profiler) {
		
	}

	public void render(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {
		FireSparks.update(ds);
		LightModel.flickerr(ds);
		sprites.displacement.update(ds);
		sprites.texture.update(ds);
		map.render(r, s, ds, renWindow, offX, offY);
	}
	
	public void renderMouse(int x, int y, int offx, int offy, int rnd) {
		CORE.renderer().shadeLight(false);
		LightModel.mouse.register(CORE.renderer(), rnd, x, y, offx, offy);
	}
	
	public LOS_MAP los() {
		return los;
	}

}
