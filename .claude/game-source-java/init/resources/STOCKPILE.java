package init.resources;

import java.io.IOException;
import java.util.Arrays;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.data.INT_O;

public interface STOCKPILE extends INT_O<RESOURCE> {

	public int get(int ri);
	
	public class StockpileImp implements STOCKPILE, SAVABLE, INT_OE<RESOURCE>{
		
		private int[] amounts = new int[RESOURCES.ALL().size()];

		@Override
		public int get(RESOURCE res) {
			return amounts[res.bIndex()];
		}
		
		@Override
		public void set(RESOURCE res, int amount) {
			amounts[res.bIndex()] = amount;
		}
		
		public void add(RESOURCE res, int inc) {
			amounts[res.bIndex()] += inc;
		}

		@Override
		public void save(FilePutter file) {
			file.isE(amounts);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.isE(amounts);
		}

		@Override
		public void clear() {
			Arrays.fill(amounts, 0);
		}

		@Override
		public int get(int ri) {
			return amounts[ri];
		}

		@Override
		public int min(RESOURCE t) {
			return 0;
		}

		@Override
		public int max(RESOURCE t) {
			return Integer.MAX_VALUE;
		}
		
		
	}
	
}
