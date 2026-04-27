package world.map.terrain;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.TAREA;
import static world.WORLD.TWIDTH;
import static world.WORLD.WATER;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import init.constant.C;
import init.paths.PATHS;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.TERRAINS;
import snake2d.PathTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_INT;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.GUTIL;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import view.tool.ToolManager;
import world.WORLD;
import world.map.terrain.WorldTerrain.WorldTerrainResource;

public class WorldMountain extends WorldTerrainResource{

	private byte[] data;
	private static byte NOTHING = -1;
	private final TILE_SHEET sheet = (new ITileSheet(PATHS.SPRITE_WORLD_MAP().get("Mountain"), 576, 316) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house2.init(0, 0, 3, 3, t);

			s.house2.setVar(0).paste(2, true);
			s.house2.setVar(1).paste(2, true);
			s.house2.setVar(2).paste(1, true);
			
			s.house2.setVar(3).paste(2, true);
			s.house2.setVar(4).paste(2, true);
			s.house2.setVar(5).paste(1, true);
			
			s.house2.setVar(6).paste(true);
			s.house2.setVar(7).paste(true);
			s.house2.setVar(8).paste(1, true);

			s.full.init(0, s.house2.body().y2(), 1, 1, 16, 3, t);
			s.full.setSkip(16, 0).paste(1, true);
			s.full.setSkip(4, 16).paste(3, true);
			return t.saveGame();

		}
	}).get();
	
	private final static int SET = 16;
	private final static int EDGES = 0;
	private final static int FULLS = EDGES+8*SET;
	private final static int TOPS = FULLS+8*SET;
	private final static int SINGLES = TOPS+4*SET;
	private final static int SINGLES_FULLS = SINGLES+2*SET;
	
	private static final int MAX_HEIGHT = 15;
	private final COLOR[] colors = COLOR.interpolate(new ColorImp(80,80,80), new ColorImp(210,210,210), MAX_HEIGHT);
	private final Bitmap1D top = new Bitmap1D(TAREA(), false);
	public final SPRITE icon;
	
	WorldMountain() throws IOException {
		data = new byte[TAREA()];
		for (int i = 0; i < data.length; i++)
			data[i] = (byte) NOTHING;
		
		icon = new SPRITE.Imp(Icon.L) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int d = (Y2-Y1)/2;
				sheet.render(r, EDGES + DIR.SE.mask(), X1, X1+d, Y1, Y1+d);
				sheet.render(r, EDGES + DIR.SW.mask(), X1+d, X1+d*2, Y1, Y1+d);
				sheet.render(r, EDGES + DIR.NE.mask(), X1, X1+d, Y1+d, Y1+d*2);
				sheet.render(r, EDGES + DIR.NW.mask(), X1+d, X1+d*2, Y1+d, Y1+d*2);
			}
		};
		
		if (false) {
			//check where the game actually thinks there is mountain!
		}
	}
	
	private abstract class Placable extends PlacableMulti{
		
		
		public Placable(CharSequence name, SPRITE icon) {
			super(name, "", icon);
		}

		@Override
		public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area,
				PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
			super.renderPlaceHolder(r, mask, x + C.TILE_SIZEH, y + C.TILE_SIZEH, tx, ty, area, type, isPlacable, areaIsPlacable);
		}
		
		
	}
	
	private void set(int tx, int ty, int value) {
		data[tx + ty*TWIDTH()] &= 0b11110000;
		data[tx + ty*TWIDTH()] |= value & 0b00001111;
		WORLD.changeTile(tx, ty);
	}
	
	private int get(int tile) {
		return data[tile] & 0b00001111;
	}
	
	private int height(int tx, int ty) {
		return height(tx+ty*TWIDTH());
	}
	
	private int height(int tile) {
		return (data[tile] >> 4) & 0b00001111;
	}
	
	private void heightSet(int tile, int h) {
		if (h > 14)
			h = 14;
		else if (h < 0)
			h = 0;
		h = h << 4;
		data[tile] &= 0b00001111;
		data[tile] |= h;
		
	}
	
	private boolean has(int tile) {
		return data[tile] != NOTHING;
	}
	
	public void pClear(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			data[tx + ty*TWIDTH()] = (byte) NOTHING;
		}
	}
	
	void placeRaw(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			data[tx + ty*TWIDTH()] = (byte) 0;
		}
	}
	
	private boolean isp(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			return data[tx + ty*TWIDTH()] != NOTHING;
		}
		return false;
	}
	

	
	boolean isp(int tx, int ty, DIR d) {
		return isp(tx+d.x(), ty+d.y());
	}
	
	void fix(int tx, int ty) {
		
		if (!isp(tx, ty))
			return;
		
		setHeight(tx, ty);
		GUTIL.flooder().init(this);
		for (int i = 0; i < DIR.ALL.size(); i++) {
			if (isp(tx, ty, DIR.ALL.get(i))) {
				GUTIL.flooder().pushSloppy(tx, ty, DIR.ALL.get(i), 0);
			}
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollAndReopen();
			if (!setHeight(t.x(), t.y()))
				continue;
			for (int i = 0; i < DIR.ALL.size(); i++) {
				if (isp(t.x(), t.y(), DIR.ALL.get(i)))
					GUTIL.flooder().pushSloppy(t.x(), t.y(), DIR.ALL.get(i), 0);
			}
		}
		
		GUTIL.flooder().done();
		
		int h = getHeight(tx, ty);
		int da = get(tx+ty*TWIDTH());
		int i = tx+ty*TWIDTH();
		top.set(i, true);
		for (DIR d : DIR.ORTHO) {
			if (IN_BOUNDS(tx, ty, d) && (d.mask() & da) != 0 && getHeight(tx+d.x(), ty+d.y()) > h && ((get(i+d.x()+d.y()*TWIDTH()) & 0x0F) != 0))
				top.set(tx+ty*TWIDTH(), false);
		}
		
	}
	
	void sink(int tx, int ty) {
		
		if (!isp(tx, ty))
			return;
		
		int h = height(tx, ty);
		int newHeight = h-1;
		if (newHeight < 0)
			newHeight = 0;
		heightSet(tx+ty*TWIDTH(), newHeight);
		
		GUTIL.flooder().init(this);
		for (int i = 0; i < DIR.ALL.size(); i++) {
			if (isp(tx, ty, DIR.ALL.get(i))) {
				GUTIL.flooder().pushSloppy(tx, ty, DIR.ALL.get(i), 0);
			}
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollAndReopen();
			if ((tx == t.x() && ty == t.y()) || !setHeight(t.x(), t.y()))
				continue;
			for (int i = 0; i < DIR.ALL.size(); i++) {
				if (isp(t.x(), t.y(), DIR.ALL.get(i)))
					GUTIL.flooder().pushSloppy(t.x(), t.y(), DIR.ALL.get(i), 0);
			}
		}
		
		GUTIL.flooder().done();
	}
	
	private boolean setHeight(int tx, int ty) {
		
		int height = getHeight(tx, ty);
		
		if (!neigboursTerrain(tx, ty)) {
			int lowest = 15;
			
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				int h = getHeight(tx, ty, d);
				if (h < lowest) {
					lowest = h;
				}
			}
			
			if (height != lowest+1 && height != 15) {
				heightSet(tx+ty*TWIDTH(), lowest);
				return true;
			}
		}else {
			heightSet(tx+ty*TWIDTH(), 0);
		}
	
		set(tx, ty, getJoin(tx, ty));
		
		
		return false;
	}
	
	private int getJoin(int tx, int ty) {
		int height = getHeight(tx, ty);
		int res = 0;
		
		for (DIR d : DIR.NORTHO) {
			int x = tx + (d.x()+1)/2;
			int y = ty + (d.y()+1)/2;
			if (!WATER().has.is(x, y)) {
				if (getHeight(tx, ty, d) >= height && getHeight(tx, ty, d.next(-1)) >= height && getHeight(tx, ty, d.next(1)) >= height)
					res |= d.mask();
				
			}
		}
		
		return res;
	}
	
	public int getHeight(int tx, int ty) {
		if (!IN_BOUNDS(tx, ty))
			return 15;
		if (!isp(tx, ty))
			return 0;
		if (neigboursTerrain(tx, ty))
			return 1;
		return 1+height(tx, ty);
	}
	
	private boolean neigboursTerrain(int tx, int ty){
		return WATER().has.is(tx, ty) || WATER().has.is(tx+1, ty+1) || WATER().has.is(tx+1, ty) || WATER().has.is(tx, ty+1);
	}
	
	private int getHeight(int tx, int ty,DIR d) {
		return getHeight(tx+d.x(), ty+d.y());
	}
	
	double getHeightNormalized(int tx, int ty) {
		return getHeight(tx, ty)/15.0;
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		saveFile.bs(data);
		top.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		saveFile.bs(data);
		top.load(saveFile);
	}
	
	@Override
	protected void clear() {
		Arrays.fill(data, NOTHING);
		top.setAll(false);
	}
	

	

	public void render(SPRITE_RENDERER r, ShadowBatch s, RenderData data){
		
		RenderIterator it = data.onScreenTiles(1, 0, 1, 0);
		
		snowHeightI = TIME.seasons().bitCurrent();
		
		while(it.has()) {
			
			if (has(it.tile())) {
				int t = get(it.tile());
				int h = height(it.tile());
				colors[Math.max(h-1, 0)].bind();
				int ran = it.ran();
				
				
				//colors[h].bind();�
				
//				int hi = heighter.get(it.tile());
//				if (hi > 0) {
//					COLOR.RED100.render(r, it.x(),it.y());
//				}
				
				int x = it.x()+C.TILE_SIZEH;
				int y = it.y()+C.TILE_SIZEH;
				if (t == 0) {
					if (h == 0) {
						colors[h].bind();
						sheet.render(r, SINGLES+(ran&0x1F), x, y);
					}
					else {
						sheet.render(r, SINGLES_FULLS+(ran&0x0F), x, y);
						if (h > 2) {
							render(h, 15, ran, SINGLES_FULLS+(ran&0x0F), x, y);
						}
					}
				}else {
					if (h == 0) {
						sheet.render(r, t+EDGES+(ran&0x07)*SET, x, y);
					}else {
						int tile = t+FULLS+(ran&7)*SET;
						sheet.render(r, tile, x, y);
						render(h, t, ran, tile, x, y);

						
						colors[h].bind();
						tile = t+TOPS+(ran&3)*SET;
						sheet.render(r, tile, x, y);
						render(h, t, ran, tile, x, y);
							
					}
					
					
					
				}
				if ( h >= 2)
					it.hiddenSet();
				
				
			}

			it.next();
		}
		COLOR.unbind();
				
	}
	
	int snowHeightI;
//	private final int[] snowHeights = new int[] {3,4,3,2};
	
	
	private void render(int h, int rot, int ran, int tile, int x, int y) {
//		rot &= 0x00F;
//		if (h >= snowHeights[snowHeightI]) {
//			
//
//			OPACITY.O99.bind();
//			COLOR.WHITE120.bind();
//			if (h > snowHeights[snowHeightI]) {
//				TextureCoords text = sheet.getTexture(0x0F);
//				sheet.renderTextured(text, tile, x, y);
//			}else if (rot != 0 && rot != 0x0F){
//				TextureCoords text = sheet.getTexture(TOPS+rot + (ran&3)*SET);
//				sheet.renderTextured(text, tile, x, y);
//			}
//			OPACITY.unbind();
//			COLOR.unbind();
//			//SPRITES.world().map.forest.render(r, 16*4+(ran&0x3), x, y);
//		}
	}
	
	
	private final DIR[] checks = new DIR[] {DIR.C, DIR.W, DIR.NW, DIR.N};
	
	public boolean coversTile(int tx, int ty) {
		if (WATER().has.is(tx, ty))
			return false;
		if (getHeight(tx, ty) == 0 && (get(tx+ty*TWIDTH()) & DIR.NW.mask()) == 0)
			return false;
		for (DIR d : checks) {
			int x = tx+d.x();
			int y = ty+d.y();
			int i = x+y*TWIDTH();
			if (!IN_BOUNDS(x, y))
				continue;
			if (!(has(i)))
				return false;
		}
		return true;
	}
	
	public boolean is(int tx, int ty) {
		return is(tx, ty, DIR.NW) || is(tx-1, ty, DIR.NE) || is(tx-1, ty-1, DIR.SE) || is(tx, ty-1, DIR.SW);
	}
	
	private final boolean[] centres = new boolean[] {
			true,false,false,false,false,true,false,true,false,false,true,true,false,true,true,true
	};
	
	private boolean is(int tx, int ty, DIR d) {
		if (!isp(tx, ty))
			return false;
		if (getHeight(tx, ty) > 1)
			return true;
		int m = get(tx + ty*TWIDTH());
		return (m & d.mask()) > 0 || centres[m];
	}
	
	public MAP_BOOLEAN haser = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return IN_BOUNDS(tx, ty) && WorldMountain.this.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			int tx = tile % TWIDTH();
			int ty = tile /TWIDTH();
			return is(tx, ty);
		}
	};
	
	public MAP_INT heighter = new MAP_INT() {
		
		@Override
		public int get(int tx, int ty) {
			int h = 16;
			for (DIR d : checks) {
				int x = tx+d.x();
				int y = ty+d.y();
				if (!IN_BOUNDS(x, y))
					continue;
				int i = x+y*TWIDTH();
				if ((has(i))) {
					h = Math.min(h, height(i)+1);
				}else
					return 0;
			}
			return h;
		}
		
		@Override
		public int get(int tile) {
			return get(tile%TWIDTH(), tile/TWIDTH());
		}
	};
	
	public final AreaTileMountain area = new AreaTileMountain();
	
	public final static class AreaTileMountain{
		
		private AreaTileMountain() {
			
		}
		
		public boolean is(int tx, int ty, DIR d) {
			
			if (d == DIR.C)
				return ispp(tx, ty, DIR.NW);
			else if (d == DIR.N) {
				return ispp(tx, ty-1, DIR.W);
			}else if (d == DIR.NE) {
				return ispp(tx, ty-1, DIR.C);
			}else if (d == DIR.E) {
				return ispp(tx, ty, DIR.N);
			}else if (d == DIR.SE) {
				return ispp(tx, ty, DIR.C);
			}else if (d == DIR.S) {
				return ispp(tx, ty, DIR.W);
			}else if (d == DIR.SW) {
				return ispp(tx-1, ty, DIR.C);
			}else if (d == DIR.W) {
				return ispp(tx-1, ty, DIR.N);
			}else { //NW
				return ispp(tx-1, ty-1, DIR.C);
			}
			
			
		}
		
		private boolean ispp(int tx, int ty, DIR d) {
			
			if (!WORLD.MOUNTAIN().isp(tx, ty))
				return false;
			if (WORLD.MOUNTAIN().getHeight(tx, ty) > 1)
				return true;
			int m = WORLD.MOUNTAIN().get(tx + ty*TWIDTH());
			if (d == DIR.C)
				return WORLD.MOUNTAIN().centres[m];
			if (d.isOrtho()) {
				return (m & d.next(1).mask()) > 0 && (m & d.next(-1).mask()) > 0;
			}
			return (m & d.mask()) > 0;
			
		}
		
		public boolean borders(int tx, int ty, DIR d) {
			
			if (d == DIR.C)
				throw new RuntimeException();
			
			if (d == DIR.N) {
				return WORLD.MOUNTAIN().is(tx, ty-1, DIR.NW);
			}else if (d == DIR.NE) {
				return WORLD.MOUNTAIN().is(tx, ty-1, DIR.NE);
			}else if (d == DIR.E) {
				return WORLD.MOUNTAIN().is(tx, ty, DIR.NE);
			}else if (d == DIR.SE) {
				return WORLD.MOUNTAIN().is(tx, ty, DIR.SE);
			}else if (d == DIR.S) {
				return WORLD.MOUNTAIN().is(tx, ty, DIR.SW);
			}else if (d == DIR.SW) {
				return WORLD.MOUNTAIN().is(tx-1, ty, DIR.SW);
			}else if (d == DIR.W) {
				return WORLD.MOUNTAIN().is(tx-1, ty, DIR.NW);
			}else { //NW
				return WORLD.MOUNTAIN().is(tx-1, ty-1, DIR.NW);
			}
			
		}
		
	}
	
	double add(WorldTerrainInfo info, int tx, int ty) {
		if (is(tx, ty)) {
			double m = 0;
			for (DIR d : DIR.ALLC) {
				if (is(tx, ty, d))
					m += 1;
			}
			m /= DIR.ALLC.size();
			info.add(TERRAINS.MOUNTAIN(), m);
			return m;
		}
		return 0;
	}

	public void clear(int tx, int ty) {
		pClear(tx, ty);
		for (int i = 0; i < DIR.ALL.size(); i++) {
			fix(tx+DIR.ALL.get(i).x(), ty+DIR.ALL.get(i).y());
		}
		WORLD.MINIMAP().update(tx, ty);
		
	}
	
	@Override
	public LIST<PLACABLE> placers(ToolManager tm) {
		ArrayListGrower<PLACABLE> placers = new ArrayListGrower<>();
		
		PLACABLE clear = new Placable("clear mountain", icon.twin(UI.icons().m.anti, DIR.C, 0)) {

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return isp(tx, ty) ? null : "";
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				pClear(tx, ty);
				for (int i = 0; i < DIR.ALL.size(); i++) {
					fix(tx+DIR.ALL.get(i).x(), ty+DIR.ALL.get(i).y());
				}
				WORLD.MINIMAP().update(tx, ty);
			}

		};
		
		PLACABLE placer = new Placable("mountain", icon) {

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return IN_BOUNDS(tx, ty) ? null : "";
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (!isp(tx, ty)) {
					placeRaw(tx, ty);
					fix(tx, ty);
					for (int i = 0; i < DIR.ALL.size(); i++) {
						fix(tx+DIR.ALL.get(i).x(), ty+DIR.ALL.get(i).y());
					}
					WORLD.MINIMAP().update(tx, ty);
				}
			}
			
			@Override
			public PLACABLE getUndo() {
				return clear;
			}
			
			@Override
			public SPRITE getIcon() {
				return icon;
			}
			
		};
	
		PLACABLE t = new Placable("sink mountain", icon.twin(UI.icons().m.arrow_down, DIR.S, 0)) {

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return isp(tx, ty) ? null : "";
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				sink(tx, ty);
			}
			
		};
		
		placers.add(placer);
		placers.add(t);
		placers.add(clear);
		return placers;
	}
	
	
}
