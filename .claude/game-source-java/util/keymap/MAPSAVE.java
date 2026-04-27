package util.keymap;

import java.io.IOException;
import java.util.Arrays;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;

public class MAPSAVE {

	public static void saveMeta(FilePutter saveFile, LIST<? extends MAPPED> all) {
		saveFile.i(all.size());
		for (MAPPED j : all) {
			saveFile.chars(j.key());
		}
	}
	
	public static int[] saveWash(FileGetter f, LIST<? extends MAPPED> all, int nothingReplacer) throws IOException {
		int am = f.i();
		int[] order = new int[am];
		KeyMap<MAPPED> map = new KeyMap<MAPPED>();
	
		for (MAPPED t : all) {
			map.put(t.key(), t);
		}
		
		Arrays.fill(order, nothingReplacer);
		boolean different = false;
		for (int i = 0; i < am; i++) {
			String k = f.chars();
			
			order[i] = map.containsKey(k) ? map.get(k).index() : nothingReplacer;
			different |= order[i] != i;
		}
		if (!different)
			return null;
		return order;
	}
	
	
}
