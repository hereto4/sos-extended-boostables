package settlement.tilemap.terrain;

import static settlement.main.SETT.GRASS;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TERRAIN;

import java.io.IOException;
import java.util.HashMap;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.constant.C;
import init.paths.PATH;
import init.paths.PATHS.ResFolder;
import init.resources.RESOURCE;
import init.sprite.UI.Icon;
import init.structure.STRUCTURES;
import init.structure.Structure;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.thing.pointlight.LOS;
import settlement.tilemap.TILE_FIXABLE;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import settlement.tilemap.terrain.TerrainDiagonal.Diagonalizer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class TBuilding  {

	public final Structure structure;
	

	private final SPRITE iconWall;
	private final SPRITE iconCeiling;
	public final SPRITE iconCombo;
	private final TILE_SHEET spriteWall;
	private final TILE_SHEET spriteOpening;
	private final TILE_SHEET spriteCeiling;
	public final Wall wall;
	final Wall broken;
	public final Ceiling roof;
	public final SoundRace sound;
	
	public static final class TBuildings {

		public final TBuilding MUD;
		private final ArrayList<TBuilding> all;
		
		TBuildings(Terrain terrain) throws IOException {
			
			ResFolder f = STRUCTURES.path();
			HashMap<String, TBuilding> others = new HashMap<>();
			
			TBuilding[] all = new TBuilding[STRUCTURES.all().size()];
			
			
			for (Structure s : STRUCTURES.all()) {
				Json d = new Json(f.init.get(s.key));
				PATH sprite = f.sprite;
				TBuilding b = new TBuilding(s, terrain, d, sprite, others);
				all[s.index()] = b;
			}
			
			MUD = all[STRUCTURES.mud().index()];
			this.all = new ArrayList<TBuilding>(all);
		}

		public TBuilding get(Structure s) {
			return all.get(s.index());
		}
		
		public LIST<TBuilding> all() {
			return all;
		}
		
	}

	TBuilding(Structure structure,  Terrain t, Json data, PATH sg,
			HashMap<String, TBuilding> otherSprites) throws IOException {
		this.structure = structure;
		String s = data.value("SPRITE");
		sound = AUDIO.race("BUILD_STRUCTURE_" + structure.key);
		
		if (otherSprites.containsKey(s)) {
			TBuilding o = otherSprites.get(s);
			iconWall = o.iconWall;
			iconCeiling = o.iconCeiling;
			iconCombo = o.iconCombo;
			spriteOpening = o.spriteOpening;
			spriteCeiling = o.spriteCeiling;
			spriteWall = o.spriteWall;
		} else {

			spriteWall = new ITileSheet(sg.get(s), 576, 372) {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					ComposerDests.Tile t = d.s16;
					s.house.init(0, 0, 4, 2, t);
					
					s.house.setVar(0).paste(1, true);
					s.house.setVar(1).pasteRotated(2, true);
					s.house.setVar(1).pasteRotated(3, true);
					
					//dia
					s.house.setVar(2).paste(1, true);
					s.house.setVar(3).pasteRotated(2, true);
					s.house.setVar(3).pasteRotated(3, true);
					
					
					
					//broken
					s.house.setVar(4).paste(1, true);
					s.house.setVar(5).pasteRotated(2, true);
					s.house.setVar(5).pasteRotated(3, true);
					
					s.house.setVar(0).pasteEdges(true);
					s.house.setVar(1).pasteEdges(true);
					s.house.setVar(2).pasteEdges(true);
					s.house.setVar(3).pasteEdges(true);

					s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, t);
					s.full.paste(true);

					s.full.init(0, s.full.body().y2(), 1, 1, 16, 1, t);
					s.full.paste(true);

					return t.saveGame();
				}
			}.get();

			spriteOpening = (new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {

					ComposerDests.Tile t = d.s16;
					s.house.init(0, s.full.body().y2(), 4, 1, t);
					s.house.setVar(0).paste(true);
					s.house.setVar(1).paste(true);
					s.house.setVar(2).paste(true);
					s.house.setVar(3).paste(true);
					
					s.house.setVar(0).pasteEdges(true);
					s.house.setVar(1).pasteEdges(true);
					s.house.setVar(2).pasteEdges(true);
					s.house.setVar(3).pasteEdges(true);
					
					s.full.init(0, s.house.body().y2(), 1, 1, 8, 1, t);
					s.full.setSkip(8, 0).paste(2, true);

					return t.saveGame();
				}

			}).get();

			spriteCeiling = (new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					ComposerDests.Tile t = d.s16;
					s.house.init(0, s.full.body().y2(), 2, 1, t);

					for (int i = 0; i < 2; i++) {
						s.house.setVar(i);
						s.house.setSkip(0, 16).paste(1, true);
					}
					s.house.setVar(0);
					s.house.setSkip(0, 1).pasteEdges(true);
					s.house.setVar(1).setSkip(0, 1).paste(true);
					return t.saveGame();
				}
			}).get();
			otherSprites.put(s, this);
			
			iconCombo = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					spriteOpening.render(r, DIR.S.mask() | DIR.E.mask(), X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
					spriteOpening.render(r, DIR.S.mask() | DIR.W.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1, Y1+C.T_PIXELS);
					spriteOpening.render(r, DIR.N.mask() | DIR.E.mask(), X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					spriteOpening.render(r, DIR.N.mask() | DIR.W.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					
				}
			};
			
			iconWall = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					spriteWall.render(r, DIR.S.mask() | DIR.E.mask(), X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
					spriteWall.render(r, DIR.S.mask() | DIR.W.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1, Y1+C.T_PIXELS);
					spriteWall.render(r, DIR.N.mask() | DIR.E.mask(), X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					spriteWall.render(r, DIR.N.mask() | DIR.W.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					
				}
			};
			
			iconCeiling = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					spriteCeiling.render(r, DIR.N.mask() | DIR.W.mask(), X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
					spriteCeiling.render(r, DIR.N.mask() | DIR.E.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1, Y1+C.T_PIXELS);
					spriteCeiling.render(r, DIR.S.mask() | DIR.W.mask(), X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					spriteCeiling.render(r, DIR.S.mask() | DIR.E.mask(), X1+C.T_PIXELS, X1+C.T_PIXELS*2, Y1+C.T_PIXELS, Y1+C.T_PIXELS*2);
					
				}
			};
		}

		wall = new WallFull("BUILDING_" + structure.key, t);
		broken = new WallBroken("BUILDING_BROKEN" + structure.key, t);
		roof = new Ceiling("BUILDING_CEILING" + structure.key, t);

		PLACABLE room = new PlacableMulti(structure.name) {

			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				if (tx > a.body().x1() && tx < a.body().x2() - 1 && ty > a.body().y1() && ty < a.body().y2() - 1) {
					if (roof.isPlacable(tx, ty)) {
						roof.placeFixed(tx, ty);
						GRASS().current.set(tx, ty, 0);
					}
				} else if (Math.abs(tx - a.body().cX()) < 2) {
					if (roof.isPlacable(tx, ty)) {
						roof.placeFixed(tx, ty);
						GRASS().current.set(tx, ty, 0);
					}
				} else if (Math.abs(ty - a.body().cY()) < 2) {
					if (roof.isPlacable(tx, ty)) {
						roof.placeFixed(tx, ty);
						GRASS().current.set(tx, ty, 0);
					}
				} else if (wall.isPlacable(tx, ty)) {
					wall.placeFixed(tx, ty);
					GRASS().current.set(tx, ty, 0);
				}
			}

			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return !roof.is(tx, ty) && wall.isPlacable(tx, ty) ? null : "";
			}

		};

		IDebugPanelSett.add("room", room);
	}

	public final MAP_BOOLEAN isser = new MAP_BOOLEAN() {

		@Override
		public boolean is(int tx, int ty) {
			return wall.is(tx, ty) || roof.is(tx, ty);
		}

		@Override
		public boolean is(int tile) {
			return wall.is(tile) || roof.is(tile);
		}
	};

	public abstract class BuildingComponent extends Terrain.TerrainTile {

		private final RESOURCE needed;

		protected BuildingComponent(String key, Terrain t, CharSequence name, SPRITE icon, COLOR c, RESOURCE needed) {
			super(key, t, name, icon, c);
			this.needed = needed;

		}

		TerrainClearing clearing = new TerrainClearing() {

			@Override
			public RESOURCE clear1(int tx, int ty) {
				shared.NADA.placeFixed(tx, ty);
				return needed;
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
				return sound;
			}

			@Override
			public boolean isStructure() {
				return true;
			}
			
			@Override
			public boolean canDestroy(int tx, int ty) {
				return false;
			}
			
			@Override
			public void destroy(int tx, int ty) {
				
			}

			@Override
			public boolean isEasilyCleared() {
				return false;
			}
			

		};

	// protected final int getDegrade(int data) {
	// data = data >> 14;
	// return data & 0b0000000000000011;
	// }
	//
	// private final int setDegrade(int data, int down) {
	// down = down << 14;
	// data &= 0b0011111111111111;
	// data |= down;
	// return data;
	// }
	//
	// public void degrade(int tx, int ty, int amount) {
	// int data = shared.data.get(tx, ty);
	// int down = getDegrade(data);
	// down += amount;
	// if (down <= downgrades && down >= 0)
	// data = setDegrade(data, down);
	// shared.data.set(tx, ty, data);
	// }
	//
	// public boolean canDegrade(int tx, int ty, int amount) {
	// int data = shared.data.get(tx, ty);
	// int down = getDegrade(data);
	// down += amount;
	// return down <= downgrades && down >= 0;
	// }

		@Override
		public TerrainClearing clearing() {
			return clearing;
		}



		public final TBuilding building() {
			return TBuilding.this;
		}

	}


	
	public class Wall extends BuildingComponent implements Diagonalizer{

		private final static int SET = 16;
		private final TILE_SHEET sheet;
		private final int DIAGONAL = 4*SET;
		private final int BROKEN = DIAGONAL + 4*SET;
		private final int CORNERS = BROKEN + 4*SET;
		private final int CORNERS_FAT = CORNERS + SET;
		private final int CORNERS_DIA = CORNERS_FAT + SET;
		private final int CORNERS_FAT_DIA = CORNERS_DIA + SET;
		private final int SINGLES = CORNERS_FAT_DIA+SET;
		private final int FULLS = SINGLES + SET;
		private final boolean broken;
		private int DIA = 0x02000;
		private int FAT = 0x04000;

		private Wall(String key, Terrain t, boolean broken) {
			super(key, t, structure.nameWall, iconWall, structure.miniColor.shade(0.9), structure.resource);
			this.broken = broken;
			this.sheet = spriteWall;
		}

		@Override
		protected boolean place(int x, int y) {

			boolean dia = shared.get(x, y) instanceof Diagonalizer && ((Diagonalizer) shared.get(x, y)).getDia(x, y);
			placeRaw(x, y);

			int res = 0;
			if (isFat(x, y)) {
				res |= FAT;
				for (DIR d : DIR.ORTHO) {
					if (joins(x, y, d) && joins(x, y, d.next(1)) && joins(x, y, d.next(2)))
						res |= d.mask() | d.next(2).mask();
				}
			}else {
				for (DIR d : DIR.ORTHO) {
					if (!isFat(x+d.x(), y+d.y()) && joins(x, y, d))
						res |= d.mask();
				}
			}
			
			
			
			int cor = 0;
			for (DIR d : DIR.NORTHO) {
				if (!joins(x, y, d) && (res & d.next(1).mask()) != 0 && (res & d.next(-1).mask()) != 0)
					cor |= d.mask();
			}
			res |= cor << 4;

			if (res != 0 && IN_BOUNDS(x, y, DIR.N)) {
				TerrainTile t = shared.get(x, y, DIR.N);
				if (t != this && t != roof.opening && t.wallIsWally() && t != TBuilding.this.broken && !t.roofIs() && ((res & DIR.N.mask()) != 0)) {
					res |= 0b0_0001_0000_0000;
				}
			}
			if (res != 0 && IN_BOUNDS(x, y, DIR.W)) {
				TerrainTile t = shared.get(x, y, DIR.W);
				if (t != this && t != roof.opening && t != TBuilding.this.broken && t.wallIsWally() && !t.roofIs() && ((res & DIR.W.mask()) != 0)) {
					res |= 0b0_0010_0000_0000;
				}
			}
//			if (res != 0 && IN_BOUNDS(x, y, DIR.W)) {
//				TerrainTile t = shared.get(x, y, DIR.W);
//				if (t != this && t != roof.opening && t.wallIsWally() && !t.roofIs()) {
//					if ((res & DIR.SW.mask()) != 0)
//						res |= 0b0_1000_0000_0000;
//					if ((res & DIR.NW.mask()) != 0)
//						res |= 0b0_0100_0000_0000;
//				}
//			}


			shared.data.set(x, y, res);
			setDia(x, y, dia);
			return false;
		}

		private boolean joins(int x, int y, DIR d) {
			x += d.x();
			y += d.y();
			if (!IN_BOUNDS(x, y))
				return false;
			return jwall.is(x, y);
		}
		
		private boolean isFat(int x, int y) {
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (joins(x, y, d) && joins(x, y, d.next(1)) && joins(x, y, d.next(2)))
					return true;
			}
			return false;
		}

		@Override
		public boolean isMassiveWall() {
			return true;
		}
		
		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

			int code = data & 0x0F;
			int cor = (data >> 4) & 0x0F;
			if (cor != 0) {
				if ((data & FAT) == 0) {
					cor += CORNERS;
				}else
					cor += CORNERS_FAT;
				if ((data & DIA) != 0)
					cor += SET*2;
			}
			structure.tint.color.bind();
			if (code == 0x0F) {
				int c = FULLS + (i.ran() & 0x07);
				if (broken) {
					c += SET/2;
				}
				sheet.render(r, c, i.x(), i.y());
				if (cor != 0) {
					
					s.setHeight(3).setDistance2Ground(8);
					sheet.render(s, c, i.x(), i.y());
					sheet.render(r, cor, i.x(), i.y());
				}
				COLOR.unbind();
				renderEdges(r, s, i, data);
				return !broken;
			}

			if (code == 0) {
				s.setHeight(3).setDistance2Ground(8);
				
				int c = (data & DIA) != 0 ? 4:0;
				c += (i.ran() & 0x03);
				c += SINGLES;
				if (broken)
					c += SET/2;
				sheet.render(r, c, i.x(), i.y());
				sheet.render(s, c, i.x(), i.y());
			} else {
				
				int c = code + (i.ran() & 0b011) * SET;				
				
				if (broken) {
					c += 8*SET;
					s.setHeight(0).setDistance2Ground(8);
					sheet.render(s, c, i.x(), i.y());
				}else if((data & DIA) != 0) {
					c += 4*SET;
				}
				s.setHeight(12).setDistance2Ground(0);
				sheet.render(r, c, i.x(), i.y());
				sheet.render(s, c, i.x(), i.y());

				if (cor != 0) {
					sheet.render(r, cor, i.x(), i.y());
				}
				
				
				renderEdges(r, s, i, data);
			}
			COLOR.unbind();
			
			return false;
		}
		
		private void renderEdges(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
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
		protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

			return false;
		}

		@Override
		public AVAILABILITY getAvailability(int x, int y) {
			return broken ? null : AVAILABILITY.SOLID;
		}

		@Override
		public boolean isPlacable(int tx, int ty) {
			return true;
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
		public int miniDepth() {
			return 2;
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
		public int heightStart(int tx, int ty) {
			return 0;
		}

		@Override
		public int heightEnd(int tx, int ty) {
			return 3;
		}
		
		@Override
		public LOS los(int tx, int ty) {
			return LOS.SOLID;
		}

		public boolean isFull(int tx, int ty) {
			int data = shared.data.get(tx, ty);
			return (data & 0x0F) == 0x0F && ((data >> 4) & 0x0F) == 0;
		}
		
		@Override
		public boolean coversCompletely(int tx, int ty) {
			return (shared.data.get(tx, ty) & 0x0F) == 0x0F;
		}
		
		@Override
		void unplace(int tx, int ty) {
			if (!SETT.ROOMS().map.is(tx, ty))
				SETT.FLOOR().clearer.clear(tx, ty);
		}

		@Override
		public boolean wantsFloorUnderneath(int tx, int ty) {
			return (shared.data.get(tx, ty) & 0x0F) != 0x0F;
		}


	}
	
	class WallFull extends Wall{

		final TerrainClearing clearing = new TerrainClearing() {

			@Override
			public RESOURCE clear1(int tx, int ty) {
				shared.NADA.placeFixed(tx, ty);
				return structure.resource;
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
				return structure.durability*20;
			}
			
			@Override
			public boolean canDestroy(int tx, int ty) {
				return true;
			}

		};

		private WallFull(String key, Terrain t) {
			super(key, t, false);
		}
		
		@Override
		public TerrainClearing clearing() {
			return clearing;
		}
		

	}
	
	class WallBroken extends Wall implements TILE_FIXABLE{


		final TerrainClearing clearing = new TerrainClearing() {


			@Override
			public RESOURCE clear1(int tx, int ty) {
				shared.NADA.placeFixed(tx, ty);
				return structure.resource;
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
				return sound;
			}

			@Override
			public boolean isStructure() {
				return true;
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

		private WallBroken(String key, Terrain t) {
			super(key, t, true);
		}
		
		@Override
		public Job fixJob(int tx, int ty) {
			return SETT.JOBS().build_structure.get(structure.index()).wall;
		}
		
		@Override
		public TerrainClearing clearing() {
			return clearing;
		}

		@Override
		public TerrainTile getTerrain(int tx, int ty) {
			return wall;
		}
		
		@Override
		public LOS los(int tx, int ty) {
			return LOS.CEILING;
		}
		

	}
	
	public class Ceiling extends BuildingComponent {

		private final static int SET = 16;
		
		private final TILE_SHEET sheet;
		private final int SHEET_CORNER;
		private final int SHEET_SHADOW;
		private final Opening opening;

		private Ceiling(String key, Terrain t) {
			super(key + "_CEILING", t, structure.nameCeiling, iconCeiling, structure.miniColor, structure.resource);
			this.sheet = spriteCeiling;
			this.SHEET_CORNER = SET * 4;
			this.SHEET_SHADOW = SHEET_CORNER + SET;
			opening = new Opening(key, t);
		}

		@Override
		protected boolean place(int x, int y) {

			if (opening.isPlacable(x, y)) {
				return opening.place(x, y);
			}
			boolean was = shared.get(x, y) == this;
			super.placeRaw(x, y);
			int data = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (joins(x, y, d)) {
					data |= d.mask();
				}
			}

			data = setCorners(x, y, data);
			data = shadowSet(x, y, data);

			shared.data.set(x, y, data);
			if (!SETT.ROOMS().map.is(x, y) && !was)
				SETT.FLOOR().clearer.clear(x, y);
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

		private int shadowSet(int x, int y, int res) {
			int s = 0;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				if (jwall.is(x, y, DIR.ORTHO.get(i)))
					s |= DIR.ORTHO.get(i).mask();
			}

			res |= (s << 8);

			return res;
		}

		private int shadowGet(int data) {
			return (data >> 8) & 0x0F;
		}

		private int getCorners(int data) {
			return (data >> 4) & 0x0F;
		}
		
		private int getData(int data, RenderData.RenderIterator i) {
			int res = data & 0xF;
			Room r = SETT.ROOMS().map.get(i.tx(), i.ty());
			if (r != null && r.constructor() != null && r.constructor().mustBeIndoors()) {
				for (DIR d : DIR.ORTHO) {
					if (!r.isSame(i.tx(), i.ty(), i.tx()+d.x(), i.ty()+d.y())) {
						if (!(TERRAIN().get(i.tx()+d.x(), i.ty()+d.y()) instanceof Opening))
							res |= d.mask();
					}
				}
			}else {
				for (DIR d : DIR.ORTHO) {
					r = SETT.ROOMS().map.get(i.tx(), i.ty(), d);
					if (r != null && r.constructor() != null && r.constructor().mustBeIndoors())
						res |= d.mask();
				}
			}
			
			return res;
		}

//		public void renderEdge(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int mask) {
//			if (mask != 0) {
//				int j = mask + (i.ran() & 0b011) * SET;
//				sheet.render(r, j, i.x(), i.y());
//			}
//		}
		
		@Override
		protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

			int a = getData(data, i);
			if (a != 0) {
				int j = a + (i.ran() & 0b011) * SET;
				sheet.render(r, j, i.x(), i.y());
			}

			a = getCorners(data);
			if (a != 0) {
				sheet.render(r, SHEET_CORNER + a, i.x(), i.y());
			}

			a = shadowGet(data);

			// s.setHard();
			s.setDistance2Ground(0).setHeight(2);
			sheet.render(s, SHEET_SHADOW, i.x(), i.y());
			// s.setSoft();

			// if (a != 0x0F) {
			// s.setDistance2Ground(2).setHeight(8);
			// sheet.render(s, SHEET_SHADOW + a, i.x(), i.y());
			//
			// }

			
			
			
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
			// for (DIR d: DIR.ALL) {
			// if (!joins(tx, ty, d) && !is(tx, ty, d))
			// return false;
			// }
			return true;
		}

		private boolean joins(int x, int y, DIR d) {
			return shared.get(x, y, d).wallIsWally();
		}

		@Override
		public boolean is(int tx, int ty) {
			return super.is(tx, ty) || opening.is(tx, ty);
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

		public class Opening extends BuildingComponent implements Diagonalizer{

			private final static int SET = 16;
			private final TILE_SHEET sheet;
			private final TILE_SHEET shadow;
			private final int CORNERS = SET*4;
			private final int SINGLES = CORNERS + 4*SET;
			private final int DIA = 0x01000;
			private final int FAT = 0x02000;

			private Opening(String key, Terrain t) {
				super(key + "_OPENING", t, structure.nameCeiling, iconCeiling, structure.miniColor, structure.resource);
				this.sheet = spriteOpening;
				shadow = spriteWall;
			}

			@Override
			protected boolean place(int x, int y) {
				if (!isPlacable(x, y))
					return Ceiling.this.place(x, y);

				
				boolean dia = shared.get(x, y) instanceof Diagonalizer && ((Diagonalizer) shared.get(x, y)).getDia(x, y);
				placeRaw(x, y);
				
				int res = 0;
				
				if (isFat(x, y)) {
					res |= FAT;
					for (DIR d : DIR.ORTHO) {
						if (joins(x, y, d) && joins(x, y, d.next(1)) && joins(x, y, d.next(2)))
							res |= d.mask() | d.next(2).mask();
					}
				}else {
					for (DIR d : DIR.ORTHO) {
						if (!isFat(x+d.x(), y+d.y()) && joins(x, y, d))
							res |= d.mask();
					}
				}
				
				int cor = 0;
				for (DIR d : DIR.NORTHO) {
					if (!joins(x, y, d) && (res & d.next(1).mask()) != 0 && (res & d.next(-1).mask()) != 0)
						cor |= d.mask();
				}
				res |= cor << 8;

				if (res != 0 && IN_BOUNDS(x, y, DIR.N)) {
					TerrainTile t = shared.get(x, y, DIR.N);
					if (t != this && t != wall && t.wallIsWally() && !t.roofIs()) {
						if ((res & DIR.NW.mask()) != 0)
							res |= 0b000100000;
						if ((res & DIR.NE.mask()) != 0)
							res |= 0b000010000;
					}
				}
				if (res != 0 && IN_BOUNDS(x, y, DIR.W)) {
					TerrainTile t = shared.get(x, y, DIR.W);
					if (t != this && t != wall && t.wallIsWally() && !t.roofIs()) {
						if ((res & DIR.SW.mask()) != 0)
							res |= 0b00010000000;
						if ((res & DIR.NW.mask()) != 0)
							res |= 0b00001000000;
					}
				}

				shared.data.set(x, y, res);
				setDia(x, y, dia);
				return false;
			}
			
			private boolean isFat(int x, int y) {
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR d = DIR.ORTHO.get(di);
					if (joins(x, y, d) && joins(x, y, d.next(1)) && joins(x, y, d.next(2)))
						return true;
				}
				return false;
			}

			@Override
			protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
				// s.setHard();
				// s.setHeight(0).setDistance2Ground(0);
				int tile = data & 0x0F;
				int cor = (data >> 8) & 0x0F;
				structure.tint.color.bind();
				if (tile == 0) {
					// shadow.render(s, wall.SINGLES, i.x(), i.y());
					// s.setSoft();
					sheet.render(r, SINGLES + (i.ran() & 0x0F), i.x(), i.y());
					s.setHeight(3).setDistance2Ground(0);
					shadow.render(s, wall.SINGLES, i.x(), i.y());
				} else {
					// shadow.render(s, data, i.x(), i.y());
					// s.setSoft();
					int j = tile + (i.ran() & 0b01) * SET;
					if (tile != 0x0F && (data & DIA) != 0)
						j += 2*SET;
					if (tile != 0x0F)
						sheet.render(r, j, i.x(), i.y());
					s.setHeight(12).setDistance2Ground(0);
					if (tile == 0x0F)
						shadow.render(s, wall.FULLS, i.x(), i.y());
					else
						shadow.render(s, tile + ((data & DIA) != 0 ? wall.DIAGONAL : 0), i.x(), i.y());
					
					if (cor != 0) {
						int c = cor + CORNERS + ((data & FAT) != 0 ? SET : 0);
						sheet.render(r, c+((data & DIA) != 0 ? 2*SET : 0), i.x(), i.y());
					}
					
//					if ((data & 0b000110000) != 0)
//						SPRITES.sett().map.wall_merge.render(r, ((data >> 4) & 0b011) - 1, i.x(), i.y() - 4);
//					if ((data & 0b011000000) != 0)
//						SPRITES.sett().map.wall_merge.render(r, (data >> 6) + 2, i.x() - 4, i.y());
				}
				COLOR.unbind();
				
				return false;

			}

			private boolean joins(int tx, int ty, DIR d) {
				return jwall.is(tx, ty, d);
			}

			@Override
			protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i,
					int data) {
				return false;
			}

			@Override
			public AVAILABILITY getAvailability(int x, int y) {
				return null;
			}

			@Override
			public boolean isPlacable(int tx, int ty) {

				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR d = DIR.ALL.get(i);
					if (! jwall.is(tx, ty, d)) {
						return true;
					}
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
			public int heightStart(int tx, int ty) {
				return 3;
			}

			@Override
			public int heightEnd(int tx, int ty) {
				return 5;
			}
			
			@Override
			public LOS los(int tx, int ty) {
				return LOS.CEILING;
			}
			
			@Override
			void unplace(int tx, int ty) {
//				if (!SETT.ROOMS().map.is(tx, ty))
//					SETT.FLOOR().clearer.clear(tx, ty);
			}

			@Override
			public boolean wantsFloorUnderneath(int tx, int ty) {
				return (shared.data.get(tx, ty) & 0x0F) != 0x0F;
			}
			
		}

		@Override
		public int heightStart(int tx, int ty) {
			return 3;
		}

		@Override
		public int heightEnd(int tx, int ty) {
			return 5;
		}
		
		@Override
		public LOS los(int tx, int ty) {
			return LOS.CEILING;
		}
		


	}

	
	private final static MAP_BOOLEAN jwall = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (TERRAIN().get(tx, ty).wallJoiner())
				return true;
			Room r = SETT.ROOMS().map.get(tx, ty);
			return r != null && r.wallJoiner();
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}
	};

}
