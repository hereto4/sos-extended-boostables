package settlement.tilemap.terrain;

import static settlement.main.SETT.IN_BOUNDS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import game.audio.AUDIO;
import game.audio.SoundRace;
import game.faction.Faction;
import game.faction.player.PlayerColors.PlayerColor;
import init.constant.C;
import init.paths.PATH;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.Icon;
import init.value.GVALUES;
import init.value.Lockable;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.sprite.RoomSprite;
import settlement.thing.pointlight.LOS;
import settlement.tilemap.TILE_FIXABLE;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import settlement.tilemap.terrain.TerrainDiagonal.Diagonalizer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.MAPPED;
import util.keymap.RMAP;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import util.text.D;
import util.text.Dic;

public class TFortification implements MAPPED{

	private final static String KEY = "FORTIFICATION";
	private final static int SET = 16;

	private final static int DIAGONAL = 4*SET;
	private final static int CORNERS = DIAGONAL+4*SET;
	private final static int BROKEN = CORNERS+SET;
	
	private final static int SINGLES = BROKEN + 4*SET;
	private final static int FULLS = SINGLES+SET;
	private final static int DIA = 0x01000;
	private final TerrainClearing clear;
	
	public final Normal tile;
	public final TFortificationTileBroken broken;
	
	public final PlayerColor tint;
	public final CharSequence desc;
	public double durability;
	public final TILE_SHEET sheet;
	public final RESOURCE resource;
	public final int resAmount;
	public int sHeight;
	private final int index;
	public final SoundRace sound;
	private final int height;
	private final String key;
	public final Lockable<Faction> reqs;
	
	
	static RMAP<TFortification> make(Terrain t) throws IOException{
		
		String f = KEY.toLowerCase(Locale.ROOT);
		PATH gData = PATHS.INIT_SETTLEMENT().getFolder(f);
		PATH gSprite = PATHS.SPRITE_SETTLEMENT().getFolder(f);
		PATH gText = PATHS.TEXT_SETTLEMENT().getFolder(f);
		
		String[] keys = gData.getFiles();
		final LISTE<TFortification> all = new ArrayList<>(keys.length);
		HashMap<String, TFortification> others = new HashMap<>();
		for (String key : keys) {
			Json data = new Json(gData.get(key));
			Json text = new Json(gText.get(key));
			String sp = data.value("SPRITE");
			if (others.containsKey(sp)) {
				new TFortification(key, t, all, data, text, others.get(sp).tile.getIcon(), others.get(sp).sheet);
			}else {
				
				
				TILE_SHEET sheet = new ITileSheet(gSprite.get(sp), 576, 144) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.house.init(0, 0, 4, 2, d.s16);
						s.house.setVar(0).paste(true);
						s.house.setVar(0).pasteRotated(2, true);
						s.house.setVar(1).paste(true);
						s.house.setVar(1).pasteRotated(2, true);
						s.house.setVar(2).paste(true);
						s.house.setVar(2).pasteRotated(2, true);
						s.house.setVar(3).paste(true);
						s.house.setVar(3).pasteRotated(2, true);
						
						s.house.setVar(0).setSkip(0, 1).pasteEdges(true);
						
						s.house.setVar(4).setSkip(0, 16).paste(true);
						s.house.setVar(4).pasteRotated(2, true);
						s.house.setVar(5).paste(true);
						s.house.setVar(5).pasteRotated(2, true);
						
						s.full.init(144, 72, 1, 1, 8, 1, d.s16);
						s.full.paste(true);
						s.full.pasteRotated(2, true);
						s.full.init(s.full.body().x1(), s.full.body().y2(), 1, 1, 8, 1, d.s16);
						s.full.paste(true);
						s.full.pasteRotated(2, true);
						
						return d.s16.saveGame();
					}
				}.get();
				
				SPRITE icon = new SPRITE.Imp(Icon.L) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						int XX = (X2-X1)/2;
						int YY = (Y2-Y1)/2;
						sheet.render(r, DIR.S.mask() | DIR.E.mask(), X1, X1+XX, Y1, Y1+YY);
						sheet.render(r, DIR.S.mask() | DIR.W.mask(), X1+XX, X1+XX*2, Y1, Y1+YY);
						sheet.render(r, DIR.N.mask() | DIR.E.mask(), X1, X1+XX, Y1+YY, Y1+YY*2);
						sheet.render(r, DIR.N.mask() | DIR.W.mask(), X1+XX, X1+XX*2, Y1+YY, Y1+YY*2);
						
					}
				};
				
				TFortification fort = new TFortification(key, t, all, data, text, icon, sheet);
				others.put(sp, fort);
			}
		}
		
		return new RMAP<TFortification>(KEY, all);
		
	}
	
	TFortification(String key, Terrain t, LISTE<TFortification> all, Json data, Json text, SPRITE icon, TILE_SHEET sheet) {
		
		this.sheet = sheet;
		this.key = key;
		durability = data.d("DURABILITY", 0, 1.0)*C.TILE_SIZE;
		desc = text.text("DESC");
		resource = RESOURCES.map().read(data);
		resAmount = data.i("RESOURCE_AMOUNT");
		sHeight = data.i("HEIGHT", 0, 20);
		height = (int) (Math.ceil(sHeight/20.0)*10);
		index = all.add(this);
		sound = AUDIO.race("BUILD_FORTIFICATION_" + key);
		tile =  new Normal(key, t, data, text, icon, sheet, this);
		broken = new TFortificationTileBroken(key, t, data, text, icon, sheet, this);
		tint = new PlayerColor(new ColorImp(data), "FORT_" + key, Dic.¤¤Fortifications, tile.name());
		reqs = GVALUES.FACTION.LOCK.push(KEY + "_" + key, tile.name(), desc, icon);
		clear = new TerrainClearing() {
			
			@Override
			public RESOURCE clear1(int tx, int ty) {
				SETT.TERRAIN().NADA.placeFixed(tx, ty);
				return resource;
			}
			
			@Override
			public boolean can() {
				return true;
			}

			@Override
			public int clearAll(int tx, int ty) {
				SETT.TERRAIN().NADA.placeFixed(tx, ty);
				return 1;
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
			public void destroy(int tx, int ty) {
				broken.placeFixed(tx, ty);
			}

			@Override
			public double strength() {
				return durability*2500;
			}
		};
	}



	public final RoomSprite rSprite = new RoomSprite() {
		
		@Override
		public int sData() {
			return 0;
		}
		
		@Override
		public boolean render(SPRITE_RENDERER r, ShadowBatch s, int data, RenderIterator it, double degrade,
				boolean isCandle) {
			tile.renderBelow(r, s, it, data);
			return false;
		}
		
		@Override
		public byte getData(int tx, int ty, int rx, int ry, FurnisherItem item, int itemRan) {
			int res = 0;
			for (DIR d : DIR.ORTHO) {
				if (item.is(rx, ry, d))
					res |= d.mask();
			}
			
			int cor = 0;
			for (DIR d : DIR.NORTHO) {
				if (!item.is(rx, ry, d) && item.is(rx, ry, d.next(1)) && item.is(rx, ry, d.next(-1)))
					cor |= d.mask();
			}
			
			res |= cor << 4;
			return (byte) res;
		}
	};
	
	@Override
	public int index() {
		return index;
	}



	public static abstract class Tile extends Terrain.TerrainTile implements Diagonalizer {
		
		public final TFortification fort;
		
		Tile(String key, Terrain shared, CharSequence name, SPRITE icon, COLOR miniC, TFortification tFortification) {
			super(key, shared, name, icon, miniC);
			this.fort = tFortification;
		}

		
		@Override
		public int heightStart(int tx, int ty) {
			return 0;
		}
		
		@Override
		protected boolean place(int tx, int ty) {
			
			boolean dia = is(tx, ty) && ((shared.data.get(tx, ty) & DIA) != 0);
			if (shared.get(tx, ty) == fort.tile || shared.get(tx, ty) == fort.broken) {
				dia |= getDia(tx, ty);
			}
			boolean full = is(tx, ty) && ((shared.data.get(tx, ty) & 0x0F) == 0x0F);
			
			placeRaw(tx, ty);
			int res = 0;
			for (DIR d : DIR.ORTHO) {
				if (j(tx, ty, d))
					res |= d.mask();
			}
			
			int cor = 0;
			for (DIR d : DIR.NORTHO) {
				if (!j(tx, ty, d) && j(tx, ty, d.next(1)) && j(tx, ty, d.next(-1)))
					cor |= d.mask();
			}
			
			res |= cor << 4;
			
//			for (int i = 0; i < DIR.NORTHO.size(); i++) {
//				DIR d = DIR.NORTHO.get(i);
//				if (joins2(tx, ty, d) && joins2(tx, ty, d.next(-1)) && joins2(tx, ty, d.next(1)))
//					res |= d.mask();
//			}
			
			if (res != 0 && IN_BOUNDS(tx, ty, DIR.N)) {
				TerrainTile t = shared.get(tx, ty, DIR.N);
				if (!(t instanceof Tile) && t.wallIsWally() && ((res & DIR.N.mask()) != 0)) {
					res |= 0b0_0001_0000_0000;
				}
			}
			if (res != 0 && IN_BOUNDS(tx, ty, DIR.W)) {
				TerrainTile t = shared.get(tx, ty, DIR.W);
				if (!(t instanceof Tile)  && t.wallIsWally() && ((res & DIR.W.mask()) != 0)) {
					res |= 0b0_0010_0000_0000;
				}
			}
			
			shared.data.set(tx, ty, res);
			setDia(tx, ty, dia);
			return full != ((shared.data.get(tx, ty) & 0x0F) == 0x0F);
		}
		

		boolean j(int tx, int ty, DIR d) {
			return fort.tile.is(tx, ty, d) || fort.broken.is(tx, ty, d) || SETT.TERRAIN().FSTAIRS.is(tx, ty, d);
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
		public boolean isMassiveWall() {
			return true;
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

			return false;
		}
		
		void renderEdges(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			int cor = (data >> 4) & 0x0F;
			if ((data & 0b0_0001_0000_0000) != 0) {
				if ((data & DIR.W.mask()) == 0 || (cor & DIR.NW.mask()) != 0)
					SETT.TERRAIN().wall_merge.render(r, 0, i.x(), i.y() - 4);
				else
					SETT.TERRAIN().wall_merge.render(r, 2, i.x(), i.y() - 4);
				if ((data & DIR.E.mask()) == 0 || (cor & DIR.NE.mask()) != 0)
					SETT.TERRAIN().wall_merge.render(r, 1, i.x(), i.y() - 4);
				else
					SETT.TERRAIN().wall_merge.render(r, 3, i.x(), i.y() - 4);
			}
			if ((data & 0b0_0010_0000_0000) != 0) {
				if ((data & DIR.N.mask()) == 0 || (cor & DIR.NW.mask()) != 0)
					SETT.TERRAIN().wall_merge.render(r, 4+1, i.x()-4, i.y());
				else
					SETT.TERRAIN().wall_merge.render(r, 4+3, i.x()-4, i.y());
				if ((data & DIR.S.mask()) == 0 || (cor & DIR.SW.mask()) != 0)
					SETT.TERRAIN().wall_merge.render(r, 4+0, i.x()-4, i.y());
				else
					SETT.TERRAIN().wall_merge.render(r, 4+2, i.x()-4, i.y());
			}
		}
		
		@Override
		public void setDia(int x, int y, boolean dia) {
			if (!is(x, y))
				return;
			int data = shared.data.get(x, y);
			if (dia)
				data |= DIA;
			else
				data &= ~DIA;
			shared.data.set(x,  y, data);
		}
		
		@Override
		public boolean getDia(int tx, int ty) {
			if (!is(tx, ty))
				return false;
			return (shared.data.get(tx, ty) & DIA) != 0;
		}
		
		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
		}

		@Override
		public int miniDepth() {
			return 2;
		}
		
		@Override
		public boolean wantsFloorUnderneath(int tx, int ty) {
			return (shared.data.get(tx, ty) & 0x0F) != 0x0F;
		}
	}
	
	public static final class Normal extends Tile {

		Normal(String key, Terrain t, Json data, Json text, SPRITE icon, TILE_SHEET sheet, TFortification tFortification) {
			super("FORTIFICATION_" + key, t, text.text("NAME"), icon, new ColorImp(data, "MINIMAP_COLOR"), tFortification);
		}
		
		@Override
		public LOS los(int tx, int ty) {
			return getAvailability(tx, ty).player < 0 ? LOS.SOLID : LOS.OPEN;
		}
		
		@Override
		public int heightEnd(int tx, int ty) {
			if ((shared.data.get(tx, ty) & 0x0F) == 0xF)
				return fort.height;
			return fort.height/2;
		}
		
		@Override
		public int heightEnt(int tx, int ty) {
			return fort.height;
		}

		@Override
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			int x = i.x();
			int y = i.y();
			int ran = i.ran();
			
			int code = data & 0x0F;
			int c = code;
			
			
			if (code == 0) {
				c = SINGLES;
				c += ran & 0x0F;
			}else if (code == 0x0F) {
				c = FULLS;
				c += ran & 0x0F;
			}else {
				c += (ran & 0b11)*SET;
			}
			
			if ((data & DIA) != 0) {
				if (code != 0 && code != 0x0F)
					c += DIAGONAL;
			}
			
			renderEdges(r, s, i, data);
			fort.tint.color.bind();
			fort.sheet.render(r, c, x, y);
			s.setHeight(fort.sHeight).setDistance2Ground(0);
			fort.sheet.render(s, c, x, y);
			
			int cor = (data >> 4) & 0x0F;
			if (cor != 0) {
				fort.sheet.render(r, CORNERS+cor, x, y);
			}
			COLOR.unbind();
			
			return false;
		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			int data = shared.data.get(x, y);
			if ((data & 0x0F) == 0x0F) {
				return AVAILABILITY.NORMAL;
			}
			return AVAILABILITY.SOLID;
		}
		
		@Override
		public TerrainClearing clearing() {
			return fort.clear;
		}

	}
	
	public static final class TFortificationTileBroken extends Tile implements TILE_FIXABLE {
		
		final TerrainClearing clearing = new TerrainClearing() {


			@Override
			public RESOURCE clear1(int tx, int ty) {
				shared.NADA.placeFixed(tx, ty);
				return null;
			}

			@Override
			public boolean can() {
				return true;
			}

			@Override
			public int clearAll(int tx, int ty) {
				shared.NADA.placeFixed(tx, ty);
				return 1;
			}

			@Override
			public SoundRace sound(int tx, int ty) {
				return fort.sound;
			}

			@Override
			public boolean isStructure() {
				return false;
			}
			
			@Override
			public double strength() {
				return 0;
			}
			
			@Override
			public boolean canDestroy(int tx, int ty) {
				return false;
			}

		};
		
		TFortificationTileBroken(String key, Terrain t, Json data, Json text, SPRITE icon, TILE_SHEET sheet, TFortification tFortification) {
			super("FORTIFICATION_B_" + key, t, text.text("NAME") + " (" + Dic.¤¤Broken + ")", icon, new ColorImp(data, "MINIMAP_COLOR"), tFortification);
		}
		
		@Override
		public LOS los(int tx, int ty) {
			return LOS.OPEN;
		}
		
		@Override
		public int heightEnd(int tx, int ty) {
			return fort.height/2;
		}
		
		@Override
		public int heightEnt(int tx, int ty) {
			return fort.height/2;
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
		public boolean isMassiveWall() {
			return true;
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			return false;
		}

		@Override
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
			int x = i.x();
			int y = i.y();
			int ran = i.ran();
			
			int code = data & 0x0F;
			int c = code;
			
			
			if (code == 0) {
				c = SINGLES;
				c += ran & 0x0F;
			}else if (code == 0x0F) {
				c = FULLS;
				c += ran & 0x0F;
			}else {
				c += BROKEN + (ran & 0b11)*SET;
			}
			
			renderEdges(r, s, i, data);
			fort.tint.color.bind();
			fort.sheet.render(r, c, x, y);
			s.setHeight(fort.sHeight/2).setDistance2Ground(0);
			fort.sheet.render(s, c, x, y);
			
			int cor = (data >> 4) & 0x0F;
			if (cor != 0) {
				fort.sheet.render(r, CORNERS+cor, x, y);
			}
			COLOR.unbind();
			
			if (code == 0 || code == 0x0F) {
				
				OPACITY.O99.bind();
				fort.sheet.renderTextured(SETT.ROOMS().util.filth.texture(0.75, i.ran()), c, x, y);
				OPACITY.unbind();
				
			}
			
			
			return false;
		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return AVAILABILITY.NORMAL;
		}
		
		@Override
		public TerrainClearing clearing() {
			return clearing;
		}

		@Override
		public Job fixJob(int tx, int ty) {
			return SETT.JOBS().build_fort.all.get(fort.index());
		}

		@Override
		public TerrainTile getTerrain(int tx, int ty) {
			return fort.tile;
		}
	}
	
	private static CharSequence ¤¤stairs = "Stairs";
	static {
		D.ts(TFortification.class);
	}
	
	public static final class Stairs extends TerrainTile {

		private final TILE_SHEET stairC;
		private final TILE_SHEET stairEdge;
		
		static Stairs make(Terrain t) throws IOException {
			
			PATH gSprite = PATHS.SPRITE_SETTLEMENT().getFolder("fortification");
			

			TILE_SHEET stairC = new ITileSheet(gSprite.get("_Stairs"), 216, 28) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, 6, 1, d.s16);
					s.full.setSkip(4, 1);
					s.full.paste(3, true);
					return d.s16.saveGame();
				}
			}.get();
			
			TILE_SHEET stairEdge = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {

					//North
					
					s.full.setSkip(1, 5).pasteRotated(0, true);
					s.full.setSkip(1, 0).pasteRotated(0, true);
					
					//East
					s.full.setSkip(1, 5).pasteRotated(1, true);
					s.full.setSkip(1, 0).pasteRotated(1, true);
					
					
					//South
					s.full.setSkip(1, 5).pasteRotated(2, true);
					s.full.setSkip(1, 0).pasteRotated(2, true);
					
					//West 
					s.full.setSkip(1, 5).pasteRotated(3, true);
					s.full.setSkip(1, 0).pasteRotated(3, true);
					
					
					return d.s16.saveGame();
				}
			}.get();
			
			SPRITE icon = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					stairC.render(r, 2*4, X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
					stairC.render(r, 2*4, X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1, Y1+C.T_PIXELS);
					stairC.render(r, 0, X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					stairC.render(r, 0, X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					stairEdge.render(r, 5, X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
					stairEdge.render(r, 4, X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1, Y1+C.T_PIXELS);
					stairEdge.render(r, 0, X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					stairEdge.render(r, 1, X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					
				}
			};
			
			return new Stairs(t, stairC, stairEdge, icon);
			
		}
		
		private Stairs(Terrain t, TILE_SHEET stairC, TILE_SHEET stairEdge, SPRITE icon) throws IOException {
			super("STAIRS", t, ¤¤stairs,  icon, null);
			
			this.stairC = stairC;
			
			this.stairEdge = stairEdge;
			
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
		}
		

		@Override
		protected boolean place(int tx, int ty) {
			super.placeRaw(tx, ty);
			
			DIR face = DIR.N;
			
			int r = 0;
			for (DIR d : DIR.ORTHO) {
				if (shared.get(tx, ty, d) instanceof Tile && !(shared.get(tx, ty, d.perpendicular()) instanceof Tile) && !is(tx, ty, d.perpendicular())) {
					face = d;
					break;
				}
				r++;
			}
			
			if (!is(tx, ty, face.next(-2))) {
				r |= 0b0100;
			}
			
			if (!is(tx, ty, face.next(2))) {
				r |= 0b1000;
			}
			
			shared.data.set(tx, ty, r);
			return false;
			
		}

		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator i, int data) {
			int o = data&0b011;
			if ((data & 0b0100) != 0) {
				stairEdge.render(r, o*2, i.x(), i.y());
			}
			if((data & 0b1000) != 0) {
				stairEdge.render(r, o*2+1, i.x(), i.y());
			}
			return false;
		}

		@Override
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator i, int data) {
			int o = (data&0b011)*4;
			stairC.render(r, o + (i.ran()&3), i.x(), i.y());
			return false;
		}

		@Override
		public AVAILABILITY getAvailability(int tx, int ty) {
			return AVAILABILITY.NORMAL;
		}
		
		@Override
		public int miniDepth() {
			return 1;
		}
		
		TerrainClearing clear = new TerrainClearing() {
			
			private final SoundRace sound = AUDIO.race("CLEAR_STAIRS");
			
			@Override
			public RESOURCE clear1(int tx, int ty) {
				shared.NADA.placeFixed(tx, ty);
				return null;
			}
			
			@Override
			public boolean can() {
				return true;
			}

			@Override
			public int clearAll(int tx, int ty) {
				shared.NADA.placeFixed(tx, ty);
				return 1;
			}
			
			@Override
			public boolean isStructure() {
				return true;
			}
			@Override
			public void destroy(int tx, int ty) {
				;
			}

			@Override
			public double strength() {
				return 0;
			}

			@Override
			public SoundRace sound(int tx, int ty) {
				return sound;
			}
			
			@Override
			public boolean canDestroy(int tx, int ty) {
				return false;
			};
		};
		
		@Override
		public TerrainClearing clearing() {
			return clear;
		}

		
	}
	
	@Override
	public String key() {
		return key;
	}

}
