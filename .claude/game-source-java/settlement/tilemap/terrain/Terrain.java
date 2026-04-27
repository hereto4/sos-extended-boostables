package settlement.tilemap.terrain;

import static settlement.main.SETT.FLOOR;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TAREA;
import static settlement.main.SETT.TERRAIN;

import java.io.IOException;
import java.util.Arrays;

import game.debug.Profiler;
import init.paths.PATH;
import init.paths.PATHS;
import init.type.TERRAIN;
import init.type.TERRAINS;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.thing.pointlight.LOS;
import settlement.tilemap.TileMap;
import settlement.tilemap.TileMap.SMinimapGetter;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_INTE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GBox;
import util.keymap.MAPPED;
import util.keymap.MAPSAVE;
import util.keymap.RMAP;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public final class Terrain extends TileMap.Resource implements MAP_OBJECT<Terrain.TerrainTile> {

	private final int NOTHING_CODE = 0;
	private final byte[] tiles = new byte[TAREA];
	
	private final short[] datad = new short[TAREA];
	private final ArrayList<TerrainTile> look = new ArrayList<>(256);
	public final TIndoors indoors = new TIndoors();
	// canBeWater 1
	// istree 1
	// tree amount / reserved 4

	public final TerrainTile NADA = new TNothing(this);
	public final TColors colors = new TColors();
	public final TDestroyed DESTROYED = new TDestroyed(this);
	public final TWater WATER = new TWater(this);;
	public final TMountain MOUNTAIN = TMountain.make(this);
	public final TMountain.Ceiling CAVE = MOUNTAIN.CAVE;
	public final TRock ROCK = new TRock(this);
	public final TForest TREES = new TForest(this);
	public final TFlower FLOWER = new TFlower(this);
	public final TBush BUSH = new TBush(this);
	public final TMushroom MUSHROOM = new TMushroom(this);
	public final LIST<TGrowable> GROWABLES = TGrowable.make(this); 
	public final RMAP<TFortification> FORTIFICATIONS = TFortification.make(this);
	public final TFortification.Stairs FSTAIRS = TFortification.Stairs.make(this);
	public final TBuilding.TBuildings BUILDINGS = new TBuilding.TBuildings(this);
	public final RMAP<TFence> FENCES = TFence.get(this);
	public final TerrainDiagonal diagonal = new TerrainDiagonal();
	
	public final TDecor DECOR_BEACH = new TDecor(this, "Beach object", "BEACH_1X1");
	public final TDecor DECOR_MID = new TDecor(this, "", "MID_FERTILITY_1X1");
	public final TDecor DECOR_WOOD = new TDecor(this, "", "WOOD_1X1");
	public final TDecor DECOR_NO = new TDecor(this, "", "INFERTILE_1X1");
	
	private final int[] counts = new int[look.size()];
	
	public final TILE_SHEET wall_merge;
	
	public final MAP_OBJECT<TerrainClearing> clearing = new MAP_OBJECT<TerrainClearing>() {

		@Override
		public TerrainClearing get(int tile) {
			return Terrain.this.get(tile).clearing();
		}

		@Override
		public TerrainClearing get(int tx, int ty) {
			return get(tx+ty*SETT.TWIDTH);
		}

	};


	final MAP_INTE data = new MAP_INTE() {
		
		@Override
		public MAP_INTE set(int tile, int value) {
			if ((value & 0xFFFF0000) != 0)
				throw new RuntimeException();
			datad[tile] = (short) value;
			
			return this;
		}
		
		@Override
		public int get(int tile) {
			return datad[tile] & 0x0FFFF;
		}

		@Override
		public int get(int tx, int ty) {
			return get(tx+ty*SETT.TWIDTH);
		}

		@Override
		public MAP_INTE set(int tx, int ty, int value) {
			return set(tx+ty*SETT.TWIDTH, value);
		}
	};
	
	public Terrain(TileMap creator) throws IOException{
		new TerrainPlacers(this, look);
		
		final PATH path = PATHS.SPRITE_SETTLEMENT().getFolder("map");
		
		wall_merge = (new ITileSheet(path.get("WallMerge"), 152, 28) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s16;
				s.full.init(0, 0, 1, 1, 4, 1, t);
				s.full.setSkip(4, 0).paste(true);
				s.full.setSkip(4, 0).pasteRotated(3, true);

				return t.saveGame();
			}

		}).get();
		
		KeyMap<TerrainTile> map = new KeyMap<Terrain.TerrainTile>();
		for (TerrainTile t : all()) {
			map.put(t.key(), t);
		}
	}


	@Override
	protected void save(FilePutter saveFile) {
		MAPSAVE.saveMeta(saveFile, look);
		saveFile.bs(tiles);
		saveFile.ss(datad);
		saveFile.isE(counts);
		WATER.groundWater.save(saveFile);
		WATER.groundWaterSalt.save(saveFile);
		WATER.deepSeaFishSpot.save(saveFile);
		WATER.fishAmount.save(saveFile);
		indoors.saver.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		
		int[] order = MAPSAVE.saveWash(saveFile, look, NOTHING_CODE);
		saveFile.bs(tiles);
		saveFile.ss(datad);
		saveFile.isE(counts);
		WATER.groundWater.load(saveFile);
		WATER.groundWaterSalt.load(saveFile);
		WATER.deepSeaFishSpot.load(saveFile);
		WATER.fishAmount.load(saveFile);
		indoors.saver.load(saveFile);
		
		
		colors.init();
		if (order != null) {
			Arrays.fill(counts, 0);
			counts[NOTHING_CODE] = SETT.TAREA;
			indoors.saver.clear();
			for (int i = 0; i < tiles.length; i++) {
				tiles[i] = (byte) order[tiles[i]];
				counts[tiles[i]] ++;
			}
		}

	}
	
	@Override
	protected void clearAll() {
		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = NOTHING_CODE;
			datad[i] = 0;
		}
		Arrays.fill(counts, 0);
		counts[NOTHING_CODE] = SETT.TAREA;
		indoors.saver.clear();
		colors.init();
		WATER.groundWater.clear();;
		WATER.groundWaterSalt.clear();
	}

	@Override
	protected void update(double ds, Profiler profiler) {
		profiler.logStart(WATER);
		WATER.update(ds);
		profiler.logEnd(WATER);
		profiler.logStart(TREES);
		TREES.update(ds);
		profiler.logEnd(TREES);
		profiler.logStart(colors);
		colors.update(ds);
		profiler.logEnd(colors);
		profiler.logStart(BUSH);
		BUSH.update(ds);
		profiler.logEnd(BUSH);
	}
	
//	public COLOR miniColor(int x, int y) {
//		return get(x, y).miniC(x, y);
//	}
//	
//	@Override
//	protected COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
//		return get(x, y).miniCPimp(c, x, y, northern, southern);
//	}

	public LIST<TerrainTile> all(){
		return look;
	}
	
	@Override
	public TerrainTile get(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return look.get(tiles[ty * SETT.TWIDTH + tx] & 0x0FF);
		return NADA;
	}

	@Override
	public TerrainTile get(int tile) {
		return look.get(tiles[tile] & 0x0FF);
	}
	
	public void renderAbove(Renderer r, ShadowBatch shadowBatch, RenderData data) {

		RenderData.RenderIterator i = data.onScreenTiles(2, 2, 2, 2);
		
		while (i.has()) {

			
			int t = tiles[i.tile()];
			int d = this.datad[i.tile()];
			if (look.get(t).renderAbove(r, shadowBatch, i, d))
				i.hiddenSet();
			else {
				
			}
				
			
			
			i.next();
		}

	}
	
	public void renderBelow(Renderer r, ShadowBatch shadowBatch, RenderData data) {

		RenderData.RenderIterator i = data.onScreenTiles(1, 0, 1, 0);
		COLOR.unbind();
		OPACITY.unbind();
		while (i.has()) {
			int t = tiles[i.tile()];
			int d = this.datad[i.tile()];
			if (look.get(t).renderBelow(r, shadowBatch, i, d))
				i.hiddenSet();
			i.next();
		}

	}
	
	public void renderMid(Renderer r, ShadowBatch shadowBatch, RenderData data) {

		RenderData.RenderIterator i = data.onScreenTiles(0, 0, 0, 0);
		COLOR.unbind();
		OPACITY.unbind();
		while (i.has()) {
			int t = tiles[i.tile()];
			int d = this.datad[i.tile()];
			if (look.get(t).renderMid(r, shadowBatch, i, d))
				i.hiddenSet();
			i.next();
		}

	}
	
	public static abstract class TerrainTile implements GAMETILE, MAP_BOOLEAN, SMinimapGetter, MAPPED {

		public final int code;
		private final CharSequence name;
		protected final COLOR miniC;
		protected final Terrain shared;
		private final SPRITE icon;
		private final String key;

		protected TerrainTile(String key, Terrain shared, CharSequence name, SPRITE icon, COLOR miniC) {
			this.shared = shared;
			this.code = shared.look.add(this);
			this.name = name;
			this.miniC = miniC != null ? miniC.shade(0.5) : null;
			this.icon = icon;
			this.key = key;
		}
		
//		protected TerrainTile(Terrain shared, CharSequence name, SPRITE icon, COLOR miniC) {
//			this.shared = shared;
//			this.code = shared.look.add(this);
//			this.name = name;
//			this.miniC = miniC != null ? miniC.shade(0.5) : null;
//			this.icon = icon;
//		}

		protected abstract boolean place(int tx, int ty);

		public void placeRaw(int x, int y) {
			if (IN_BOUNDS(x, y)) {
				shared.counts[shared.tiles[y * SETT.TWIDTH + x]]--;
				shared.tiles[y * SETT.TWIDTH + x] = (byte) code;
				shared.counts[code]++;
					
				shared.data.set(x,  y, 0);
			}
		}
		
		@Override
		public COLOR miniC(int x, int y) {
			return miniC;
		}
		
		@Override
		public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
			return miniC;
		}

		@Override
		public SPRITE getIcon() {
			return icon;
		}

		@Override
		public CharSequence name() {
			return name;
		}

		protected abstract boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data);

		protected abstract boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data);
		
		protected boolean renderMid(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			return false;
		}


		@Override
		public boolean is(int tile) {
			return shared.get(tile) == this;
		}

		boolean wallJoiner() {
			return false;
		}

		boolean wallIsWally() {
			return false;
		}
		
		public boolean isMassiveWall() {
			return false;
		}

		/**
		 * 
		 * @return true for ceilings and openings
		 */
		public boolean roofIs() {
			return false;
		}

		/**
		 * 
		 * @return true if under roof. Not true for openings
		 */
//		public boolean isIndoors() {
//			return false;
//		}

		public abstract AVAILABILITY getAvailability(int tx, int ty);

		public LOS los(int tx, int ty) {
			return LOS.OPEN;
		}
		
		void unplace(int tx, int ty) {

		}
		
		public void hoverInfo(GBox box, int tx, int ty) {
			box.textLL(name);
		}

		private void placeFixed(int tx, int ty, int it) {
			if (it > 128)
				return;
			if (!IN_BOUNDS(tx, ty))
				return;
			
			TerrainTile old = shared.get(tx, ty);
			old.unplace(tx, ty);
			
			boolean res = TERRAIN().indoors.remove(tx, ty);
			if (place(tx, ty) || old != shared.get(tx, ty)) {
				TERRAIN().indoors.add(tx, ty, res);
				if (old.miniC(tx, ty) != null || shared.get(tx, ty).miniC(tx, ty) != null)
					SETT.TILE_MAP().miniCUpdate(tx, ty);;
				PATH().availability.updateAvailability(tx, ty);

				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR d = DIR.ALL.get(i);
					shared.get(tx + d.x(), ty + d.y()).placeFixed(tx + d.x(), ty + d.y(), it+1);
				}
			}else {
				TERRAIN().indoors.add(tx, ty, res);
			}
			
			FLOOR().updateStructure(tx, ty);
			
			
			
		}
		
		@Override
		public void placeFixed(int tx, int ty) {
			placeFixed(tx, ty, 0);

				
		}

		public TerrainClearing clearing() {
			return TerrainClearing.dummy;
		}
		
		public int miniDepth() {
			return 0;
		}
		
		public TERRAIN terrain(int tx, int ty) {
			return TERRAINS.NONE();
		}
		
		public int heightStart(int tx, int ty) {
			return 0;
		}
		public int heightEnd(int tx, int ty) {
			return 0;
		}
		
		public int heightEnt(int tx, int ty) {
			return 0;
		}

		public boolean coversCompletely(int tx, int ty) {
			return false;
		}
		
		public int count() {
			return SETT.TERRAIN().counts[code];
		}
		
		@Override
		public final int index() {
			return code;
		}
		
		@Override
		public final String key() {
			return key;
		}
		
		@Override
		public boolean is(int tx, int ty) {
			return SETT.IN_BOUNDS(tx, ty) && is(tx+ty*SETT.TWIDTH);
		}
		
		public boolean wantsFloorUnderneath(int tx, int ty) {
			return false;
		}
	}

}
