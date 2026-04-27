package snake2d.util.misc;

import java.io.File;

import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import snake2d.util.sets.Tree;

public final class Dictionary {

	private final CharSequence[] keys;
	private final CharSequence[] values;
	private final String path;

	private Dictionary(String path) {
		this.path = path;
		Json json = new Json(new File(path).toPath());
		LIST<String> keys = json.keys();
		this.keys = new CharSequence[keys.size()];
		values = new CharSequence[keys.size()];
		
		Tree<String> t = new Tree<String>(values.length) {

			@Override
			protected boolean isGreaterThan(String current, String cmp) {
				return compare(current, cmp) == -1;
			}
		};

		for (String s : keys) {
			t.add( s);
		}

		for (int i = 0; i < this.keys.length; i++) {
			String s = t.pollSmallest();
			this.keys[i] = s;
			values[i] = json.text(s);
		}
	}

	public CharSequence get(CharSequence key) {

	
		int start= 0;
		int length = keys.length-1;
		
		while (length >= start) {
			int mid = start + (length - start) / 2;

			int c = compare(keys[mid], key);
			
			if (c == 0) {
				return values[mid];
			}

			if (c == -1) {
				length = mid-1;
			}else {
				start = mid+1;
			}
		}
		notify(key);
		return key;

	}
	
	void notify(CharSequence key) {
//		new Exception().printStackTrace(System.out);
	}
	
	public CharSequence camel(CharSequence key) {

		
		int start= 0;
		int length = keys.length-1;
		
		while (length >= start) {
			int mid = start + (length - start) / 2;

			int c = compare(keys[mid], key);
			
			if (c == 0) {
				return values[mid];
			}

			if (c == -1) {
				length = mid-1;
			}else {
				start = mid+1;
			}
		}
		System.err.println("Couldn't find mapping for: " + key);
		System.err.println("in: : " + path);
		return key;

	}

	public static int compare(CharSequence current, CharSequence cmp) {

		if (current == null)
			return -1;
		if (cmp == null)
			return 1;
		
		for (int i = 0; i < current.length(); i++) {
			if (i >= cmp.length())
				return 1;
			if (current.charAt(i) < cmp.charAt(i))
				return -1;
			if (current.charAt(i) > cmp.charAt(i))
				return 1;
		}
		return cmp.length() > current.length() ? -1 : 0;

	}

}
