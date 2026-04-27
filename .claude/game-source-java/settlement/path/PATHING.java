package settlement.path;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;

import game.debug.Profiler;
import init.sprite.SPRITES;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.path.components.SCOMPONENTS;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentEdge;
import settlement.path.finders.SFINDERS;
import settlement.path.thread.FinderThread;
import settlement.room.main.throne.THRONE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_DOUBLE;
import util.data.BOOLEAN;
import util.data.BOOLEAN.BOOLEANImp;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.sett.IDebugPanelSett;

public final class PATHING extends SettResource {

	public final CostMethods coster = new CostMethods();
	public final SFINDERS finders = new SFINDERS();
	
	private BOOLEAN_MUTABLE performanceTest = new BOOLEAN.BOOLEANImp(false);

	public final PlayerHuristics huristics = new PlayerHuristics();

	
	public final SCOMPONENTS comps = new SCOMPONENTS();
	public final AvailabilityMap availability = new AvailabilityMap(comps);
	public final FinderThread thread = new FinderThread(comps);
	public PATHING() {
		super("PATHING", true);
		new ON_TOP_RENDERABLE() {
			{
				IDebugPanelSett.add("availability", new BOOLEANImp() {
					@Override
					public BOOLEAN_MUTABLE set(boolean bool) {
						if (bool)
							add();
						else
							remove();
						return super.set(bool);
						
					}

					@Override
					public boolean is() {
						// TODO Auto-generated method stub
						return false;
					}
				});
			}
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				
				RenderData.RenderIterator i = data.onScreenTiles();
				COLOR.WHITE65.bind();
				while(i.has()) {
					if (cost.get(i.tile()) < 0)
						COLOR.RED100.bind();
					else if (cost.get(i.tile()) == 1)
						COLOR.BLUE100.bind();
					else
						COLOR.YELLOW100.bind();
					SPRITES.cons().BIG.dashed.render(r, 0x0F, i.x(), i.y());
					i.next();
				}
				COLOR.unbind();
				
			}
		};
		

		
		IDebugPanelSett.add("2100 paths/s", performanceTest);
	}
	
	public SFINDERS finders() {
		return finders;
	}
	
	@Override
	protected void save(FilePutter saveFile) {
		thread.stop();
		huristics.saver.save(saveFile);
		thread.start();
		finders.saver.save(saveFile);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		thread.stop();
		huristics.saver.load(saveFile);
		finders.saver.load(saveFile);
	}
	
	@Override
	protected void clear() {
		thread.stop();
		huristics.saver.clear();
		comps.clear();
		finders.saver.clear();
	}
	
//	private Coo start = new Coo();
	
	public boolean willUpdateTile(int tx, int ty) {
		return comps.zero.updating().is(tx, ty);
	}
	
	public boolean willUpdate() {
		return comps.zero.uping();
	}
	
	
	@Override
	protected void update(double ds, Profiler profiler) {
		thread.setStop();
		huristics.update(ds);
		finders.update(ds);
		thread.stop();
		comps.update();
		thread.start();
	}
	
	@Override
	protected void init(boolean loaded) {
		thread.stop();
		availability.init();
		comps.init();
		thread.start();
	}
	

	/**
	 * Answers if a specific tile is solid from the players viewpoint
	 */
	public final MAP_BOOLEAN solidity = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tile) {
			return availability.get(tile).player < 0; 
		}

		@Override
		public boolean is(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return true;
			return is(tx+ty*TWIDTH);
		}
	};
	
	/**
	 * Answers if a specific tile is reachable from the super components
	 */
	public final MAP_BOOLEAN reachability = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			SComponent c = comps.superComp.get(tx, ty);
			if (c != null && c.is(THRONE.coo()))
				return true;;
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				c = comps.superComp.get(tx, ty, DIR.ORTHO.get(i));
				if (c != null && c.is(THRONE.coo())) {
					return true;
				}
			}
			return false;
		};
		
		@Override
		public boolean is(int tile) {
			throw new RuntimeException();
		}
	};
	
	
	/**
	 * Answers if a specific tile is connected to the super components
	 */
	public final MAP_BOOLEAN connectivity = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			SComponent c = comps.superComp.get(tx, ty);
			if (c != null && c.is(THRONE.coo()))
				return true;
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%TWIDTH, tile/TWIDTH);
		}
	};
	
	
	public AVAILABILITY getAvailability(int x, int y) {
		if (!IN_BOUNDS(x, y))
			return AVAILABILITY.SOLID;
		return availability.get(x, y);
	}
	

	
	/**
	 * The player cost of the current tile
	 */
	public final MAP_DOUBLE cost = new MAP_DOUBLE() {
		
		@Override
		public double get(int tile) {
			return availability.get(tile).player; 
		}

		@Override
		public double get(int tx, int ty) {
			AVAILABILITY a = availability.get(tx, ty);
			if (a == null)
				return -1;
			return availability.get(tx, ty).player; 
		}
	};
	
	public boolean isInTheNeighbourhood(int tx, int ty, int dx, int dy) {
		SComponent c = comps.levels.get(0).get(tx, ty);
		if (c == null)
			return false;
		SComponent d = comps.levels.get(0).get(dx, dy);
		if (d == null)
			return false;
		if (c == d)
			return true;
		SComponentEdge e = c.edgefirst();
		while (e != null) {
			if (e.to() == d)
				return true;
			e = e.next();
		}
		return false;
	}



	

	
}
