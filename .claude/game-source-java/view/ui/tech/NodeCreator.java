package view.ui.tech;

import init.tech.TECH;
import init.tech.TECH.TechRequirement;
import init.tech.TECHS;
import init.tech.TechTree;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.GUTIL;

final class NodeCreator {

	final static int DIM = 32;
	
	public final LIST<RENDEROBJ> rows;
	private final MAP_OBJECT<RENDEROBJ> map;
	private final RECTANGLE bounds;
	
	NodeCreator(int width){
		int maxW = 2*width/(Node.WIDTH+DIM);
		if (maxW < TechTree.MAX_COLS*2)
			maxW = TechTree.MAX_COLS*2;
		RENDEROBJ[][] res = create(maxW);
		bounds = new Rec(res[0].length, res.length);
		map = new MAP_OBJECT<RENDEROBJ>() {
			
			@Override
			public RENDEROBJ get(int tx, int ty) {
				if (bounds.holdsPoint(tx, ty))
					return res[ty][tx];
				return null;
			}
			
			@Override
			public RENDEROBJ get(int tile) {
				// TODO Auto-generated method stub
				return null;
			}
		};
		path();
		ArrayList<RENDEROBJ> rows = new ArrayList<>(res.length/2);
		for (int i = 0; i < res.length; i+=2) {
			GuiSection s = new GuiSection();
			for (int x = 0; x < res[i].length; x++) {
				s.add(res[i][x], s.body().x2(), 0);
				s.add(res[i+1][x], s.getLast().x1(), s.getLastY2());
			}
			rows.add(s);
		}
		this.rows = rows;
	}
	
	private RENDEROBJ[][] create(int maxW){
		LinkedList<RENDEROBJ[][]> rows = new LinkedList<>();
		
		KeyMap<LinkedList<RENDEROBJ[][]>> cats = new KeyMap<LinkedList<RENDEROBJ[][]>>();
		
		NodeBoosts boosts = new NodeBoosts();
		
		for (TechTree tree : TECHS.TREES()) {
			
			RENDEROBJ[][] oo = new RENDEROBJ[(tree.nodes.length)*2+2][];
			
			int mw = 0;
			
			for (int ri = 0; ri < tree.nodes.length; ri++) {
				mw = Math.max(mw, tree.nodes[ri].length);
				
			}
			for (int ri = 0; ri < oo.length; ri++) {
				oo[ri] = new RENDEROBJ[mw*2];

			}
			for (int ri = 0; ri < tree.nodes.length; ri++) {
				for (int ci = 0; ci < tree.nodes[ri].length; ci++) {
					if (tree.nodes[ri][ci] != null) {
						oo[ri*2+2][ci*2] = new Node(tree.nodes[ri][ci], boosts);
					}
				}
			}
			
			oo[1][oo[0].length-1] = new Edge(tree, mw, ((oo[0].length-1)&1) == 0 ? Node.WIDTH : DIM);
			
			for (int y = 0; y < oo.length; y++) {
				for (int x = 0; x < oo[0].length; x++) {
					if (oo[y][x] == null) {
						int wi = (x&1) == 1 ? DIM : Node.WIDTH;
						int hi = (y&1) == 1 ? DIM : Node.HEIGHT();
						oo[y][x] = new Edge(wi, hi);
					}

				}
			}
			
			if (!cats.containsKey(""+tree.cat))
				cats.put(""+tree.cat, new LinkedList<>());
			cats.get(""+tree.cat).add(oo);
		}
		
		for (LinkedList<RENDEROBJ[][]> remaining : cats.all()) {
			while(!remaining.isEmpty()) {
				
				RENDEROBJ[][] oo = remaining.removeFirst();
				
				int bestV = Integer.MAX_VALUE;
				RENDEROBJ[][] best = null;
				
				for (RENDEROBJ[][] other : remaining) {
					if (oo[0].length + other[0].length <= maxW) {
						int h = Math.max(oo.length, other.length);
						int w = oo[0].length + other[0].length;
						int a = w*h;
						
						a -= oo.length*oo[0].length;
						a -= other.length*other[0].length;
						if (a < bestV) {
							bestV = a;
							best = other;
						}
						
					}
				}
				if (best == null) {
					rows.add(oo);
				}else {
					remaining.remove(best);
					int h = Math.max(oo.length, best.length);
					int w = oo[0].length + best[0].length;
					
					RENDEROBJ[][] nn = new RENDEROBJ[h][w];
					
					for (int y = 0; y < oo.length; y++) {
						for (int x = 0; x < oo[0].length; x++) {
							nn[y][x] = oo[y][x];
						}
					}
					
					for (int y = 0; y < best.length; y++) {
						for (int x = 0; x < best[0].length; x++) {
							nn[y][x+oo[0].length] = best[y][x];
						}
					}
					remaining.add(nn);
				}
				
			}
		}
		
		
		
		
		int h = 0;
		for (RENDEROBJ[][] row : rows) {
			h+= row.length;
		}
		
		
		
		RENDEROBJ[][] res = new RENDEROBJ[h][maxW];

		h = 0;
		for (RENDEROBJ[][] row : rows) {
			for (int y = 0; y < row.length; y++) {
				for (int x = 0; x < row[0].length; x++) {
					res[y+h][x] = row[y][x];
				}
			}
			h+= row.length;
		}
		
		for (int y = 0; y < res.length; y++) {
			int hi = 0;
			for (int x = 0; x < res[0].length; x++) {
				if (res[y][x] != null)
					hi = Math.max(hi, res[y][x].body().height());
			}
			for (int x = 0; x < res[0].length; x++) {
				if (res[y][x] == null) {
					int wi = (x&1) == 1 ? DIM : Node.WIDTH;
					res[y][x] = new Edge(wi, hi);
				}

			}
		}

		return res;

		
	}

	
	private void path() {
		
		Bitmap1D m = new Bitmap1D(TECHS.ALL().size()*TECHS.ALL().size(), false);
		for (COORDINATE c : bounds) {
			RENDEROBJ o = map.get(c);
			if (o instanceof Node) {
				Node n = (Node) o;
				m.set(n.tech.index()*TECHS.ALL().size() + n.tech.index(), true);
			}
		}
		
		Flooder f = GUTIL.flooder();
		
		
		
		outer:
		while(true) {
			
			f.init(this);
			
			
			Node start = null;
			fucker:
			for (COORDINATE c : bounds) {
				
				RENDEROBJ o = map.get(c);
				if (o instanceof Node) {
					Node n = (Node) o;
					for (int ri = 0; ri < n.tech.requiresNodes().size(); ri++) {
						TechRequirement r = n.tech.requiresNodes().get(ri);
						if (!m.get(n.tech.index()*TECHS.ALL().size() + r.tech.index())) {
							start = n;
							f.pushSloppy(c.x(), c.y(), 0);
							f.setValue2(c.x(), c.y(), n.tech.index());
							break fucker;
						}
						
					}
				}
			}
			
			if (start == null) {
				f.done();
				return;
			}
			
			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				RENDEROBJ ro = map.get(t);
				
				if (ro instanceof Node) {
					Node n = (Node) ro;
					if (!m.get((int) (t.getValue2()*TECHS.ALL().size() + n.tech.index()))) {
						
						PathTile root = t;
						while(root.getParent() != null)
							root = root.getParent();
						Node node = (Node) map.get(root);
						m.set(n.tech.index()*TECHS.ALL().size()+node.tech.index(), true);
						m.set(node.tech.index()*TECHS.ALL().size()+n.tech.index(), true);
						PathTile parent = t;
						t = t.getParent();
						while(t != null) {
							
							if (map.get(t) instanceof Edge) {
								
								Edge e = (Edge) map.get(t);
								
								
								DIR ori = DIR.get(t, parent);
								int mm = ori.mask();
								
								
								if (t.getParent() != null ) {
									mm |= DIR.get(t, t.getParent()).mask();
								}
								if (t.getParent().getParent() == null)
									e.a |= DIR.get(t, t.getParent()).mask();
								e.m |= mm;
								
								node.addEdge(n, e, mm); 
								
							}
							parent = t;
							t = t.getParent();
							
						}
						m.set(n.tech.index(), false);
						f.done();
						continue outer;	
					}
				}
				
				for (DIR d : DIR.ORTHO) {
					push(t.x(), t.y(), d, t);
				}
			}
			
			LOG.ln("fuckit: " + start.tech.name());
			for (int ri = 0; ri < start.tech.requiresNodes().size(); ri++) {
				TechRequirement r = start.tech.requiresNodes().get(ri);
				if (!m.get(start.tech.index()*TECHS.ALL().size() + r.tech.index())) {
					LOG.ln("  : " + r.tech.name());
				}
				
				m.set(start.tech.index()*TECHS.ALL().size() + r.tech.index(), true);
				m.set(start.tech.index() + r.tech.index()*TECHS.ALL().size(), true);
			}
			f.done();
			
			
			
		}
		
		
		
	}

	
	
	private void push(int dx, int dy, DIR d, PathTile parent) {
		if (!bounds.holdsPoint(dx, dy, d))
			return;
		double v = 0;
		if (map.get(dx, dy) instanceof Edge) {
			
			Edge e = (Edge) map.get(dx, dy);
			if (d.x() != 0)
				v += e.body().width()/2;
			else
				v += e.body().height()/2;
			if ((e.m & d.mask()) != 0)
				v*= 0.25;
		}else if (((Node)map.get(dx, dy)).tech.index() != parent.getValue2())
			return;
		dx += d.x();
		dy += d.y();
		if (map.get(dx, dy) instanceof Edge) {
			
			Edge e = (Edge) map.get(dx, dy);
			if (d.x() != 0)
				v += e.body().width()/2;
			else
				v += e.body().height()/2;
			if ((e.m & d.mask()) != 0)
				v*= 0.25;
		}else {
			Node n = (Node) map.get(dx, dy);
			if (!req(parent, n))
				return;
		}
		if (parent.getParent() != null) {
			if (DIR.get(parent.getParent(), parent) != d)
				v += DIM/4;
		}
		
		if (GUTIL.flooder().pushSmaller(dx, dy, v+parent.getValue(), parent) != null) {
			GUTIL.flooder().setValue2(dx, dy, parent.getValue2());
		}
	}

	private boolean req(PathTile t, Node node) {
		TECH p = TECHS.ALL().get((int) t.getValue2());
		for (int i = 0; i < p.requiresNodes().size(); i++) {
			if (p.requiresNodes().get(i).tech == node.tech)
				return true;
		}
		return false;
	}

	
}
