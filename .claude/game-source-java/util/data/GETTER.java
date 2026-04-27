package util.data;

public interface GETTER<T> {
	
	public T get();
	
	public static class GETTER_IMP<T> implements GETTERE<T>{

		public T a;
		
		public GETTER_IMP() {
			// TODO Auto-generated constructor stub
		}
		
		public GETTER_IMP(T t) {
			this.a = t;
		}
		
		@Override
		public void set(T t) {
			this.a = t;
		}
		
		@Override
		public T get() {
			return a;
		}
		
	}
	
	public interface GETTERE<T> extends GETTER<T> {
		public void set(T t);
	}
	
}