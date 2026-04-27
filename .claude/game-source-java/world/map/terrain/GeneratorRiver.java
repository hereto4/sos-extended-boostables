package world.map.terrain;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.MOUNTAIN;
import static world.WORLD.THEIGHT;
import static world.WORLD.TWIDTH;
import static world.WORLD.WATER;

import java.util.Collections;
import java.util.LinkedList;

import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import world.WORLD;

class GeneratorRiver {

	private static int dX;
	private static int dY;
	private static int trials = 0;
	private static int maxLength;
	private static boolean fromOcean = false;

	GeneratorRiver() {
		largeRivers();
		smallRivers();
	}

	private void smallRivers() {
		
		LinkedList<Coo> coo = new LinkedList<Coo>();

		for (int y = 0; y < THEIGHT(); y++) {
			for (int x = 0; x < TWIDTH(); x++) {
				
				if (smallStart(x, y) != null) {
					coo.add(new Coo(x, y));
				}
			}
		}

		Collections.shuffle(coo);
		
		double am = 200*WORLD.TAREA()/(224.0*224.0);
		
		while (am-- > 0 && !coo.isEmpty()) {
			Coo c = coo.removeFirst();
			
			if (smallRiver(c.x(), c.y()))
				;
			
			
		}
	}
	
	private DIR smallStart(int tx, int ty) {
		if (!WORLD.WATER().is(tx, ty))
			return null;
		
		int ri = RND.rInt(DIR.ORTHO.size());
		
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR d = DIR.ORTHO.getC(ri+i);
			
			if (!IN_BOUNDS(tx, ty, d))
				continue;
			if (!MOUNTAIN().is(tx+d.x(), ty+d.y()) && WATER().get(tx, ty, d) == WATER().NOTHING)
				return d;
		}
		return null;
	}
	
	private boolean smallRiver(int sx, int sy) {
		
		DIR d = smallStart(sx, sy);
		if (d == null)
			return false;
		
		sx += d.x();
		sy += d.y();
		

		if (!IN_BOUNDS(sx, sy))
			return false;
		if (MOUNTAIN().is(sx, sy))
			return false;
		if (WATER().get(sx, sy) != WATER().NOTHING)
			return false;
		
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d2 = DIR.ORTHO.get(di);
			if (WATER().RIVER_SMALL.is(sx, sy, d2))
				return false;
		}
		
		int straights = 2;
		
		return smallRiver(sx, sy, d, straights, 24);
	}
	
	private boolean smallRiver(int tx, int ty, DIR dir, int straights, int length){
		if (length-- < 0)
			return false;
		if (!WORLD.IN_BOUNDS(tx, ty))
			return smallSuccess(tx, ty);
		if (MOUNTAIN().is(tx, ty))
			return false;
		
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d2 = DIR.ORTHO.get(di);
			if (d2.perpendicular() == dir)
				continue;
			if (WATER().get(tx, ty, d2) != WATER().NOTHING) {
				return smallSuccess(tx, ty);
			}
		}
		
		straights --;
		if (straights <= 0) {
			dir = dir.next(2*RND.rInt0(1));
			straights = 3;
		}
		
		if (smallRiver(tx+dir.x(), ty+dir.y(), dir, straights, length)) {
			return smallSuccess(tx, ty);
		}
		return false;
	}
	
	private boolean smallSuccess(int tx, int ty) {
		WATER().RIVER_SMALL.placeRaw(tx, ty);
		return true;
	}
	
	static int largeRivers() {

		
		double am = 10*WORLD.TAREA()/(224.0*224.0);
		
		
		
		LinkedList<Coo> coo = new LinkedList<Coo>();

		for (int y = 0; y < THEIGHT(); y++) {
			for (int x = 0; x < TWIDTH(); x++) {
				if (isDeltable(x, y)) {
					coo.add(new Coo(x, y));
				}
			}
		}

		Collections.shuffle(coo);

		int x;
		int y;

		while (!coo.isEmpty() && am > 0) {

			Coo v = coo.removeFirst();

			y = v.y();
			x = v.x();

			if (WATER().RIVER.is(x, y))
				continue;

			maxLength = 16 + RND.rInt(TWIDTH() - 1);

			if (WATER().has.is(x, y - 1)) {
				dX = 0;
				dY = 1;
			} else if (WATER().has.is(x, y + 1)) {
				dX = 0;
				dY = -1;
			} else if (WATER().has.is(x - 1, y)) {
				dX = 1;
				dY = 0;
			} else if (WATER().has.is(x + 1, y)) {
				dX = -1;
				dY = 0;
			} else {
				continue;
			}

			fromOcean = WATER().bordersCount(x, y, WATER().OCEAN.normal) == 1;

			trials = 0;

			if (start(y, x, 0, false, false, 0)) {
				placeDelta(x, y);
				am--;
			}

		}

		for (int i = 0; i < 75; i++) {
			x = RND.rInt(TWIDTH());
			y = RND.rInt(THEIGHT());
			dX = -1 + RND.rInt(3);
			dY = dX == 0 ? -1 + RND.rInt(3) : 0;
			trials = 0;
			maxLength = 16 + RND.rInt(TWIDTH() - 1);
			branch(y, x, 0, false, false, 0);
		}
		
		return 0;
	}

	private static void placeDelta(int x, int y) {
		if (WATER().LAKE.delta.isPlacable(x, y, null, null) == null) {
			WATER().LAKE.delta.placeRaw(x, y);
		} else {
			WATER().OCEAN.delta.placeRaw(x, y);
		}
	}

	private static boolean isDeltable(int x, int y) {
		if (WATER().LAKE.delta.isPlacable(x, y, null, null) == null || WATER().OCEAN.delta.isPlacable(x, y, null, null) == null) {
			return true;
		}
		return false;
	}

	private static boolean start(int y, int x, int straight, boolean left, boolean right, int length) {

		trials++;

		if (length > maxLength)
			return true;

		if (WATER().has.is(x, y))
			return false;

		if (trials > 1000)
			return false;

		if (length > 0 && isDeltable(x, y)) {
			if (length < 3)
				return false;
			if (fromOcean && WATER().OCEAN.normal.is(x, y))
				return false;
			if (straight <= 1)
				return false;
			if (WATER().borders(x, y, WATER().RIVER))
				return false;
			if (dY == 1 && (WATER().has.is(x, y + 1))) {
				placeDelta(x, y);
				return true;
			} else if (dY == -1 && (WATER().has.is(x, y - 1))) {
				placeDelta(x, y);
				return true;
			} else if (dX == 1 && WATER().has.is(x + 1, y)) {
				placeDelta(x, y);
				return true;
			} else if (dX == -1 && WATER().has.is(x - 1, y)) {
				placeDelta(x, y);
				return true;
			}
			return false;
		}

		if (fromOcean && length > 2 && WATER().borders(x, y, WATER().OCEAN.normal))
			return false;

		if ((x == TWIDTH() - 1 || y == THEIGHT() - 1 || x == 0 || y == 0)) {
			if (length < 3) {
				return false;
			}
			WATER().RIVER.placeRaw(x, y);
			return true;
		}

		if (WORLD.MOUNTAIN().is(x, y) && length > 3 && RND.rInt(8) == 0)
			return true;

		if (WATER().borders(x, y, WATER().RIVER)) {
			if (length > 4 ) {
				WATER().RIVER.placeRaw(x, y);
				return true;
			}
			return false;
		}

		if (straight == 2) {
			straight = 0;
			left = true;
			right = true;
		}

		if (dX == 0) {
			if (RND.rInt(4) == 1 && right) {
				if (start(y, x + 1, 0, false, true, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else if (RND.rInt(4) == 1 && left) {
				if (start(y, x - 1, 0, true, false, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else {
				if (start(y + dY, x, straight + 1, left, right, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				} else {
					if (RND.rBoolean()) {
						if (right && start(y, x + 1, 0, false, true, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					} else {
						if (left && start(y, x - 1, 0, true, false, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					}

				}
			}
		}

		if (dY == 0) {
			if (RND.rInt(4) == 1 && right) {
				if (start(y + 1, x, 0, false, true, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else if (RND.rInt(4) == 1 && left) {
				if (start(y - 1, x, 0, true, false, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else {
				if (start(y, x + dX, straight + 1, left, right, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				} else {
					if (RND.rBoolean()) {
						if (right && start(y + 1, x, 0, false, true, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					} else {
						if (left && start(y - 1, x, 0, true, false, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					}

				}
			}
		}

		return false;

	}
	
	private static boolean branch(int y, int x, int straight, boolean left, boolean right, int length) {

		trials++;

		if (WATER().RIVER.is(x, y))
			return true;
		
		if (WATER().has.is(x, y))
			return false;

		if (trials > 1000)
			return false;

		if ((x == TWIDTH() - 1 || y == THEIGHT() - 1 || x == 0 || y == 0)) {
			if (length < 3) {
				return false;
			}
			WATER().RIVER.placeRaw(x, y);
			return true;
		}

		if (WORLD.MOUNTAIN().is(x, y) && length > 3 && RND.rInt(8) == 0)
			return true;

		if (WATER().borders(x, y, WATER().RIVER)) {
			if (length > 4 ) {
				WATER().RIVER.placeRaw(x, y);
				return true;
			}
			return false;
		}

		if (straight == 2) {
			straight = 0;
			left = true;
			right = true;
		}

		if (dX == 0) {
			if (RND.rInt(4) == 1 && right) {
				if (start(y, x + 1, 0, false, true, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else if (RND.rInt(4) == 1 && left) {
				if (start(y, x - 1, 0, true, false, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else {
				if (start(y + dY, x, straight + 1, left, right, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				} else {
					if (RND.rBoolean()) {
						if (right && start(y, x + 1, 0, false, true, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					} else {
						if (left && start(y, x - 1, 0, true, false, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					}

				}
			}
		}

		if (dY == 0) {
			if (RND.rInt(4) == 1 && right) {
				if (start(y + 1, x, 0, false, true, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else if (RND.rInt(4) == 1 && left) {
				if (start(y - 1, x, 0, true, false, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				}
			} else {
				if (start(y, x + dX, straight + 1, left, right, length + 1)) {
					WATER().RIVER.placeRaw(x, y);
					return true;
				} else {
					if (RND.rBoolean()) {
						if (right && start(y + 1, x, 0, false, true, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					} else {
						if (left && start(y - 1, x, 0, true, false, length + 1)) {
							WATER().RIVER.placeRaw(x, y);
							return true;
						}
					}

				}
			}
		}

		return false;

	}
	

}
