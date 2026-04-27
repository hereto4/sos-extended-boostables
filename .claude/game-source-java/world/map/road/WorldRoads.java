package world.map.road;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.REGIONS;
import static world.WORLD.TAREA;
import static world.WORLD.TBOUNDS;
import static world.WORLD.TWIDTH;

import java.io.IOException;

import init.constant.C;
import init.constant.Config.ConfigWorld;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.Bitsmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.RenderData.RenderIterator;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import world.WORLD;
import world.WORLD.WorldError;
import world.WORLD.WorldResource;
import world.WORLD.WorldResourceManager;
import world.WRenContext;
import world.map.regions.Region;
import world.region.RD;

public final class WorldRoads extends WorldResource implements MAP_BOOLEANE{
	
	private COLOR[] rColors;
	
	private final Bitmap2D is = new Bitmap2D(TBOUNDS(), false);
	private final Bitmap2D mini = new Bitmap2D(TBOUNDS(), false);
	public final Bitmap2D bridge = new Bitmap2D(TBOUNDS(), false);
	private final Bitsmap1D data = new Bitsmap1D(0, 4, TAREA());
	private final Bitmap2D miniHack = new Bitmap2D(TBOUNDS(), false);

	public WorldRoads(WORLD data2) {
		super("roads", "ROADS");
		Json j = ConfigWorld.json("Road");
		ColorImp low = new ColorImp(j, "COLOR_SMALL");
		ColorImp hi = new ColorImp(j, "COLOR_BIG");
		rColors = COLOR.interpolate(low, hi, 16);
	}
	


	
	public void render(WRenContext data, RenderIterator it) {
		
		if (!is.is(it.tile()))
			return;
		
		if (miniHack.is(it.tile())) {
			int m = 0;
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				if (is.is(it.tx(), it.ty(), DIR.ORTHO.get(di)) && mini.is(it.tx(), it.ty(), DIR.ORTHO.get(di))) {
					m |= DIR.ORTHO.get(di).mask();
					
				}
			}
			rColors[0].bind();
			WORLD.BUILDINGS().sprites.roadsMini.render(data.r,
					m + 16 * (it.ran() & 0b0111), it.x(), it.y());
		}
		
		double d = levelRoad(it.tile());
		TILE_SHEET sheet = WORLD.BUILDINGS().sprites.roads;
		if (mini.is(it.tile())) {
			sheet = WORLD.BUILDINGS().sprites.roadsMini;
			d *= 0.25;
		}else
			d = 0.75 + 0.25*d;
		d = CLAMP.d(d, 0, 1);
		rColors[(int) (d*15)].bind();
		sheet.render(data.r,
				this.data.get(it.tile()) + 16 * (it.ran() & 0b0111), it.x(), it.y());
		

		
		COLOR.unbind();
	}
	
	private double levelRoad(int tile) {
		Region reg = REGIONS().map.get(tile);
		if (reg != null) {
			return RD.BUILDINGS().levelRoad.get(reg);
		}
		return 0;
	}
	
	private final DIR[] dds = new DIR[] {DIR.N, DIR.E};
	
	public void renderBridge(WRenContext con, RenderIterator it) {
		
		if (!is.is(it.tile()))
			return;
		
		if (mini.is(it.tile()))
			return;
		
		if (!WORLD.WATER().isBig.is(it.tile()))
			return;
		

		
		if (bridge.is(it.tile())) {
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (!WORLD.WATER().isBig.is(it.tx(), it.ty() , d) && !WORLD.WATER().isBig.is(it.tx(), it.ty() , d.perpendicular())) {
					if (WORLD.PATH().map.isOnly(it.tx(), it.ty(), d)) {
						int level = (int)(levelRoad(it.tile())*4);
						WORLD.BUILDINGS().sprites.bridge.render(con.r, level+ di, it.x(), it.y());
						return;
					}
					
				}
			}
		}
		
		if (WORLD.REGIONS().cTile.is(it.tile()))
			return;
		
		int data = this.data.get(it.tile());
		con.s.setDistance2Ground(0).setHeight(1);
		DIR d = pDir(data).perpendicular();
		int x = it.x() - 4*C.SCALE;
		int y = it.y() - 4*C.SCALE;
		int tile = d.orthoID()*16 + (it.ran()&0b11);
		
		
		if (Integer.bitCount(data) != 1) {
			tile += 3*4;
		}else {
			Region r = WORLD.REGIONS().map.get(it.tile());
			if (r == null)
				tile += 2*4;
			
			tile += 3*(1.0-RD.BUILDINGS().levelRoad.get(r));
			
		}
		
		WORLD.BUILDINGS().sprites.harbour.render(con.r, tile, x, y);
		WORLD.BUILDINGS().sprites.harbour.render(con.s, tile, x, y);
		
	}
	
	private DIR pDir(int data) {
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.get(i);
			if ((data & d.mask()) != 0)
				return d;
		}
		return DIR.N;
	}

	@Override
	public boolean is(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return is(tx + ty * TWIDTH());
		return false;
	}

	@Override
	public boolean is(int tile) {
		return is.is(tile);
	}
	
	@Override
	public MAP_BOOLEANE set(int tile, boolean value) {
		int tx = tile % TWIDTH();
		int ty = tile / TWIDTH();
		return set(tx, ty, value);
	}

	@Override
	public MAP_BOOLEANE set(int tx, int ty, boolean value) {
		if (!WORLD.IN_BOUNDS(tx, ty))
			return this;
		
		bridge.set(tx, ty, false);
		
		if (value == is(tx, ty) && !mini.is(tx, ty))
			return this;
		
		mini.set(tx, ty, false);
		
		is.set(tx, ty, value);
		
		fix(tx, ty);
		
		return this;
	}
	
	private void fix(int tx, int ty) {
		for (int di = 0; di < DIR.ORTHOC.size(); di++) {
			DIR d = DIR.ORTHOC.get(di);
			pfix(tx+d.x(), ty+d.y());
		}
	}
	
	private void pfix(int tx, int ty) {
		if (!is.is(tx, ty))
			return;
		
		int m = 0;
		boolean min = mini.is(tx, ty);
		boolean hack = false;
		
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.get(i);
			int dx = d.x() + tx;
			int dy = d.y() + ty;


			if (is.is(dx, dy)) {
				
				if (min != mini.is(dx, dy)) {
					hack = true;
				}else {
					m |= d.mask();
				}
			}
		}
		data.set(tx + ty * TWIDTH(), m);
		this.miniHack.set(tx, ty, hack);
	}

	public final MAP_BOOLEAN harbour = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return is.is(tx, ty) && WORLD.WATER().isBig.is(tx, ty) && !mini.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return is.is(tile) && WORLD.WATER().isBig.is(tile) && !mini.is(tile);
		}

	};
	
	public final MAP_BOOLEAN isBig = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return is.is(tx, ty) && !mini.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return is.is(tile) && !mini.is(tile);
		}
	
	};
	
	public final MAP_BOOLEANE minified = new MAP_BOOLEANE() {
		
		@Override
		public boolean is(int tx, int ty) {
			return is.is(tx, ty) && mini.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return is.is(tile) && mini.is(tile);
		}
		
		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			if (is.is(tx, ty)) {
				mini.set(tx, ty, value);
				fix(tx, ty);
			}
			return this;
		}
		
		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			int tx = tile % TWIDTH();
			int ty = tile / TWIDTH();
			return set(tx, ty, value);
		}
	};
	
	public final MAP_BOOLEAN placable = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (WORLD.MOUNTAIN().coversTile(tx, ty))
				return false;
			
			if (WORLD.WATER().isBig.is(tx, ty)) {
				
				if (!hasLand(tx, ty))
					return false;
				
				for (DIR d : dds) {
					if (WORLD.IN_BOUNDS(tx, ty, d) && WORLD.IN_BOUNDS(tx, ty, d.perpendicular()) && WORLD.WATER().isBig.is(tx, ty, d) && WORLD.WATER().isBig.is(tx, ty, d.perpendicular()))
						return true;
					
				}
				return false;
			}
			
			return true;
		}
		
		@Override
		public boolean is(int tile) {
			int tx = tile % TWIDTH();
			int ty = tile / TWIDTH();
			return is(tx, ty);
		}
	};
	
	public final MAP_BOOLEAN canBridge = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (WORLD.MOUNTAIN().coversTile(tx, ty))
				return false;
			
			if (!WORLD.WATER().isBig.is(tx, ty))
				return false;
			
			if (!hasLand(tx, ty))
				return false;
			
			for (DIR d : dds) {
				if (WORLD.IN_BOUNDS(tx, ty, d) && WORLD.WATER().isBig.is(tx, ty, d)) {
					d = d.perpendicular();
					if (WORLD.IN_BOUNDS(tx, ty, d) && WORLD.WATER().isBig.is(tx, ty, d)) {
						return true;
					}
				}
			}
			return false;
		}
		

		
		@Override
		public boolean is(int tile) {
			int tx = tile % TWIDTH();
			int ty = tile / TWIDTH();
			return is(tx, ty);
		}

	};
	
	private boolean hasLand(int tx, int ty) {
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			if (WORLD.IN_BOUNDS(tx, ty, d) && !WORLD.WATER().isBig.is(tx, ty, d)) {
				return true;
			}
		}
		return false;
	}

	private final WorldResourceManager saver = new WorldResourceManager() {
		
		final Placer placers = new Placer();
		@Override
		public void save(FilePutter f) {
			is.save(f);
			mini.save(f);
			data.save(f);
			miniHack.save(f);
			bridge.save(f);
		}

		@Override
		public void load(FileGetter f) throws IOException {
			is.load(f);
			mini.load(f);
			data.load(f);
			miniHack.load(f);
			bridge.load(f);
		}
		
		@Override
		public void clear() {
			is.clear();
			mini.clear();
			data.clear();
			miniHack.clear();
			bridge.clear();
		}
		
		@Override
		public void validateInit(WorldError error) {
			
			if (!WORLD.IN_BOUNDS(WORLD.REGIONS().player.cx(), WORLD.REGIONS().player.cy())) {
				error.problem = "The world has no player region centre";
				error.coo.set(WORLD.TBOUNDS().cX(), WORLD.TBOUNDS().cY());
				return;
			}
			

			
		};
		
		@Override
		public void generate(ACTION loadPrint) {
			clear();
			new Gen().generateAll(WORLD.REGIONS().player.cx(), WORLD.REGIONS().player.cy(), loadPrint);;
			
		};
		
		@Override
		public LIST<PLACABLE> makePlacers(ToolManager tm) {
			return placers.placers;
		};
		
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}
	


	
}
