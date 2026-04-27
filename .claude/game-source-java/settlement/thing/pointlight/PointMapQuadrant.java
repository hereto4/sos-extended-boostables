package settlement.thing.pointlight;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

class PointMapQuadrant implements SAVABLE{
	
	private long[] added = new long[16];
	private byte last = 0;
	
	void add(long d){
		
		if (last >= Byte.MAX_VALUE)
			return;
		
		if (last == added.length) {
			long[] n = new long[added.length + 16];
			for (int i = 0; i < added.length; i++)
				n[i] = added[i];
			added = n;
		}
		
		added[last] = d;
		last++;
		
	}
	
	void remove(int tx, int ty){
		
		for (int i = 0; i < last; i++) {
			Light q = Light.init(added[i]);
			if (q.tx() == tx && q.ty() == ty) {
				if (i < last)
					added[i] = added[last-1];
				last--;
				i--;
			}
		}
		
	}
	
	boolean is(int tx, int ty){
		
		for (int i = 0; i < last; i++) {
			Light q = Light.init(added[i]);
			if (q.tx() == tx && q.ty() == ty) {
				return true;
			}
		}
		return false;
		
	}
	
	@Override
	public void clear(){
		last = 0;
	}

	@Override
	public void save(FilePutter file) {
		file.i(last);
		for (int i = 0; i < last; i++)
			file.l(added[i]);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		last = 0;
		int k = file.i();
		for (int i = 0; i < k; i++)
			add(file.l());
	}
	
	public int last() {
		return last;
	}
	
	public long get(int i) {
		return added[i];
	}

	public void set(int i, long d) {
		added[i] = d;
	}
	
	
	
}
