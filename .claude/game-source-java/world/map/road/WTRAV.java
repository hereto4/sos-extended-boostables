package world.map.road;

import game.GAME;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import world.WORLD;

public class WTRAV {

	public static final int PORT_PENALTY = 10;
	
	public static boolean can(int fromX, int fromY, DIR d, boolean roaded) {
		
		if (!d.isOrtho() && WORLD.REGIONS().map.get(fromX, fromY) != WORLD.REGIONS().map.get(fromX+d.x(), fromY+d.y()))
			return false;
		
		TravTile from = get(fromX, fromY, roaded);
		int toX = fromX + d.x();
		int toY = fromY + d.y();
		TravTile to = get(toX, toY, roaded);
		return from.isPossible(fromX, fromY, toX, toY, to, d, roaded) && to.isPossible(toX, toY, fromX, fromY, from, d.perpendicular(), roaded);
	}
	
	public static boolean canLand(int fromX, int fromY, DIR d, boolean roaded) {
		if (!can(fromX, fromY, d, roaded))
			return false;
		int toX = fromX + d.x();
		int toY = fromY + d.y();
		
		if (WORLD.WATER().isBig.is(fromX, fromY) && WORLD.WATER().isBig.is(toX, toY))
			return false;
		return true;
	}
	
	public static int cost(int fromX, int fromY, DIR d) {
		if (WORLD.WATER().isBig.is(fromX, fromY)) {
			return 1;
		}
		int toX = fromX + d.x();
		int toY = fromY + d.y();
		if (WORLD.WATER().isBig.is(toX, toY))
			return PORT_PENALTY*2;
		
		if (WORLD.MOUNTAIN().coversTile(fromX, fromY))
			return 12;
		if (WORLD.FOREST().amount.get(fromX, fromY) == 1.0)
			return 4;
		return 3;
	}
	

	
	public static boolean isHarbour(int tx, int ty) {
		return get(tx, ty, false) == HARBOUR;
	}
	
	public static boolean isGoodLandTile(int tx, int ty) {
		return LAND.isPossible(tx, ty, false);
	}
	
	static abstract class TravTile {
		
		public final String name;
		
		TravTile(String name){
			this.name = name;
		}
		
		public abstract boolean isPossible(int tx, int ty, boolean roaded);
		public abstract boolean isPossible(int fromX, int fromY, int tx, int ty, TravTile to, DIR d, boolean roaded);
		public abstract void road(int fromX, int fromY, int tx, int ty, TravTile to, DIR d);
		public int extraCost(TravTile to) {
			return 0;
		}
	}
	
	static void makeRoad(PathTile dest) {

		PathTile t = dest;
		PathTile from = null;

		while (t != null) {

			
			
			if (WORLD.ROADS().placable.is(t)) {
				
				if (WORLD.WATER().isBig.is(t)) {
					if ((from != null && !WORLD.WATER().isBig.is(from)) || (t.getParent() != null && !WORLD.WATER().isBig.is(t.getParent()))) {
						boolean bridge = false;
						if (!WORLD.ROADS().is(t) && WORLD.ROADS().canBridge.is(t) && from != null && t.getParent() != null) {
							if (!WORLD.WATER().isBig.is(from) && !WORLD.WATER().isBig.is(t.getParent()))
								bridge = true;
							
						}
						
						WORLD.ROADS().set(t, true);
						if (bridge) {
							WORLD.ROADS().bridge.set(t, true);
						}
					}
				}else {
					WORLD.ROADS().set(t, true);
					if (from != null) {
						DIR d = DIR.get(from, t);
						
						
						if (!d.isOrtho()) {
							DIR d1 = d.next(-1);
							DIR d2 = d.next(1);
							if (LAND.isPossible(from.x()+d1.x(), from.y()+d1.y(), false) && WORLD.ROADS().is(from, d1)) {
								
							}else if (LAND.isPossible(from.x()+d2.x(), from.y()+d2.y(), false) && WORLD.ROADS().is(from, d2)) {
								
							}else if(LAND.isPossible(from.x()+d1.x(), from.y()+d1.y(), false)) {
								WORLD.ROADS().set(from, d1, true);
							}else if(LAND.isPossible(from.x()+d2.x(), from.y()+d2.y(), false)) {
								WORLD.ROADS().set(from, d2, true);
							}
						}
					}
					
				}
				
			}
			

			
			from = t;
			t = t.getParent();
			
			
			
		}
		
		
		checkProblem(dest);
	}
	
	
	
	static void checkProblem(PathTile dest) {
		PathTile to = dest;
				
		boolean problem = false;
		while (to.getParent() != null) {
			
			PathTile from = to;
			to = to.getParent();
			
			if (!WTRAV.can(from.x(), from.y(), DIR.get(from, to), true)) {
				if (!problem) {

					TravTile ff = get(from.x(), from.y(), true);
					TravTile tt = get(to.x(), to.y(), true);
					GAME.Notify("here" + " " + ff.name + " " + tt.name);
					problem = true;
				}
				
				LOG.ln(from + " -> " + to + " (" + dest + ")");
			}
		}
	}
	
	static int extracost(int fromX, int fromY, DIR d) {
		
		TravTile from = get(fromX, fromY, false);
		int toX = fromX + d.x();
		int toY = fromY + d.y();
		TravTile to = get(toX, toY, false);
		return from.extraCost(to);
	}
	
	static TravTile get(int tx, int ty, boolean roaded) {
		if (!WORLD.IN_BOUNDS(tx, ty))
			return NOTHING;
		if (LAND.isPossible(tx, ty, roaded))
			return LAND;
		else if (HARBOUR.isPossible(tx, ty, roaded))
			return HARBOUR;
		else if (WATER.isPossible(tx, ty, roaded))
			return WATER;
		return NOTHING;
	}
	
	final static TravTile LAND = new TravTile("Land") {
		
		@Override
		public boolean isPossible(int fromX, int fromY, int tx, int ty, TravTile to, DIR d, boolean roaded) {
			if (!isPossible(fromX, fromY, roaded))
				return false;
			if (to == LAND) {
				if (!LAND.isPossible(tx, ty, roaded))
					return false;
				if (!d.isOrtho()) {
					if (isPossible(fromX, ty, false) && isPossible(tx, fromY, false)) {
						if (roaded)
							return isPossible(fromX, ty, true) || isPossible(tx, fromY, true);
						return true;
					}
					return false;
				}
				return true;
			}else if (to == HARBOUR) {
				return d.isOrtho() && HARBOUR.isPossible(tx, ty, roaded);
			}
			return false;
		}
		
		@Override
		public boolean isPossible(int tx, int ty, boolean roaded) {
			if (!WORLD.IN_BOUNDS(tx, ty))
				return false;
			if (WORLD.MOUNTAIN().coversTile(tx, ty))
				return false;
			if (WORLD.WATER().isBig.is(tx, ty)) {
				return false;
			}
			if (roaded && (!WORLD.ROADS().is(tx, ty) || WORLD.ROADS().minified.is(tx, ty)))
				return false;
			return true;
		}
		
		@Override
		public int extraCost(TravTile to) {
			if (to == HARBOUR)
				return PORT_PENALTY;
			return 0;
		}

		@Override
		public void road(int fromX, int fromY, int tx, int ty, TravTile to, DIR d) {
			if (to == LAND || to == HARBOUR) {
				WORLD.ROADS().set(fromX, fromY, true);
				WORLD.ROADS().set(tx, ty, true);
				if (!d.isOrtho()) {
					if (isPossible(tx, fromY, true))
						return;
					if (isPossible(fromX, ty, true))
						return;
					
					if (isPossible(tx, fromY, false) && WORLD.REGIONS().map.get(tx, fromY) == WORLD.REGIONS().map.get(fromX, fromY))
						WORLD.ROADS().set(tx, fromY, true);
					else if (isPossible(fromX, ty, false))
						WORLD.ROADS().set(fromX, ty, true);
				}
				
			}
		};
	};


	
	final static TravTile HARBOUR = new TravTile("Harbour") {
		
		@Override
		public boolean isPossible(int fromX, int fromY, int tx, int ty, TravTile to, DIR d, boolean roaded) {
			
			
			if (!isPossible(fromX, fromY, roaded))
				return false;
			if (to == LAND) {
				if (!d.isOrtho())
					return false;
				return LAND.isPossible(tx, ty, roaded);
			
			}else if (to == WATER || to == this) {
				return true;
			}else if (roaded && WORLD.ROADS().is(fromX, fromY)) {
				return to == LAND;
			}
			return false;
		}
		
		@Override
		public boolean isPossible(int tx, int ty, boolean roaded) {
			if (WORLD.WATER().isBig.is(tx, ty) && canBe(tx, ty)) {
				if (roaded)
					return WORLD.ROADS().is(tx, ty);
				return true;
			}
			return false;
		}
		
		private boolean canBe(int tx, int ty) {
			if (WORLD.MOUNTAIN().coversTile(tx, ty))
				return false;
			if (WORLD.WATER().isBig.is(tx, ty)) {
				return ok(tx, ty, DIR.N) || ok(tx, ty, DIR.E);
			}
			return false;
		}
		
		private boolean ok(int tx, int ty, DIR d) {
			
			return WORLD.WATER().isBig.is(tx, ty, d) && WORLD.WATER().isBig.is(tx, ty, d.perpendicular()) && (!WORLD.WATER().isBig.is(tx, ty, d.next(2)) || !WORLD.WATER().isBig.is(tx, ty, d.perpendicular().next(2)));
		}

		@Override
		public void road(int fromX, int fromY, int tx, int ty, TravTile to, DIR d) {
			if (to == LAND)
				WORLD.ROADS().set(fromX, fromY, true);
		}
		
		
	};
	

	
	final static TravTile WATER = new TravTile("Water") {
		
		@Override
		public boolean isPossible(int fromX, int fromY, int tx, int ty, TravTile to, DIR d, boolean roaded) {
			if (!isPossible(fromX, fromY, roaded))
				return false;
			else if (to == HARBOUR) {
				if (!HARBOUR.isPossible(tx, ty, roaded))
					return false;
				if (!d.isOrtho()) {
					return WORLD.WATER().isBig.is(fromX, ty) && WORLD.WATER().isBig.is(tx, fromY);
				}
				return true;
			}else if (to == WATER) {
				if (!WATER.isPossible(tx, ty, roaded))
					return false;
				if (!d.isOrtho()) {
					return WORLD.WATER().isBig.is(fromX, ty) && WORLD.WATER().isBig.is(tx, fromY);
				}
				return true;
			}
			return false;
		}
		
		@Override
		public boolean isPossible(int tx, int ty, boolean roaded) {
			return WORLD.WATER().isBig.is(tx, ty);
		}

		@Override
		public void road(int fromX, int fromY, int tx, int ty, TravTile to, DIR d) {
			// TODO Auto-generated method stub
			
		}
	};
	
	final static TravTile NOTHING = new TravTile("Nothing") {
		
		@Override
		public boolean isPossible(int fromX, int fromY, int tx, int ty, TravTile to, DIR d, boolean roaded) {
			return false;
		}
		
		@Override
		public boolean isPossible(int tx, int ty, boolean roaded) {
			return false;
		}

		@Override
		public void road(int fromX, int fromY, int tx, int ty, TravTile to, DIR d) {
			// TODO Auto-generated method stub
			
		}
	};

	
}
