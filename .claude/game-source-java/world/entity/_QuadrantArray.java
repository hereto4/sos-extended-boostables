package world.entity;

import java.util.Iterator;

class _QuadrantArray implements Iterable<WEntity>{
	
	private WEntity first;
	private WEntity last;
	
	void add(WEntity e){
		
		if (first == null) {
			first = e;
			last = e;
		}else {
			last.renderNext = e;
			last = e;
		}
	}
	
	void remove(WEntity e){
		
		WEntity f = first;
		first = null;
		last = null;
		boolean removed = false;
		
		while(f != null) {
			WEntity n = f.renderNext;
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
	public Iterator<WEntity> iterator() {
		iter.current = first;
		return iter;
	}
	
	private final Iter iter = new Iter();
	
	private final class Iter implements Iterator<WEntity>{

		WEntity current;
		
		@Override
		public boolean hasNext() {
			return current != null;
		}

		@Override
		public WEntity next() {
			WEntity e = current;
			current = e.renderNext;
			return e;
		}
		
	}
	
}
