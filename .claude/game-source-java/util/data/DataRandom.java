package util.data;

import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;

public class DataRandom<T> {
	
	private final LIST<INT_OE<T>> randomness;
	private static final double rd = 1.0/0b0111_1111_1111_1111_1111_1111_1111_1111;
	
	public DataRandom(DataOSimple<T> data, int ints){
		ArrayListGrower<INT_OE<T>> all = new ArrayListGrower<>();
		for (int i = 0; i < ints; i++)
			all.add(data.new DataInt() {
				@Override
				public int min(T t) {
					return Integer.MIN_VALUE;
				}
			});
		randomness = all;
	}
	
	public DataRandom(DataO<T> data, int ints){
		ArrayListGrower<INT_OE<T>> all = new ArrayListGrower<>();
		for (int i = 0; i < ints; i++)
			all.add(data.new DataInt("RANDOM" + i) {
				@Override
				public int min(T t) {
					return Integer.MIN_VALUE;
				}
			});
		randomness = all;
	}
	
	public double getD(T r, int startBit) {
		
		int ii = get(r, startBit);
		return ii*rd;
		
	}
	
	public int get(T r, int startBit, int bits) {
		
		if (bits >= 32)
			throw new RuntimeException();
		
		startBit &= (32*randomness.size())-1;
		int ii = startBit/32;
		
		long a = randomness.get(ii).get(r);
		long b = randomness.getC(ii+1).get(r);
		a |= b <<32;
		startBit &= 32-1;
		
		a = a >>> startBit;
		a &= (1 << (bits))-1;
		
		return (int) a;
		
		
	}

	public void copyFrom(T dest, T source) {
		for (int i = 0; i < randomness.size(); i++) {
			randomness.get(i).set(dest, randomness.get(i).get(source));
		}
	}
	
	public int get(T r, int startBit) {
		return get(r, startBit, 31);
		
		
	}
	
	public long getL(T r, int startBit) {
		long res = get(r, startBit);
		res = res << 32;
		res |= get(r, startBit+32);
		return res;
	}
	
	public void setLong(T r, int li, long ll) {
		randomness.get(li*2).set(r, (int) (ll>>32));
		randomness.get(li*2+1).set(r, (int) (ll));
	}
	
	public LIST<INT_OE<T>> all(){
		return randomness;
	}
	
	public void randomize(T r) {
		for (INT_OE<T> i : randomness)
			i.set(r, RND.rInt());
	}
	
}

