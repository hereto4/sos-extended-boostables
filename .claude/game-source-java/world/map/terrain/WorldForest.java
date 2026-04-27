package world.map.terrain;

import static world.WORLD.CLIMATE;
import static world.WORLD.IN_BOUNDS;
import static world.WORLD.REGIONS;
import static world.WORLD.TAREA;
import static world.WORLD.TWIDTH;
import static world.WORLD.WATER;

import java.io.IOException;

import game.debug.Profiler;
import init.constant.C;
import init.paths.PATHS;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAINS;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.IColorSampler;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import view.tool.ToolManager;
import world.WORLD;
import world.map.terrain.WorldTerrain.WorldTerrainResource;

public class WorldForest extends WorldTerrainResource {
	
	private final Bitsmap1D data = new Bitsmap1D(0, 4, TAREA());
	private static final int SET = 16;

	private static final int max = 3;

	private final static int colorA = 64;

	public final SPRITE icon;

	private final Sprites sprites = new Sprites();

	WorldForest(WORLD m) throws IOException {
		
		icon = new SPRITE.Imp(Icon.L) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int t = 16*2;
				COLOR.WHITE100.bind();
				sprites.bg.render(r, t, X1-1, X2-1, Y1-1, Y2-1);
				COLOR.BLACK.bind();
				sprites.bg.render(r, t, X1+1, X2+1, Y1+1, Y2+1);
				sprites.colors[0][0].bind();
				
				sprites.bg.render(r, t, X1, X2, Y1, Y2);
				sprites.sheet.render(r, t, X1, X2, Y1, Y2);
				COLOR.unbind();
			}
		};
	}

	public final MAP_DOUBLEE amount = new MAP_DOUBLEE() {

		private final double amI = 1.0 / max;

		@Override
		public double get(int tile) {
			return data.get(tile) * amI;
		}

		@Override
		public double get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx + ty * TWIDTH());
			return 0;
		}

		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			data.set(tile, CLAMP.i((int) Math.ceil(value * max), 0, max));
			WORLD.changeTile(tile%TWIDTH(), tile/TWIDTH());
			return this;
		}

		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (IN_BOUNDS(tx, ty))
				set(tx + ty * TWIDTH(), value);
			return this;
		}
	};

	public final MAP_BOOLEAN is = new MAP_BOOLEAN() {
		@Override
		public boolean is(int tx, int ty) {
			return amount.get(tx, ty) > 0;
		}

		@Override
		public boolean is(int tile) {
			return amount.get(tile) > 0;
		}
	};

	public final MAP_BOOLEAN placable = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return false;

			if (WATER().coversTile.is(tx, ty))
				return false;
			return true;
		}

		@Override
		public boolean is(int tile) {
			return is(tile % TWIDTH(), tile / TWIDTH());
		}
	};

	@Override
	protected void save(FilePutter saveFile) {
		data.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		data.load(saveFile);
	}
	
	@Override
	protected void clear() {
		data.setAll(0);
	}

	@Override
	protected void update(double ds, Profiler prof) {

	}

	private int[] cols = new int[3];

	public void render(SPRITE_RENDERER r, ShadowBatch s, RenderData data) {

		// SPRITES.world().map.forest_colors.get((int)
		// (TIME.year().partOf()*16)).bind();

		for (CLIMATE z : CLIMATES.ALL()) {
			cols[z.index()] = (int) ((colorA - colorA / 4) + z.getPartOfYear() * colorA) % colorA;
		}

		int off = (C.SCALE * 24 - C.TILE_SIZE) / 2;
		int rMask = colorA - 1;

		RenderIterator it = data.onScreenTiles(1, 1, 1, 0);
		s.setHeight(4);
		s.setDistance2Ground(0);
		s.setSoft();
		while (it.has()) {
			int t = this.data.get(it.tile());
			if (REGIONS().isCentre.is(it.tx(), it.ty())) {
				it.next();
				continue;
			}
			
			if (t != 0) {

				int x = it.x() - off;
				int y = it.y() - off;


				t -= 1;
				
				if (WORLD.ROADS().is(it.tile()) && !WORLD.ROADS().minified.is(it.tile())) {
					t = CLAMP.i(t / (2 + (it.ran() & 0b11)), 0, 8);
					DIR d = DIR.NORTHO.get((it.ran()>>8)&0b11);
					x += d.x()*C.TILE_SIZEH;
					y += d.y()*C.TILE_SIZEH;
				}

				t *= SET;
				t += it.ran() & 0x0F;

				sprites.colors[cols[CLIMATE().getter.get(it.tile()).index()]][it.ran() & rMask].bind();

				sprites.sheet.render(r, t, x, y);

			}
			it.next();
		}
		
		CORE.renderer().newLayer(true, CORE.renderer().getZoomout());
		
		it = data.onScreenTiles(2, 2, 2, 0);
		while (it.has()) {
			int t = this.data.get(it.tile());
			if (REGIONS().isCentre.is(it.tx(), it.ty())) {
				it.next();
				continue;
			}
			
			if (t != 0) {

				int x = it.x() - off;
				int y = it.y() - off;


				t -= 1;
				
				if (WORLD.ROADS().is(it.tile()) && !WORLD.ROADS().minified.is(it.tile())) {
					t = CLAMP.i(t / (2 + (it.ran() & 0b11)), 0, 8);
					DIR d = DIR.NORTHO.get((it.ran()>>8)&0b11);
					x += d.x()*C.TILE_SIZEH;
					y += d.y()*C.TILE_SIZEH;
				}else if (t >= max)
					it.hiddenSet();

				t *= SET;
				t += it.ran() & 0x0F;

				sprites.colors[cols[CLIMATE().getter.get(it.tile()).index()]][it.ran() & rMask].bind();

				sprites.bg.render(r, t, x, y);
				sprites.bg.render(s, t, x, y);

			}
			it.next();
		}
		
		COLOR.unbind();

	}

	double add(WorldTerrainInfo info, int tx, int ty) {
		info.add(TERRAINS.FOREST(), amount.get(tx, ty));
		return amount.get(tx, ty);
	}

	private static class Sprites {

		private final static int colorA = 64;
		private final COLOR[][] colors = new COLOR[colorA][colorA];

		public final TILE_SHEET bg = (new ITileSheet(PATHS.SPRITE_WORLD_MAP().get("Forest"), 972, 280) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				s.singles.init(0, 0, 1, 1, 16, max*2, t);
				for (int i = 0; i < max; i++)
					s.singles.setSkip(i * 16, 16).paste(true);
				return t.saveGame();

			}
		}).get();
		
		public final TILE_SHEET sheet = (new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				for (int i = 0; i < max; i++)
					s.singles.setSkip((i+max) * 16, 16).paste(true);
				return t.saveGame();

			}
		}).get();

		Sprites() throws IOException {

			final ColorImp wa = new ColorImp();
			final ColorImp wb = new ColorImp();

			final int seasons = 4;
			final double stepsPerSeason = colorA / seasons;

			final int randoms = 4;
			final double stepsPerRandom = colorA / randoms;
			LIST<COLOR> cols = new IColorSampler() {

				@Override
				protected COLOR next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
					return s.singles.setSkip(i, 1).sample();
				}

				@Override
				protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, s.singles.body().y2(), 1, 1, 4, 4, d.s16);
					return 16;
				}
			}.getHalf();
			for (double y = 0; y < colorA; y++) {

				int seasonFrom = (int) Math.floor(y / stepsPerSeason);
				int seasonTo = (int) Math.ceil(y / stepsPerSeason);
				seasonTo %= seasons;
				double seasonDelta = (y - seasonFrom * stepsPerSeason) / stepsPerSeason;
				for (double r = 0; r < colorA; r++) {
					int ranFrom = (int) Math.floor(r / stepsPerRandom);
					int ranTo = (int) Math.ceil(r / stepsPerRandom);
					ranTo %= randoms;
					double ranDelta = (r - ranFrom * stepsPerRandom) / stepsPerRandom;

					wa.interpolate(cols.get(seasonFrom * randoms + ranFrom), cols.get(seasonFrom * randoms + ranTo),
							ranDelta);
					wb.interpolate(cols.get(seasonTo * randoms + ranFrom), cols.get(seasonTo * randoms + ranTo),
							ranDelta);
					ColorImp res = new ColorImp();
					res.interpolate(wa, wb, seasonDelta);
					colors[(int) y][(int) r] = res;
				}
			}

		}
	}
	
	@Override
	public LIST<PLACABLE> placers(ToolManager tm) {
		ArrayListGrower<PLACABLE> placers = new ArrayListGrower<>();
		
		PLACABLE CLEAR = new PlacableMulti("clear forest", "", icon.twin(UI.icons().m.anti, DIR.C, 0)) {

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				amount.set(tx, ty, 0);
			}

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		};

		placers.add(new PlacableMulti("forest") {

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return WorldForest.this.placable.is(tx, ty) ? null : "";
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				int i = tx + ty * TWIDTH();
				data.set(i, CLAMP.i(data.get(i) + 1, 0, max));
			}
			
			@Override
			public PLACABLE getUndo() {
				return CLEAR;
			}
			
			@Override
			public SPRITE getIcon() {
				return icon;
			}
		});
		
		placers.add(CLEAR);
		
		return placers;
	}

}
