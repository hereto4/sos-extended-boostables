package snake2d.util.bit;

public class BitsLong {

	public final int scroll;
	public final long mask;
	
	public BitsLong(long mask) {
		this.scroll = Long.numberOfTrailingZeros(mask);
		long m = mask;
		m = m >>> this.scroll;
		
		this.mask = m;
	}
	
	public long set(long data, long value) {
		if (value < 0 || value > mask)
			throw new RuntimeException(""+value);
		
		value = value << scroll;
		data &= ~(mask<<scroll);
		data |= value;
		return data;
	}
	
	public int get(long data) {
		return (int) ((data >> scroll) & mask);
	}
	
	public long inc(long data, long inc) {
		long a = get(data) + inc;
		if (a < 0)
			a = 0;
		if (a > mask)
			a = mask;
		return set(data, a);
	}
	
	public boolean isMaximum(int data){
		return get(data) == mask;
	}
	
}
