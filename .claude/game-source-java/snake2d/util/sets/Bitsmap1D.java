package snake2d.util.sets;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

/**
 * dear mortal. This is not your ordinary bitmap. This is a bit(s)map. That's right.
 * How many bits you want? Up to you. Took me two days to get this right...
 * @author mail__000
 *
 */
public class Bitsmap1D implements SAVABLE, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final long[] bits;
	public final int outof;
	private final int max;
	private final int stride;
	private final long mask;
	/**
	 * 
	 * @param outof - value to return if index is out of bounds
	 * @param bits - the number of bits you want to store a value. Will be positive. Should be < 32 
	 * @param amount - how many of these memory chunks do you want, huh?
	 */
	public Bitsmap1D(int outof, int bits, int amount){
		this.bits = new long[(int) (Math.ceil(bits*amount/64.0))];
		this.outof = outof;
		max = amount;
		stride = bits;
		mask = (1 << (bits))-1;
	}
	
	@Override
	public void save(FilePutter fp) {
		fp.ls(bits);
	}
	
	@Override
	public void load(FileGetter fp) throws IOException {
		fp.ls(bits);
	}
	
	/**
	 * 
	 * @param index - your index
	 * @return - your value, or outof value if out of bounds
	 */
	public int get(int index){
		
		if (index < 0 || index >= max)
			return outof;
		
		int i = (index*stride);
		
		int l1 = i >> 6;
		int ls = 64 -stride-(i & 63);
		
		if (ls >= 0)
			return (int) ((bits[l1] >> (ls))&mask);
		
		long v = bits[l1] << -ls;
		ls += 64;
		v |= bits[l1+1] >>> ls;
		
		return (int) (v&mask);
		
	}

	@Override
	public void clear() {
		Arrays.fill(bits, 0l);
	}
	
	/**
	 * 
	 * @param index
	 * @param value
	 */
	public void set(int index, int value){
		
		long v = value;
		v &= mask;
		int i = (index*stride);
		
		int l1 = i >> 6;
		int ls = 64-stride-(i & 63);
		

		
		if (ls >= 0) {
			bits[l1] &= ~(mask << ls);
			bits[l1] |= v << ls;
		}else {
			
			bits[l1] &= ~(mask >> -ls);
			bits[l1] |= v >> -ls;
			
			ls += 64;
			
			bits[l1+1] &= ~(mask << ls);
			bits[l1+1] |= v << ls;
			
		}
		
//		
//		
//		
//		int scroll = index & 0b011111;
//		index = index >> 5;
//		
//		for (int i = 0; i < bits.length; i++) {
//			bits[i][index] &= ~(1 << scroll);
//			bits[i][index] |= (value & 0b01) << scroll;
//			value = value >> 1;
//		}
		
	}
	
	public void inc(int index, int delta) {
		set(index, get(index)+delta);
	}
	
	public void setAll(int value) {
		
		clear();
		for (int i = 0; i < max; i++) {
			set(i, value);
		}
//		for (int y = 0; y < bits.length; y++) {
//			int i = ((value >>> y) & 1);
//			i = (i == 1) ? -1 : 0;
//			for (int x = 0; x < bits[0].length; x++) {
//				bits[y][x] = i;
//			}
//		}
	}
	
	
	public int maxIndex() {
		return max;
	}
	
	public int maxValue() {
		return (int) mask;
	}
	

	
}
