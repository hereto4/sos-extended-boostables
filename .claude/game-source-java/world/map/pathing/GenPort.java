package world.map.pathing;

import static world.WORLD.TBOUNDS;

import init.sprite.SPRITES;
import snake2d.PathTile;
import snake2d.SPRITE_RENDERER;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.Polymap;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.Bitsmap2D;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.MapIndexed;
import util.GUTIL;
import util.rendering.ShadowBatch;
import util.rendering.RenderData.RenderIterator;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.map.road.WTRAV;
import world.overlay.WorldOverlays;

final class GenPort {

	private final ArrayListResize<Port> ports = new ArrayListResize<>(WREGIONS.MAX, WREGIONS.MAX*10);
	private final Bitsmap2D wRegs;
	GenPort(ACTION util){
		
		wRegs = new GenPortRegs(util);
		
		WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {
			
			@Override
			protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
				if (WORLD.PATH().portArea.is(it.tile())) {
					COLOR.UNIQUE.getC(wRegs.get(it.tile())).bind();
					SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
					COLOR.unbind();
				}
				if (WORLD.PATH().map.is.is(it.tile())) {
					COLOR.ORANGE100.bind();
					for (int di = 0; di < DIR.ALL.size(); di++) {
						DIR d = DIR.ALL.get(di);
						if (WORLD.PATH().map.can(it.tx(), it.ty(), d))
							SPRITES.cons().ICO.arrows2.get(d.id()).render(r, it.x(),
									it.y());
					}
					COLOR.unbind();
				}
				
			}
		};
		
		
		Flooder f = GUTIL.flooder();
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (mPort.is(c)) {
				f.setValue2(c, ports.size());
				ports.add(new Port(ports.size(), c));
			}
		}
		
		setPortAreas(util);
		
		util.exe();
		connectNeighs(util);
		util.exe();
		

		LIST<PortGroup> groups = makeGroups();
		
		
		Bitmap2D network = network(ports);
		Bitmap1D check = new Bitmap1D(ports.size(), false);
		for (PortGroup g : groups) {
			
			final int whome = wRegs.get(g.all.get(0).coo);
			check.clear();
			f.init(this);
			for (Port p : g.all)
				f.pushSloppy(p.coo, 0);
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				
				int wnow = wRegs.get(t);
				
				
				
				if (mPort.is(t)) {
					if (wnow != whome) {
						Port po = ports.get((int) t.getValue2());
						if (!check.get(po.group.index)) {
							check.set(wnow, true);
							Gen.connect(t);
						}
					}
				}
				for (DIR d : DIR.ALL) {
					if (network.is(t, d) || WORLD.PATH().map.can(t, d)){
						
						double v = 1;
						if (!WORLD.PATH().map.can(t, d))
							v = 8;
						f.pushSmaller(t, d, t.getValue()+v*d.tileDistance(), t);
						
						
					}
				}
			}
			f.done();
			util.exe();
			
			
		}
		
		util.exe();
		
		
	
		

		
	}

	private void setPortAreas(ACTION util) {
		WORLD.PATH().portArea.clear();
		Flooder f = GUTIL.flooder();
		f.init(this);
		for (Port p : ports) {
			Region reg = WORLD.REGIONS().map.get(p.coo);
			if (reg != null) {
				f.pushSloppy(p.coo.x(), p.coo.y(), 0, null);
			}		
		}
		

		
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			Region r = WORLD.REGIONS().map.get(t);
			if (r == null || r != ports.get((int) t.getValue2()).region())
				continue;
			WORLD.PATH().portArea.set(t, true);
			for (DIR d : DIR.ALL) {
				if (WORLD.WATER().isBig.is(t, d) && WTRAV.can(t.x(), t.y(), d, false)) {
					f.pushSmaller(t, d, t.getValue()+d.tileDistance(), t);
				}
			}
		}
		f.done();
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			Region reg = WORLD.REGIONS().map.get(c);
			if (reg != null && WORLD.WATER().isBig.is(c) && WORLD.ROADS().bridge.is(c)) {
				WORLD.PATH().portArea.set(c, true);
			}
		}
		
		for (Region reg : WORLD.REGIONS().all()) {
			if (WORLD.WATER().isBig.is(reg.cx(), reg.cy()))
				f.pushSloppy(reg.cx(), reg.cy(), 0, null);
		}
	}
	
	
	private void connectNeighs(ACTION util) {
		Flooder f = GUTIL.flooder();
		
		Bitmap2D coast =  coast(); 
		
		f.init(this);
		
		for (Port p : ports) {
			f.pushSloppy(p.coo.x(), p.coo.y(), 0, null);
		}
		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			
			Port pFrom = ports.get((int) t.getValue2());
			int from = wRegs.get(pFrom.coo);
			
			
			
			for (DIR d : DIR.ALL) {
				if (coast.is(t, d) && WTRAV.can(t.x(), t.y(), d, false) && from == wRegs.get(t.x(), t.y(), d)) {
					double v = WORLD.WATER().coversTile.is(t, d) ? 1 : 3;
					f.pushSmaller(t,d, t.getValue()+v*d.tileDistance(), t);
				}
			}
		}
		f.done();
		util.exe();
		
		MapIndexed<Connection> cons = new MapIndexed<>();
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (f.hasBeenPushed(c.x(), c.y())) {
				
				int portFrom = (int) f.getValue2(c.x(), c.y());
				double dist = f.getValue(c);
				
				for (DIR d : DIR.ALL) {
					if (f.hasBeenPushed(c.x(), c.y(), d) && WTRAV.can(c.x(), c.y(), d, false)){
						int portTo = (int) f.getValue2(c.x(), c.y(), d);
						if (portFrom != portTo) {
							double dd = (d.isOrtho() ? 0 : 100) + dist + f.getValue(c.x()+d.x(), c.y()+d.y());
							int ii = ii(portFrom, portTo);
							Connection con = cons.getTry(ii);
							if (con == null) {
								con = new Connection(ii);
								cons.add(con);
							}
							if (dd < con.cost) {
								con.cost = dd;
								con.a = f.get(c.x(), c.y());
								con.b = f.get(c.x()+d.x(), c.y()+d.y());
							}
						}
					}
				}
			}
		}
		
		for (Connection c : cons.toList()) {
			Gen.connect(c.a);
			Gen.connect(c.b);
			DIR d = DIR.get(c.a, c.b);
			WORLD.PATH().map.add(c.a, d);
			WORLD.PATH().map.add(c.b, d.perpendicular());
		}
	
	}
	
	private LIST<PortGroup> makeGroups(){
		ArrayList<PortGroup> groups = new ArrayList<PortGroup>(ports.size());
		Flooder f = GUTIL.flooder();
		int gi = 0;
		for (Port p : ports) {
			if (p.group != null)
				continue;
			
			final int wi = wRegs.get(p.coo);
			
			PortGroup g = new PortGroup(gi++);
			groups.add(g);
			f.init(this);
			f.pushSloppy(p.coo, 0);
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				if (wRegs.get(t) != wi)
					continue;
				
				if (mPort.is(t)) {
					Port po = ports.get((int) t.getValue2());
					g.all.add(po);
					po.group = g;
				}
				for (DIR d : DIR.ALL) {
					if (WORLD.PATH().map.can(t, d) && WORLD.WATER().isBig.is(t, d)){
						f.pushSmaller(t, d, t.getValue()+d.tileDistance());
					}
				}
			}
			f.done();
		}
		
		groups.shuffle();
		return groups;
	}
	
	private Bitmap2D coast() {
		Bitmap2D coast = new Bitmap2D(WORLD.TWIDTH(), WORLD.THEIGHT(), false);
		
		Flooder f = GUTIL.flooder();
		
		f.init(this);
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.WATER().isBig.is(c)  && !WORLD.WATER().coversTile.is(c)) {
				f.pushSloppy(c.x(), c.y(), 0, null);
			}
		}
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			coast.set(t, true);
			if (t.getValue() > 3)
				continue;
			
			for (DIR d : DIR.ALL) {
				if (WORLD.WATER().isBig.is(t, d))
					f.pushSmaller(t, d, t.getValue()+d.tileDistance());
				
			}
		}
		f.done();
		return coast;
	}
	
	private Bitmap2D network(LIST<Port> ports) {
		Bitmap2D network = new Bitmap2D(WORLD.TBOUNDS(), false);
		Polymap polly = new Polymap(TBOUNDS(), 12, 1);

		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.WATER().isBig.is(c) && polly.isEdge(c.x(), c.y())) {
				network.set(c, true);
			}
		}
		Flooder f = GUTIL.flooder();
		
		f.init(this);
		for (Port p : ports) {
			f.pushSloppy(p.coo, 0);
		}
		
		Bitmap1D check = new Bitmap1D(ports.size(), false);
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			if (check.get((int) t.getValue2()))
				continue;
			
			if (network.is(t)) {
				check.set((int) t.getValue2(), true);
				while(t != null) {
					network.set(t, true);
					t = t.getParent();
				}
				continue;
			}
			
			for (DIR d : DIR.ALL) {
				if (WORLD.WATER().isBig.is(t, d)) {
					double v = 1;
					if (!WORLD.PATH().map.can(t, d))
						v = 4;
					f.pushSmaller(t, d, t.getValue()+v*d.tileDistance());
				}
					
			}
		}
		f.done();
		return network;
	}
	
	public final MAP_BOOLEAN mPort = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (!WORLD.WATER().isBig.is(tx, ty))
				return false;
			
			
			if (WORLD.ROADS().harbour.is(tx, ty) && !WORLD.ROADS().bridge.is(tx, ty)) {
				return true;
			}else if (WORLD.REGIONS().cTile.get(tx, ty) != null) {
				return true;
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			// TODO Auto-generated method stub
			return false;
		}
	};

	private static class Port {
		
		public final Coo coo = new Coo();
		public PortGroup group;
		
		
		Port(int index, COORDINATE c){
			this.coo.set(c);
		}


		public Region region() {
			return WORLD.REGIONS().map.get(coo);
		}
		
	}
	
	private static class PortGroup {
		
		public final ArrayListGrower<Port> all = new ArrayListGrower<>();
		public final int index;
		
		PortGroup(int index){
			this.index = index;
		}
		
	}
	
	private static class Connection implements INDEXED{

		private double cost = Double.MAX_VALUE;
		private PathTile a;
		private PathTile b;
		private final int index;
		
		Connection(int ii){
			index = ii;
		}
		
		@Override
		public int index() {
			return index;
		}
		

		
	}
	
	public int ii(int a, int b) {
		if (a > b) {
			int c = a;
			a = b;
			b = c;
		}
		return a*ports.size() + b;
	}
	
	
}
