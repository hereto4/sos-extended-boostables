package settlement.thing.halfEntity;

import java.util.Iterator;

class _Quadrant implements Iterable<HalfEntity>{
	
	private HalfEntity first;
	private HalfEntity last;
	
	void add(HalfEntity e){
		e.renderNext = null;
		if (first == null) {
			first = e;
			last = e;
		}else {
			last.renderNext = e;
			last = e;
		}
	}
	
	void remove(HalfEntity e){
		
		HalfEntity f = first;
		first = null;
		last = null;
		boolean removed = false;
		
		while(f != null) {
			HalfEntity n = f.renderNext;
			f.renderNext = null;
			if (f == e) {
				removed = true;
			}else {
				add(f);
			}
			
			f = n;
		}
		if (!removed)
			throw new RuntimeException();
	}
	
	void clear(){
		first = null;
		last = null;
	}
	
	@Override
	public Iterator<HalfEntity> iterator() {
		iter.current = first;
		return iter;
	}
	
	private final Iter iter = new Iter();
	
	private final class Iter implements Iterator<HalfEntity>{

		HalfEntity current;
		
		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public HalfEntity next() {
			HalfEntity e = current;
			current = e.renderNext;
			return e;
		}
		
	}
	
}
