package world.entity;

import java.io.IOException;

import game.debug.Profiler;
import init.constant.C;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Tree;
import util.rendering.ShadowBatch;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import world.WORLD;
import world.WORLD.WorldResourceManager;
import world.entity.army.WArmyConstructor;
import world.entity.caravan.Shipments;
import world.entity.haven.WHavens;
import world.map.regions.Region;

public class WEntities extends WORLD.WorldResource {

	private final ArrayListResize<WEntity> fast;
	private final ArrayListResize<WEntity> slow;
	private final _WEntityMap map;
	private final ArrayList<WEntity> tmp = new ArrayList<WEntity>(2056);
	private final ArrayList<WEntityConstructor<?>> constructors = new ArrayList<>(20);
	private final Rec rectmp = new Rec();

	public final Shipments caravans = new Shipments(constructors);
	public final WArmyConstructor armies = new WArmyConstructor(constructors);
	public final WHavens havens = new WHavens(constructors);
	private double slowUp = 0;
	
	private final Tree<WEntity> renderables = new Tree<WEntity>(2056) {

		@Override
		protected boolean isGreaterThan(WEntity current, WEntity cmp) {
			return current.getZ() < cmp.getZ();
		}
		
	};

	public WEntities(WORLD world) throws IOException{
		super("entities", "ENTS");
		fast = new ArrayListResize<WEntity>(1024, 64000);
		slow = new ArrayListResize<WEntity>(256, 64000);
		map = new _WEntityMap(WORLD.PWIDTH(), WORLD.PHEIGHT());

	}

	private final WorldResourceManager saver = new WorldResourceManager() {
		
		@Override
		public void save(FilePutter file) {
			for (WEntityConstructor<?> c : constructors)
				c.save(file);
			file.d(slowUp);
			file.i(fast.size() + slow.size());
			for (WEntity e : fast) {
				file.i(e.constructor().index);
				e.save(file);
				e.hitBox.save(file);
			}
			for (WEntity e : slow) {
				file.i(e.constructor().index);
				e.save(file);
				e.hitBox.save(file);
			}
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			for (WEntityConstructor<?> c : constructors)
				c.load(file);
			fast.clear();
			slow.clear();
			map.clear();
			slowUp = file.d();
			int am = file.i();

			for (int i = 0; i < am; i++) {
				WEntityConstructor<?> c = constructors.get(file.i());
				WEntity e = c.create();
				e = e.load(file);
				e.hitBox.load(file);
				WEntities.this.clear(e);
				e.index = c.fast ? fast.add(e) : slow.add(e);
				e.renderNext = null;
				e.regionNext = null;
				e.regionI = -1;
				map.add(e);
			}
		}
		
		@Override
		public void clear() {
			for (WEntityConstructor<?> c : constructors)
				c.clear();
			fast.clear();
			slow.clear();
			map.clear();
		}
		
		@Override
		public void generate(ACTION loadPrint) {
			clear();
			loadPrint.exe();
			new Generator();
		}

		@Override
		public LIST<PLACABLE> makePlacers(ToolManager tm) {
			ArrayListGrower<PLACABLE> res = new ArrayListGrower<>();
			res.add(new Placers(havens.types));
			return res;
		}
	};
	
	@Override
	public WorldResourceManager saver() {
		return saver;
	}


	private void clear(WEntity e) {
		e.index = -1;
		e.renderNext = null;
		e.regionNext = null;
		e.regionI = -1;
		e.gridX = -1;
		e.gridY = -1;
	}
	

	
	public boolean canAdd(boolean fast) {
		return fast ? this.fast.hasRoom() : this.slow.hasRoom();
	}
	
	void add(WEntity e) {
		if (e.index != -1)
			throw new RuntimeException();
		int i = e.constructor().fast ? fast.add(e) : slow.add(e);
		clear(e);
		e.index = i;
		map.add(e);

	}
	
	WEntity regFirst(Region reg) {
		return map.regFirst(reg);
	}

	void remove(WEntity e) {
		if (e.index == -1)
			throw new RuntimeException();
		map.remove(e);

		ArrayListResize<WEntity> ents = e.constructor().fast ? this.fast : slow;
		
		WEntity e2 = ents.remove(ents.size() - 1);

		if (e2 != e) {
			ents.replace(e.index, e2);
			e2.index = e.index;
		}
		clear(e);
	}


	@Override
	public void update(double ds, Profiler prof) {
		prof.logStart(this);
		
		for (WEntityConstructor<?> c : constructors) {
			c.update(ds);
		}
		
		for (int i = 0; i < fast.size(); i++) {
			WEntity e = fast.get(i);
			e.update(ds);
			if (!e.added())
				i--;
			else
				map.move(e);
		}
		
		
		
		if (slowUp > 10000) {
			slowUp -= 10000;
		}
		
		int from = (int) slowUp;
		slowUp += ds*0.1;
		int to = (int) slowUp;
		int am = to-from;
		for(int k = 0; k < am; k++) {
			
			if (slow.size() <= 0)
				break;
			
			int i = from + k;
			i %= slow.size();
			
			if (k != 0 && i == from)
				break;
			
			WEntity e = slow.get(i);
			e.update(ds);
			if (!e.added())
				i--;
			else
				map.move(e);
		}
		
		prof.logEnd(this);
	}

	public void fill(RECTANGLE area, LISTE<WEntity> result) {
		map.fill(area, result);
	}
	
	public LIST<WEntity> fill(RECTANGLE area) {
		tmp.clear();
		fill(area, tmp);
		return tmp;
	}
	
	public LIST<WEntity> fill(int x1, int x2, int y1, int y2) {
		tmp.clear();
		map.fill(x1, x2, y1, y2, tmp);
		return tmp;
	}
	
	public LIST<WEntity> fillTiles(int x1, int x2, int y1, int y2) {
		return fill(x1*C.TILE_SIZE, x2*C.TILE_SIZE, y1*C.TILE_SIZE, y2*C.TILE_SIZE);

	}
	
	public LIST<WEntity> fill(int x1, int y1) {
		tmp.clear();
		map.fill(x1, x1+1, y1, y1+1, tmp);
		return tmp;
	}
	
	public void fill(COORDINATE coo, LISTE<WEntity> result) {
		fill(coo.x(), coo.y(), result);
	}

	public void fill(int x, int y, LISTE<WEntity> result) {
		map.fill(x, y, result);
	}

	public WEntity getTallest(COORDINATE coo) {
		tmp.clear();
		fill(coo, tmp);
		WEntity tallest = null;
		double dist = Double.MAX_VALUE;
		for (WEntity e : tmp) {
			double d = coo.distance(e.body().cX(), e.body().cY());
			if (tallest == null || d < dist) {
				tallest = e;
				dist = d;
			}
		}
		return tallest;
	}

	public boolean areaIsClearOfEnts(RECTANGLE rec) {
		return fill(rec).isEmpty();
	}

	public LISTE<WEntity> getTempsAtTile(int tileX, int tileY, int tilesX, int tilesY) {

		tmp.clear();

		rectmp.set(tileX * C.TILE_SIZE, (tileX + tilesX) * C.TILE_SIZE, tileY * C.TILE_SIZE,
				(tileY + tilesY) * C.TILE_SIZE);

		fill(rectmp, tmp);

		return tmp;
	}

	public void renderBelowTerrain(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {
		
		
		
		offX = offX - renWindow.x1();
		offY = offY - renWindow.y1();

		

		for (WEntity e : tmp) {
			e.renderBelowTerrain(r, s, ds, e.body().x1() + offX, e.body().y1() + offY);
		}

	}

	public void renderAboveTerrain(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {

		renderables.clear();
		map.fill(renWindow, renderables);
		
		offX = offX - renWindow.x1();
		offY = offY - renWindow.y1();
		tmp.clear();
		
		
		while (renderables.hasMore()) {
			WEntity e = renderables.pollGreatest();
			e.handleFow();
			tmp.add(e);
			
		}
		for (WEntity e : tmp) {
			e.renderAboveTerrain(r, s, ds, e.body().x1() + offX, e.body().y1() + offY);
		}
	}
	
	public LIST<WEntity> allFast(){
		return fast;
	}
	
	public LIST<WEntity> allSlow(){
		return slow;
	}
	

	
}
