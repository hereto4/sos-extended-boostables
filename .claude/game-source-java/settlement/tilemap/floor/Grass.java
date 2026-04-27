package settlement.tilemap.floor;

import static settlement.main.SETT.TAREA;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;

import game.audio.AUDIO;
import game.audio.SoundRace;
import game.debug.Profiler;
import init.paths.PATHS;
import settlement.main.SETT;
import settlement.tilemap.TileMap;
import settlement.tilemap.ground.GroundType;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_INTE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitsmap1D;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.spritecomposer.ComposerThings;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public class Grass extends TileMap.Resource{
	
	private final Bitsmap1D data = new Bitsmap1D(0, 4, TAREA);
	
	public final SoundRace clearSound = AUDIO.race("CLEAR_GRASS");
	public final static int TYPES = 0x0F;
	private final static double TYPESI = 1.0/TYPES;

	final GrassRenderer renderer;
	
	public Grass() throws IOException{
		
		renderer = new GrassRenderer(this);
		
		new ComposerThings.IInit(PATHS.SPRITE_SETTLEMENT_MAP().get("Grass"), 972, 390);
		
		final PlacableMulti ppu = new PlacableMulti("Remove") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				data.set(tx+ty*TWIDTH, CLAMP.i(data.get(tx+ty*TWIDTH)-1, 0, TYPES));
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		};
		
		final PlacableMulti pp = new PlacableMulti("Grass") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				data.set(tx+ty*TWIDTH, CLAMP.i(data.get(tx+ty*TWIDTH)+1, 0, TYPES));
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
			
			@Override
			public PLACABLE getUndo() {
				return ppu;
			}
		};
		
		IDebugPanelSett.add(pp);
		
	}
	
	public void grow(int tx, int ty) {
		grow (tx, ty, 1 + RND.rInt(2));
	}
	
	private final double[] treepenalty = new double[] {
		0.1,0.20,0.20,0.1
	};
	
	public void grow(int tx, int ty, int amount) {
		int tile = tx+ty*TWIDTH;

		
		int b = growthMax(tx, ty);
		int c = data.get(tile);
		
		if (c < b) {
			c += amount;
			if (c > b)
				c = b;
			
		}else if (c > b) {
			c -= amount;
			if (c < b)
				c = b;
			
		}
		
		data.set(tile, c);
	}
	
	public int growthMax(int tx, int ty) {
		
		GroundType t = SETT.GROUND().MAP.get(tx, ty);
		double v = SETT.GROUND().MOISTURE_CURRENT.get(tx, ty);
		v *= t.vegitation;
		if (t == SETT.GROUND().types.FOREST)
			v *= 0.6;
		else if (t == SETT.GROUND().types.PASTURE)
			v *= 0.8;
		
		if (v > 0.4) {
			for (int i = 0; i < treepenalty.length; i++) {
				if (SETT.TERRAIN().TREES.isTree(tx, ty+i)) {
					v = CLAMP.d(v -treepenalty[i], 0.4, v);
				}
			}
		}
		
		
		int b = CLAMP.i((int) (TYPES*v), 0, TYPES);
		return b;
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		renderer.update(ds);
	}
	
	private final OPACITY[] op = new OPACITY[TYPES];
	{
		for (int i = 0; i < TYPES; i++) {
			int p = (int) (127*(i+1.0)/TYPES);
			op[i] = new OpacityImp(p);
 		}
	}
	
	public void render(double ds, Renderer r, RenderData data) {
	
		renderer.render(ds, r, data);
		
	}
	
	public void render(RenderIterator it) {
		
		renderer.render(it, CORE.renderer());
		
	}
	
	public COLOR color(int ran) {
		return renderer.color(ran);
	}
	
	
	@Override
	protected void save(FilePutter saveFile) {
		data.save(saveFile);
		
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		data.load(saveFile);
	}
	
	
	@Override
	protected void clearAll() {
		data.clear();
	}
	
	public final MAP_DOUBLEE current = new MAP_DOUBLEE.DoubleMapImp(TWIDTH, THEIGHT) {
		
		@Override
		public double get(int tile) {
			 return (double) data.get(tile)*TYPESI;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			currentI.set(tile, (int) (value*TYPES));
			return this;
		}
	};
	
	public final MAP_INTE currentI = new MAP_INTE.INT_MAPEImp(TWIDTH, THEIGHT) {
		
		@Override
		public int get(int tile) {
			return data.get(tile);
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			value = CLAMP.i(value, 0, TYPES);
			data.set(tile, value);
			return this;
		}
	};
	

	
}