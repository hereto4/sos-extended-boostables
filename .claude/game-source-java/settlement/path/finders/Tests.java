package settlement.path.finders;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TAREA;
import static settlement.main.SETT.TWIDTH;

import init.constant.C;
import init.resources.RBIT;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.main.ON_TOP_RENDERABLE;
import settlement.main.SETT;
import settlement.path.path.SPath;
import snake2d.LOG;
import snake2d.PathGame;
import snake2d.PathTile;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.Coo;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import util.GUTIL;
import util.gui.misc.GButt;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.Dic;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.subview.GameWindow;
import view.tool.PlacableSimpleTile;

class Tests {

	@SuppressWarnings("unused")
	private final SFINDERS finders;
	
	Tests(SFINDERS finders){
		this.finders = finders;
		IDebugPanelSett.add("Path Compare astar", new PlacerCompare());
		
		new ON_TOP_RENDERABLE() {
			
			Bitmap1D map;
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				if (!TestPath.tester.isSuccessful())
					return;
				
				if (map == null)
					return;
				
				if (TestPath.tester.hasNext()) {
					map = new Bitmap1D(TAREA, false);
					
					map.set(TestPath.tester.x()+TestPath.tester.y()*TWIDTH, true);
					while(TestPath.tester.setNext()) {
						map.set(TestPath.tester.x()+TestPath.tester.y()*TWIDTH, true);
					}
				}
				
				
				RenderIterator it = data.onScreenTiles();
				while(it.has()) {
					if (map.get(it.tile())) {
						SPRITES.cons().BIG.dots.render(r, 0, it.x(), it.y());
					}
					it.next();
				}
			}
		}.add();;
		
		IDebugPanelSett.add("Path Per Test", new ACTION() {
			final SPath p = new SPath();
			@Override
			public void exe() {
				
				final int am = 1000;
				int[] sx = new int[am];
				int[] sy = new int[am];
				
				for (int i = 0; i < am; i++) {
					sx[i] = RND.rInt(SETT.TWIDTH);
					sy[i] = RND.rInt(SETT.THEIGHT);
				}
				
				{
					int[] dx = new int[am];
					int[] dy = new int[am];
					for (int i = 0; i < am; i++) {
						dx[i] = RND.rInt(SETT.TWIDTH);
						dy[i] = RND.rInt(SETT.THEIGHT);
					}
					long now = System.currentTimeMillis();
					int a = 0;
					now = System.currentTimeMillis();
					a = 0;
					for (int i = 0; i < am; i++) {
						if (find(sx[i], sy[i], dx[i], dy[i], false))
							a++;
					}
					double d = a;
					d /= (System.currentTimeMillis()-now);
					d*= 1000;
					LOG.ln("long paths: " + d + "p/s, paths: " + a);
				}
				
				{
					int[] dx = new int[am];
					int[] dy = new int[am];
					for (int i = 0; i < am; i++) {
						dx[i] = CLAMP.i(sx[i]+RND.rInt0(100), 0, TWIDTH); 
						dy[i] = CLAMP.i(sy[i]+RND.rInt0(100), 0, TWIDTH); 
					}
					long now = System.currentTimeMillis();
					int a = 0;
					now = System.currentTimeMillis();
					a = 0;
					for (int i = 0; i < am; i++) {
						if (find(sx[i], sy[i], dx[i], dy[i], false))
							a++;
					}
					double d = a;
					d /= (System.currentTimeMillis()-now);
					d*= 1000;
					LOG.ln("short paths: " + d + "p/s, paths: " + a);
				}
				
				{
					long now = System.currentTimeMillis();
					int a = 0;
					
					now = System.currentTimeMillis();
					a = 0;
					for (int i = 0; i < am; i++) {
						if (findR(sx[i], sy[i], false))
							a++;
					}
					double d = a;
					d /= (System.currentTimeMillis()-now);
					d*= 1000;
					LOG.ln("res closest: " + d + "p/s, paths: " + a);
				}
				
				{
					long now = System.currentTimeMillis();
					int a = 0;
					
					now = System.currentTimeMillis();
					a = 0;
					for (int i = 0; i < am; i++) {
						if (findJ(sx[i], sy[i]))
							a++;
					}
					double d = a;
					d /= (System.currentTimeMillis()-now);
					d*= 1000;
					LOG.ln("job: " + d + "p/s, paths: " + a);
				}
				
				
				
			}
			
			private boolean find(int startX, int startY, int destX, int destY, boolean full) {
				
				if (!p.request(startX, startY, destX, destY, full)) {
					return false;
				}
				
				return forward();
				
			}
			
			private boolean forward() {
				while(!p.isDest() && p.isSuccessful() && p.setNext()) {
					;
				}
				return p.isSuccessful();
			}
			
			private boolean findR(int startX, int startY, boolean full) {
				RESOURCE res = SETT.PATH().finders.resource.scattered.reserve(startX, startY, RBIT.ALL,  p, 250);
				if (res == null) {
					return false;
				}
				
				SETT.PATH().finders.resource.unreserve(res, p.destX(), p.destY(), 1);
				
				return forward();
				
			}
			
			private boolean findJ(int startX, int startY) {
				if (SETT.PATH().finders.job.find(startX, startY, p, true) != null) {
					return forward();
				}
				return false;
				
			}
		});
		
		IDebugPanelSett.add(new FF());

		
	}
	
	private class FF extends PlacableSimpleTile {

		private Coo start = new Coo(-1,-1);
		
		public FF() {
			super("Path Place");
		}

		@Override
		public CharSequence isPlacable(int tx, int ty) {
			return SETT.PATH().solidity.is(tx, ty) ? Dic.empty : null;
		}

		@Override
		public void place(int tx, int ty) {
			start.set(tx, ty);
			
		}
		
		ON_TOP_RENDERABLE rr = new ON_TOP_RENDERABLE() {
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				
				remove();
				
				if (start.x() == -1 || SETT.PATH().solidity.is(start))
					return;
				
				int dx = VIEW.s().getWindow().tile().x();
				int dy = VIEW.s().getWindow().tile().y();

				if (SETT.PATH().solidity.is(dx, dy))
					return;
				
				PathTile t = SETT.PATH().finders.finder().cDebug(start.x(), start.y(), dx, dy, true);
				int dd = ((int)(VIEW.renderSecond()*8))%1024;
				
				while(t != null) {
					
					int sx = t.x();
					int sy = t.y();
					while (t != null) {
						(dd == 0 ? COLOR.WHITE100 : COLOR.ORANGE100).bind();
						int rx = data.transformGX(t.x()*C.TILE_SIZE);
						int ry = data.transformGY(t.y()*C.TILE_SIZE);
						SPRITES.cons().ICO.tile.render(r, rx, ry);
						//LOG.ln(" -- " + t);

						t = t.getParent();
						dd--;
					}
					
					if (Math.abs(sx-dx) <= 1 && Math.abs(sy - dy) <= 1)
						break;
					
					t = SETT.PATH().finders.finder().find(sx, sy, dx, dy, true);
					//LOG.ln(sx + " " + sy + " -> " + dx + " " + dy + " " + t + " " + t.getParent());
				}
				
			}
		};
		
		@Override
		public void renderOverlay(GameWindow window) {
			
			rr.add();
		}
		
	}
	
	private class PlacerCompare implements ACTION{
		
		private Bitmap1D map = null;
		private Bitmap1D map2 = null;
		int sx,sy;
		private final SPath path = new SPath();
		private final PathGame.PathFancy path2 = new PathGame.PathFancy(256);
		
		ON_TOP_RENDERABLE ren = new ON_TOP_RENDERABLE() {
			
			@Override
			public void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds) {
				if (map == null) {
					remove();
					return;
				}
				RenderIterator it = data.onScreenTiles();
				while(it.has()) {
					if (map.get(it.tile())) {
						SPRITES.cons().BIG.dots.render(r, 0, it.x(), it.y());
					}
					it.next();
				}
				
				if (map2 == null)
					return;
				it = data.onScreenTiles();
				while(it.has()) {
					COLOR.RED100.bind();
					if (map2.get(it.tile())) {
						SPRITES.cons().BIG.dots.render(r, 0, it.x()+4, it.y()+4);
					}
					it.next();
				}
				COLOR.unbind();
			}
		};
		
		PlacableSimpleTile p1 = new PlacableSimpleTile("set start") {
			
			@Override
			public void place(int tx, int ty) {
				sx= tx;
				sy = ty;
				VIEW.s().tools.place(p2);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				if (PATH().solidity.is(tx, ty))
					return E;
				return null;
			}
		};
		
		PlacableSimpleTile p2 = new PlacableSimpleTile("set dest") {
			
			boolean full = false;
			
			final ArrayList<CLICKABLE> pp = new ArrayList<CLICKABLE>(
				new GButt.Panel("F") {
					@Override
					protected void clickA() {
						full = !full;
					};
					@Override
					protected void renAction() {
						selectedSet(full);
					};
				}
			);
			
			@Override
			public void place(int tx, int ty) {
				if (path.request(sx, sy, tx, ty, full)) {
					map = new Bitmap1D(TAREA, false);
					map.set(path.x()+ path.y()*TWIDTH, true);
					while(path.isSuccessful() && path.setNext()) {
						map.set(path.x()+ path.y()*TWIDTH, true);
					}
					ren.add();
					
				}
				
				if (GUTIL.astar().getShortest(path2, SETT.PATH().coster.player, sx, sy, tx, ty)) {
					map2 = new Bitmap1D(TAREA, false);
					map2.set(path2.x()+ path2.y()*TWIDTH, true);
					do {
						while(path2.setNext()) {
							map2.set(path2.x()+ path2.y()*TWIDTH, true);
						}
					}while(!path2.isCompleate() && GUTIL.astar().getShortest(path2, SETT.PATH().coster.player, path2.x(), path2.y(), tx, ty));
					
					ren.add();
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return null;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return pp;
			};
			
			
		};

		@Override
		public void exe() {
			path.clear();
			VIEW.s().tools.place(p1);
		}
		
		
		
	}
	

	
}
