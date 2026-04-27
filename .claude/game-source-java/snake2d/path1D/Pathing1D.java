package snake2d.path1D;

public final class Pathing1D {

	private final PTile1D[] tiles;
	int id = 0;
	final RBTileTree tree = new RBTileTree();
	private Object user;

	public Pathing1D(int size){
		tiles = new PTile1D[size];
		for (int i = 0; i < size; i++)
			tiles[i] = new PTile1D(i);
	}
		
	public Pathing1D init(Object user) {
		if (this.user != null) {
			throw new RuntimeException("already in use by: " + this.user.toString());
		}
		this.user = user;
		id ++;
		if (id == 0){
			for (int i = 0; i < tiles.length; i++)
				tiles[i].pathId = 0;
			id = 1;
		}
		tree.clear();
		return this;
	}

	public void done() {
		user = null;
	}
	
	public PTile1D getTile(int index) {
		return tiles[index];
	}
	
	public PTile1D pushGreater(int index, double value){
		return pushGreater(index, value, null);
	}

	public PTile1D pushGreater(int index, double value, PTile1D parent){

		PTile1D t = tiles[index];
		
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
	
	public PTile1D pushSloppy(int index, double value){
		return pushSloppy(index, value, null);
	}
	
	public PTile1D pushSloppy(int index, double value, PTile1D parent){
		PTile1D t = tiles[index];
		
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
	
	public PTile1D pushSmaller(int index, double value){
		return pushSmaller(index, (float)value, null);
	}
	
	public PTile1D pushSmaller(int index, double value, PTile1D parent){
		
		PTile1D t = tiles[index];
		
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
	
	public boolean hasBeenPushed(int index) {
		PTile1D t = tiles[index];
		return t.pathId == id;
	}
	
	public void unclose(int index) {
		PTile1D t = tiles[index];
		t.closed = false;
	}
	
	

	/**
	 * 
	 * @return a tile that will be considered again.
	 */
	public PTile1D pollAndReopen() {
		PTile1D t = tree.pollGreatest();
		t.pathId = id -1;
		return t;
	}
	
	/**
	 * closes the tile
	 * @return the tile that has the highest value
	 */
	public PTile1D pollGreatest(){
		PTile1D t = tree.pollGreatest();
		t.closed = true;
		return t;
	}
	
	public int pushed() {
		return tree.size();
	}
	

	public PTile1D pollSmallest(){
		PTile1D t = tree.pollSmallest();
		t.closed = true;
		return t;
	}
	

	public void closeGreater(int index, double value) {
		
		PTile1D t = tiles[index];
		if (t.pathId == id && value > t.value)
			t.value = (float) value;
		t.pathId = id;
		t.closed = true;
	}
	

	public PTile1D close(int index, double value) {
		
		PTile1D t = tiles[index];
		
		t.value = (float) value;
		t.pathId = id;
		t.closed = true;
		return t;
	}
	

	public PTile1D close(int index, double value, PTile1D parent) {
		
		PTile1D t = tiles[index];;
		t.value = (float) value;
		t.pathId = id;
		t.closed = true;
		t.pathParent = parent;
		return t;
	}

	public float getValue(int index){
		PTile1D t = tiles[index];
		if (t.pathId == id){
			return t.value;
		}
		return 0;
	}

	public boolean hasMore() {
		return tree.size() > 0;
	}
	
	public float getValue2(int index) {
		PTile1D t = tiles[index];
		return t.value2;
	}

	public void setValue2(int index, double f) {
		PTile1D t = tiles[index];
		t.value2 = (float) f;
	}

	public PTile1D force(int index, float value, PTile1D parent) {
		PTile1D t = tiles[index];
		t.value = (float) value;
		t.pathId = id;
		t.closed = true;
		t.pathParent = parent;
		return t;
	}

	public PTile1D reverse(PTile1D t) {
		init(this);
		
		PTile1D p = t.pathParent;
		t.pathParent = null;
		t = reverse(p, t);
		
		done();


		return t;
	}

	private PTile1D reverse(PTile1D t, PTile1D newparent) {
		if (t == null)
			return newparent;
		PTile1D parent = t.pathParent;
		t.pathParent = newparent;
		return reverse(parent, t);
		
	}
	

	

	
}
