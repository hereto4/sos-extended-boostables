package world;

import java.io.IOException;

import game.GAME.GameResource;
import game.debug.Profiler;
import init.constant.C;
import settlement.main.SETT;
import snake2d.Errors;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.file.SuperSaver;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import world.army.AD;
import world.battle.WBattles;
import world.entity.WEntities;
import world.entity.haven.WHavens;
import world.log.WorldLog;
import world.map.buildings.WorldBuildings;
import world.map.fow.FOW;
import world.map.landmark.WorldLandmarks;
import world.map.pathing.WPATHING;
import world.map.regions.WREGIONS;
import world.map.regions.centre.WCentre;
import world.map.road.WorldRoads;
import world.map.terrain.WorldClimate;
import world.map.terrain.WorldForest;
import world.map.terrain.WorldGround;
import world.map.terrain.WorldMountain;
import world.map.terrain.WorldTerrain;
import world.map.terrain.WorldWater;
import world.overlay.WorldOverlays;
import world.region.RD;

public class WORLD extends GameResource{

	private static Data w;
	
	public static int TWIDTH() {
		return w.tWidth;
	}
	
	public static int THEIGHT() {
		return w.tHeight;
	}
	
	public static RECTANGLE TBOUNDS() {
		return w.tDim;
	}
	
	public static RECTANGLE PIXELS() {
		return w.dim;
	}
	
	public static int PWIDTH() {
		return w.width;
	}
	
	public static int PHEIGHT() {
		return w.height;
	}
	
	public static int TAREA() {
		return w.tHeight*w.tWidth;
	}
	
	public static boolean IN_BOUNDS(int tx, int ty) {
		return w.tDim.holdsPoint(tx, ty);
	}
	
	public static boolean IN_BOUNDS(COORDINATE c, DIR d) {
		return IN_BOUNDS(c.x()+d.x(), c.y()+d.y());
	}
	
	public static boolean IN_BOUNDS(int tx, int ty, DIR d) {
		return IN_BOUNDS(tx+d.x(), ty+d.y());
	}
	
	public static WorldTerrain TERRAIN() {
		return w.terrain;
	}
	
	public static WorldMountain MOUNTAIN() {
		return w.terrain.mountain;
	}
	
	public static WorldWater WATER() {
		return w.terrain.water;
	}
	
	public static WorldGround GROUND() {
		return w.terrain.ground;
	}
	
	public static WorldForest FOREST() {
		return w.terrain.forest;
	}
	
	public static WEntities ENTITIES() {
		return w.ENTITIES;
	}
	
	public static WorldClimate CLIMATE() {
		return w.terrain.climate;
	}
	
	public static MAP_DOUBLE MOISTURE() {
		return w.terrain.ground.moisture;
	}
	
	public static WREGIONS REGIONS() {
		return w.areas;
	}
	
	public static AD ARMIES() {
		return w.armies;
	}
	
	public static WorldBuildings BUILDINGS() {
		return w.buildings;
	}
	
	public static WorldLandmarks LANDMARKS() {
		return w.landmarks;
	}
	
	public static WorldMinimap MINIMAP() {
		return w.minimap;
	}
	
	public static WorldOverlays OVERLAY() {
		return w.overlay;
	}
	
	public static Sprites sprites() {
		return w.sprites;
	}
	
	public static WHavens camps() {
		return w.ENTITIES.havens;
	}
	
	public static WorldGen GEN() {
		return w.stage;
	}
	
	public static WorldRoads ROADS() {
		return w.roads;
	}
	
	public static WPATHING PATH() {
		return w.pathing;
	}
	
	public static WCentre CENTRE() {
		return w.centre;
	}
	
	public static FOW FOW() {
		return w.fow;
	}
	
	public static WorldLog LOG() {
		return w.log;
	}
	
	public static WBattles BATTLES() {
		return w.battles;
	}
	
	public static RD RD() {
		return w.rd;
	}
	
	
	public static LIST<WorldResource> RESOURCES(){
		return w.resources;
	}
	
	private final class Data {
		
		private final ArrayList<WorldResource> resources = new ArrayList<WorldResource>(100);
		private final RECTANGLE dim;
		private final RECTANGLE tDim;
		private final int tHeight;
		private final int tWidth;
		private final int height;
		private final int width;

		private final Sprites sprites;
		private final WorldTerrain terrain;
		private final WorldLandmarks landmarks;
		private final WEntities ENTITIES;
		private final WREGIONS areas;
		private final AD armies;
		private final WorldBuildings buildings;
		private final WorldMinimap minimap;
		private final WorldOverlays overlay;
		private final WorldRoads roads;
		private final WPATHING pathing;
		private final WorldGen stage;
		private final WCentre centre;
		private final Render render;
		private final FOW fow;
		private final WorldLog log;
		private final WBattles battles;
		private final RD rd;
		final SuperSaver<WorldResource> saver;
		
		private Data(int tileSizeX, int tileSizeY) throws IOException{
			w = this;
			tWidth = tileSizeX;
			tHeight = tileSizeY;

			if (tWidth > 512 || tHeight > 512)
				throw new Errors.DataError("too big a map!");

			width = tWidth * C.TILE_SIZE;
			height = tHeight * C.TILE_SIZE;
			tDim = new Rec(0, tWidth, 0, tHeight);
			dim = new Rec(0, width, 0, height);
			
			sprites = new Sprites();
			render = new Render(tileSizeX, tileSizeY);
			
			terrain = new WorldTerrain(WORLD.this);
			landmarks = new WorldLandmarks(WORLD.this);
			areas = new WREGIONS();
			centre = new WCentre();
			rd = new RD(null);
			armies = new AD(WORLD.this);
			buildings = new WorldBuildings();
			roads = new WorldRoads(WORLD.this);
			pathing = new WPATHING();
			ENTITIES = new WEntities(WORLD.this);
			fow = new FOW();
			minimap = new WorldMinimap(tileSizeX, tileSizeY);
			log = new WorldLog();
			overlay = new WorldOverlays();
			stage = new WorldGen(WORLD.this);
			battles = new WBattles();
			saver = new SuperSaver<WORLD.WorldResource>(getClass(), resources) {

				@Override
				protected String key(WorldResource t) {
					return t.key;
				}

				@Override
				protected void save(WorldResource t, FilePutter f) {
					t.saver().save(f);
				}

				@Override
				protected void load(WorldResource t, FileGetter f) throws IOException {
					t.saver().load(f);
				}

				@Override
				protected void clear(WorldResource t) {
					t.saver().clear();
				}
				
			};
			
			
			
		}

	}
	
	public WORLD(int tileSizeX, int tileSizeY) throws IOException {
		super("WORLD", false);
		new Data(tileSizeX, tileSizeY);
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		w.saver.save(saveFile);
		w.stage.save(saveFile);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		w.saver.load(file);
		w.stage.load(file);
		
		if (SETT.exists()) {
			SETT.WORLD_AREA().info.initCity(SETT.WORLD_AREA().tiles().x1(), SETT.WORLD_AREA().tiles().y1());
		}
	}
	
	@Override
	protected void update(double ds, Profiler prof) {
		prof.logStart(WORLD.class);
		for (WorldResource r : w.resources) {
			r.update(ds, prof);
		}
		w.minimap.update();
		prof.logEnd(WORLD.class);
	}
	
	@Override
	protected void afterTick() {
		for (WorldResource r : w.resources) {
			r.afterTick();
		}
		w.battles.poll();

	};
	
	public static void initBeforePlay() {
		for (int i = 0; i < w.resources.size(); i++) {
			WorldResource r = w.resources.get(i);
			r.initBeforePlay();
		}
	}
	
	public void render(Renderer r, float ds, int zoomout,
			RECTANGLE renWindow, int offX, int offY) {
		w.render.render(r, ds, zoomout, renWindow, offX, offY);
	}

	public static abstract class WorldResource {

		public final CharSequence name;
		final String key;
		
		protected WorldResource(CharSequence name, String key) {
			this.name = name;
			w.resources.add(this);
			this.key = key;
		}

		public abstract WorldResourceManager saver();
		
		protected void update(double ds, Profiler prof) {

		}
		
		protected void afterTick() {
			
		}
		
		protected void afterRender() {
			
		}
		
		protected void initBeforePlay() {
			
		}
		
	}
	
	public static abstract class WorldResourceManager implements SAVABLE{
		public void generate(ACTION loadPrint) {
			
		}
		
		public void validateInit(WorldError error) {
			
		}
		
		public LIST<PLACABLE> makePlacers(ToolManager tm){
			return new ArrayListGrower<>();
		}
		
		public void addDebugView() {
			
		}
	}


	
	public static void changeTile(int tx, int ty) {
		WORLD.MINIMAP().update(tx, ty);
	}
	
	public static class WorldError {
		
		public final Coo coo = new Coo();
		public CharSequence problem = null;
		public CharSequence warning = null;
	}
	

}
