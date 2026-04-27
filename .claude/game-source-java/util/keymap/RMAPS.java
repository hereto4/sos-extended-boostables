package util.keymap;

import java.io.IOException;
import java.util.Arrays;

import game.save.Savable;
import init.INIT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class RMAPS<T extends MAPPED> extends RMAP<T>{
	
	private Saver saver;
	private Loader loader;

	public RMAPS(String key, LIST<T> all) {
		super(key, all);
		INIT.addSaver(new Savable(key) {
			
			@Override
			public void save(FilePutter file) {
				saver = new Saver(file);
				
			}
			
			@Override
			public void load(FileGetter file) throws IOException {
				loader = new Loader(file);
			}
			

		});
	}

	

	
	public Saver saver() {
		return saver;
	}

	
	public Loader loader() {
		return loader;
	}
	
	public final class Saver {
		
		private Saver(FilePutter f) {
			f.mark(RMAPS.this);
			f.i(all().size());
			for (T s : all()) {
				f.chars(s.key());
			}
			f.mark(RMAPS.this);
		}

		public void save(T t, FilePutter f) {
			if (t == null)
				f.i(-1);
			else
				f.i(t.index());
		}
		
		public void save(int[] amounts, FilePutter f) {
			check(amounts.length);
			f.is(amounts);
		}
		
		public void save(short[] amounts, FilePutter f) {
			check(amounts.length);
			f.ss(amounts);
		}
		
		public void save(SAVABLE[] amounts, FilePutter f) {
			check(amounts.length);
			for (SAVABLE s : amounts)
				s.save(f);
		}
		
		public void save(LIST<? extends SAVABLE> amounts, FilePutter f) {
			check(amounts.size());
			for (SAVABLE s : amounts)
				s.save(f);
		}
		
		public void save(double[] amounts, FilePutter f) {
			check(amounts.length);
			f.ds(amounts);
		}

		public void save(long[] amounts, FilePutter f) {
			check(amounts.length);
			f.ls(amounts);
		}
		
	}
	
	private void check(int ams) {
		if (ams != all().size())
			throw new RuntimeException(ams + " " + all().size());
	}
	
	public final class Loader {
		
		private final boolean isSame;
		private final int am;
		private final int[] order;
		
		private Loader(FileGetter f) throws IOException {
			f.check(RMAPS.this);
			boolean isSame = true;
			am = f.i();
			if (am != all().size())
				isSame = false;
			
			order = new int[Math.max(all().size(), am)];
			Arrays.fill(order, -1);
			
			
			for (int i = 0; i < am; i++) {
				String k = f.chars();
				if (map.get(k) != null) {
					order[i] = map.get(k).index();
					isSame &= i == map.get(k).index();
				}else
					isSame = false;
			}
			
			
			this.isSame = isSame;
			f.check(RMAPS.this);
		}
		
		public T loadB(FileGetter f, T pref) throws IOException {
			int i = f.i();
			if (i < 0)
				return null;
			if (isSame)
				return all().get(i);
			if (order[i] == -1) {
				return pref == null ? all().get(0) : pref;
			}
			return all().get(order[i]);
		}
		
		public int loadI(FileGetter f) throws IOException {
			int i = f.i();
			if (i < 0)
				return -1;
			if (isSame)
				return all().get(i).index();
			if (order[i] == -1) {
				return -1;
			}
			return all().get(order[i]).index();
		}
		
		public T get(int index) {
			if (index < 0)
				return null;
			if (isSame)
				return all().get(index);
			if (order[index] == -1) {
				return null;
			}
			return all().get(order[index]);
		}
		
		public T load(FileGetter f) throws IOException {
			int i = f.i();
			if (i < 0)
				return null;
			if (isSame)
				return all().get(i);
			if (order[i] == -1) {
				return null;
			}
			return all().get(order[i]);
		}
		
		public byte[] fix(byte[] old, byte defValue) {
			if (isSame) {
				return old;
			}
			if (old.length != am)
				throw new RuntimeException();
			
			byte[] amounts = new byte[all().size()];
			
			Arrays.fill(amounts, defValue);
			for (int i = 0; i < old.length; i++) {
				int o = order[i];
				if (o != -1) {
					amounts[o] = old[i];
				}
			}
			return old;
			
		}
		
		public int fix(int old, int fallback) {
			if (isSame) {
				return old;
			}
			
			if (old < 0 || old >= order.length)
				return fallback;
			int o = order[old];
			if (o != -1)
				return o;
			return fallback;
		}
		
		public int[] fix(int[] old, int defValue) {
			if (isSame) {
				return old;
			}
			
			int[] nn = new int[all().size()];
			Arrays.fill(nn, defValue);
			for (int i = 0; i < am; i++) {
				int o = order[i];
				if (o != -1) {
					nn[o] = old[i];
				}
			}
			return nn;
		}
		
		public boolean isSame() {
			return isSame;
		}
		
		public void load(int[] amounts, FileGetter f, int defValue) throws IOException {
			check(amounts.length);
			if (isSame) {
				f.is(amounts);
				return;
			}
			int[] old = new int[am];
			f.is(old);
			Arrays.fill(amounts, defValue);
			for (int i = 0; i < am; i++) {
				int o = order[i];
				if (o != -1) {
					amounts[o] = old[i];
				}
			}
			
		}
		
		public void load(long[] amounts, FileGetter f, long defValue) throws IOException {
			check(amounts.length);
			if (isSame) {
				f.ls(amounts);
				return;
			}
			long[] old = new long[am];
			f.ls(old);
			Arrays.fill(amounts, defValue);
			for (int i = 0; i < am; i++) {
				int o = order[i];
				if (o != -1) {
					amounts[o] = old[i];
				}
			}
			
		}
		
		public void load(short[] amounts, FileGetter f, short defValue) throws IOException {
			check(amounts.length);
			if (isSame) {
				f.ss(amounts);
				return;
			}
			short[] old = new short[am];
			f.ss(old);
			Arrays.fill(amounts, defValue);
			for (int i = 0; i < am; i++) {
				int o = order[i];
				if (o != -1) {
					amounts[o] = old[i];
				}
			}
			
		}
		
		public void load(double[] amounts, FileGetter f, double defValue) throws IOException {
			check(amounts.length);

			if (isSame) {
				f.ds(amounts);
				
				return;
			}
			double[] old = new double[am];
			f.ds(old);
			Arrays.fill(amounts, defValue);
			for (int i = 0; i < am; i++) {
				int o = order[i];
				if (o != -1) {
					amounts[o] = old[i];
				}
			}
		}
		
		public void load(SAVABLE[] amounts, FileGetter f) throws IOException {
			check(amounts.length);
			if (isSame) {
				for (SAVABLE s : amounts)
					s.load(f);
				return;
			}
			
			load(new ArrayList<SAVABLE>(amounts), f);
			
		}
		
		public void load(LIST<? extends SAVABLE> amounts, FileGetter f) throws IOException {
			check(amounts.size());
			if (isSame) {
				for (SAVABLE s : amounts)
					s.load(f);
				return;
			}
			
			for (SAVABLE s : amounts)
				s.clear();
			
			
			int matches = 0;
			for (int i = 0; i < am; i++) {
				int o = order[i];
				if (o != -1) {
					matches++;
				}
			}
			for (int i = matches; i < am; i++) {
				amounts.get(0).load(f);
			}
			amounts.get(0).clear();
			for (int i = 0; i < am; i++) {
				int o = order[i];
				if (o != -1) {
					amounts.get(o).load(f);
				}
			}
		}

	}
	
	

}
