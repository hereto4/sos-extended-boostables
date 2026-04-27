package snake2d;

import snake2d.util.datatypes.COORDINATE;

public class PathTile implements Comparable<PathTile>, COORDINATE {
	// 12
	final short x; // 2
	final short y; // 2
	float accCost; // 4
	float value; // 4
	PathTile pathParent; // 4
	int pathId = 0; // 4
	boolean closed; // 1
					// 33
	PathTile left; // 4
	PathTile right; // 4
	PathTile parent; // 4
	boolean color; // 1
					// 34 + 12 = 46 = 64
	// possible: // 2+2+4+4+1+4+4+4+4 = 29

	PathTile(short x, short y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int compareTo(PathTile o) {
		if (o == this)
			return 0;
		return value < o.value ? -1 : 1;
	}

	public float getValue() {
		return value;
	}

	public float getValue2() {
		return accCost;
	}

	public void setValue2(double v) {
		accCost = (float) v;
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	public PathTile getParent() {
		return pathParent;
	}

	public final int parents() {
		int p = 0;
		PathTile pa = pathParent;
		while (pa != null) {
			p++;
			pa = pa.pathParent;
		}
		return p;
	}
	
	public void parentSet(PathTile p) {
		this.pathParent = p;
	}
	
	@Override
	public String toString() {
		return "PathTile: (" + x + ", " + y + ")";
	}

}