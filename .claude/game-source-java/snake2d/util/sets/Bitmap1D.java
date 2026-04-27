package snake2d.util.sets;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public class Bitmap1D implements SAVABLE{
	
	private final int[] bits;
	private final int max; 
	static int[] masks = new int[32];
	static int[] imasks = new int[32];
	private final boolean outof;
	
	static{
		int m = 1;
		for (int i = 0; i < 32; i ++){
			masks[i] = m;
			imasks[i] = ~m;
			m = m << 1;
		}
	}
	
	@Override
	public final void save(FilePutter fp) {
		fp.isE(bits);
	}
	
	@Override
	public final void load(FileGetter fp) throws IOException {
		fp.isE(bits);
	}
	
	public Bitmap1D(int size, boolean outof){
		max = size;
		int l = size/32;
		if (size % 32 != 0)
			l++;
		bits = new int[l];
		this.outof = outof;
	}
	
	public boolean get(int bit){
		
		if (bit < 0 || bit >= max)
			return outof;
		
		int m = bit & 0x0000001F;
		int i = bit >> 5;
		
		return (bits[i] & masks[m]) == masks[m];
		
	}
	
	public void setTrue(int bit){
		
		if (bit < 0 || bit >= max)
			return; 
		
		int m = bit & 0x0000001F;
		int i = bit >> 5;
		bits[i] |= masks[m];
		
	}
	
	public void setFalse(int bit){
		
		if (bit < 0 || bit >= max)
			return; 
		
		int m = bit & 0x0000001F;
		int i = bit >> 5;
		bits[i] &= imasks[m];
		
	}
	
	public void set(int bit, boolean bool){
		if (bool)
			setTrue(bit);
		else
			setFalse(bit);
		
	}
	
	public void toggle(int bit){
		set(bit, !get(bit));
		
	}
	
	@Override
	public void clear() {
		for (int i = 0; i < bits.length; i++)
			bits[i] = 0;
	}
	
	public int size() {
		return max;
	}

	public void setAll(boolean b) {
		int k = b ? -1 : 0;
		for (int i = 0; i < bits.length; i++)
			bits[i] = k;
	}
	
}
