package world.map.terrain;

import java.io.IOException;

import game.debug.Profiler;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import world.WORLD;
import world.WORLD.WorldError;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;

public class WorldTerrain extends WorldResource{

	private static final ArrayListGrower<WorldTerrainResource> resources = new ArrayListGrower<>();
	
	public final WorldClimate climate;
	public final WorldForest forest;
	public final WorldGround ground;
	public final WorldMountain mountain;
	public final WorldWater water;
	
	public WorldTerrain(WORLD world) throws IOException{
		super("terrain", "TERRAIN");
		resources.clear();
		climate = new WorldClimate();
		ground = new WorldGround();
		mountain = new WorldMountain();
		water = new WorldWater();
		forest = new WorldForest(world);
	}
	
	private final WorldResourceManager saver = new WorldResourceManager() {
		
		@Override
		public void save(FilePutter file) {
			for (WorldTerrainResource r : resources)
				r.save(file);
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			for (WorldTerrainResource r : resources)
				r.load(file);
		}
		
		@Override
		public void clear() {
			for (WorldTerrainResource r : resources)
				r.clear();
			WORLD.MINIMAP().repaint();
		}
		
		@Override
		public LIST<PLACABLE> makePlacers(ToolManager tm) {
			ArrayListGrower<PLACABLE> res = new ArrayListGrower<>();
			for (WorldTerrainResource r : resources)
				res.add(r.placers(tm));
			return res;
		}
		
		@Override
		public void validateInit(WorldError error) {
			new GeneratorValidator(error);
		}
		
		@Override
		public void generate(ACTION loadPrint) {
			new Generator(WORLD.GEN(), loadPrint);
		}
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}

	public void secretFixWays() {
		new GeneratorValidator(null);
	}

	
	@Override
	protected void update(double ds, Profiler prof) {
		for (WorldTerrainResource r : resources)
			r.update(ds, prof);
	}
	

	
	static abstract class WorldTerrainResource {

		protected WorldTerrainResource() {
			resources.add(this);
		}

		protected abstract void save(FilePutter file);

		protected abstract void load(FileGetter file) throws IOException;

		protected void clear() {
			
		}
		
		protected void update(double ds, Profiler prof) {

		}
		
		public abstract LIST<PLACABLE> placers(ToolManager tm);
		
	}


	
}
