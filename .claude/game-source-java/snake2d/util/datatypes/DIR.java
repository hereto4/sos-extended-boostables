package snake2d.util.datatypes;

import java.io.IOException;
import java.io.ObjectStreamException;

import snake2d.UTIL;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public enum DIR implements COORDINATE{


	

	N(0,-1, 0b0001, "north", 1){
		
	},
	NE(1,-1, 0b0001, "north-east", 2){
		
	},
	E(1,0, 0b0010, "east", 5) {
		
	},
	SE(1,1, 0b0010, "south-east",8){
		
	},
	S(0,1, 0b0100, "south", 7){
		
	},
	SW(-1,1, 0b0100, "south-west", 6){
		
	},
	W(-1,0, 0b1000, "west", 3),
	NW(-1,-1, 0b01000, "north-west", 0),
	C(4){
		
	};
	
	public static final LIST<DIR> ALL = new ArrayList<DIR>(N,NE,E,SE,S,SW,W,NW);
	public static final LIST<DIR> ALLC = new ArrayList<DIR>(N,NE,E,SE,S,SW,W,NW,C);
	public static final LIST<DIR> ORTHO = new ArrayList<>(N,E,S,W);
	public static final LIST<DIR> ORTHOC = new ArrayList<>(C,N,E,S,W);
	public static final LIST<DIR> NORTHO = new ArrayList<>(NE,SE,SW,NW);
//	public static final LIST<DIR> ALL_NW = new ArrayList<DIR>(DIR.W, DIR.NW,DIR.N, DIR.NE);
//	public static final LIST<DIR> ALL_SE = new ArrayList<DIR>(DIR.E, DIR.SE,DIR.S, DIR.SW);
	
	public static DIR get(COORDINATE coo){
		return get(coo.x(), coo.y());
	}
	
	public static DIR get(COORDINATE from, COORDINATE to){
		return get(to.x()-from.x(), to.y()-from.y());
	}

	public static DIR get(int fx, int fy, COORDINATE to){
		return get(to.x()-fx, to.y()-fy);
	}
	
	public static DIR get(COORDINATE from, int tx, int ty){
		return get(tx-from.x(), ty-from.y());
	}
	
	public static DIR get(RECTANGLE a, RECTANGLE b) {
		double dx = b.cX() - a.cX();
		double dy = b.cY() - a.cY();
		return get(dx, dy);
	}
	
	public static DIR get(int fx, int fy, int tx, int ty){
		return get(tx-fx, ty-fy);
	}
	
	public static DIR get(double norX2, double norY2){
		
		if (norX2 == 0 && norY2 == 0)
			return C;
		if (norX2 == 0)
			return norY2 < 0 ? N : S;
		if (norY2 == 0)
			return norX2 < 0 ? W : E;
		
		double ratio = Math.abs(norX2/norY2);
		
		if (ratio < 0.38)
			return norY2 < 0 ? N : S;
		if (ratio > 2.43)
			return norX2 < 0 ? W : E;
		
		if (norY2 < 0){
			if (norX2 < 0)
				return NW;
			return NE;
		}
		
		if (norX2 < 0)
			return SW;
		return SE;
	}
	
	private final transient int x;
	private final transient int y;
	private final transient double norX;
	private final transient double norY;
	private final transient String name;
	private final byte index;
	public final int bit;
	private final transient int mask;
	private final transient double distance;
	public final int boxID;
	
	private DIR(int boxId){
		this.x = 0;
		this.y = 0;
		index = (byte) ordinal();
		bit = 0;
		this.mask = 0;
		this.norX = 0;
		this.norY = 0;
		name = "centre";
		distance = 0;
		this.boxID = boxId;
	}
	
	private DIR(int x, int y, int mask, String name, int boxID){
		this.x = x;
		this.y = y;
		index = (byte) ordinal();
		bit = 1 << index;
		this.name = name;
		this.mask = mask;
		if (x != 0 && y != 0){
			this.norX = x*Math.sqrt(0.5);
			this.norY = y*Math.sqrt(0.5);
			this.distance = UTIL.SQRT2;
		}else{
			this.norX = x;
			this.norY = y;
			this.distance = 1;
		}
		this.boxID = boxID;
	}
	
	@Override
	public double tileDistance() {
		return distance;
	}
	
	Object readResolve() throws ObjectStreamException{
		return ALL.get(index);
	}
	
	public int mask(){
		return mask;
	}
	
	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	public double xN() {
		return norX;
	}

	public double yN() {
		return norY;
	}
	
	public String getName() {
		return name;
	}

	public int id() {
		return index;
	}
	
//	public int bit() {
//		return index;
//	}
	
	public DIR next(int nr) {
		return ALL.get((index+nr)&7);
	}
	
	public DIR perpendicular(){
		return ALL.get((id() + ALL.size()/2)%ALL.size());
	}
	
	/**
	 * |----|---|
	 * |    |   |
	 * |    |---|
	 * |        |
	 * |--------|
	 * @param target: the rectangle to position
	 * @param ref: the reference
	 */
	public void positionWithin(RECTANGLEE target, RECTANGLE ref){
		int x = ref.x1() + ref.width()/2*(x()+1); 
		int y = ref.y1() + ref.height()/2*(y()+1);
		target.moveX2(x);
		target.moveY2(y);
	}
	
	/**
	 *        |---|
	 * |------|-|-|
	 * |      |-|-|
	 * |        |
	 * |        |
	 * |--------|
	 * @param target: the rectangle to position
	 * @param ref: the reference
	 */
	public void positionCentered(RECTANGLEE target, RECTANGLE ref){
		int x = ref.x1() + ref.width()/2*(x()+1); 
		int y = ref.y1() + ref.height()/2*(y()+1);
		target.moveC(x, y);
	}
	
	/**
	 *          |---| <p>
	 *          |   | <p>
	 * |--------|---| <p>
	 * |        | <p>
	 * |        | <p>
	 * |        | <p>
	 * |--------| <p>
	 * @param target: the rectangle to position
	 * @param ref: the reference
	 */
	public void positionEdge(Rec target, RECTANGLE ref){
		int x = ref.x1() + ref.width()/2*(x()+1); 
		int y = ref.y1() + ref.height()/2*(y()+1);
		target.moveX1Y1(x, y);
	}

	public void reposition(Rec old, int nWidth, int nHeight){
		
		if (x < 0) {
			old.setWidth(nWidth);
		}else if (x > 0){
			int x2 = old.x2();
			old.setWidth(nWidth);
			old.moveX2(x2);
		}else {
			int cx = old.cX();
			old.setWidth(nWidth);
			old.moveCX(cx);
		}
		
		if (y < 0) {
			old.setHeight(nHeight);
		}else if (y > 0){
			int y2 = old.y2();
			old.setHeight(nHeight);
			old.moveY2(y2);
		}else {
			int cY = old.cY();
			old.setHeight(nHeight);
			old.moveCY(cY);
		}
		
	}

	public boolean isOrtho() {
		return absSum() == 1;
	}
	
	private final static int[] toBox = new int[16];
	static {
		toBox[W.mask | S.mask | E.mask] = 1;
		toBox[W.mask | S.mask] = 2;
		toBox[N.mask | S.mask | E.mask] = 3;
		toBox[0xF] = 4;
		toBox[N.mask | S.mask | W.mask] = 5;
		toBox[N.mask | E.mask] = 6;
		toBox[N.mask | E.mask | W.mask] = 7;
		toBox[N.mask | W.mask] = 8;
	}
	
	public static int toBoxID(int orthoMask) {
		return toBox[orthoMask];
	}
	
	public int orthoID() {
		return index>>1;
	}

	public static void save(DIR dir, FilePutter file) {
		byte b = (byte) (dir == null ? -1 : dir.id());
		file.b(b);
	}

	public static DIR load(FileGetter file) throws IOException {
		byte b = file.b();
		if (b < 0)
			return null;
		return ALL.get(b);
	}
	
}
