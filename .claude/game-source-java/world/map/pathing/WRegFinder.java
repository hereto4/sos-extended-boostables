package world.map.pathing;

import game.GAME;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.GUTIL;
import util.data.BOOLEANO;
import util.data.BOOLEANO.BOOLEAN_OE;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import world.WORLD;
import world.map.pathing.Comps.WComp;
import world.map.regions.Region;
import world.map.regions.WREGIONS;

public class WRegFinder{
	
	private final RegDist[] regs = new RegDist[WREGIONS.MAX];
	private final ArrayList<RegDist> li = new ArrayList<WRegFinder.RegDist>(WREGIONS.MAX);
	
	private int upI = -1;
	private Treaty lastTreaty = null;
	private BOOLEANO<Region> lastSelector = null;
	private int lx,ly;
	
	public WRegFinder(){
		for (int i = 0; i < regs.length; i++)
			regs[i] = new RegDist();
	}

	public LIST<RegDist> all(Faction f, Treaty trav, WRegSel selector) {
		return all(f.capitolRegion().cx(), f.capitolRegion().cy(), trav, selector);
	}
	
	public LIST<RegDist> all(Region home, Treaty trav, WRegSel selector) {
		return all(home.cx(), home.cy(), trav, selector);
	}
	
	public LIST<RegDist> all(int tx, int ty, Treaty treaty, WRegSel selector) {
		
		if (upI == GAME.updateI() && tx == lx && ty == ly && lastTreaty == treaty && selector == lastSelector)
			return li;
		
		upI = GAME.updateI();
		lastTreaty = treaty;
		lastSelector = selector;
		lx = tx;
		ly = ty;
		
		final Region origin = reg(tx, ty);
		
		
		li.clearSloppy();
		
		LIST<PathTile> ll = WORLD.PATH().comps.finder.getComps(tx, ty);
		
		Flooder f = GUTIL.flooder();
		f.init(f);
		for (PathTile t : ll) {
			t = f.pushSloppy(t, t.getValue());
			isWater.set(t, false);
			prevReg.set(t, origin);
		}
		
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			if (t.getParent() != null) {
				t.setValue2(t.getParent().getValue2());
				if (WORLD.WATER().isBig.is(t) && WORLD.ROADS().harbour.is(t)) {
					isWater.set(t, true);
				}
			}
			
			Region from = reg(t.x(), t.y());
			
			if (from != null && t.isSameAs(from.cx(), from.cy()) && selector.is(from)) {
				RegDist rr = regs[from.index()];
				rr.reg = from;
				rr.distance =  (int) t.getValue();
				rr.water = isWater.is(t);
				li.add(rr);
			}
			
			if (from != null) {
				prevReg.set(t, from);
			}else {
				from = prevReg.get(t);
			}
			
			WComp c = WORLD.PATH().comps.get(t);
			for (int i = 0; i < c.neighs(); i++) {
				WComp to = c.neigh(i);
				
				Region rto = reg(to.x(), to.y());
				
				double v = t.getValue() + c.dist(i);
				if (treaty.can(origin, from, rto, to.x(), to.y(), v))
					f.pushSmaller(to.x(), to.y(), v, t);
			}
			
			
			
		}
		f.done();
		return li;
	}
	
	public RegDist single(int tx, int ty, Treaty treaty, WRegSel selector) {
		
		final Region origin = reg(tx, ty);
		upI = -1;
		
		
		LIST<PathTile> ll = WORLD.PATH().comps.finder.getComps(tx, ty);
		
		Flooder f = GUTIL.flooder();
		f.init(f);
		for (PathTile t : ll) {
			t = f.pushSloppy(t, t.getValue());
			isWater.set(t, false);
			prevReg.set(t, origin);
		}
		
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			if (t.getParent() != null) {
				t.setValue2(t.getParent().getValue2());
				if (WORLD.WATER().isBig.is(t) && WORLD.ROADS().harbour.is(t)) {
					isWater.set(t, true);
				}
			}
			
			Region from = reg(t.x(), t.y());
			
			if (from != null && t.isSameAs(from.cx(), from.cy()) && selector.is(from)) {
				RegDist rr = regs[from.index()];
				rr.reg = from;
				rr.distance =  (int) t.getValue();
				rr.water = isWater.is(t);
				f.done();
				return rr;
			}
			
			if (from != null) {
				prevReg.set(t, from);
			}else {
				from = prevReg.get(t);
			}
			
			WComp c = WORLD.PATH().comps.get(t);
			for (int i = 0; i < c.neighs(); i++) {
				WComp to = c.neigh(i);
				
				Region rto = reg(to.x(), to.y());
				
				double v = t.getValue() + c.dist(i);
				if (treaty.can(origin, from, rto, to.x(), to.y(), v))
					f.pushSmaller(to.x(), to.y(), v, t);
			}
			
			
			
		}
		f.done();
		return null;
	}
	
	private Region reg(int tx, int ty) {
		return WORLD.PATH().regMap.get(tx, ty);
	
	}
	
	private final GETTER_TRANSE<PathTile, Region> prevReg = new GETTER_TRANSE<PathTile, Region>() {
		
		@Override
		public Region get(PathTile f) {
			int i = (int) f.getValue2();
			i = i >> 4;
			if (i <= 0 || i > WREGIONS.MAX)
				return null;
			return WORLD.REGIONS().all().get(i-1);
		}
		
		@Override
		public void set(PathTile f, Region t) {
			int i = t == null ? -1 : t.index();
			i++;
			int v = (int) f.getValue2();
			v &= 0b01111;
			v |= (i << 4);
			f.setValue2(v);
			
		}
	};
	
	private final BOOLEAN_OE<PathTile> isWater = new BOOLEAN_OE<PathTile>() {
		
		@Override
		public boolean is(PathTile t) {
			int i = (int) t.getValue2();
			return (i & 1) == 1;
		}
		
		@Override
		public BOOLEAN_OE<PathTile> set(PathTile t, boolean b) {
			int v = (int) t.getValue2();
			v &= ~0b1;
			if (b)
				v |= 1;
			t.setValue2(v);
			return this;
		}
	};
	
	public static final class RegDist {
		
		public Region reg;
		public int distance;
		public boolean water;
		
		private RegDist() {
			
		}
		
	}

	public static abstract class Treaty {
		
		public Treaty() {

		}

		public abstract boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist);
		
		/**
		 * returns the neighbours to the origin region.
		 */
		public static final Treaty REG_NEIGHS = new Treaty() {
			
			@Override
			public boolean can(Region origin, Region prevReg, Region to, int tx, int ty, double dist) {
				if (prevReg == null)
					return true;
				if (prevReg == origin)
					return true;
				return prevReg == to;
			}
		};
		
		/**
		 * gives you regions that are diplomatic reachable from the origin's faction's perspective. 
		 * Will return neighbouring kingdoms. 
		 * Also kingdoms that that can be reached through the neighbouring kingdoms that have transit agreements.
		 * If the origin faction is null, it will get the neighbouring factions that are null.
		 */
		public static final Treaty FACTION_REACHABLE = new Treaty() {
			
			@Override
			public boolean can(Region origin, Region from, Region to, int tx, int ty, double dist) {
				if (origin == null)
					return false;
				if (from == null)
					return false;
				if (to == null)
					return true;
				Faction o = origin.faction();
				
				if (o == null) {
					return to.faction() == null;
				}
				
				if (to.faction() == null)
					return false;
				
				if (from.faction() == origin.faction())
					return true;
				
				if (from.faction() == to.faction())
					return true;

				if (origin.faction() == null)
					return false;
				
				if (DIP.get(origin.faction(), from.faction()).transit)
					return true;
				return false;
			}
		};
		
		public static final Treaty FACTION_CAN_ATTACK = new Treaty() {
			
			@Override
			public boolean can(Region origin, Region from, Region to, int tx, int ty, double dist) {
				if (origin == null)
					return false;
				Faction o = origin.faction();
				if (o == null) {
					return false;
				}
				
				if (from == null)
					return true;
				
				if (from.faction() == null)
					return false;
				
				if (from.faction() == o) {
					return true;
				}
				
				if (DIP.get(o, from.faction()).ally) {
					return true;
				}
				
				if (to == null)
					return false;
				
				if (to.faction() == null)
					return false;
				
				return from.faction() == to.faction();
				
			}
		};
		
		/**
		 * 
		 */
		public static final Treaty FACTION_REACHABLE_NPC_TRADE = new Treaty() {
			
			@Override
			public boolean can(Region origin, Region from, Region to, int tx, int ty, double dist) {
				if (origin == null)
					return false;
				if (to == null)
					return true;
				Faction o = origin.faction();
				
				if (o == null) {
					return to.faction() == null;
				}
				
				if (to.faction() == null)
					return from.faction() == null || from.faction() == o;
				else {
					if (from.faction() == null || from.faction() == o)
						return true;
				}
				
				if (from.faction() == origin.faction())
					return true;
				
				if (from.faction() == to.faction())
					return true;
				
				
				return false;
			}
		};
		
		/**
		 * regions that surrounds a kingdom. Will not yield the factions regions that are isolated from the origin
		 */
		public static final Treaty FACTION_BORDERS = new Treaty() {
			
			@Override
			public boolean can(Region origin, Region from, Region to, int tx, int ty, double dist) {
				if (origin == null)
					return false;
				if (from == null)
					return false;
				
				if (to == null)
					return true;
				if (from == to)
					return true;
				
				Faction o = origin.faction();
				if (from.faction() == o)
					return true;
				
				return false;
			}
		};
		
		/**
		 * regions that belong to the faction
		 */
		public static final Treaty FACTION = new Treaty() {
			
			@Override
			public boolean can(Region origin, Region from, Region to, int tx, int ty, double dist) {
				if (origin == null)
					return false;
				if (from == null)
					return false;
				
				if (to == null)
					return true;
				if (from == to)
					return true;
				
				Faction o = origin.faction();
				if (from.faction() == o && to.faction() == o)
					return true;
				
				return false;
			}
		};
		
		/**
		 * returns all regions.
		 */
		public static final Treaty DUMMY = new Treaty() {
			
			@Override
			public boolean can(Region origin, Region from, Region to, int tx, int ty, double dist) {
				return true;
			}
		};
		
	}
}
