package snake2d.util.sets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class KeyMap<T> {

	private final HashMap<String, T> map = new HashMap<>();
	
	public void put(String key, T t) {
		if (map.containsKey(key))
			throw new RuntimeException("'" + key + "' " + map.get(key) + " " + t);
		map.put(key, t);
	}
	
	public void putReplace(String key, T t) {
		map.put(key, t);
	}
	
	public void remove(String key) {
		map.remove(key);
	}
	
	public boolean containsKey(String key) {
		return map.containsKey(key);
	}
	
	public T get(String key) {
		return map.get(key);
	}
	
	public void debug() {
		for (String s : map.keySet())
			System.err.println(s);
	}
	
	public void expand() {
		ArrayList<String> bb = new ArrayList<String>(50);
		for (String s : map.keySet()) {
			if (s.startsWith("_")) {
				
				String key = s.substring(1, s.length());
				if (map.containsKey(key))
					continue;
				bb.add(s);
				
			}
		}
		
		for (String s : bb) {
			
			map.put(s.substring(1, s.length()), map.get(s));
		}
		
		
	}
	
	public int size() {
		return map.size();
	}
	
	public LIST<T> all(){
		return new ArrayList<T>(map.values());
	}
	
	public LIST<T> allSorted() {
		String[] keys = new String[map.values().size()];
		int i = 0;
		for (String k : keys()) {
			keys[i++] = k;
		}
		Arrays.sort(keys);
		ArrayList<T> res = new ArrayList<>(keys.length);
		for(i = 0; i < keys.length; i++)
			res.add(get(keys[i]));
		return res;
	}

	public Set<String> keys() {
		return map.keySet();
	}
	
	public LIST<String> keysSorted() {
		String[] keys = new String[map.values().size()];
		int i = 0;
		for (String k : keys()) {
			keys[i++] = k;
		}
		Arrays.sort(keys);
		ArrayList<String> res = new ArrayList<>(keys.length);
		for(i = 0; i < keys.length; i++)
			res.add(keys[i]);
		return res;
	}
	
	public String keysString() {
		String s = "";
		for (String ss : keysSorted()) {
			s += ss + System.lineSeparator();
		}
		return s;
	}
	
	public void clear() {
		map.clear();
	}
	
	public final static class CharMap<T> {

		private String table[] = new String[0];
		private Object content[] = new Object[0];
		private int last = 0;


		
		public void putReplace(String key, T t) {
			int i = search(key);
			if (i != -1)
				content[i] = t;
			else
				put(key, t);
		}
		
		@SuppressWarnings("unchecked")
		public T get(CharSequence key) {
			int i = search(key);
			if (i == -1)
				return null;
			return (T) content[i];
		}
		
		
		public boolean containsKey(CharSequence key) {
			return search(key) != -1;
		}

		public void remove(CharSequence key) {
			int i = search(key);
			if (i < 0)
				throw new RuntimeException();
			last--;
			for (; i < last; i++) {
				table[i] = table[i+1];
			}
		}
		
		public int size() {
			return last;
		}

		public boolean isEmpty() {
			return last == 0;
		}

		public int put(String key, T e) {
			if (search(key) != -1)
				throw new RuntimeException("'" + key + "' " + get(key) + " " + e);
			last++;
			if (last >= table.length) {
				String table2[] = new String[table.length+1];
				Object content2[] = new Object[table.length+1];
				for (int i = 0; i < table.length; i++) {
					table2[i] = table[i];
					content2[i] = content[i];
				}
				table = table2;
				content = content2;
			}
			for (int i = last-1; i >= 0; i--) {
				if (i == 0) {
					table[i] = key;
					content[i] = e;
					return 0;
				}
				int comp = compare(key, i-1);
				if (comp > 0) {
					table[i] = key;
					content[i] = e;
					return i;
				}
				if (comp == 0)
					throw new RuntimeException();
				table[i] = table[i-1];
				content[i] = content[i-1];
			}
			return -1;
		}
		
		private int compare(CharSequence key, int index) {
			String other = table[index];
			int le = Math.max(key.length(), other.length());
			
			for (int i = 0; i < le; i++) {
				if (i >= key.length())
					return -1;
				if (i >= other.length())
					return 1;
				int bb = key.charAt(i)-other.charAt(i);
				if (bb != 0)
					return bb;
			}
			return 0;
			
		}
		

		private int search(CharSequence key) {
			return runBinarySearchIteratively(key, 0, last-1);
		}
		
		private int runBinarySearchIteratively(CharSequence key, int low, int high) {

			while (low <= high) {
				int mid = low + ((high - low) / 2);
				int comp = compare(key, mid);
				
				if (comp > 0) {
					low = mid + 1;
				} else if (comp < 0) {
					high = mid - 1;
				} else if (comp == 0) {
					return mid;
				}
			}
			return -1;
		}

		@SuppressWarnings("unchecked")
		public LIST<T> all(){
			ArrayList<T> tt = new ArrayList<T>(content.length);
			for (Object o : content)
				tt.add((T) o);
			return tt;
		}
		
		public LIST<String> keysSorted() {
			ArrayList<String> res = new ArrayList<>(table);
			return res;
		}
		
		public String keysString() {
			String s = "";
			for (String ss : keysSorted()) {
				s += ss + System.lineSeparator();
			}
			return s;
		}

	}
	
	
}
