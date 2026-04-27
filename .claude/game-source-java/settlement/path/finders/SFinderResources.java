package settlement.path.finders;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THINGS;

import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResGroup;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.path.components.SComponent;
import settlement.path.path.SPath;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.room.main.throne.THRONE;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsResources.ScatteredResource;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GButt;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimpleTile;

public final class SFinderResources {

	public final Normal normal = new Normal();
	public final Scattered scattered = new Scattered();

	private final RBITImp bscattered = new RBITImp();
	private final RBITImp bstored = new RBITImp();
	private final RBITImp bprio = new RBITImp();
	SFinderResources() {
		
		IDebugPanelSett.add("Unreserve everything", new ACTION() {
			
			@Override
			public void exe() {
				for (COORDINATE c : new Rec(SETT.TILE_BOUNDS)) {
					
					
					
					while(unres(c))
						;
					
					
					
					Room room = ROOMS().map.get(c.x(), c.y());
					if (room != null) {
						RESOURCE_TILE res = room.resourceTile(c.x(), c.y());
						while (res != null && res.findableReservedIs() && res.resource() != null) {
							res.findableReserveCancel();
						}
					}
					
					
				}
			}
			
			private boolean unres(COORDINATE c) {
				for (Thing t : THINGS().get(c.x(), c.y())) {
					if (t instanceof ScatteredResource) {
						ScatteredResource sc = ((ScatteredResource) t);
						if (sc.findableReservedIs() && sc.resource() != null) {
							sc.findableReserveCancel();
							return true;
						}
					}
				}
				return false;
			}
		});
		
		IDebugPanelSett.add(new PlacableSimpleTile("find resource") {
			
			RBITImp bits = new RBITImp();
			LIST<CLICKABLE> li;
			{
				GuiSection s = new GuiSection();
				for (RESOURCE r : RESOURCES.ALL()) {
					s.addGrid(new GButt.ButtPanel(r.icon()) {
						@Override
						protected void clickA() {
							bits.toggle(r);
						};
						@Override
						protected void renAction() {
							selectedSet(bits.has(r));
						};
					}, r.index(), 10, 0, 0);
				}
				li = new ArrayList<CLICKABLE>(s);
			}
			
			@Override
			public void place(int tx, int ty) {
				SPath p = new SPath();
				RESOURCE res = find(bits, new Coo(tx, ty), p, 250);
				
				if (res == null) {
					LOG.ln("nope");
				}else {
					LOG.ln(p.destX() + " " + p.destY());
					RESOURCE_TILE.GETTER.reserved(res, p.destX(), p.destY());
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return null;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return li;
			}
		});
		
	}
	
	public boolean has(int sx, int sy, RBIT bits) {
		return has(sx, sy, bits, bits, bits);
	}
	
	public boolean has(int sx, int sy, RBIT scattered, RBIT stored, RBIT prio) {
		return PATH().comps.data.resScattered.has(sx, sy, scattered) || 
				PATH().comps.data.resCrate.has(sx, sy, stored) || PATH().comps.data.resPriority.has(sx, sy, prio);
	}
	
	public RESOURCE find(RBIT scattered, RBIT stored, RBIT prio, COORDINATE start, SPath path, int maxdistance) {
		return find(scattered, stored, prio, start.x(), start.y(), path, maxdistance);
	}
	
	public RESOURCE find(RBIT bits, COORDINATE start, SPath path, int maxdistance) {
		return find(bits, start.x(), start.y(), path, maxdistance);
	}
	
	public RESOURCE find(RBIT bits, int sx, int sy, SPath path, int maxdistance) {
		return find(bits,bits,bits, sx, sy, path, maxdistance);
	}
	
	public RESOURCE find(RBIT scattered, RBIT stored, RBIT prio, int sx, int sy, SPath path, int maxdistance) {

		if (has(sx, sy, scattered, stored, prio)) {
			this.bscattered.clearSet(scattered);
			this.bstored.clearSet(stored);
			this.bprio.clearSet(prio);
			
			if (path.request(sx, sy, finder, maxdistance)) {
				RESOURCE_TILE t = RESOURCE_TILE.GETTER.reservable(bscattered, bstored, bprio, path.destX(), path.destY());
				t.findableReserve();
				return t.resource();
			}
			
		}
		
		return null;
	}
	
	public RESOURCE_TILE find(RBIT scattered, RBIT stored, RBIT prio, RoomInstance ins, int maxdistance) {

		if (has(ins.mX(), ins.mY(), scattered, stored, prio)) {
			this.bscattered.clearSet(scattered);
			this.bstored.clearSet(stored);
			this.bprio.clearSet(prio);
			
			COORDINATE r = SETT.PATH().finders.finder().findDest(ins, finder, maxdistance);
			if (r != null)
				return RESOURCE_TILE.GETTER.reservable(bscattered, bstored, bprio, r.x(), r.y());
			
		}
		
		return null;
	}
	
	private SFINDER finder = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return PATH().comps.data.resScattered.has(c, bscattered)
					|| PATH().comps.data.resCrate.has(c, bstored) || PATH().comps.data.resPriority.has(c, bprio);
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			return RESOURCE_TILE.GETTER.reservable(bscattered, bstored, bprio, tx, ty) != null;
		}
	};
	
	public int reserveExtra(boolean stored, boolean fetch, RESOURCE r, int tx, int ty, int amount) {
		return RESOURCE_TILE.GETTER.reserve(stored, fetch, r, tx, ty, amount);
	}
	
	public boolean isReservedAndAvailable(RESOURCE r, int x, int y) {
		return RESOURCE_TILE.GETTER.reserved(r, x, y) != null;
	}
	
	public final int pickup(RESOURCE r, int tx, int ty, int amount) {
		return RESOURCE_TILE.GETTER.pickup(r, tx, ty, amount);
	}

	
	public final void unreserve(RESOURCE r, int tx, int ty, int amount) {
		RESOURCE_TILE.GETTER.unreserve(r, tx, ty, amount);
	}
	
	public void reportPresence(RESOURCE_TILE r) {
		if (!r.isFindable())
			return;
		if (r.isPrio()) {
			PATH().comps.data.resPriority.reportPresence(r.x(), r.y(), r.resource());
		}else if(r.isStorage()) {
			PATH().comps.data.resCrate.reportPresence(r.x(), r.y(), r.resource());
		}else {
			PATH().comps.data.resScattered.reportPresence(r.x(), r.y(), r.resource());
		}
		
	}
	
	public void reportAbsence(RESOURCE_TILE r) {
		if (!r.isFindable())
			return;
		if (r.isPrio()) {
			PATH().comps.data.resPriority.reportAbsence(r.x(), r.y(), r.resource());
		}else if(r.isStorage()) {
			PATH().comps.data.resCrate.reportAbsence(r.x(), r.y(), r.resource());
		}else {
			PATH().comps.data.resScattered.reportAbsence(r.x(), r.y(), r.resource());
		}
	}

	
	public class Normal {
		
		private Normal() {
			
		}
		
		public boolean has(int sx, int sy, RESOURCE r) {
			return has(sx, sy, r.bit);
		}
		
		public boolean has(int sx, int sy, ResGroup<?> group) {
			return has(sx, sy, group.mask);
		}
		
		public boolean has(int sx, int sy, RBIT mask) {
			return SFinderResources.this.has(sx, sy, mask, mask, mask);
		}
		
		public boolean reserve(COORDINATE start, RESOURCE r, SPath path, int maxdistance) {
			return reserve(start, r.bit, path, maxdistance) != null;
		}
		
		public RESOURCE reserve(COORDINATE start, RBIT mask, SPath path, int maxdistance) {
			return SFinderResources.this.find(mask, mask, mask, start, path, maxdistance);
		}

		public int reserveExtra(RESOURCE r, int x, int y, int amount) {
			return SFinderResources.this.reserveExtra(true, true, r, x, y, amount);
		}

		public boolean has(RESOURCE r) {
			return has(THRONE.coo().x(),THRONE.coo().y(), r);
		}

		

		
	}
	

	
	public class Scattered {
		
		private Scattered() {
			
		}
		
		public boolean has(RESOURCE r) {
			return has(THRONE.coo().x(),THRONE.coo().y(), r);
		}
		
		public boolean has(int sx, int sy, RESOURCE r) {
			return has(sx, sy, r.bit);
		}
		
		public boolean has(int sx, int sy, RBIT mask) {
			return SFinderResources.this.has(sx, sy, mask, RBIT.NONE, RBIT.NONE);
		}
		
		public boolean reserve(COORDINATE start, RESOURCE r, SPath path, int maxdistance) {
			return reserve(start, r.bit, path, maxdistance) != null;
		}
		
		public RESOURCE reserve(COORDINATE start, RBIT resMask, SPath path, int maxdistance) {
			return reserve(start.x(), start.y(), resMask, path, maxdistance);
		}
		
		public RESOURCE reserve(int sx, int sy, RBIT resMask, SPath path, int maxdistance) {
			return SFinderResources.this.find(resMask, RBIT.NONE, RBIT.NONE, sx, sy, path, maxdistance);
		}

		public int reserveExtra(RESOURCE r, int x, int y, int amount) {
			return SFinderResources.this.reserveExtra(false, false, r, x, y, amount);
		}
		
	}



}
