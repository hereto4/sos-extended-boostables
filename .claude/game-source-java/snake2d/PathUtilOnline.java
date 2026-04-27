package snake2d;



import snake2d.PathGame.COST;
import snake2d.PathGame.DEST;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLEE;
import snake2d.util.datatypes.Rec;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_INTE;
import snake2d.util.map.MAP_SETTER;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;



public final class PathUtilOnline {

	private final PathTile[][] tiles;
	int id = 0;
	final RBTileTree tree = new RBTileTree();
	private final Flooder flooder = new Flooder();
	public final Filler filler;
	public final AStar astar = new AStar();
	public final Marker marker = new Marker();
	private final RECTANGLEE bounds;
	private Object user;

	public PathUtilOnline(int size){
		tiles = new PathTile[size][size];
		bounds = new Rec(0, size, 0, size);
		for (int y = 0; y < tiles.length; y++)
			for (int x = 0; x < tiles.length; x++)
				tiles[y][x] = new PathTile((short)x, (short)y);
		filler = new Filler();
	}
	
	private void lock(Object user) {
		if (this.user != null) {
			throw new RuntimeException("already in use by: " + this.user.toString());
		}
		this.user = user;
		id ++;
		if (id == 0){
			for (int y = 0; y < tiles.length; y++)
				for (int x = 0; x < tiles.length; x++)
					tiles[y][x].pathId = 0;
			id = 1;
		}
	}
	
	public boolean isLocked() {
		return user != null;
	}
	
	private void unlock() {
		user = null;
	}
	
	public PathTile getTile(int x, int y){
		return tiles[y][x];
	}
	
	public Flooder getFlooder(){
		return flooder;
	}
	
	public class Flooder {
		
		Flooder(){
			
		}
		
		/**
		 * Must be called before starting the use of this utility.
		 */
		public void init(Object user){
			lock(user);
			tree.clear();
		}
		
		public void done() {
			unlock();
		}
		
		/**
		 * push a tile. If the tile already has been pushed and not closed,
		 * the tile will only be pushed if it's value is GREATER than the
		 * already pushed tile.
		 * @param x
		 * @param y
		 * @param value
		 */
		public PathTile pushGreater(int x, int y, double value){
			return pushGreater(x, y, value, null);
		}
		
		public PathTile pushGreater(COORDINATE c, double value){
			return pushGreater(c.x(), c.y(), value, null);
		}
		
		public PathTile pushGreater(COORDINATE c, DIR d, double value){
			return pushGreater(c.x()+d.x(), c.y()+d.y(), value, null);
		}
		
		public PathTile pushGreater(int x, int y, DIR d, double value){
			return pushGreater(x+d.x(), y+d.y(), value, null);
		}
		
		/**
		 * push a tile. If the tile already has been pushed and not closed,
		 * the tile will only be pushed if it's value is GREATER than the
		 * already pushed tile.
		 * @param x
		 * @param y
		 * @param value
		 * @param parent
		 */
		public PathTile pushGreater(int x, int y, double value, PathTile parent){
			
			if (!bounds.holdsPoint(x, y))
				return null;
			
			PathTile t = getTile(x, y);
			
			if (t.pathId == id){
				if (t.value >= value)
					return t;
				if (t.closed)
					return t;
				tree.remove(t);
			}
			
			t.pathId = id;
			t.closed = false;
			t.value = (float) value;
			t.pathParent = parent;
			tree.put(t);
			return t;
		}
		
		public void setValue(int tx, int ty, double v) {
			getTile(tx, ty).value = (float) v;
		}
		
		public boolean hasBeenPushed(int tx, int ty) {
			if (!bounds.holdsPoint(tx, ty))
				return true;
			PathTile t = getTile(tx, ty);
			return t.pathId == id;
		}
		
		
		
		public void unclose(int tx, int ty) {
			PathTile t = getTile(tx, ty);
			t.closed = false;
		}
		
		public boolean hasBeenPushed(COORDINATE c) {
			return hasBeenPushed(c.x(), c.y());
		}
		
		public boolean hasBeenPushed(COORDINATE c, DIR d) {
			return hasBeenPushed(c.x()+d.x(), c.y()+d.y());
		}
		
		public boolean hasBeenPushed(int tx, int ty, DIR d) {
			return hasBeenPushed(tx+d.x(), ty+d.y());
		}
		
		/**
		 * Push a tile. If the tile is already pushed, nothing will be pushed
		 * @param x
		 * @param y
		 * @param value
		 */
		public PathTile pushSloppy(int x, int y, double value){
			return pushSloppy(x, y, value, null);
		}
		
		public PathTile pushSloppy(COORDINATE c, double value){
			return pushSloppy(c.x(), c.y(), value, null);
		}
		
		public PathTile pushSloppy(COORDINATE c, DIR d, double value){
			return pushSloppy(c.x()+d.x(), c.y()+d.y(), value, null);
		}
		
		public PathTile pushSloppy(int x, int y, DIR d, double value){
			return pushSloppy(x+d.x(), y+d.y(), value, null);
		}
		
		public PathTile pushSloppy(int x, int y, DIR d, double value, PathTile parent){
			return pushSloppy(x+d.x(), y+d.y(), value, parent);
		}
		
		/**
		 * Push a tile. If the tile is already pushed, nothing will be pushed
		 * @param x
		 * @param y
		 * @param value
		 * @param parent
		 */
		public PathTile pushSloppy(int x, int y, double value, PathTile parent){
			
			if (!bounds.holdsPoint(x, y))
				return null;
			
			PathTile t = getTile(x, y);
			
			if (t.pathId == id){
				return null;
			}
			
			t.pathId = id;
			t.value = (float)value;
			t.pathParent = parent;
			tree.put(t);
			t.closed = true;
			return t;
		}
		
		/**
		 * push a tile. If the tile already has been pushed and not closed,
		 * the tile will only be pushed if it's value is SMALLER than the
		 * already pushed tile.
		 * @param x
		 * @param y
		 * @param value
		 */
		public PathTile pushSmaller(int x, int y, double value){
			return pushSmaller(x, y, (float)value, null);
		}
		
		public PathTile pushSmaller(COORDINATE c, double value){
			return pushSmaller(c.x(), c.y(), (float)value, null);
		}
		
		public PathTile pushSmaller(int x, int y, DIR d, double value){
			return pushSmaller(x+d.x(), y+d.y(), (float)value, null);
		}
		
		public PathTile pushSmaller(int x, int y, DIR d, double value, PathTile parent){
			return pushSmaller(x+d.x(), y+d.y(), (float)value, parent);
		}
		
		public PathTile pushSmaller(COORDINATE c, double value, PathTile parent){
			return pushSmaller(c.x(), c.y(), (float)value, parent);
		}
		
		public PathTile pushSmaller(COORDINATE c, DIR d, double value){
			return pushSmaller(c.x()+d.x(), c.y()+d.y(), (float)value, null);
		}
		
		public PathTile pushSmaller(COORDINATE c, DIR d, double value, PathTile parent){
			return pushSmaller(c.x()+d.x(), c.y()+d.y(), (float)value, parent);
		}		
		
		/**
		 * push a tile. If the tile already has been pushed and not closed,
		 * the tile will only be pushed if it's value is SMALLER than the
		 * already pushed tile.
		 * @param x
		 * @param y
		 * @param value
		 * @param parent
		 */
		public PathTile pushSmaller(int x, int y, double value, PathTile parent){
			
			PathTile t = getTile(x, y);
			
			if (t.pathId == id){
				if (t.value <= value)
					return null;
				if (t.closed)
					return null;
				tree.remove(t);
			}
			
			t.pathId = id;
			t.closed = false;
			t.value = (float)value;
			t.pathParent = parent;
			tree.put(t);
			return t;
			
		}
		
		/**
		 * 
		 * @return a tile that will be considered again.
		 */
		public PathTile pollAndReopen() {
			PathTile t = tree.pollGreatest();
			t.pathId = id -1;
			return t;
		}
		
		/**
		 * closes the tile
		 * @return the tile that has the highest value
		 */
		public PathTile pollGreatest(){
			PathTile t = tree.pollGreatest();
			t.closed = true;
			return t;
		}
		
		public int pushed() {
			return tree.size();
		}
		
		/**
		 * closes the tile
		 * @return the tile that has the lowest value
		 */
		public PathTile pollSmallest(){
			PathTile t = tree.pollSmallest();
			t.closed = true;
			return t;
		}
		
		/**
		 * if tile at x,y has been pushed, set it's value to value if value is
		 * greater that x,y's.
		 * @param x
		 * @param y
		 * @param value
		 */
		public void closeGreater(int x, int y, double value) {
			
			PathTile t = tiles[y][x];
			if (t.pathId == id && value > t.value)
				t.value = (float) value;
			t.pathId = id;
			t.closed = true;
		}
		
		/**
		 * closes this tile and sets its value
		 * @param x
		 * @param y
		 * @param value
		 */
		public PathTile close(int x, int y, double value) {
			
			PathTile t = tiles[y][x];
			
			t.value = (float) value;
			t.pathId = id;
			t.closed = true;
			return t;
		}
		
		public PathTile close(COORDINATE c, DIR d, double value) {
			return close(c.x()+d.x(), c.y()+d.y(), value);
		}
		
		/**
		 * closes this tile and sets its value
		 * @param x
		 * @param y
		 * @param value
		 */
		public PathTile close(int x, int y, double value, PathTile parent) {
			
			PathTile t = tiles[y][x];
			t.value = (float) value;
			t.pathId = id;
			t.closed = true;
			t.pathParent = parent;
			return t;
		}
		
		/**
		 * get value of tile
		 * @param x
		 * @param y
		 * @return
		 */
		public float getValue(int x, int y){
			PathTile t = getTile(x, y);
			if (t.pathId == id){
				return t.value;
			}
			return 0;
		}
		
		public PathTile get(int x, int y){
			PathTile t = getTile(x, y);
			if (t.pathId == id){
				return t;
			}
			return null;
		}
		
		public float getValue(COORDINATE c){
			return getValue(c.x(), c.y());
		}

		/**
		 * Must be checked before polling operations
		 * @return
		 */
		public boolean hasMore() {
			return tree.size() > 0;
		}
		
		public float getValue2(int x, int y) {
			PathTile t = getTile(x, y);
			return t.getValue2();
		}
		
		public float getValue2(int x, int y, DIR d) {
			PathTile t = getTile(x+d.x(), y+d.y());
			return t.getValue2();
		}
		
		public void setValue2(COORDINATE c, double f) {
			setValue2(c.x(), c.y(), f);
		}
		
		public void setValue2(COORDINATE c, DIR d, double f) {
			setValue2(c.x()+d.x(), c.y()+d.y(), f);
		}
		
		public void setValue2(int x, int y, double f) {
			PathTile t = getTile(x, y);
			t.setValue2(f);
		}
		
		public void setValue2(int x, int y, DIR d, double f) {
			setValue2(x+d.x(), y+d.y(), f);
		}

		public PathTile force(short x, short y, float value, PathTile parent) {
			PathTile t = tiles[y][x];
			t.value = (float) value;
			t.pathId = id;
			t.closed = true;
			t.pathParent = parent;
			return t;
		}

		public PathTile reverse(PathTile t) {
			init(this);
			
			PathTile p = t.pathParent;
			t.pathParent = null;
			t = reverse(p, t);
			
			done();


			return t;
		}

		private PathTile reverse(PathTile t, PathTile newparent) {
			if (t == null)
				return newparent;
			PathTile parent = t.pathParent;
			t.pathParent = newparent;
			return reverse(parent, t);
			
		}
		
		public void reopen(PathTile t) {
			if (!t.closed)
				throw new RuntimeException();
			t.closed = false;
			t.pathId = id-1;
		}
		
	}
	
	public class Filler {
		
		private PathTile last;
		
		public Filler(){
			
		}
		
		/**
		 * Must be called before starting the use of this utility.
		 */
		public void init(Object user){
			lock(user);
			last = null;
		}
		
		public void done() {
			unlock();
		}

		public boolean fill(COORDINATE c) {
			return fill(c.x(), c.y());
		}
		
		public boolean fill(COORDINATE c, DIR d) {
			return fill(c.x()+d.x(), c.y()+d.y());
		}
		
		public boolean fill(int x, int y, DIR d) {
			return fill(x+d.x(), y+d.y());
		}
		
		public boolean fill(int x, int y){
			
			if (!bounds.holdsPoint(x, y))
				return false;
			
			PathTile t = getTile(x, y);
			
			if (t.pathId == id){
				return false;
			}
			
			t.pathId = id;
			
			if (last == null) {
				t.pathParent = null;
			}else {
				t.pathParent = last;
			}
			last = t;
			return true;
		}
	
		public final MAP_SETTER filler = new MAP_SETTER() {
			
			@Override
			public MAP_SETTER set(int tx, int ty) {
				fill(tx, ty);
				return this;
			}
			
			@Override
			public MAP_SETTER set(int tile) {
				throw new RuntimeException();
			}
		};
		
		public final MAP_SETTER closer = new MAP_SETTER() {
			
			@Override
			public MAP_SETTER set(int x, int y) {
				if (!bounds.holdsPoint(x, y))
					return this;
				
				PathTile t = getTile(x, y);
				t.pathId = id;
				return this;
			}
			
			@Override
			public MAP_SETTER set(int tile) {
				throw new RuntimeException();
			}
		};
		
		public final MAP_DOUBLEE value = new MAP_DOUBLEE() {
			
			
			@Override
			public double get(int tx, int ty) {
				return getTile(tx, ty).value;
			}
			
			@Override
			public double get(int tile) {
				throw new RuntimeException();
			}
			
			@Override
			public MAP_DOUBLEE set(int tx, int ty, double value) {
				getTile(tx, ty).value = (float) value;
				return this;
			}
			
			@Override
			public MAP_DOUBLEE set(int tile, double value) {
				throw new RuntimeException();
			}
		};
		
		public MAP_BOOLEAN isser = new MAP_BOOLEAN() {
			
			@Override
			public boolean is(int tx, int ty) {
				return bounds.holdsPoint(tx, ty) && getTile(tx, ty).pathId == id;
			}
			
			@Override
			public boolean is(int tile) {
				throw new RuntimeException();
			}
		};
		
		public boolean hasMore() {
			return last != null;
		}
		
		public COORDINATE poll() {
			PathTile t = last;
			last = t.pathParent;
			return t;
		}
		
		public boolean isFilled(int tx, int ty) {
			return getTile(tx, ty).pathId == id;
		}
		
	}
	
	public final class Marker implements MAP_BOOLEANE{
		
		
		public Marker(){
			
		}
		
		/**
		 * Must be called before starting the use of this utility.
		 */
		public void init(Object user){
			lock(user);
		}
		
		public final MAP_DOUBLEE v1 = new MAP_DOUBLEE() {
			
			@Override
			public double get(int tx, int ty) {
				if (!bounds.holdsPoint(tx, ty))
					return 0;
				PathTile t = tiles[ty][tx];
				return t.value;
			}
			
			@Override
			public double get(int tile) {
				int x = tile%bounds.width();
				int y = tile/bounds.width();
				return get(x, y);
			}
			
			@Override
			public MAP_DOUBLEE set(int tx, int ty, double value) {
				if (!bounds.holdsPoint(tx, ty))
					return this;
				PathTile t = tiles[ty][tx];
				t.value = (float) value;
				return this;
			}
			
			@Override
			public MAP_DOUBLEE set(int tile, double value) {
				int x = tile%bounds.width();
				int y = tile/bounds.width();
				return set(x, y, value);
			}
		};
		
		public final MAP_DOUBLEE v2 = new MAP_DOUBLEE() {
			
			@Override
			public double get(int tx, int ty) {
				if (!bounds.holdsPoint(tx, ty))
					return 0;
				PathTile t = tiles[ty][tx];
				return t.getValue2();
			}
			
			@Override
			public double get(int tile) {
				int x = tile%bounds.width();
				int y = tile/bounds.width();
				return get(x, y);
			}
			
			@Override
			public MAP_DOUBLEE set(int tx, int ty, double value) {
				if (!bounds.holdsPoint(tx, ty))
					return this;
				PathTile t = tiles[ty][tx];
				t.setValue2(value);
				return this;
			}
			
			@Override
			public MAP_DOUBLEE set(int tile, double value) {
				int x = tile%bounds.width();
				int y = tile/bounds.width();
				return set(x, y, value);
			}
		};
		
		public final MAP_INTE ii = new MAP_INTE() {
			
			@Override
			public int get(int tx, int ty) {
				if (!bounds.holdsPoint(tx, ty))
					return 0;
				PathTile t = tiles[ty][tx];
				return t.pathId;
			}
			
			@Override
			public int get(int tile) {
				int x = tile%bounds.width();
				int y = tile/bounds.width();
				return get(x, y);
			}
			
			@Override
			public MAP_INTE set(int tx, int ty, int value) {
				if (!bounds.holdsPoint(tx, ty))
					return this;
				PathTile t = tiles[ty][tx];
				t.pathId = value;
				return this;
			}
			
			@Override
			public MAP_INTE set(int tile, int value) {
				int x = tile%bounds.width();
				int y = tile/bounds.width();
				return set(x, y, value);
			}
		};
		
		
		public void done() {
			unlock();
		}

		@Override
		public boolean is(int tile) {
			int x = tile%bounds.width();
			int y = tile/bounds.width();
			return is(x, y);
		}

		@Override
		public boolean is(int tx, int ty) {
			if (!bounds.holdsPoint(tx, ty))
				return true;
			PathTile t = tiles[ty][tx];
			return t.pathId == id;
		}

		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			int x = tile%bounds.width();
			int y = tile/bounds.width();
			return set(x, y, value);
		}

		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			if (!bounds.holdsPoint(tx, ty))
				return this;
			PathTile t = tiles[ty][tx];
			t.pathId = id;
			return this;
		}
	}
	
	private final LIST<DIR> ortho = new ArrayList<DIR>(DIR.ORTHO);
	private final LIST<DIR> northo = new ArrayList<DIR>(DIR.ALL);
	
	public class AStar {
		
		private AStar(){
			
		}
		
		private final SHORTEST s = new SHORTEST();
		
		public void close(int x, int y){
			PathTile t = getTile(x, y);
			t.pathId = id;
			t.closed = true;
		}
		
		public final boolean getShortest(PathGame.PathFancy p, COST cost, int startX, int startY, int endX, int endY){
			return getShortest(p, cost, startX, startY, endX, endY, false);
		}
		
		public final boolean getShortest(PathGame.PathFancy p, COST cost, int startX, int startY, int endX, int endY, boolean includeLast){
			return getShortest(p, cost, startX, startY, endX, endY, includeLast, northo);
		}
		
		public final boolean getShortestNoDiagonal(PathGame.PathFancy p, COST cost, int startX, int startY, int endX, int endY){
			return getShortest(p, cost, startX, startY, endX, endY, true, ortho);
		}
		
		public final PathTile getShortest(COST cost, int startX, int startY, int endX, int endY){
			return getShortest(cost, startX, startY, endX, endY, false);
		}
		
		public final PathTile getShortest(COST cost, int startX, int startY, int endX, int endY, boolean includeLast){
			return getShortest(cost, startX, startY, endX, endY, includeLast, northo);
		}
		
		public final PathTile getShortestNoDiagonal(COST cost, int startX, int startY, int endX, int endY){
			return getShortest(cost, startX, startY, endX, endY, true, ortho);
		}
		
		private final boolean getShortest(PathGame.PathFancy p, COST cost, int startX, int startY, int endX, int endY, boolean includeLast, LIST<DIR> dirs){
			if (startX == endX && startY == endY) {
				p.setOne(startX, startY);
				return true;
			}
			PathTile t = getShortest(cost, startX, startY, endX, endY, includeLast, dirs);
			if (t != null) {
				p.set(t);
				return true;
			}
			return false;
			
		}
		
		private final PathTile getShortest(COST cost, int startX, int startY, int endX, int endY, boolean includeLast, LIST<DIR> dirs){
			if (!bounds.holdsPoint(endX, endY))
				return null;
			if (!bounds.holdsPoint(startX, startY))
				return null;
			
			SHORTEST dest = s;
			dest.set(endX, endY);
			return find(cost, dest, startX, startY, includeLast, dirs);
			
		}
		
		public final boolean getNearest(PathGame.PathFancy p, COST cost, DEST dest, int startX, int startY){
			PathTile t = find(cost, dest, startX, startY, true, northo);
			if (t != null) {
				p.set(t);
				return true;
			}
			return false;
		}
		
		
		public PathTile find(COST cost, DEST dest, int startX, int startY, boolean includeLast, LIST<DIR> dirs){
			lock(null);
			tree.clear();
			PathTile t = getTile(startX, startY);
			add2OpenSet(t, null, 0, dest);
			
			while(tree.size() > 0){
				t = tree.pollSmallest();
				int x = t.x;
				int y = t.y;
				t.closed = true;
				
				if (includeLast && dest.isDest(x, y)){
					return t;
				}
				
				for (DIR dir : dirs){
					
					int xtemp = x + dir.x();
					int ytemp = y + dir.y();
					
					if (!bounds.holdsPoint(xtemp, ytemp))
						continue;
					
					if (!includeLast && dest.isDest(xtemp, ytemp)){
						return t;
					}
					
					PathTile next = getTile(xtemp, ytemp);
					if (next.pathId == id && next.closed) {
						continue;
					}
					double tempCost = cost.getCost(x, y, xtemp, ytemp);
					if (tempCost < 0){
						if (tempCost == COST.BLOCKED){
							close(xtemp,ytemp);
						}
						continue;
					}
					tempCost *= dir.tileDistance();
					tempCost += t.accCost;
					
					if (next.pathId == id){
						if (tempCost >= next.accCost)
							continue;
						tree.remove(next);
					}	
					add2OpenSet(next, t, tempCost, dest);
				}
			}
			
			
			return null;
		}
		
		private PathTile add2OpenSet(PathTile t, PathTile parent, double accCost, DEST method){
			t.accCost = (float) accCost;
			t.value = (float) (accCost + method.getOptDistance(t.x, t.y));
			t.pathId = id;
			t.closed = false;
			t.pathParent = parent;
			tree.put(t);
			return t;
		}
		
	}
	
	final static class SHORTEST extends DEST{

		int destX,destY;
		private final static float SQRT2 = (float) Math.sqrt(2);
		
		public final void set(int destX, int destY){
			this.destX = destX;
			this.destY = destY;
		}
		
		@Override
		protected final float getOptDistance(int x, int y) {
			x = Math.abs(x-destX);
			y = Math.abs(y-destY);
			
			if (x > y){
				return SQRT2*y + x-y;
			}else if(x < y){
				return SQRT2*x + y-x;
			}else{
				return SQRT2*x;
			}
		}

		@Override
		protected final boolean isDest(int x, int y) {
			return x == destX && y == destY;
		}
		
		
	}
	
}
