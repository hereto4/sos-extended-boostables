package settlement.tilemap.terrain;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.MINERALS;

import java.io.IOException;

import game.GAME;
import game.audio.AUDIO;
import game.audio.SoundRace;
import init.constant.C;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.type.TERRAIN;
import init.type.TERRAINS;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.thing.pointlight.LOS;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GBox;
import util.info.INFO;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public class TMountain extends Terrain.TerrainTile{

	private final static int SET = 16;
	private final int SINGLES;
	final Ceiling CAVE;
	private final TerrainClearing clear;
	private final TILE_SHEET sheet;
	
	static TMountain make(Terrain t) throws IOException {
		TILE_SHEET sheet = new ITileSheet(PATHS.SPRITE_SETTLEMENT().getFolder("map").get("Mountain"), 576, 256) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s16;
				s.house2.init(0, 0, 3, 1, t);
				s.house2.setVar(0).paste(1, true);
				s.house2.setVar(1).paste(2, true);
				s.house2.setVar(2).paste(2, true);

				s.full.init(0, s.house2.body().y2(), 1, 1, 16, 1, t);
				s.full.setSkip(16, 0).paste(true);
				;

				return t.saveGame();
			}
		}.get();
		
		TILE_SHEET mountain_ceiling = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s16;
				
				s.house.init(0, s.full.body().y2(), 4, 1, t);
				for (int i = 0; i < 3; i++) {
					s.house.setVar(i);
					s.house.setSkip(0, 16).paste(1, true);
				}
				s.house.setVar(0);
				s.house.setSkip(0, 1).pasteEdges(true);
				
				//ceiling shadow
				s.house.setVar(3).setSkip(0, 16).paste(true);
				s.house.setVar(3).setSkip(0, 1).pasteEdges(true);
				
				s.house2.init(0, s.house.body().y2(), 3, 1, t);
				
				s.house2.paste(true);
				s.house2.setVar(1).paste(true);
				s.house2.setVar(2).paste(1, true);
				s.full.init(0, s.house2.body().y2(), 1, 1, 16, 1, t);
				s.full.paste(true);

				return t.saveGame();
			}


		}).get();
		
		SPRITE icon = new SPRITE.Imp(Icon.L) {
			private COLOR bg = new ColorImp(102, 87, 65);
			private COLOR bg2 = bg.shade(0.6);
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				bg2.render(r, X1, X2, Y1, Y2);
				bg.render(r, X1+2, X2-2, Y1+2, Y2-2);
				rr(r, X1, X2, Y1, Y2);
			}
			
			private void rr(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int c = C.T_PIXELS*(X2-X1)/Icon.L;
				
				sheet.render(r, DIR.SE.mask(), X1, X1+c, Y1, Y1+c);
				sheet.render(r, DIR.SW.mask(), X1+c, X1+c*2, Y1, Y1+c);
				sheet.render(r, DIR.NE.mask(), X1, X1+c, Y1+c, Y1+c*2);
				sheet.render(r, DIR.NW.mask(), X1+c, X1+c*2, Y1+c, Y1+c*2);
			}
		};
		
		SPRITE iconC = new SPRITE.Imp(Icon.L) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				COLOR.WHITE200.bind();
				rr(r, X1-1, X2-1, Y1-1, Y2-1);
				COLOR.unbind();
				rr(r, X1, X2, Y1, Y2);
			}
			
			private void rr(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int c = C.T_PIXELS*(X2-X1)/Icon.L;
				
				mountain_ceiling.render(r, DIR.SE.mask(), X1, X1+c, Y1, Y1+c);
				mountain_ceiling.render(r, DIR.SW.mask(), X1+c, X1+c*2, Y1, Y1+c);
				mountain_ceiling.render(r, DIR.NE.mask(), X1, X1+c, Y1+c, Y1+c*2);
				mountain_ceiling.render(r, DIR.NW.mask(), X1+c, X1+c*2, Y1+c, Y1+c*2);
			}
		};
		
		return new TMountain(t, sheet, mountain_ceiling, icon, iconC);
	}
	
	TMountain(Terrain t, TILE_SHEET sheet, TILE_SHEET sheetC, SPRITE icon, SPRITE iconC) {
		super("MOUNTAIN", t, new INFO(new Json(PATHS.TEXT().getFolder("settlement").getFolder("structure").get("_MOUNTAIN"))).name, icon, t.colors.minimap.mountain);
		this.sheet = sheet;
		SINGLES = SET*8;
		CAVE = new Ceiling(t, sheetC, iconC);
		clear = new TerrainClearing() {
			
			private final SoundRace sound = AUDIO.race("CLEAR_MOUNTAIN");
			
			@Override
			public RESOURCE clear1(int tx, int ty) {
				if (!is(tx, ty)) {
					GAME.Notify(tx + " " + ty);
					return null;
				}
				int a = resourceGet(shared.data.get(tx, ty));
				RESOURCE res = a%4 == 0 ? RESOURCES.STONE() : null;
				a --;
				if (a < 0) {
					shared.CAVE.placeFixed(tx, ty);
					return res;
				}
				a = resourceSet(shared.data.get(tx, ty), a);
				shared.data.set(tx, ty, a);
				
				return res;
			}
			
			@Override
			public boolean can() {
				return true;
			}

			@Override
			public int clearAll(int tx, int ty) {
				int a = 1 + resourceGet(shared.data.get(tx, ty))/4;
				shared.CAVE.placeFixed(tx, ty);
				return a;
			}
			
			@Override
			public SoundRace sound(int tx, int ty) {
				return sound;
			}

			@Override
			public boolean isStructure() {
				return true;
			}
			
			@Override
			public boolean canDestroy(int tx, int ty) {
				return true;
			}
			
			@Override
			public void destroy(int tx, int ty) {
				shared.CAVE.placeFixed(tx, ty);
			}
			
			@Override
			public double strength() {
				return 1000*C.TILE_SIZE;
			}
		};

	}
	
	@Override
	public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
		return mini.miniCPimp(c, x, y, northern, southern);
	}
	
	private final Minimap mini = new Minimap();
	
	private static class Minimap {
		
		
		private final int w = 64;
		private final int h = 32;
		private byte[][] values = new byte[h][w];
		
		Minimap() {
			for (int i = 0; i < h; i+= 4) {
				int sx = (i&4) != 0 ? 8 : 0;
				drawWave(sx, i, (byte) -50, 8);
				drawWave(sx, i+1, (byte) 40, 8);
				drawWave(sx, i+2, (byte) 30, 8);
				drawWave(sx, i+3, (byte) 20, 8);
				drawWave(sx, i+4, (byte) 10, 8);
				drawWave(sx, i+5, (byte) 5, 8);
				
			}
			
			
			//drawWave(15, (byte) 32, 8);
		}
		
		private void drawWave(int sx, int sy, byte v, int period) {
			
			for(int i = 0; i < w; i++) {
				int x = (i+sx)&(w-1);
				int y = (i&(period-1));
				if ((i & period) != 0)
					y = period-y;
				y += sy;
				y &= h-1;
				values[y][x] = v;
			}
		}
		
		COLOR miniCPimp(ColorImp c, int tx, int ty, boolean northern, boolean southern) {
			int x = tx & (w-1);
			int y = ty & (h-1);
			if (values[y][x] != 0) {
				double v = 1.0-(0.5*values[y][x]/128.0);
				c.shadeSelf(v);
			}
			
			if (northern != southern) {
				if (northern)
					c.shadeSelf(1.5);
				else
					c.shadeSelf(0.75);
			}else {
				for (DIR d : DIR.ORTHO)
					if (SETT.TERRAIN().CAVE.is(tx, ty, d)) {
						c.interpolate(c, SETT.TERRAIN().CAVE.miniC, 0.5);
						break;
					}
			}
				
			
			return c;
		}
		
	}
	
	@Override
	public TERRAIN terrain(int tx, int ty) {
		return TERRAINS.MOUNTAIN();
	}
	
	@Override
	public LOS los(int tx, int ty) {
		return LOS.SOLID;
	}
	

	
	@Override
	public boolean isMassiveWall() {
		return true;
	}
	
	@Override
	protected boolean place(int tx, int ty) {
		int am = resourceGet(shared.data.get(tx, ty));
		boolean was = is(tx, ty);

		placeRaw(tx, ty);
		int res = 0;
		for (int i = 0; i < DIR.NORTHO.size(); i++) {
			DIR d = DIR.NORTHO.get(i);
			if (joins(tx, ty, d) && joins(tx, ty, d.next(-1)) && joins(tx, ty, d.next(1)))
				res |= d.mask();
		}
		
		if (res != 0 && IN_BOUNDS(tx, ty, DIR.N)) {
			TerrainTile t = shared.get(tx, ty, DIR.N);
			if (t != this && t != CAVE.opening && t.wallIsWally() && !t.roofIs() && ((res & DIR.NW.mask()) != 0)) {
				res |= 0b0_0001_0000;
			}
		}
		if (res != 0 && IN_BOUNDS(tx, ty, DIR.W)) {
			TerrainTile t = shared.get(tx, ty, DIR.W);
			if (t != this && t != CAVE.opening && t.wallIsWally() && !t.roofIs() && ((res & DIR.NW.mask()) != 0)) {
				res |= 0b0_0010_0000;
			}
		}
		
		
		if (was) {
			shared.data.set(tx, ty, resourceSet(res, am));
		}
		else {
			shared.data.set(tx, ty, resourceSet(res, 15));
			
		}
		
		return false;
	}
	
	@Override
	public void placeRaw(int x, int y) {
		super.placeRaw(x, y);
		strengthSet(x, y, 15);
	}
	
	private int resourceGet(int data) {
		return (data >> 8) & 15;
	}
	
	public double strength(int tile) {
		return resourceGet(shared.data.get(tile))/15.0;
	}
	
	private int resourceSet(int data, int amount) {
		

		if (amount > 15 || amount < 0)
			GAME.Notify(""+amount);
		amount = CLAMP.i(amount, 0, 15);
		
		amount = amount << 8;
		data &= 0x0FF;
		data |= amount;
		return data;
	}
	
	public void strengthSet(int tx, int ty, double s) {
		if (is(tx, ty)) {
			int am = CLAMP.i((int) (s*15), 1, 15);
			int data = shared.data.get(tx, ty);
			data = resourceSet(data, am);
			shared.data.set(tx, ty, data);
		}
	}
	
	@Override
	public void hoverInfo(GBox box, int tx, int ty) {
		super.hoverInfo(box, tx, ty);
		int am = ((shared.data.get(tx, ty) >> 8) & 15);
		am = 1 + am/4;
		box.setResource(RESOURCES.STONE(), am);
	}
	
	private boolean joins(int x, int y, DIR d) {
		x += d.x();
		y += d.y();
		if (!IN_BOUNDS(x, y))
			return true;
		return  shared.get(x, y).wallJoiner();
	}
	
	@Override
	boolean wallJoiner() {
		return true;
	}
	
	@Override
	public boolean wallIsWally() {
		return true;
	}
	
	public boolean isMountain(int tx, int ty) {
		return is(tx, ty) || CAVE.is(tx, ty);
	}
	
	@Override
	public boolean coversCompletely(int tx, int ty) {
		return (shared.data.get(tx, ty) & 0x0F) == 0x0F;
	}

	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		i.countCave();
		int x = i.x();
		int y = i.y();
		int ran = i.ran();
		
		int code = data & 0x0F;
		
		TILE_SHEET sheet = this.sheet;
		
		if (code == 0x0F) {
			sheet.render(r, 15, x, y);
			return true;
		}
		
		if (code == 0) {
			s.setHeight(3).setDistance2Ground(0);
			int c = SINGLES + (ran&0x0F);
			sheet.render(r,c, x, y);
			sheet.render(s,c, x, y);
		}else {
			s.setHeight(10).setDistance2Ground(0);
			int c = code +(ran&7)*SET;
			sheet.render(r, c, x, y);
			sheet.render(s, c, x, y);
			renderEdges(r, s, i, data);
		}
		
		return false;
	}
	
	@Override
	public boolean wantsFloorUnderneath(int tx, int ty) {
		return (shared.data.get(tx, ty) & 0x0F) != 0x0F;
	}
	
	private void renderEdges(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		int cor = data;
		if ((data & 0b0_0001_0000) != 0) {
			if ((cor & DIR.NW.mask()) == 0)
				SETT.TERRAIN().wall_merge.render(r, 0, i.x(), i.y() - 4);
			else
				SETT.TERRAIN().wall_merge.render(r, 2, i.x(), i.y() - 4);
			if ((cor & DIR.NE.mask()) == 0)
				SETT.TERRAIN().wall_merge.render(r, 1, i.x(), i.y() - 4);
			else
				SETT.TERRAIN().wall_merge.render(r, 3, i.x(), i.y() - 4);
		}
		if ((data & 0b0_0010_0000) != 0) {
			if ((cor & DIR.NW.mask()) == 0)
				SETT.TERRAIN().wall_merge.render(r, 4+1, i.x()-4, i.y());
			else
				SETT.TERRAIN().wall_merge.render(r, 4+3, i.x()-4, i.y());
			if ((cor & DIR.SW.mask()) == 0)
				SETT.TERRAIN().wall_merge.render(r, 4+0, i.x()-4, i.y());
			else
				SETT.TERRAIN().wall_merge.render(r, 4+2, i.x()-4, i.y());
		}
	}

	@Override
	protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			
		return false;
	}

	@Override
	public AVAILABILITY getAvailability(int x, int y) {
		return AVAILABILITY.SOLID;
	}
	
	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}
	
	@Override
	public TerrainClearing clearing() {
		return clear;
	}
	
	@Override
	public int heightStart(int tx, int ty) {
		return 0;
	}
	
	@Override
	public int heightEnd(int tx, int ty) {
		return 20;
	}
	
	public static class Ceiling extends Terrain.TerrainTile{
		
		private final static int SET = 16;
		private final TILE_SHEET sheet;
		private final int SHEET_CORNER;
		private final int SHEET_FIX;
		final Opening opening;
		private final TerrainClearing clear;
		
		private Ceiling(Terrain t, TILE_SHEET s, SPRITE icon) {
			super("CAVE", t, "cave", icon, t.colors.minimap.mountain.shade(1.5));
			this.sheet = s;
			this.SHEET_CORNER = SET*6;
			this.SHEET_FIX = SHEET_CORNER + SET;
			opening = new Opening(t, this.sheet, icon, SHEET_FIX+SET*2);
			clear = new TerrainClearing() {
				
				
				@Override
				public RESOURCE clear1(int tx, int ty) {
					return null;
				}
				
				@Override
				public boolean can() {
					return false;
				}

				@Override
				public int clearAll(int tx, int ty) {
					return 0;
				}
				
				@Override
				public SoundRace sound(int tx, int ty) {
					return null;
				}

				@Override
				public boolean isStructure() {
					
					return true;
				}
				
				@Override
				public boolean needs() {
					return false;
				}
				
				@Override
				public boolean canDestroy(int tx, int ty) {
					return false;
				}
				
				@Override
				public double strength() {
					return 0;
				}
			};
		}
		
		@Override
		public TERRAIN terrain(int tx, int ty) {
			return TERRAINS.MOUNTAIN();
		}
		
		@Override
		public TerrainClearing clearing() {
			return clear;
		}
		
		@Override
		protected boolean place(int x, int y) {

			if (opening.isPlacable(x, y)){
				return opening.place(x, y);
			}

			super.placeRaw(x, y);
			int data = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				TerrainTile t = shared.get(x, y, d);
				if (t.wallIsWally()) {
					data |= d.mask();
				}
			}
			
			data = setCorners(x, y, data);
			
			shared.data.set(x, y, data);
			return false;
			
		}
		
		private int setCorners(int x, int y, int res) {
			int corner = 0;
			for (int i = 0; i < DIR.NORTHO.size(); i++) {
				DIR d = DIR.NORTHO.get(i);
				if (joins(x, y, d) && !joins(x, y, d.next(-1)) && !joins(x, y, d.next(1))) {
					corner |= d.mask();
				}
			}
			res |= (corner << 4);
			return res;
		}
		
		private boolean joins(int x, int y, DIR d) {
			TerrainTile t = shared.get(x, y, d);
			return t.wallIsWally();
		}
		
		
		private int getCorners(int data) {
			return (data >> 4) & 0x0F;
		}
		
		private int getData(int data, RenderData.RenderIterator i) {
			int res = data;
			Room r = SETT.ROOMS().map.get(i.tx(), i.ty());
			if (r != null)
				res |= 0x0100;
			if (r != null && r.constructor() != null && r.constructor().mustBeIndoors()) {
				res |= 0x0100;
				for (DIR d : DIR.ORTHO) {
					if (!r.isSame(i.tx(), i.ty(), i.tx()+d.x(), i.ty()+d.y()))
						res |= d.mask();
				}
			}else {
				for (DIR d : DIR.ORTHO) {
					r = SETT.ROOMS().map.get(i.tx(), i.ty(), d);
					if (r != null && r.constructor() != null && r.constructor().mustBeIndoors()) {
						res |= d.mask();
						res |= 0x0100;
					}
				}
			}
			
			return res;
		}


		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			
			int x = i.x();
			int y = i.y();
			int ran = i.ran();

			data = getData(data, i);
			
			int a = data & 0xF;
			
			if ((data & 0x0100) != 0) {
				if (a!=0) {
					int j = SHEET_FIX + a;
					sheet.render(r, j, x, y);
				}
				
				
				a = getCorners(data);
				if (a!= 0) {
					sheet.render(r, SHEET_FIX+SET+a, x, y);
				}
			}else {
				if (a!=0) {
					int j = a + (ran%5)*SET;
					sheet.render(r, j, x, y);
				}
				
				
				a = getCorners(data);
				if (a!= 0) {
					sheet.render(r, SHEET_CORNER+a, x, y);
				}
			}
			
			s.setDistance2Ground(0).setHeight(0);
			if (!SETT.OVERLAY().added) {
				
				s.setHard();
				SETT.TERRAIN().MOUNTAIN.sheet.render(s, 0x0F, x, y);
				s.setSoft();
			}else {
				s.setSoft();
				SETT.TERRAIN().MOUNTAIN.sheet.render(s, 0x0F, x, y);
			}
			i.countCave();
			return false;
		}
		

		@Override
		protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			return false;
		}

		public boolean canFix(int tx, int ty) {
			if (!super.is(tx, ty))
				return false;
			int d = shared.data.get(tx, ty);
			return (d & 0x0100) == 0 && d != 0;
		}
		
		public void fix(int tx, int ty) {
			if (!is(tx, ty))
				return;
			int d = shared.data.get(tx, ty);
			d |= 0x0100;
			shared.data.set(tx, ty, d);
		}
		
		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return null;
		}
		
		@Override
		public boolean isPlacable(int tx, int ty) {
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				if (!joins(tx, ty, d) && !is(tx, ty, d))
					return opening.isPlacable(tx, ty);
			}
			return true;
		}
		
		@Override
		boolean wallJoiner() {
			return true;
		}
		
		@Override
		public boolean wallIsWally() {
			return false;
		}
		
		@Override
		public boolean roofIs() {
			return true;
		}
		
		@Override
		public COLOR miniC(int x, int y) {
			if (MINERALS().getter.is(x, y))
				return SETT.GROUND().minimap.miniC(x, y);
			return super.miniC(x, y);
		}
		
		@Override
		public boolean is(int tx, int ty) {
			return super.is(tx, ty) || opening.is(tx, ty);
		}
		
		@Override
		public int heightStart(int tx, int ty) {
			return 0;
		}
		
		@Override
		public int heightEnd(int tx, int ty) {
			return 0;
		}
		
		@Override
		public LOS los(int tx, int ty) {
			return LOS.CEILING;
		}
		
		
		public class Opening extends Terrain.TerrainTile{
			
			private final TILE_SHEET sheet;
			private final int SHEET_START;
			private final int SINGLES;
			
			private Opening(Terrain t, TILE_SHEET sheet, SPRITE icon, int sheetStart) {
				super("CAVE_ENTRANCE", t, "cave entrance", icon, t.colors.minimap.mountain.shade(1.5));
				this.sheet = sheet;
				this.SHEET_START = sheetStart;
				this.SINGLES = this.SHEET_START + 4*SET;
			}
			
			private boolean joins(int x, int y, DIR d) {
				TerrainTile t = shared.get(x, y, d);
				return t.wallIsWally() || t.roofIs();
			}
			
			@Override
			protected boolean place(int tx, int ty) {
				if (!isPlacable(tx, ty))
					return shared.CAVE.place(tx, ty);
				placeRaw(tx, ty);

				
				placeRaw(tx, ty);
				int res = 0;
				for (int i = 0; i < DIR.NORTHO.size(); i++) {
					DIR d = DIR.NORTHO.get(i);
					if (joins(tx, ty, d) && joins(tx, ty, d.next(-1)) && joins(tx, ty, d.next(1)))
						res |= d.mask();
				}
				
				if (res != 0x0F && res != 0 && IN_BOUNDS(tx, ty, DIR.N)) {
					TerrainTile t = shared.get(tx, ty, DIR.N);
					if (t != this && t!= shared.MOUNTAIN && t.wallIsWally() && !t.roofIs()) {
						if ((res & DIR.NW.mask()) != 0)
							res |= 0b000100000;
						if ((res & DIR.NE.mask()) != 0)
							res |= 0b000010000;
					}
				}
				if (res != 0x0F && res != 0 && IN_BOUNDS(tx, ty, DIR.W)) {
					TerrainTile t = shared.get(tx, ty, DIR.W);
					if (t != this && t!= shared.MOUNTAIN && t.wallIsWally() && !t.roofIs()) {
						if ((res & DIR.SW.mask()) != 0)
							res |= 0b00010000000;
						if ((res & DIR.NW.mask()) != 0)
							res |= 0b00001000000;
					}
				}
				
				shared.data.set(tx, ty, res);
				return false;
			}

			@Override
			protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
				
				int x = i.x();
				int y = i.y();
				int ran = i.ran();
				
				TILE_SHEET sheets = SETT.TERRAIN().MOUNTAIN.sheet;
				
				
				int code = data & 0x0F;
				if (code == 0) {
					s.setHeight(3).setDistance2Ground(0);
					sheet.render(r, SINGLES+(ran&0x0F), x, y);
					sheets.render(s, shared.MOUNTAIN.SINGLES+(ran&0x0F), x, y);
				}else {
					s.setHeight(10).setDistance2Ground(0);
					sheet.render(r, SHEET_START + code+(ran&0b011)*SET, x, y);
					sheets.render(s, code+(ran&0b011)*SET, x, y);
					if ((data & 0b000110000) != 0)
						SETT.TERRAIN().wall_merge.render(r, ((data>>4)&0b011)-1, i.x(), i.y()-4);
					if ((data & 0b011000000) != 0)
						SETT.TERRAIN().wall_merge.render(r, ((data>>6)&3)+2, i.x()-4, i.y());
				}
				i.countCave();
				return false;
			}

			@Override
			protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
				return false;
			}

			@Override
			public AVAILABILITY getAvailability(int x, int y) {
				return null;
			}
			
			@Override
			public boolean isPlacable(int tx, int ty) {
				
				for (DIR d : DIR.ALL) {
					if (IN_BOUNDS(tx, ty, d) && !shared.get(tx, ty, d).wallIsWally() && !shared.get(tx, ty, d).roofIs())
						return true;
				}
				return false;
			}
			
			@Override
			boolean wallJoiner() {
				return true;
			}
			
			@Override
			public boolean wallIsWally() {
				return true;
			}
			
			@Override
			public boolean roofIs() {
				return true;
			}

			
			@Override
			public TerrainClearing clearing() {
				return clear;
			}
			
			@Override
			public TERRAIN terrain(int tx, int ty) {
				return TERRAINS.MOUNTAIN();
			}
			
			@Override
			public int heightStart(int tx, int ty) {
				return 3;
			}
			
			@Override
			public int heightEnd(int tx, int ty) {
				return 200;
			}
			
			@Override
			public LOS los(int tx, int ty) {
				return LOS.CEILING;
			}
			
		}
		
	}

	@Override
	public int miniDepth() {
		return 2;
	}

	public boolean isFilled(int tx, int ty) {
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			if (!is(tx, ty, DIR.ORTHO.get(di)))
				return false;
		}
		return true;
	}


}
