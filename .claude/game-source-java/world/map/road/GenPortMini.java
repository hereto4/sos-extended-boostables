package world.map.road;

import java.util.Arrays;

import init.sprite.SPRITES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap2D;
import util.rendering.RenderData.RenderIterator;
import util.GUTIL;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.overlay.WorldOverlays;

final class GenPortMini {

	private final Bitmap2D debug = new Bitmap2D(WORLD.TBOUNDS(), false);
	private final ACTION util;
	private final ArrayList<Reg> regs = new ArrayList<>(WREGIONS.MAX);
	private final ArrayListGrower<Port> ports = new ArrayListGrower<Port>();
	private final MAP_BOOLEANE marked;
	
	GenPortMini(ACTION util, MAP_BOOLEANE marked){
		this.util = util;
		this.marked = marked;
		WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {
			
			@Override
			protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
				if (marked.is(it.tile())) {
					COLOR.BLUEISH.bind();
					SPRITES.cons().ICO.crosshair.render(r, it.x(), it.y());
					COLOR.unbind();
				}else if (debug.is(it.tile())) {
					COLOR.ORANGE100.bind();
					SPRITES.cons().BIG.line.render(r, 0, it.x(), it.y());
					COLOR.unbind();
				}
			}
		};
		
		for (Region r : WORLD.REGIONS().all()) {
			regs.add(new Reg(r));
		}

		setLand();
		
		for (Port p : ports) {
			p.dists = new double[ports.size()];
			Arrays.fill(p.dists, Double.MAX_VALUE);
		}
		
		util.exe();
		

		
		setPortDists();
		util.exe();

		for (Port p : new ArrayList<Port>(ports)) {
			if (!isConnected(p)) {
				marked.set(p, false);
			}
		}

		
	}

	private void setLand(){
		
		Flooder f = GUTIL.flooder();
		f.init(this);

		for (Region r : WORLD.REGIONS().all()) {
			
			if (r.info.area() > 0) {
				f.pushSloppy(r.cx(), r.cy(), 0, null);
				f.setValue2(r.cx(), r.cy(), r.index());
			}
		}

		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			
			Region rr = WORLD.REGIONS().map.get(t);
			
			
			for (DIR d : DIR.ALL) {
				if (WTRAV.can(t.x(), t.y(), d, true)) {
					Region other = WORLD.REGIONS().map.get(t,d);
					if (other != rr) {
						continue;
					}
					f.pushSmaller(t,d, t.getValue()+d.tileDistance(), t);
				}
			}
		}
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (marked.is(c)) {
				Port p = new Port(c, map.get(c), 0, ports.size());
				map.get(c).ports.add(p);
				ports.add(p);
			}
			
			if (f.hasBeenPushed(c.x(), c.y())) {
				
				
				
				if (!WTRAV.LAND.isPossible(c.x(), c.y(), true))
					continue;
				else {
					int from = (int) f.getValue2(c.x(), c.y());
					for (DIR d : DIR.ALL) {
						if (f.hasBeenPushed(c.x(), c.y(), d) && WTRAV.canLand(c.x(), c.y(), d, true)){
							int regTo = (int) f.getValue2(c.x(), c.y(), d);
							if (from != regTo) {
								double dd = f.getValue(c) + f.getValue(c.x()+d.x(), c.y()+d.y());
								connect(f.get(c.x(), c.y()));
								regs.get(from).land.add(new Reg.Connect(regs.get(regTo), dd));
								regs.get(regTo).land.add(new Reg.Connect(regs.get(from), dd));
							}
						}
					}
				}
			}
		}
		
		f.done();
		
		
		for (Reg home : regs) {
			if (home.land.size() == 0)
				continue;
			if (home.reg.index() % 10 == 0) {
				util.exe();
			}
			f.init(this);
			f.pushSloppy(home.reg.cx(), home.reg.cy(), 0, null);
			
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				Reg o = map.get(t);
				o.dists[home.reg.index()] = t.getValue();
				home.dists[o.reg.index()] = t.getValue();
				for (Reg.Connect l : o.land) {
					f.pushSmaller(l.to.reg.cx(), l.to.reg.cy(),  t.getValue()+l.cost, t);
				}
			}
			f.done();
		}
	}
	
	private void connect(PathTile t) {
		while(t != null) {
			debug.set(t, true);
			t = t.getParent();
		}
	}
	
	private void setPortDists(){
		
		Flooder f = GUTIL.flooder();
		f.init(this);
		for (Port p : ports){
			f.pushSloppy(p.x(), p.y(), 0, null);
			f.setValue2(p, p.index);
		}

		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			
			
			for (DIR d : DIR.ALL) {
				if (WORLD.WATER().isBig.is(t, d) && WTRAV.can(t.x(), t.y(), d, false)) {
					
					double v = WORLD.WATER().coversTile.is(t, d) ? 1 : 3;
					f.pushSmaller(t,d, t.getValue()+v*d.tileDistance(), t);
				}
			}
		}

		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (f.hasBeenPushed(c.x(), c.y())) {
				
				Port portFrom = ports.get((int) f.getValue2(c.x(), c.y()));
				double dist = f.getValue(c) + portFrom.cost;
				
				for (DIR d : DIR.ALL) {
					if (f.hasBeenPushed(c.x(), c.y(), d) && WTRAV.can(c.x(), c.y(), d, false)){
						Port portTo = ports.get((int) f.getValue2(c.x(), c.y(), d));
						if (portFrom != portTo) {
							double dd = dist + f.getValue(c.x()+d.x(), c.y()+d.y());
							
							if (dd < portFrom.dists[portTo.index]) {
								portFrom.dists[portTo.index] = dd;
								portTo.dists[portFrom.index] = dd;
							}
						}
					}
				}
			}
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (f.hasBeenPushed(c.x(), c.y())) {
				
				Port portFrom = ports.get((int) f.getValue2(c.x(), c.y()));
				double dist = f.getValue(c) + portFrom.cost;
				
				for (DIR d : DIR.ALL) {
					if (f.hasBeenPushed(c.x(), c.y(), d) && WTRAV.can(c.x(), c.y(), d, false)){
						Port portTo = ports.get((int) f.getValue2(c.x(), c.y(), d));
						if (portFrom != portTo) {
							double dd = dist + f.getValue(c.x()+d.x(), c.y()+d.y());
							
							if (dd <= portFrom.dists[portTo.index]) {
								connect(f.get(c.x(), c.y()));
								connect(f.get(c.x()+d.x(), c.y()+d.y()));
							}
						}
					}
				}
			}
		}
		
		f.done();
		
		for (Port home : new ArrayList<>(ports)) {
			for (Port other : ports) {
				if (home == other || home.dists[other.index] == Double.MAX_VALUE)
					continue;
				home.cons.add(new Port.Connect(other, home.dists[other.index]));
			}
		}
		
		for (Port home : ports) {
			if (home.cons.size() == 0)
				continue;
			if (home.index % 10 == 0) {
				util.exe();
			}
			f.init(this);
			f.pushSloppy(home.x(), home.y(), 0, null);
			
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				Port o = port.get(t);
				o.dists[home.index] = t.getValue();
				home.dists[o.index] = t.getValue();
				for (Port.Connect l : o.cons) {
					f.pushSmaller(l.to.x(), l.to.y(),  t.getValue()+l.cost, t);
				}
			}
			f.done();
		}
		
		

		

	}
	
	
	private boolean isConnected(Port p) {
		
		for (Port o : ports) {

			double w = p.dists[o.index];
			double l = p.reg.dists[o.reg.reg.index()];

			if (w == Double.MAX_VALUE)
				continue;
			
			if (l == Double.MAX_VALUE)
				return true;
			
			w += o.cost;
			w += p.cost;
			
			if ((w + 32) < l)
				return true;
			
		}
		

		return false;
		
		
	}

	static class Reg {
		
		public final Region reg;
		public ArrayListGrower<Connect> land = new ArrayListGrower<>();
		public ArrayListGrower<Port> ports = new ArrayListGrower<>();
		public final double[] dists = new double[WREGIONS.MAX];
		
		private Reg(Region reg) {
			this.reg = reg;
			Arrays.fill(dists, Double.MAX_VALUE);
		}
		
		static class Connect {
			
			final Reg to;
			final double cost;
			
			Connect(Reg to, double cost){
				this.to = to;
				this.cost = cost;
			}
			
		}
		
	}
	


	public MAP_OBJECT<Reg> map = new MAP_OBJECT<Reg>() {
		
		@Override
		public Reg get(int tx, int ty) {
			if (WORLD.IN_BOUNDS(tx, ty))
				return get(tx+ty*WORLD.TWIDTH());
			return null;
		}
		
		@Override
		public Reg get(int tile) {
			Region reg = WORLD.REGIONS().map.get(tile);
			if (reg != null) {
				return regs.get(reg.index());
			}
			return null;
		}
	};
	
	private static class Port extends Coo{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final int index;
		private final Reg reg;
		private final double cost;
		private double[] dists;
		private ArrayListGrower<Connect> cons = new ArrayListGrower<>();
		
		private Port(COORDINATE c, Reg reg, double cost, int index){
			super.set(c);
			this.reg = reg;
			this.cost = cost;
			this.index = index;
		}
	
		static class Connect {
			
			final Port to;
			final double cost;
			
			Connect(Port to, double cost){
				this.to = to;
				this.cost = cost;
			}
			
		}
		
	}
	
	public MAP_OBJECT<Port> port = new MAP_OBJECT<Port>() {
		
		@Override
		public Port get(int tx, int ty) {
			if (WORLD.IN_BOUNDS(tx, ty))
				return get(tx+ty*WORLD.TWIDTH());
			return null;
		}
		
		@Override
		public Port get(int tile) {
			Reg reg = map.get(tile);
			if (reg != null) {
				for (int i = 0; i < reg.ports.size(); i++) {
					if (reg.ports.get(i).x() + reg.ports.get(i).y()*WORLD.TWIDTH() == tile)
						return reg.ports.get(i);
				}
			}
			return null;
		}
	};
	
	public Reg get(Region reg) {
		return regs.get(reg.index());
	}


	
}
