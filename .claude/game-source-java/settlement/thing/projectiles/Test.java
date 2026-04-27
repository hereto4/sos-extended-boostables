package settlement.thing.projectiles;

import init.constant.C;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT.INTE;
import util.gui.slider.GTarget;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PlacableSimple;

class Test {

	static final int vel = C.TILE_SIZE*40;
	static final double ang = 75;
	public Test() {
		
		IDebugPanelSett.add(new Single());
		
		
		IDebugPanelSett.add(new MASS());
		
	}
	
	private static class Single extends PlacableSimple {
		
		int sx,sy;
		final Trajectory t = new Trajectory();
		private final INTE type = new INTE() {
			
			int i = 0;
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return STATS.EQUIP().RANGED().size()-1;
			}
			
			@Override
			public int get() {
				return i;
			}
			
			@Override
			public void set(int t) {
				i = t;
			}
		};
		private final ArrayList<CLICKABLE> extra = new ArrayList<CLICKABLE>(
			new GTarget(80, false, true, type)
		);
		
		
		private final PlacableSimple next = new PlacableSimple(this.name()) {
			
			@Override
			public void place(int x, int y) {
				if (t.calcLow(0, sx, sy, x, y, ang, vel)) {
					SETT.PROJS().launch(sx, sy, 0, t, Projectile.ALL.getLast(), (byte)0, (byte)0, null);
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				return t.calcLow(0, sx, sy, x, y, ang, vel) ? null : E;
			}
			
			@Override
			public PLACABLE getUndo() {
				return Single.this;
			}
		};
		
		public Single() {
			super("projectile");
		}

		@Override
		public CharSequence isPlacable(int x, int y) {
			return SETT.PIXEL_IN_BOUNDS(x, y) ? null : E;
		}

		@Override
		public void place(int x, int y) {
			sx = x;
			sy = y;
			VIEW.s().tools.place(next);
		}
		
		@Override
		public LIST<CLICKABLE> getAdditionalButt() {
			return extra;
		}
		
		
	}
	
	private static class MASS extends PlacableSimple {

		
		final VectorImp vec = new VectorImp();
		int sx,sy;
		final Trajectory t = new Trajectory();
		private final PlacableSimple next = new PlacableSimple(this.name()) {
			
			@Override
			public void place(int x, int y) {
				if (t.calcLow(0, sx, sy, x, y, ang, vel)) {
					vec.set(sx, sy, x, y);
					vec.rotate90();
					for (int i = -8; i <= 8; i++) {
						int xx = (int) (sx+vec.nX()*i*C.TILE_SIZEH);
						int yy = (int) (sy+vec.nY()*i*C.TILE_SIZEH);
						SETT.PROJS().launch(xx, yy, 0, t, Projectile.ALL.getLast(), 0.05, (short)0, null);
					}
					
					
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				return t.calcLow(0, sx, sy, x, y, ang, vel) ? null : E;
			}
			
			@Override
			public PLACABLE getUndo() {
				return MASS.this;
			}
		};
		
		public MASS() {
			super("projectile mass");
		}

		@Override
		public CharSequence isPlacable(int x, int y) {
			return SETT.PIXEL_IN_BOUNDS(x, y) ? null : E;
		}

		@Override
		public void place(int x, int y) {
			sx = x;
			sy = y;
			VIEW.s().tools.place(next);
		}
		
		
	}
	
}
