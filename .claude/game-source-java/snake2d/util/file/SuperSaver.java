package snake2d.util.file;

import java.io.IOException;

import snake2d.LOG;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;

public abstract class SuperSaver<T> implements SAVABLE{

	private final LIST<T> tt;
	private final KeyMap<T> map = new KeyMap<T>();
	private Class<?> clas;
	
	public SuperSaver(Class<?> clas, LIST<T> tt) {
		this.tt = tt;
		for (T t : tt) {

			map.put(key(t), t);
		}
		this.clas = clas;
	}
	
	protected abstract String key(T t);
	protected abstract void save(T t, FilePutter f);
	protected abstract void load(T t, FileGetter f) throws IOException;
	protected abstract void clear(T t);
	
	@Override
	public void save(FilePutter f) {
		f.i(tt.size());

		for (int i = 0; i < tt.size(); i++) {
			T e = tt.get(i);
			f.chars(key(e));
			int pos = f.getPosition();
			f.i(0);
			save(e, f);
			int le = f.getPosition()-pos-4;
			f.setAtPosition(pos, le);

		}

	}
	
	@Override
	public void load(FileGetter f) throws IOException {
		clear();
		int am = f.i();

		for (int i = 0; i < am; i++) {
			String k = f.chars();

			int pos = f.getPosition()+f.i()+4;
			T e = map.get(k);
			if (e != null) {
				load(e, f);
				if (f.getPosition() != pos) {
					LOG.ln(clas + " " + k + " " + f.getPosition() + " " + pos + " " + e.getClass().getSimpleName());
					f.setPosition(pos);
					clear(e);
				}
			}else {
				LOG.ln(clas + " " + k);
				f.setPosition(pos);
			}
		}

	}
	
	@Override
	public void clear() {
		for (int i = 0; i < tt.size(); i++) {
			T e = tt.get(i);
			clear(e);
		}
	}
}
