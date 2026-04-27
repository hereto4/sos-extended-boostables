package world.map.regions;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.REGIONS;
import static world.WORLD.TWIDTH;

import java.io.IOException;

import game.debug.Profiler;
import game.faction.Faction;
import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LIST;
import util.rendering.RenderData.RenderIterator;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import world.WORLD;
import world.WORLD.WorldError;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;
import world.map.regions.centre.WCentre;

public final class WREGIONS extends WorldResource {

	public final static COLOR cNone = new ColorImp(100, 100, 100);
	public final static int MAX = 1023;
	private final ArrayList<Region> areas = new ArrayList<>(MAX);
	private final ArrayList<Region> active = new ArrayList<>(MAX);
	final RegionMap pmap = new RegionMap();
	public final Region player;
	public final MAP_OBJECT<Region> map = pmap;
	private final Bitmap2D edge = new Bitmap2D(WORLD.TBOUNDS(), false);
	private final Bitmap2D besige = new Bitmap2D(WORLD.TBOUNDS(), false);
	private final Bitmap2D ctile = new Bitmap2D(WORLD.TBOUNDS(), false);
	public WREGIONS() {
		super("Regions", "REGIONS");
		player = new Region(0);
		areas.add(player);
		for (int i = 1; i < MAX; i++) {
			Region r = new Region(i);
			areas.add(r);
			
		}
		active.add(areas);
	}

	public Region getByIndex(int index) {
		return areas.get(index);
	}
	
	public LIST<Region> all(){
		return areas;
	}
	
	private final WorldResourceManager saver = new WorldResourceManager() {
		
		@Override
		public void save(FilePutter file) {
			pmap.save(file);
			for (Region a : areas)
				a.save(file);
			
		}


		@Override
		public void load(FileGetter file) throws IOException {
			pmap.load(file);
			for (Region a : areas)
				a.load(file);
			
			dirty = true;
			init();
		}
		
		@Override
		public void clear() {
			pmap.clear();
			for (Region r : REGIONS().all())
				r.clear();
			
			active.clearSloppy();
			active.add(areas);
			WORLD.MINIMAP().repaint();
			dirty = true;
		}
		
		@Override
		public LIST<PLACABLE> makePlacers(ToolManager tm) {
			return new Placer();
		}
		
		@Override
		public void generate(ACTION loadPrint) {
			new Gen(loadPrint);
			validateInit(null);
		}
		
		@Override
		public void validateInit(WorldError error) {
			new GenValidator(error);
			init();
		};
	};
	
	private void init() {
		edge.clear();
		for (COORDINATE c : WORLD.TBOUNDS()) {
			Region r = map.get(c);
			for (DIR d : DIR.ALL) {
				if (WORLD.IN_BOUNDS(c, d) && map.get(c,d) != r) {
					edge.set(c, true);
					break;
				}
			}
		}
		
		besige.clear();
		ctile.clear();
		Rec bb = new Rec(WCentre.TILE_DIM+2, WCentre.TILE_DIM+2);
		for (Region reg : active) {
			bb.moveX1Y1(reg.cx(), reg.cy());
			bb.incr(-Math.ceil(WCentre.TILE_DIM/2.0), -Math.ceil(WCentre.TILE_DIM/2.0));
			for (COORDINATE c : bb) {
				if (WORLD.TBOUNDS().holdsPoint(c) && bb.isOnEdge(c.x(), c.y())) {
					besige.set(c, true);
				}
			}
			
			for (DIR d : DIR.ALLC) {
				ctile.set(reg.cx(), reg.cy(), d, true);
			}
		}
		
		WORLD.FOW().setDirty();
		dirty = true;
	}
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}
	

	
	@Override
	protected void update(double ds, Profiler prof) {

	}
	
	boolean dirty = true;
	
	public LIST<Region> active(){
		if (dirty) {
			dirty = false;
			active.clearSloppy();
			for (Region r : areas) {
				if (r.info.area() > 0 && map.get(r.cx(), r.cy()) == r) {
					active.add(r);
				}
			}
		}
		return active;
	}

	
	public MAP_BOOLEAN border() {
		return edge;
	}
	
	public MAP_BOOLEAN centreEdgeTile() {
		return besige;
	}
	
	public MAP_BOOLEAN centreTile() {
		return ctile;
	}
	
	public final MAP_BOOLEAN isCentre = new MAP_BOOLEAN() {
		
		final int min = WCentre.TILE_DIM/2;
		final int max = WCentre.TILE_DIM/2;
		@Override
		public boolean is(int tx, int ty) {
			Region r = map.get(tx, ty);
			if (r != null) {
				int dx = tx-r.info.cx();
				int dy = ty-r.info.cy();
				return dx >= -min && dx <= max && dy >= -min && dy <= max;
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile % WORLD.TWIDTH(), tile/WORLD.TWIDTH());
		}
	};
	
	public final MAP_OBJECT<Region> cTile = new MAP_OBJECT<Region>() {

		@Override
		public Region get(int tile) {
			int tx = tile%WORLD.TWIDTH();
			int ty = tile/WORLD.TWIDTH();
			return get(tx, ty);
		}

		@Override
		public Region get(int tx, int ty) {
			Region r = map.get(tx, ty);
			if (r != null && r.cx() == tx && r.cy() == ty)
				return r;
			return null;
		}
	
	
	};
	
	public final MAP_OBJECT<Region> centre = new MAP_OBJECT<Region>() {

		@Override
		public Region get(int tile) {
			int tx = tile%WORLD.TWIDTH();
			int ty = tile/WORLD.TWIDTH();
			return get(tx, ty);
		}

		@Override
		public Region get(int tx, int ty) {
			
			final int min = WCentre.TILE_DIM/2;
			final int max = WCentre.TILE_DIM-min;
			
			for (int dy = -min; dy < max; dy++) {
				for (int dx = -min; dx < max; dx++) {
					Region r = map.get(tx+dx, ty+dy);
					if (r != null && r.cx() == tx+dx && r.cy() == ty+dy)
						return r;
				}
			}
			return null;
		}
	
	
	};
	
	public final MAP_OBJECT<Faction> faction = new MAP_OBJECT<Faction>() {
		@Override
		public Faction get(int tile) {
			Region reg = map.get(tile);
			if (reg != null)
				return reg.faction();
			return null;
		}

		@Override
		public Faction get(int tx, int ty) {
			if (!WORLD.IN_BOUNDS(tx, ty))
				return null;
			return get(tx+ty*TWIDTH());
		}
	};
	
	public final void renderBorders(Renderer r, RenderIterator it) {
		
		if (!border().is(it.tile()))
			return;
		

		Region a = map.get(it.tile());
		if (a != null) {
			int m = 0;
			for (DIR d : DIR.ORTHO) {
				if (!IN_BOUNDS(it.tx(), it.ty(), d)) {
					m |= d.mask();
					continue;
				}
				if (faction.get(it.tx(), it.ty(), d) == a.faction()) {
					m |= d.mask();
					
				}
			}
			int c = 0;
			for (DIR d : DIR.NORTHO) {
				if (!IN_BOUNDS(it.tx(), it.ty(), d)) {
					continue;
				}
				if (faction.get(it.tx(), it.ty(), d) != a.faction() && faction.get(it.tx(), it.ty(), d.next(1)) == a.faction() && faction.get(it.tx(), it.ty(), d.next(-1)) == a.faction() ) {
					c |= d.mask();
					
				}
			}
			if (m != 0x0F || c != 0) {
				if (a.faction() == null)
					COLOR.WHITE35.bind();
				else
					a.faction().banner().colorBG().bind();
				OPACITY.O50.bind();
				SPRITES.cons().BIG.outline.render(r, m, c, it.x(), it.y());
				OPACITY.O75.bind();
				SPRITES.cons().BIG.dashed_hollow.render(r, m, c, it.x(), it.y());
			}
			OPACITY.unbind();
		}
		
		
	}
	

	
	
}
