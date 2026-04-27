package snake2d.util.sets;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A hashmap that works with indexed objects. It is slow at inserting objects, but does not take more space than its objects
 * @author Jake
 *
 * @param <T>
 */
public final class MapIndexed<T extends INDEXED> implements ADDABLE<T> {

	private INDEXED table[] = new INDEXED[0];
	private int last = 0;

	public MapIndexed() {
		
	}
	
	public MapIndexed(T[] ts) {
		
		table = new INDEXED[ts.length];
		last = table.length;
		for (int i = 0; i < table.length; i++)
			table[i] = ts[i];
		Arrays.sort(table, new Comparator<INDEXED>() {

			@Override
			public int compare(INDEXED o1, INDEXED o2) {
				return o1.index() - o2.index();
			}
			
		});
	}
	
	@SuppressWarnings("unchecked")
	public T get(int hash) {
		return (T) table[search(hash)];
	}
	
	@SuppressWarnings("unchecked")
	public T getTry(int index) {
		int ss = search(index);
		if (ss >= 0)
			return (T) table[ss];
		return null;
	}
	
	public void remove(int hash) {
		int i = search(hash);
		if (i < 0)
			throw new RuntimeException();
		last--;
		for (; i < last; i++) {
			table[i] = table[i+1];
		}
	}

	public boolean contains(T object) {
		return search(object.index()) >= 0;
	}
	
	public boolean contains(int hash) {
		return search(hash) >= 0;
	}

	public int size() {
		return last;
	}

	public boolean isEmpty() {
		return last == 0;
	}

	@Override
	public int add(T e) {
		last++;
		if (last >= table.length) {
			INDEXED table2[] = new INDEXED[table.length+1];
			for (int i = 0; i < table.length; i++) {
				table2[i] = table[i];
			}
			table = table2;
		}
		for (int i = last-1; i >= 0; i--) {
			if (i == 0) {
				table[i] = e;
				return 0;
			}
			if (e.index() > table[i-1].index()) {
				table[i] = e;
				return i;
			}
			if (e.index() == table[i-1].index())
				throw new RuntimeException();
			table[i] = table[i-1];
				
		}
		return -1;
	}
	
	@Override
	public boolean hasRoom() {
		return true;
	}

	@Override
	public int tryAdd(T e) {
		return add(e);
	}

	private int search(int value) {
		return runBinarySearchIteratively(value, 0, last-1);
	}
	
	private int runBinarySearchIteratively(int key, int low, int high) {

		while (low <= high) {
			int mid = low + ((high - low) / 2);
			if (table[mid].index() < key) {
				low = mid + 1;
			} else if (table[mid].index() > key) {
				high = mid - 1;
			} else if (table[mid].index() == key) {
				return mid;
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<T> toList(){
		ArrayList<T> tt = new ArrayList<>(last);
		for (int i = 0; i < last; i++)
			tt.add((T) table[i]);
		return tt;
	}

	
}
