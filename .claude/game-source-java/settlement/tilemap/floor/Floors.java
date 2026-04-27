package settlement.tilemap.floor;

import static settlement.main.SETT.GRASS;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TAREA;
import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;

import game.faction.Faction;
import game.faction.player.PlayerColors.PlayerColor;
import init.paths.PATH;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.game.SheetType;
import init.value.GVALUES;
import init.value.Lockable;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import settlement.tilemap.TileMap;
import settlement.tilemap.TileMap.SMinimapGetter;
import settlement.tilemap.terrain.TBuilding;
import settlement.tilemap.terrain.TBuilding.BuildingComponent;
import settlement.tilemap.terrain.TBuilding.Wall;
import settlement.tilemap.terrain.TFortification;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.CORE;
import snake2d.Errors;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.bit.Bits;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_CLEARER;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.keymap.MAPPED;
import util.keymap.MAPSAVE;
import util.keymap.RMAP;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public class Floors extends TileMap.Resource{

	private static CharSequence ¤¤dName = "Country road";
	
	static {
		D.ts(Floors.class);
	}
	
	private final ArrayList<Floor> all;
	private final RFloorExtra extra;
	public final LIST<Floor> roads;
	public final RMAP<Floor> roadMap;
	private final byte[] tiles = new byte[TAREA];
	private final Bits bDegradeO = new Bits(0b0_1111_0000);
	public final Bitmap2D floorundernot = new Bitmap2D(SETT.TILE_BOUNDS, false);
	
	private final int NOTHING = 0;
	private final Bitsmap1D types = new Bitsmap1D(-1, 6, TAREA) {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void set(int index, int value) {
			Floor f = getter.get(index);
			if (f != null)
				f.amount--;
			super.set(index, value);
			f = getter.get(index);
			if (f != null)
				f.amount++;
		};
		
	};
	public final Bitmap2D square = new Bitmap2D(SETT.TILE_BOUNDS, false);
	public final RMAP<Floor> map;
	
	public final Floor defaultRoad;
	public final Floor mainStartRoad;
	
	private final ColorImp miniRoad = new ColorImp(180, 180, 180);


	public Floors(TileMap tileMap) throws IOException {

		
		ResFolder gg = PATHS.SETT().folder("floor");
		
		String[] keys = gg.init.getFiles();
		{
			String[] kk = new String[keys.length];
			for (int i = 0; i < keys.length; i++) {
				kk[i] = keys[i];
			}
			kk[kk.length-1] = "_GRASS";
		}
		{
			LinkedList<Floor> all = new LinkedList<>();
			
			for(String k : gg.init.getFiles()) {
				Json data = new Json(gg.init.get(k));
				Json text = data.has("ROAD") ? new Json(gg.text.get(k)) : null;
				new Floor(all, k, data, text);
			}
			mainStartRoad = new Floor(all, "_MAIN_TOAD", new Json(gg.init.get("_MAIN_ROAD")), null);
			mainStartRoad.name = ¤¤dName;
			this.all = new ArrayList<>(all);
		}
		
		
		
		this.map = new RMAP<Floors.Floor>("FLOOR", all);
		
		if (all.size() > 0b111111) {
			throw new Errors.GameError("Too many floors have been declared. Maximum is " + 0b111111);
		}
		
		int r = 0;
		for (Floor fl : all) {
			if (fl != null && fl.isRoad)
				r++;
		}
		ArrayList<Floor> roads = new ArrayList<>(r);
		r = 0;
		for (Floor fl : all) {
			if (fl != null && fl.isRoad) {
				fl.indexroad = r++;
				roads.add(fl);
			}
		}
		this.roads = roads;
		this.roadMap = new RMAP<>("ROAD", roads);
		
		extra = new RFloorExtra(gg.sprite);
		
		for (Floor f : all) {
			if (f == null)
				continue;
			IDebugPanelSett.add(this.map.key, new PlacableMulti(f.key() + (f.isRoad ? " (road)" : ""), f.desc, null) {
				
				@Override
				public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
					f.placeFixed(tx, ty);
					
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
					return f.isPlacable(tx, ty) ? null : "";
				}
				
			});
		}
		
		IDebugPanelSett.add(this.map.key, new PlacableMulti("clear roads and floor") {

			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				clearer.clear(tx, ty);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return IN_BOUNDS(tx, ty) && getter.is(tx, ty) ? null : "";
			}
		});
		
		IDebugPanelSett.add(this.map.key, new PlacableMulti("floor degrade") {

			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				degradeInc(tx+ty*SETT.TWIDTH, 1);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return IN_BOUNDS(tx, ty) && getter.is(tx, ty) ? null : "";
			}
		});

		defaultRoad = map.read("FILE", new Json(gg.init.get("_DEFAULT_ROAD")));
		
		

	}
	
	private void setFloorMatch(int tx, int ty) {
		Room r = SETT.ROOMS().map.get(tx, ty); 
		
		if (r != null) {
			
		}else {
			TerrainTile t = SETT.TERRAIN().get(tx, ty);
			if (t.wantsFloorUnderneath(tx, ty))
				setFloorMatch(tx, ty, getter.get(tx, ty));
		}
			
	}
	
	
	public void setFloorMatch(int tx, int ty, Floor res) {
		
		
		if (SETT.JOBS().getter.is(tx, ty))
			return;
		
		if(!floorundernot.is(tx, ty)){
			int am = 0;
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (SETT.ROOMS().map.is(tx, ty, d))
					continue;
				if (SETT.TERRAIN().get(tx, ty, d) instanceof BuildingComponent)
					continue;
				Floor f = getter.get(tx, ty, d);
				if (f != null && f != res) {
					int a = testFloor(tx, ty, f);
					if (a > 1 && a > am) {
						am = a;
						res = f;
					}
				}
			}
		}
		
		Floor f = getter.get(tx, ty);
		
		if (res == null) {
			if (f != null)
				clearer.clear(tx, ty);
		}else if (f != res)
			res.placeFixed(tx, ty);
		
	}
	
	private int testFloor(int tx, int ty, Floor f) {
		int am = 0;
		for (int di = 0; di < DIR.ALL.size(); di++) {
			DIR d = DIR.ALL.get(di);
			if (SETT.ROOMS().map.is(tx, ty, d))
				continue;
			if (SETT.TERRAIN().get(tx, ty, d) instanceof BuildingComponent)
				continue;
			Floor f2 = getter.get(tx, ty, d);
			if (f2 == f) {
				am++;
			}
		}
		return am;
	}
	
	@Override
	protected void clearAll() {
		types.clear();
		square.clear();
	}

	@Override
	protected void save(FilePutter saveFile) {
		MAPSAVE.saveMeta(saveFile, all);
		saveFile.bs(tiles);
		types.save(saveFile);
		square.save(saveFile);
		floorundernot.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		int[] order = MAPSAVE.saveWash(saveFile, all, defaultRoad.index());
		saveFile.bs(tiles);
		types.load(saveFile);
		square.load(saveFile);
		if (order != null) {
			for (int i = 0; i < tiles.length; i++) {
				int bi = types.get(i)-1;
				if (bi >= 0) {
					int oi = order[bi];
					types.set(i, oi+1);
				}
			}
		}
		for (Floor f : all)
			f.amount = 0;
		
		for (int i = 0; i < tiles.length; i++) {
			Floor f = getter.get(i);
			if (f != null)
				f.amount++;
		}
		floorundernot.load(saveFile);
	}

	
	public final SMinimapGetter minimap = new SMinimapGetter() {
		
		@Override
		public COLOR miniColorPimped(ColorImp col, int x, int y, boolean n, boolean s) {
			if (n == s)
				return col;
			if (n)
				col.shadeSelf(1.2);
			else
				col.shadeSelf(0.8);
			return col;
		}
		
		@Override
		public COLOR miniC(int tx, int ty) {
			return getter.is(tx, ty) && (getter.get(tx, ty) == mainStartRoad || getter.get(tx, ty).isRoad) ? miniRoad : null;
		}
	};


	
	public void render(Renderer r, float ds, ShadowBatch shadowBatch, RenderData data) {

		RenderData.RenderIterator i = data.onScreenTiles();
		
		while (i.has()) {

			Floor f = getter.get(i.tile()); 
			
			if (f != null) {
				renderDetailed(i, f, tiles[i.tile()]);
			} 
			ROOMS().renderAfterGround(r, shadowBatch, i);
			
			if (((VIEW.RI() + i.tx()) & 0x0FF) == 0) {
				setFloorMatch(i.tx(), i.ty());
			}
			
			i.next();
		}


	}
	
	public int rotMask(RenderData.RenderIterator i) {
		return tiles[i.tile()] & 0x0F;
	}
	
	public void renderSimple(SPRITE_RENDERER ren, RenderData.RenderIterator i, Floor f){
		f.sheet.render(ren, i.ran()&15, i.x(), i.y());
	}
	
	public TextureCoords texture(RenderData.RenderIterator i, Floor f){
		return f.sheet.getTexture(i.ran()&15);
	}
	

	private int edge(RenderData.RenderIterator i, int mask) {
		
		if (square.is(i.tile())) {
			return 0x0F;
		}
		
		if (CORE.renderer().getZoomout() > 1)
			return 0x0F;
		
		if (SETT.ROOMS().map.get(i.tile()) != null)
			return 0x0F;
		
		int edge = 0;
		
		for (DIR d : DIR.NORTHO) {
			int m1 = d.next(-1).mask();
			int m2 = d.next(1).mask();
			Room r = SETT.ROOMS().map.get(i.tx(), i.ty(), d);
			if ((mask & m1) != 0 && ((mask & m2) != 0) && (getter.get(i.tile()+d.x()+d.y()*TWIDTH) != null || (r != null && r.constructor() != null && r.constructor().joinsWithFloor())))
				edge |= d.mask();
		}
		return edge;
	}
	
	private void renderDetailed(RenderData.RenderIterator i, Floor f, int t) {

		TILE_SHEET texture = f.sheet;
		
		int mask = square.is(i.tile()) ? 0x0F : t & 0x0F;
		int textureTile = i.ran() % texture.tiles();
		int de = degrade(i.tx(), i.ty(), f); 
		int broken = (de>>2);
		int filth = CLAMP.i(de, 0, 8)-1;
		int ran = i.ran();
		
		
		
		
		int edge = edge(i, mask) ;
		int stencil = broken*RFloorExtra.eSet;
		
		if (mask == 0) {
			stencil += RFloorExtra.eSingles + (ran&0x0F);
			ran = ran >> 4;
		}else if (mask == 0x0F) {
			stencil += RFloorExtra.eFulls + (ran&0x0F);
			ran = ran >> 4;
		}else {
			stencil += mask + (ran & 3)*16;
			ran = ran >> 2;
		}
		
		f.tint().bind();
		
		if (edge == 0x0F) {
			
			if (mask == 0x0F && broken == 0)
				i.hiddenSet();
			
			extra.stencil.renderTextured(texture.getTexture(textureTile), stencil, i.x(), i.y());

			int mm = 0;
			
			for (DIR d : DIR.ORTHO) {
				Floor fl = getter.get(i.tx(), i.ty(), d);
				if (fl == f)
					mm |= d.mask();
			}
			
			
			if (mm != 0x0F)
				extra.stencil.renderTextured(extra.normalEdge.getTexture(mm), stencil, i.x(), i.y());
			
			COLOR.unbind();
			
			if (filth > 0) {
				OPACITY.O99.bind();
				filth *= 8;
				filth += (ran&0x07);
				ran = ran >> 3;
				extra.stencil.renderTextured(extra.filth.getTexture(filth), stencil, i.x(), i.y());
				OPACITY.unbind();
			}
			
		}else {
			
			
			if (edge == 0x0F && mask == 0x0F && broken == 0)
				i.hiddenSet();
			
			//render the main part of the road
			extra.stencilDetail.renderTextured(texture.getTexture(textureTile), stencil, i.x(), i.y());
			
			//render an outline
			extra.edge.render(CORE.renderer(), stencil, i.x(), i.y());
			
			//fill in the corners
			if (edge != 0 && broken == 0) {
				extra.stencilDetail.renderTextured(texture.getTexture(textureTile), RFloorExtra.eCorner+edge, i.x(), i.y());
			}
			
			int mm = 0;
			
			for (DIR d : DIR.ORTHO) {
				Floor fl = getter.get(i.tx(), i.ty(), d);
				if (fl == f)
					mm |= d.mask();
			}
			
			
			if (mm != 0x0F)
				extra.stencilDetail.renderTextured(extra.normalEdge.getTexture(mm), stencil, i.x(), i.y());
			
			COLOR.unbind();
			
			if (filth > 0) {
				OPACITY.O99.bind();
				filth *= 8;
				filth += (ran&0x07);
				ran = ran >> 3;
				extra.stencilDetail.renderTextured(extra.filth.getTexture(filth), stencil, i.x(), i.y());
				OPACITY.unbind();
			}
		}
		
		
		

		
		
	}
	
	public void renderOntop(RenderData.RenderIterator i, Floor f, int mask) {

		i.hiddenSet();
		
		Renderer r = CORE.renderer();
		
		TILE_SHEET sheet = f.sheet;
		int tile = i.ran() % sheet.tiles();
		int de = (degrade(i.tx(), i.ty(), f)>>1)&0b0111; 
		f.tint().bind();
		sheet.render(r, tile, i.x(), i.y());
		COLOR.unbind();
		
		if (de > 0) {
			OPACITY.O50.bind();
			extra.filth.render(r, (de-1)*8+(i.ran()&0x07), i.x(), i.y());
			OPACITY.unbind();
		}
		
		if (mask != 0x0FF) {
			extra.normalEdge.render(r, mask, i.x(), i.y());
		}

	}



	public class Floor implements MAPPED{

		private int amount;
		
		public final PlayerColor tint;
		public CharSequence name;
		public final CharSequence desc;
		public final boolean isRoad;
		private final double[] envValues = new double[SETT.ENV().map.all().size()];
		public final AVAILABILITY speed;
		public final double durability;
		public final TILE_SHEET sheet;
		public final RESOURCE resource;
		public final int resAmount;
		protected final int code;
		private final Icon icon;
		private int indexroad;
		public final String key;
		public final boolean isGrass;
		public final Lockable<Faction> reqs;
		private double[] preference = new double[RACES.all().size()];
		
		protected Floor(LISTE<Floor> all, String key, Json data, Json text) throws IOException {
			code = all.add(this);
			this.sheet = SPRITES.GAME().raw(SheetType.sTex, data);
			this.key = key;
			
			isRoad = data.has("ROAD");
			isGrass = data.bool("IS_GRASS", false);

			this.icon = new Icon(new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					int w = (X2-X1)/2;
					int h = (Y2-Y1)/2;
					int scale = (X2-X1)/width();
					for (int y = 0; y < 2; y++) {
						for (int x = 0; x < 2; x++) {
							int i = y*2+x;
							extra.icon.render(r, i, X1+x*w, X1+x*w+w, Y1+y*h, Y1+y*h+h);
							tint().bind();
							extra.icon.renderTextured(sheet.getTexture(i), i+4, X1+x*w, Y1+y*h, scale);
							COLOR.unbind();
						}
					}
					
				}
			});
			if (isRoad) {
				Json road = data.json("ROAD");
				name = text.text("NAME");
				desc = text.text("DESC");
				SETT.ENV().map.rmap.readFill(envValues, road, 1.0);
				speed = AVAILABILITY.ROADS[road.i("SPEED", 0, AVAILABILITY.ROADS.length-1)];
				durability = road.d("DURABILITY", 0, 1);
				resource = RESOURCES.map().readTry(road);
				if (road.has("RESOURCE")) {
					resAmount = road.i("RESOURCE_AMOUNT");
				}else {
					
					resAmount = 0;
				}
				reqs = GVALUES.FACTION.LOCK.push("FLOOR_" + key, name, desc, icon);
				RACES.map().readFill("PREFERENCE", preference, road, 0, 1);
			}else {
				name = Dic.¤¤floor + "#" + index();
				desc = "";
				durability = 1.0;
				resource = null;
				resAmount = 0;
				speed = AVAILABILITY.ROAD0;
				reqs = GVALUES.FACTION.LOCK.empty;
			}
			
			
			tint = new PlayerColor(new ColorImp(data, "COLOR_MASK"), "FLOOR_"+key, Dic.¤¤floor, name);
			
		}

		public int indexRoad() {
			return indexroad;
		}
		
		public void placeFixed(int tx, int ty) {

			place(tx, ty);

			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				if (getter.is(tx, ty, d))
					getter.get(tx, ty, d).place(tx + d.x(), ty + d.y());
			}

		}

		private void place(int tx, int ty) {

			Floor old = getter.get(tx, ty);

			
			if (!IN_BOUNDS(tx, ty))
				return;

			if (old == null)
				square.set(tx, ty, false);
			
			int d = 0;
			
			if (TERRAIN().get(tx, ty).roofIs() || ROOMS().map.is(tx, ty))
				d = 0x0F;
			else {
				for (int i = 0; i < DIR.ORTHO.size(); i++) {
					DIR dir = DIR.ORTHO.get(i);
					TerrainTile t = TERRAIN().get(tx, ty, dir);
					if (t instanceof TBuilding.Wall) {
						TBuilding.Wall w = (Wall) t;
						if (w.getDia(tx+dir.x(), ty+dir.y()))
							d |= DIR.ORTHO.get(i).mask();
					}
					if (!SETT.IN_BOUNDS(tx, ty, dir) || getter.is(tx, ty, dir) || TERRAIN().get(tx, ty, dir) instanceof TFortification.Tile || SETT.ROOMS().map.is(tx, ty, dir)) {
						d |= DIR.ORTHO.get(i).mask();
					}
				}
			}
			

			


			if (old == this) {
				int deg = tiles[ty * SETT.TWIDTH + tx] & 0xF0;
				d |= deg;
			}

			tiles[ty * SETT.TWIDTH + tx] = (byte) d;
			types.set(ty * SETT.TWIDTH + tx, code+1);
			if (old != this)
				SETT.TILE_MAP().miniCUpdate(tx, ty);
			
			GRASS().currentI.set (tx, ty, 0);
			PATH().availability.updateAvailability(tx, ty);
		}

		public CharSequence name() {
			return name;
		}

		public boolean isPlacable(int x, int y) {
			return true;
		}

		public Icon getIcon() {
			return icon;
		}

		@Override
		public int index() {
			return code;
		}
		
		public double envValue(SettEnv e, int tile) {
			return degrade.get(tile) < 1 ? envValues[e.index()] : 0;
		}
		
		public double envValue(SettEnv e) {
			return envValues[e.index()];
		}
		
		public COLOR tint() {
			if (isGrass)
				return SETT.GRASS().color(0);
			return tint.color;
		}

		@Override
		public String key() {
			return key;
		}
		
		public double pref(Race race) {
			return preference[race.index()];
		}
		
		public void prefSet(Race race, double pref) {
			preference[race.index] = pref;
		}

		public int amountPlaced() {
			return amount;
		}
		
	}

	public AVAILABILITY getAvailability(int tx, int ty) {
		return (getter.is(tx, ty) && degrade.get(tx, ty) != 1.0) ? getter.get(tx, ty).speed : null;
	}
	
	public void updateStructure(int tx, int ty) {
		Floor f = getter.get(tx, ty);
		if (f != null)
			f.place(tx, ty);
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			f = getter.get(tx, ty, DIR.ORTHO.get(i));
			if (f != null)
				f.place(tx+DIR.ORTHO.get(i).x(), ty+DIR.ORTHO.get(i).y());
		}
		
		
	}

	public final MAP_OBJECT<Floor> getter = new MAP_OBJECT<Floors.Floor>() {
		
		@Override
		public Floor get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH);
			return null;
		}
		
		@Override
		public Floor get(int tile) {
			if (tile >= 0 && tile < TAREA) {
				int c = types.get(tile);
				if (c > 0) {
					return all.get(c-1);
				}
			}
				
			return null;
		}
	};
	
	public final MAP_DOUBLEE degrade = new MAP_DOUBLEE() {
		
		private final double i = 1/7.0;
		
		@Override
		public double get(int tx, int ty) {
			return get(tx+ty*TWIDTH);
		}
		
		@Override
		public double get(int tile) {
			return (bDegradeO.get(tiles[tile])>>1)*i;
		}
		
		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			set(tx+ty*TWIDTH, value);
			return this;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			double o = get(tile);
			value = CLAMP.d(value, 0, 1);
			int v = ((int) (value*0x07)*2)<<1;
			v = CLAMP.i(v, 0, 15);
			
			tiles[tile] = (byte) bDegradeO.set(tiles[tile], v);
			
			if ((o != 0 && v == 0) || (o == 0 && v > 0))
				SETT.PATH().availability.updateAvailability(tile%SETT.TWIDTH, tile/SETT.THEIGHT);
			
			return this;
		}
	};
	
	public int degrade(int tx, int ty) {
		Floor f = getter.get(tx, ty);
		if (f == mainStartRoad)
			return 0x0F;
				
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null)
			return (int) (0x0F*r.getDegrade(tx, ty));
		return bDegradeO.get(tiles[tx+ty*SETT.TWIDTH]);
	}
	
	private int degrade(int tx, int ty, Floor f) {
		if (f == mainStartRoad)
			return 0x0F;
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null)
			return (int) (0x0F*r.getDegrade(tx, ty));
		return bDegradeO.get(tiles[tx+ty*SETT.TWIDTH]);
	}
	
	public void degradeInc(int tile, int am) {
		tiles[tile] = (byte) (bDegradeO.inc(tiles[tile], am));
			
	}
	
	
	
	public final MAP_CLEARER clearer = new MAP_CLEARER() {
		
		@Override
		public MAP_CLEARER clear(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				clear(tx+ty*TWIDTH);
			return this;
		}
		
		@Override
		public MAP_CLEARER clear(int tile) {
			if (getter.is(tile)) {
				types.set(tile, NOTHING);
				SETT.TILE_MAP().miniCUpdate(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
				int tx = tile%SETT.TWIDTH;
				int ty = tile/SETT.TWIDTH;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					Floor f = getter.get(tx, ty, d);
					if (f != null) {
						f.place(tx+d.x(), ty+d.y());
					}
				}
				PATH().availability.updateAvailability(tx, ty);
			}
			
			return this;
		}
	};

	public LIST<Floor> all() {
		return all;
	}

	private static final class RFloorExtra{

		public final static int eSingles = 4*16;
		public final static int eFulls = 5*16;
		public final static int eSet = 6*16;
		
		public final static int eCorner = eSet*4;
		
//		public final static int eEdgeStencil = eDegSet*4;
//		public final static int eEdgeNormal = eDegSet*4+16;
//		public final static int eEdge2 = eEdgeNormal + 16;
		
		public final TILE_SHEET stencil;
		public final TILE_SHEET stencilDetail;
		public final TILE_SHEET edge;
		public final TILE_SHEET filth;
		public final TILE_SHEET normalEdge;
		public final TILE_SHEET icon;
		
		RFloorExtra(PATH g) throws IOException{
			stencil = new ITileSheet(g.get("_FloorExtra"), 1152, 50) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return stencil(c, s, d, 0);
				}
			}.get();
			stencilDetail = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return stencil(c, s, d, 128);
				}
			}.get();
			edge = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return stencil(c, s, d, 256);
				}
			}.get();
			
			normalEdge = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.init(0, 256+128, 1, 1, d.s16);
					s.house.paste(true);
					return d.s16.saveGame();
				}
			}.get();
			filth = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(s.house.body().x2(), s.house.body().y1(), 1, 1, 8, 8, d.s16);
					s.full.paste(true);
					return d.s16.saveGame();
				}
			}.get();
			icon = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(s.full.body().x2(), s.full.body().y1(), 2, 1, 2, 2, d.s16);
					s.full.paste(true);
					s.full.setVar(1).paste(true);
					return d.s16.saveGui();
				}
			}.get();
		}
		
		private TILE_SHEET stencil(ComposerUtil c, ComposerSources s, ComposerDests d, int y2) {
			int x1 = 0;
			for (int x = 0; x < 4; x++) {
				int y1 = y2;
				for (int y = 0; y < 1; y++) {
					s.house.init(x1, y1, 2, 1, d.s16);
					s.house.setVar(0).paste(1, true);
					s.house.setVar(1).paste(1, true);
					
					s.full.init(x1, s.house.body().y2(), 1, 1, 8, 1, d.s16);
					s.full.paste(1, true);
					s.full.init(x1, s.full.body().y2(), 1, 1, 8, 1, d.s16);
					s.full.paste(1, true);
					y1 = s.full.body().y2();
				}
				
				x1 = s.house.body().x2();
			}
			
			s.house.init(0, y2, 1, 1, d.s16);
			s.house.setVar(0).pasteEdges(true);
//			s.full.init(0, s.house.body().y2(), 1, 1, 8, 1, d.s16);
//			s.full.init(0, s.full.body().y2(), 1, 1, 8, 1, d.s16);
//			s.house.init(0, s.full.body().y2(), 1, 1, d.s16);
//			s.house.setVar(0).pasteEdges(true);
			
			
			return d.s16.saveGame();
		}
		
	}

}
