package world.map.pathing;

import java.io.IOException;

import game.faction.Faction;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LIST;
import util.GUTIL;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.WORLD.WorldError;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;
import world.map.pathing.Comps.WComp;
import world.map.pathing.WRegFinder.Treaty;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.map.road.WTRAV;

public class WPATHING extends WorldResource{

	public final WDirMap map = new WDirMap();
	final Bitmap2D portArea = new Bitmap2D(WORLD.TBOUNDS(), false);
	Comps comps = new Comps(map);

	public final WRegFinder regFinder = new WRegFinder();
	
	private static LIST<DIR> dirs = new ArrayList<DIR>(DIR.ALL);
	
	public WPATHING() {
		super("pathing", "PATHING");
		IDebugPanelWorld.add("path test", new ACTION() {
			
			@Override
			public void exe() {
				new DebugTest();
			}
		});
		
		IDebugPanelWorld.add("path overlay", new ACTION() {
			
			@Override
			public void exe() {
				WORLD.OVERLAY().debug = new Comps.DebugOverlay();
			}
		});
		
		IDebugPanelWorld.add(new DebugPlacer());
	}
	
	private final WorldResourceManager saver = new WorldResourceManager() {
		
		{
			IDebugPanelWorld.add("generate paths", new ACTION() {
				
				@Override
				public void exe() {
					ACTION load = new ACTION() {
						
						@Override
						public void exe() {
							LOG.ln("gen...");
						}
					};
					generate(load);
					validateInit(null);
				}
				
				
			});
		}
		
		@Override
		public void save(FilePutter file) {
			map.saver.save(file);
			portArea.save(file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			map.saver.load(file);
			portArea.load(file);
			comps = Comps.generate(ACTION.NOP, map);
			validateInit(null);
			WORLD.OVERLAY().debug = null;
		}
		
		@Override
		public void clear() {
			map.saver.clear();
			portArea.clear();
			comps = new Comps(map);
		}
		
		@Override
		public void validateInit(WorldError error) {
			
			if (!WORLD.IN_BOUNDS(WORLD.REGIONS().player.cx(), WORLD.REGIONS().player.cy())) {
				if (error != null) {
					error.problem = "The world has no player region centre";
					error.coo.set(WORLD.TBOUNDS().cX(), WORLD.TBOUNDS().cY());
				}else {
					LOG.ln("bad stuff");
				}
				
				return;
			}
			

			boolean[] reached = new boolean[WREGIONS.MAX];
			
			GUTIL.flooder().init(this);
			GUTIL.flooder().pushSloppy(WORLD.REGIONS().player.cx(), WORLD.REGIONS().player.cy(), 0);
			while(GUTIL.flooder().hasMore()) {
				PathTile t = GUTIL.flooder().pollSmallest();
				Region reg = WORLD.REGIONS().map.get(t);
				if (reg != null && t.isSameAs(reg.cx(), reg.cy()))
					reached[reg.index()] = true;
				
				map.push(t, t.getValue());
			}
			GUTIL.flooder().done();
			

			for (int ri = 0; ri < WREGIONS.MAX; ri++) {
				Region reg = WORLD.REGIONS().getByIndex(ri);
				if (reg.active()) {
					if (!reached[reg.index()]) {
						if (error != null) {
							error.problem = "This region is not connected to other regions through roads " + reg.index();
							error.coo.set(reg.cx(), reg.cy());
						}else {
							LOG.ln("reg " + reg);
						}
						
						return;
					}
				}
			}
			
		};
		
		@Override
		public void generate(ACTION loadPrint) {
			clear();
			new Gen().generateAll(WORLD.REGIONS().player.cx(), WORLD.REGIONS().player.cy(), loadPrint);
			comps = Comps.generate(loadPrint, map);
			WORLD.OVERLAY().debug = null;
			//new DebugTest();
			
		};
		
		@Override
		public LIST<PLACABLE> makePlacers(ToolManager tm) {
			return new ArrayList<PLACABLE>(0);
		};
		
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}


	public static int cost(int fromX, int fromY, DIR d) {
		if (WORLD.WATER().isBig.is(fromX, fromY)) {
			return 1;
		}
		int toX = fromX + d.x();
		int toY = fromY + d.y();
		if (WORLD.WATER().isBig.is(toX, toY))
			return WTRAV.PORT_PENALTY;
		
		if (WORLD.MOUNTAIN().coversTile(fromX, fromY))
			return 6;
		if (WORLD.FOREST().amount.get(fromX, fromY) == 1.0)
			return 4;
		return 3;
	}
	
	public static double movementSpeed(int tx, int ty) {
		if (WORLD.WATER().isBig.is(tx, ty)) {
			return 1;
		}
		if (WORLD.MOUNTAIN().heighter.get(tx, ty) >= 1)
			return 0.16;
		if (WORLD.FOREST().amount.get(tx, ty) == 1.0)
			return 0.25;
		return 0.33;
	}
	
	public PathTile path(int sx, int sy, int destX, int destY) {
		return comps.finder.find(sx, sy, destX, destY, Treaty.DUMMY);
	}
	
	public PathTile path(COORDINATE start, int endX, int endY, Treaty trav) {
		return  comps.finder.find(start.x(), start.y(), endX, endY, trav);
	}
	
	public PathTile path(int startX, int startY, COORDINATE end, Treaty trav) {
		return  comps.finder.find(startX, startY, end.x(), end.y(), trav);
	}
	
	public PathTile path(COORDINATE start, COORDINATE end, Treaty trav) {
		return  comps.finder.find(start.x(), start.y(), end.x(), end.y(), trav);
	}
	
	public PathTile path(int sx, int sy, int destX, int destY, Treaty treaty) {
		return comps.finder.find(sx, sy, destX, destY, treaty);
	}
	
	public double distance(Region from, Region to) {
		if (from == to)
			return 0;
		return comps.finder.dist(from.cx(), from.cy(), to.cx(), to.cy(), Treaty.DUMMY);
		
	}
	
	public MAP_OBJECT<WComp> c(){
		return comps;
	}
	
	public COORDINATE rnd(Region home) {
		GUTIL.filler().init(this);
		GUTIL.filler().fill(home.cx(), home.cy());
		GUTIL.coos().set(0);
		while(GUTIL.filler().hasMore()) {
			COORDINATE c = GUTIL.filler().poll();
			if (!home.is(c))
				continue;
			if (!WORLD.REGIONS().centre.is(c) && !WORLD.WATER().isBig.is(c)) {
				GUTIL.coos().get().set(c);
				GUTIL.coos().inc();
			}
			
			for (DIR d : dirs) {
				if (map.can(c, d))
					GUTIL.filler().fill(c, d);
			}
		}
		GUTIL.filler().done();
		
		if (GUTIL.coos().getI() == 0) {
			GUTIL.coos().get().set(home.cx(), home.cy());
			GUTIL.coos().inc();
		}
		GUTIL.coos().set(RND.rInt(GUTIL.coos().getI()));
		return GUTIL.coos().get();
		
		
	}
	
	public COORDINATE rndDist(int cx, int cy, int dist) {
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(cx, cy, 0);
		PathTile backup = null;
		while(GUTIL.flooder().hasMore()) {
			PathTile c = GUTIL.flooder().pollSmallest();

			if (c.getValue() >= dist) {
				GUTIL.flooder().done();
				return c;
			}
			
			if (!WORLD.REGIONS().centre.is(c)) {
				backup = c;
			}
			
			DIR d = dirs.rnd();
			for (int i = 0; i < dirs.size(); i++) {
				if (map.can(c, d) && !WORLD.REGIONS().cTile.is(c, d))
					GUTIL.flooder().pushSmaller(c, d, c.getValue()+d.tileDistance());
				d = d.next(1);
			}
		}
		GUTIL.filler().done();
		return backup;
		
		
	}
	
	public COORDINATE rndDistOwn(int cx, int cy, int dist) {
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(cx, cy, 0);
		PathTile backup = null;
		
		Faction fa = WORLD.REGIONS().map.get(cx, cy).faction();
		
		while(GUTIL.flooder().hasMore()) {
			PathTile c = GUTIL.flooder().pollSmallest();

			Region rr = WORLD.REGIONS().map.get(c);
			
			if (rr != null) {
				if (rr.faction() != null && rr.faction() != fa)
					continue;
			}
			
			if (c.getValue() >= dist) {
				GUTIL.flooder().done();
				return c;
			}
			
			if (!WORLD.REGIONS().cTile.is(c)) {
				backup = c;
			}
			
			DIR d = dirs.rnd();
			for (int i = 0; i < dirs.size(); i++) {
				if (map.can(c,d) && !WORLD.REGIONS().cTile.is(c, d))
					GUTIL.flooder().pushSmaller(c, d, c.getValue()+d.tileDistance());
				d = d.next(1);
			}
		}
		GUTIL.filler().done();
		return backup;
		
		
	}

	public MAP_OBJECT<Region> regMap = new MAP_OBJECT<Region>() {
		
		@Override
		public Region get(int tx, int ty) {
			if (WORLD.WATER().isBig.is(tx, ty)) {
				if (portArea.is(tx, ty))
					return WORLD.REGIONS().map.get(tx, ty);
				return null;
			}
			return WORLD.REGIONS().map.get(tx, ty);
		}
		
		@Override
		public Region get(int tile) {
			if (WORLD.WATER().isBig.is(tile)) {
				if (portArea.is(tile))
					return WORLD.REGIONS().map.get(tile);
				return null;
			}
			return WORLD.REGIONS().map.get(tile);
		}
	};
	

}
