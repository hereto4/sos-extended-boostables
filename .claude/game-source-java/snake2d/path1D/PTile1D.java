package snake2d.path1D;

public class PTile1D {

	public final int index; // 2
	public float value2; // 4
	float value; // 4
	public PTile1D pathParent; // 4
	int pathId = 0; // 4
	boolean closed; // 1
	PTile1D left; // 4
	PTile1D right; // 4
	PTile1D parent; // 4
	boolean color; // 1

	PTile1D(int index) {
		this.index = index;
	}

	public float getValue() {
		return value;
	}

	public PTile1D getParent() {
		return pathParent;
	}

	public final int parents() {
		int p = 0;
		PTile1D pa = pathParent;
		while (pa != null) {
			p++;
			pa = pa.pathParent;
		}
		return p;
	}
	
	public void parentSet(PTile1D p) {
		this.pathParent = p;
	}
	
	@Override
	public String toString() {
		return "PathTile: (" + index + ")";
	}

}