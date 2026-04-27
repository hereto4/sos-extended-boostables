package snake2d.util.sets;

import java.io.IOException;

import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;

public final class MapInt implements SAVABLE{

	private int table[];
	private int last;

	public MapInt() {
		this(20);
	}

	public MapInt(int initialCapacity) {
		table = new int[initialCapacity];
		last = 0;
	}

	public void remove(int e) {
		int i = search(e);
		if (i < 0)
			throw new RuntimeException();
		last--;
		for (; i < last; i++) {
			table[i] = table[i+1];
		}
	}
	
	public void removeIndex(int index) {
		if (index < 0 || index >= last)
			throw new RuntimeException();
		last--;
		for (; index < last; index++) {
			table[index] = table[index+1];
		}
	}
	
	public boolean contains(int i) {
		return search(i) >= 0;
	}

	public int size() {
		return last;
	}
	
	public int atIndex(int i) {
		return table[i];
	}

	public boolean isEmpty() {
		return last == 0;
	}
	
	public int poll() {
		last--;
		return table[last];
	}
	
	public int peek() {
		return table[last-1];
	}
	

	public int add(int e) {
		last++;
		if (last >= table.length) {
			int table2[] = new int[table.length*2];
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
			if (e > table[i-1]) {
				table[i] = e;
				return i;
			}
			if (e == table[i-1])
				throw new RuntimeException();
			table[i] = table[i-1];
				
		}
		return -1;
	}

	public boolean tryAdd(int e) {
		if (!contains(e)) {
			add(e);
			return true;
		}
		return false;
	}

	private int search(int value) {
		return runBinarySearchIteratively(value, 0, last-1);
	}
	
	private int runBinarySearchIteratively(int key, int low, int high) {

		while (low <= high) {
			int mid = low + ((high - low) / 2);
			if (table[mid] < key) {
				low = mid + 1;
			} else if (table[mid] > key) {
				high = mid - 1;
			} else if (table[mid] == key) {
				return mid;
			}
		}
		return -1;
	}
	
	@Override
	public void clear() {
		last = 0;
	}
	
	public static void main(String[] args) {
		
		int[] in = new int[500];
		int st = RND.rInt();
		MapInt map = new MapInt();
		int a = 0;
		for (int k = 0; k < in.length; k++) {
			in[k] = st+k;
			if ((k & 1) == 0) {
				map.add(in[k]);
				a++;
			}
		}
		
		for (int t : in)
			if (map.contains(t))
				a--;
		
		LOG.ln(a + " " + in.length + " " + map.contains(in[0]));
		
		
	}

	@Override
	public void save(FilePutter file) {
		file.i(table.length);
		file.is(table);
		file.i(last);
		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		table = new int[file.i()];
		file.is(table);
		last = file.i();
	}

}
