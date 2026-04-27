package world.map.landmark;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.TAREA;
import static world.WORLD.TWIDTH;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LIST;
import util.text.D;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import world.WORLD;
import world.WORLD.WorldError;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;

public final class WorldLandmarks extends WorldResource {

	private static CharSequence ¤¤name = "Landmark";
	
	static {
		D.ts(WorldLandmarks.class);
	}
	
	
	final static int nothing = 0;
	final static int MAX = 255;
	private final ArrayList<WorldLandmark> areas = new ArrayList<>(MAX+1);
	private final Bitsmap1D mapID = new Bitsmap1D(0, 8, TAREA());
	
	
	
	public WorldLandmarks(WORLD world) {
		super(¤¤name, "LANDMARKS");
		areas.add((WorldLandmark)null);
		for (int i = 1; i <= MAX; i++)
			areas.add(new WorldLandmark(i));
	}

	public WorldLandmark getByIndex(int index) {
		return areas.get(index);
	}
	
	public LIST<WorldLandmark> all(){
		return areas;
	}
	
	public MAP_OBJECTE<WorldLandmark> setter = new MAP_OBJECTE<WorldLandmark>() {

		@Override
		public WorldLandmark get(int tile) {
			int i = mapID.get(tile);
			return areas.get(i);
		}

		@Override
		public WorldLandmark get(int tx, int ty) {
			if (IN_BOUNDS(tx, ty))
				return get(tx+ty*TWIDTH());
			return null;
		}

		@Override
		public void set(int tile, WorldLandmark object) {
			if (object == null)
				mapID.set(tile, nothing);
			else
				mapID.set(tile, object.index());
		}

		@Override
		public void set(int tx, int ty, WorldLandmark object) {
			if (IN_BOUNDS(tx, ty)) {
				set(tx+ty*TWIDTH(), object);
			}
		}
	};
	
	private final WorldResourceManager saver = new WorldResourceManager() {
		
		private final PlacerOverlay overlay = new PlacerOverlay();
		
		@Override
		public void save(FilePutter file) {
			for (WorldLandmark a : areas)
				if (a != null)
					a.save(file);
			mapID.save(file);
		}


		@Override
		public void load(FileGetter file) throws IOException {
			for (WorldLandmark a : areas)
				if (a != null)
					a.load(file);
			mapID.load(file);
		}
		
		@Override
		public void clear() {
			mapID.setAll(0);
			for (WorldLandmark a : areas)
				if (a != null)
					a.clear();
		}
		
		@Override
		public void generate(ACTION loadPrint) {
			new GeneratorLandmark(loadPrint);
		}
		
		@Override
		public void validateInit(WorldError error) {
			new GeneratorLandmarkValidator(error);
		}
		
		@Override
		public LIST<PLACABLE> makePlacers(ToolManager tm) {
			return new Placers(WorldLandmarks.this, overlay);
		}
		
		@Override
		public void addDebugView() {
			overlay.add();
		};
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}


	



	


	

	
}
