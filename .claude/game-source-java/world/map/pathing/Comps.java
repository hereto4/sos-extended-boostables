package world.map.pathing;

import init.sprite.SPRITES;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.misc.IntChecker;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.Bitsmap2D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.GUTIL;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.pathing.WRegFinder.Treaty;
import world.map.regions.Region;
import world.overlay.WorldOverlays;

public final class Comps implements MAP_OBJECT<Comps.WComp> {

	private final Bitsmap2D is;
	public final WComp[] all;
	public final Finder finder;

	
	private Comps(Bitmap2D is, WComp[] comps, WDirMap m){
		this.all = new WComp[comps.length+1];
		int bits = 32 - Integer.numberOfLeadingZeros(this.all.length);
		this.is = new Bitsmap2D(0, bits, WORLD.TBOUNDS());
		for (COORDINATE c : WORLD.TBOUNDS())
			this.is.set(c, all.length-1);
		
		
		for (WComp c : comps) {
			this.is.set(c.tx, c.ty, c.id);
			this.all[c.id] = c;
		}
		finder = new Finder(this, m);

	}

	Comps(WDirMap m){
		this(new Bitmap2D(WORLD.TBOUNDS(), false), new WComp[0], m);
	}
	
	@Override
	public WComp get(int tile) {
		int o = is.get(tile);
		return all[o];
	}

	@Override
	public WComp get(int tx, int ty) {
		return get(tx + ty*WORLD.TWIDTH());
	}
	
	
	static Comps generate(ACTION aa, WDirMap m) {
		
		Gen gen = new Gen(aa, m);
		return new Comps(gen.mark, gen.comps, m);
		
	}
	
	private static class Gen {
		
		private final WComp[] comps;
		private final WDirMap m;
		private Bitmap2D mark = new Bitmap2D(WORLD.TBOUNDS(), false);
		private boolean log = false;
		private static double dist = 8;
		
		Gen(ACTION aa, WDirMap m){
			this.m = m;
			WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {

				@Override
				protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {

					if (WORLD.PATH().map.is.is(it.tile())) {
						COLOR.WHITE100.bind();
						for (int di = 0; di < DIR.ALL.size(); di++) {
							DIR d = DIR.ALL.get(di);
							if (WORLD.PATH().map.can(it.tx(), it.ty(), d))
								SPRITES.cons().ICO.arrows2.get(d.id()).render(r, it.x(), it.y());
						}
						COLOR.unbind();
					}
					if (mark.is(it.tx(), it.ty())) {
						COLOR.RED100.bind();
						SPRITES.cons().ICO.clear.render(r, it.x(), it.y());
						COLOR.unbind();
					}
				}
			};
			

			LinkedList<WComp> comps = new LinkedList<>();
			int id = 0;
			

			
			for (COORDINATE c : WORLD.TBOUNDS()) {
				if (isComp(c)) {
					mark.set(c, true);
					comps.add(new WComp(c.x(), c.y(), id));
					id++;
				}
			}
			
			createAdditional(comps, aa);
			
			
			this.comps = new WComp[comps.size()];
			id = 0;
			for (WComp c : comps) {
				this.comps[id++] = c;
			}
			
			if (log)
				LOG.ln("Components " + comps.size());
			
			int ii = 0;
			Bitmap1D check = new Bitmap1D(this.comps.length, false);
			
			Bitmap2D tmp = new Bitmap2D(WORLD.TBOUNDS(), false);
			WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {

				@Override
				protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {

					if (WORLD.PATH().map.is.is(it.tile())) {
						
						if (tmp.is(it.tile())) {
							COLOR.ORANGE100.bind();
						}else
							COLOR.WHITE100.bind();
						for (int di = 0; di < DIR.ALL.size(); di++) {
							DIR d = DIR.ALL.get(di);
							if (WORLD.PATH().map.can(it.tx(), it.ty(), d))
								SPRITES.cons().ICO.arrows2.get(d.id()).render(r, it.x(), it.y());
						}
						COLOR.unbind();
					}
					
					
					if (mark.is(it.tx(), it.ty())) {
						(check.get((int) GUTIL.flooder().getValue2(it.tx(), it.ty())) ? COLOR.GREEN100 : COLOR.RED100).bind();
						SPRITES.cons().ICO.clear.render(r, it.x(), it.y());
						COLOR.unbind();
					}
				}
			};
			
			for (COORDINATE c : WORLD.TBOUNDS()) {
				GUTIL.flooder().setValue2(c, -1);
			}
			
			for (WComp c : this.comps) {
				GUTIL.flooder().setValue2(c.tx, c.ty, c.id);
			}
			
			for (WComp c : this.comps) {
				if (!check.get((int) GUTIL.flooder().getValue2(c.tx, c.ty))) {
					init(c, check, tmp);
					if (ii ++ % 5 == 0)
						aa.exe();
				}
			}
			
			for (COORDINATE c : WORLD.TBOUNDS()) {
				if (m.is.is(c) && ! tmp.is(c)) {
					m.remove(c.x(), c.y());
				}
			}
			
		}
		
		private boolean isComp(COORDINATE c) {
			if (WORLD.REGIONS().cTile.is(c))
				return true;
			if (!m.is.is(c))
				return false;
			
			if (WORLD.WATER().isBig.is(c)) {
				boolean water = false;
				boolean land = false;
				
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					if (m.can(c, d)) {
						if (WORLD.WATER().isBig.is(c, d)) {
							water = true;
						}
						else
							land = true;
					}
				}
				
				if (water && land)
					return true;
				
			}
			
			Region h = WORLD.PATH().regMap.get(c);
			if (h != null) {
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					if (m.can(c, d) && WORLD.PATH().regMap.get(c, d) !=  h) {
						return true;
					}
				}
			}
			
			return false;
		}
		
		private void createAdditional(LinkedList<WComp> comps, ACTION aa) {
			
			for (COORDINATE c : WORLD.TBOUNDS()) {
				GUTIL.flooder().setValue2(c, 0);
			}
			
			
			for (int i = 0; i < 20; i++) {
				aa.exe();
				createAdditionalP(comps);
			}
		}
		
		private boolean createAdditionalP(LinkedList<WComp> comps) {
			Flooder f = GUTIL.flooder();
			f.init(this);
			
			int id = 0;
			int pp = 0;
			for (WComp c : comps) {
				if (f.getValue2(c.tx, c.ty) == 0) {
					f.pushSloppy(c.tx, c.ty, 0);
					pp++;
				}
				id++;
			}
			
			if (pp == 0) {
				f.done();
				return false;
			}
			
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				if (t.getValue2() != 0)
					continue;
				
				if (t.getValue() > dist) {
					int c = 0;
					for (int di = 0; di < DIR.ALL.size(); di++) {
						DIR d = DIR.ALL.get(di);
						if (m.can(t, d))
							c++;
						
					}
					if (c == 2) {
						comps.add(new WComp(t.x(), t.y(), id++));
						mark.set(t, true);
						t = t.getParent();
						while(t != null) {
							f.setValue2(t, 1);
							t = t.getParent();
						}
						continue;
					}
				}
				m.pushSimple(t);
			}
			
			
			
			f.done();
			return true;
		}
		
		private void init(WComp start, Bitmap1D check, Bitmap2D tmp) {
			
			Flooder f = GUTIL.flooder();
			f.init(this);
			f.pushSloppy(start.tx, start.ty, 0);
			
			int home = (int) f.getValue2(start.tx, start.ty);
			
			while(f.hasMore()) {
				
				PathTile t = f.pollSmallest();
				int id = (int) t.getValue2();
				if (id >= 0 && id != home) {
					if (!check.get(id)) {
						comps[home].push(id, t.getValue());
						comps[id].push(home, t.getValue());
						while(t != null) {
							tmp.set(t, true);
							t = t.getParent();
						}
					}
					continue;
				}
				
				m.push(t, t.getValue());
				
			}
			
			
			check.set(home, true);
			f.done();
			
		}
		
		
	}
	

	
	static class Finder {
		

		Comps cc;
		private final WDirMap m;
		private final ArrayList<PathTile> result;
		private final IntChecker destCheck;
		private final IntChecker absPath;
		private final short[] prevRegion;
		
		Finder(Comps cc, WDirMap m){
			this.cc = cc;
			this.m = m;
			prevRegion = new short[cc.all.length];
			result = new ArrayList<PathTile>(cc.all.length);
			destCheck = new IntChecker(cc.all.length);
			absPath = new IntChecker(cc.all.length);
			
		}
		
		public PathTile find(COORDINATE start, int endX, int endY, Treaty trav) {
			return find(start.x(), start.y(), endX, endY, trav);
		}
		
		public PathTile find(int startX, int startY, COORDINATE end, Treaty trav) {
			return find(startX, startY, end.x(), end.y(), trav);
		}
		
		public PathTile find(COORDINATE start, COORDINATE end, Treaty trav) {
			return find(start.x(), start.y(), end.x(), end.y(), trav);
		}
		
		public PathTile find(int startX, int startY, int endX, int endY, Treaty trav){
			
			Flooder f = GUTIL.flooder();
			f.init(this);
			
			//check if start == end
			{
				PathTile start = f.pushSloppy(endX, endY, 0);
				
				if (startX == endX && startY == endY) {
					f.done();
					return start;
				}
			}
			
			
			//find the end comps, mark them. If you find the start, return.
			{
				destCheck.init();
				result.clearSloppy();
				boolean succ = false;
				while(f.hasMore()) {
					PathTile t = f.pollSmallest();
					
					if (t.isSameAs(startX, startY)) {
						f.done();
						return f.reverse(t);
					}
					
					WComp c = cc.get(t);
					if (c != null) {
						destCheck.isSetAndSet(c.id);
						result.add(t);
						t.setValue2(t.getValue());
						c.wayBackDest = t.getParent();
						succ = true;
						continue;
					}
					
					m.push(t, t.getValue());
				}
				if (!succ) {
					f.done();
					return null;
				}
				for (PathTile c : result) {
					f.reopen(c);
				}
				
			}
			
			
			
			//find the start components, save them in result, store the cost in the abs finder
			{

				result.clearSloppy();
				f.pushSloppy(startX, startY, 0);
				while(f.hasMore()) {
					PathTile t = f.pollSmallest();
					WComp c = cc.get(t);
					if (c != null) {
						
						if (t.isSameAs(endX, endY)) {
							f.done();
							return t;
						}
						result.add(t);
						c.wayBack = t.getParent();
						f.setValue2(t, t.getValue());
						continue;
					}
					
					m.push(t, t.getValue());
				}

				if (result.size() == 0) {
					f.done();
					return null;
				}
				
				
			}
			
			f.done();
			
			Region origin = WORLD.PATH().regMap.get(startX, startY);
			
			//time to find our abstract path. We start by pushing all the start components
			boolean succ = false;
			{
				f.init(f);
	
				absPath.init();
				
				for (PathTile c : result) {
					f.pushSloppy(c.x(), c.y(), c.getValue2());
					regSetStart(c);
				}
				
				while(f.hasMore()) {
					PathTile t = f.pollSmallest();
					WComp c = cc.get(t);
					
					Region from = regSet(t);
					
					if (destCheck.isSet(c.id)) {
						while(t != null) {
							absPath.isSetAndSet(cc.get(t).id);
							t = t.getParent();
							
						}
						succ = true;
						break;
					}
					
					for (int i = 0; i < c.neighs.length; i++) {
						WComp n = cc.all[c.neighs[i]];
						double v = c.dists[i] + t.getValue();
						if (destCheck.isSet(n.id)) {
							v += f.getValue2(n.tx, n.ty);
						}
						Region to = WORLD.PATH().regMap.get(n.tx, n.ty);
						if (trav.can(origin, from, to, n.tx, n.ty, v))
							f.pushSmaller(n.tx, n.ty, v, t);
						
					}
					
				}
				f.done();
				if (!succ)
					return null;
			}
			
			f.init(this);
			//now finally we can path a tile path. Push all the starts with the way back tiles.
			for (PathTile t : result) {
				WComp c = cc.get(t);
				if (absPath.isSet(c.id)) {
					f.pushSloppy(t.x(), t.y(), t.getValue(), c.wayBack);
					
					PathTile w = c.wayBack;
					while (w != null) {
						f.close(w.x(), w.y(), w.getValue());
						w = w.getParent();
					}
				}
				
			}

			while(f.hasMore()) {
				
				PathTile t = f.pollSmallest();
				WComp c = cc.get(t);
				
				if (c != null && destCheck.isSet(c.id)) {
					double v = t.getValue();
					
					while(c.wayBackDest != null) {
						PathTile p = c.wayBackDest;
						c.wayBackDest = c.wayBackDest.getParent();
						p.parentSet(t);
						
						t = p;
						if (p.isSameAs(startX, startY)) {
							p.parentSet(null);
							break;
						}
					}
					
					f.setValue(t.x(), t.y(), v);
					f.done();
					return t;
				}
				
				if (c != null && !absPath.isSet(c.id))
					continue;
				
				int md = m.get(t);
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					if ((md & (d.bit)) != 0) {
						WComp to = cc.get(t, d);
						double v = t.getValue()+d.tileDistance()*WDirMap.cost(t.x(), t.y(), d);
						if (to != null && destCheck.isSet(to.id) && to.wayBackDest != null) {
							v += to.wayBackDest.getValue();
						}
						f.pushSmaller(t, d, v, t);
					}
				}
				
			}
			f.done();
			return null;
			
			
			
			
		}

		public LIST<PathTile> getComps(int sx, int sy) {
			result.clearSloppy();
			Flooder f = GUTIL.flooder();
			f.init(this);
			f.pushSloppy(sx, sy, 0);
			
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				WComp c = cc.get(t);
				if (c != null) {
					result.add(t);
					continue;
				}
				
				m.push(t, t.getValue());
			}

			f.done();
			return result;
		}
		
		public double dist(int startX, int startY, int endX, int endY, Treaty treaty) {
			destCheck.init();
			for (PathTile t : getComps(endX, endY)) {
				t.setValue2(t.getValue());
				destCheck.isSetAndSet(cc.get(t).id);
			}
			
			Region origin = WORLD.PATH().regMap.get(startX, startY);
			
			LIST<PathTile> ss = getComps(startX, startY);
			Flooder f = GUTIL.flooder();
			f.init(this);
			for (PathTile t : ss) {
				f.pushSloppy(t.x(), t.y(), t.getValue());
				regSetStart(t);
			}
			
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				Region prev = regSet(t);
				WComp c = cc.get(t);
				if (destCheck.isSet(c.id)) {
					f.done();
					return t.getValue();
				}
				for (int i = 0; i < c.neighs.length; i++) {
					WComp n = cc.all[c.neighs[i]];
					double v = c.dists[i] + t.getValue();
					if (destCheck.isSet(n.id)) {
						v += f.getValue2(n.tx, n.ty);
					}
					if (treaty.can(origin, prev, WORLD.PATH().regMap.get(n.tx, n.ty), n.tx, n.ty, v)) {
						f.pushSmaller(n.tx, n.ty, v, t);
					}
					
				}
			}
			f.done();
			return 0;
			
		}
		
		private Region regSet(PathTile t) {
			Region reg = WORLD.PATH().regMap.get(t);
			if (reg != null) {
				prevRegion[cc.get(t).id] = (short) (reg.index()+1);
				return reg;
			}else if (t.getParent() != null){
				short i = prevRegion[cc.get(t.getParent()).id];
				if (i == 0)
					return null;
				return WORLD.REGIONS().getByIndex(i);
			}
			return null;
		}
		
		private void regSetStart(PathTile t) {
			Region reg = WORLD.PATH().regMap.get(t);
			prevRegion[cc.get(t).id] = reg != null ? (short) (reg.index()+1) : 0;
		}

	}
	
	public static class WComp implements COORDINATE{

		public final short tx;
		public final short ty;
		private short[] neighs = new short[0];
		private short[] dists = new short[0];
		public final int id;
		private PathTile wayBack;
		private PathTile wayBackDest;
		
		
		WComp(int tx, int ty, int id){
			this.tx = (short) tx;
			this.ty = (short) ty;
			this.id = id;
			
		}
		

		private void push(int to, double dist) {
			short[] n = new short[neighs.length+1];
			short[] d = new short[neighs.length+1];
			
			for (int i = 0; i < neighs.length; i++) {
				n[i+1] = neighs[i];
				d[i+1] = dists[i];
			}
			n[0] = (short) to;
			d[0] = (short) CLAMP.i((int) dist, 0, Short.MAX_VALUE);
			neighs = n;
			dists = d;
		}
		
		public int neighs() {
			return neighs.length;
		}
		
		public WComp neigh(int i) {
			return WORLD.PATH().comps.all[neighs[i]];
		}
		
		public double dist(int i) {
			return dists[i];
		}


		@Override
		public int x() {
			return tx;
		}


		@Override
		public int y() {
			return ty;
		}
		
	}
	
	static final class DebugOverlay extends WorldOverlays.OverlayTile{

		private WComp hovered;
		private IntChecker check = new IntChecker(1);
		
		DebugOverlay() {
			super(true, false);
		}
		

		Str str = new Str(16);
		
		@Override
		public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
			
			hovered = WORLD.PATH().comps.get(mouse(data));
			
			if (check.size() < WORLD.PATH().comps.all.length)
				check = new IntChecker(WORLD.PATH().comps.all.length);
			if (hovered != null) {
				
				check.init();
				for (int i = 0; i < hovered.neighs.length; i++) {
					check.isSetAndSet(hovered.neighs[i]);
				}
				
			}
			
			
			
			super.renderAbove(r, s, data);
		}
		
		@Override
		public void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
			if (WORLD.PATH().map.is.is(it.tile())) {
				COLOR.ORANGE100.bind();
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					if (WORLD.PATH().map.can(it.tx(), it.ty(), d))
						SPRITES.cons().ICO.arrows2.get(d.id()).render(r, it.x(), it.y());
				}
				COLOR.unbind();
			}
			
			if (WORLD.PATH().comps.get(it.tx(), it.ty()) != null) {
				(check.isSet(WORLD.PATH().comps.get(it.tx(), it.ty()).id) ? COLOR.GREEN100 : COLOR.RED100).bind();
				SPRITES.cons().ICO.clear.render(r, it.x(), it.y());
				COLOR.unbind();
			}
			
			hovered = null;
			
		}
		
	}
	
}
