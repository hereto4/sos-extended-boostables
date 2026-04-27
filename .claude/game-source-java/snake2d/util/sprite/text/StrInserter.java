package snake2d.util.sprite.text;

public abstract class StrInserter<T> {

	public final String key;
	private final static Str tmp = new Str(128);
	public StrInserter(String key){
		this.key = key;
	}
	
	protected abstract void set(T t, Str str);
	
	public boolean insert(T t, Str str) {
		boolean has = false;
		while(str.hasinsert(key)) {
			tmp.clear();
			has = true;
			set(t, tmp);
			str.insert(key, tmp);
		}
		return has;
	}
	
	public static class Simple extends StrInserter<CharSequence> {

		public Simple(String key) {
			super(key);
		}

		@Override
		protected void set(CharSequence t, Str str) {
			str.add(t);
		}
	}
	
}
