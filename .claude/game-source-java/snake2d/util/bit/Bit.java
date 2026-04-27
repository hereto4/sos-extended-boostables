package snake2d.util.bit;

public class Bit {

	public final int mask;
	
	public Bit(int mask) {
		this.mask = mask;
		if (mask == 0 || ((mask - 1) & mask) != 0)
			throw new RuntimeException();
	}
	
	public boolean is(int data) {
		return (data & mask) != 0;
	}
	
	public int set(int data) {
		return data | mask;
	}
	
	public int set(int data, boolean b) {
		if (b)
			return set(data);
		return clear(data);
	}
	
	public int clear(int data) {
		return data & ~mask;
	}
	

	
}
