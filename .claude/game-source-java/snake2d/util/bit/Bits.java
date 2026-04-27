package snake2d.util.bit;

public class Bits {

	public final int scroll;
	public final int mask;
	
	public Bits(int mask) {
		this.scroll = Long.numberOfTrailingZeros(mask);
		long m = mask & 0x0FFFFFFFFl;
		m = m >> this.scroll;
		
		this.mask = (int) m;
	}
	
	public int set(int data, int value) {
		if ((value & ~mask) != 0)
			throw new RuntimeException(""+value);
		
		value = value << scroll;
		data &= ~(mask<<scroll);
		data |= value;
		return data;
	}
	
	public int get(int data) {
		return (data >> scroll) & mask;
	}
	
	public int inc(int data, int inc) {
		int a = get(data) + inc;
		if (a < 0)
			a = 0;
		if (a > mask)
			a = mask;
		return set(data, a);
	}
	
	public boolean isMaximum(int data){
		return get(data) == mask;
	}
	
	public static int getDistance(int a, int b, int mask) {
		a &= mask;
		b &= mask;
		
		if (b >= a)
			return b-a;
		else
			return mask-a + b;
	}
	
	public static double getDistanceD(int a, int b, int mask) {
		return (double)getDistanceD(a, b, mask)/mask;
	}
	
}
