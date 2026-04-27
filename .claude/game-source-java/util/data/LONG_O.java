package util.data;

public interface LONG_O<T> {

	public long get(T t);

	public interface LONG_OE<T> extends LONG_O<T>{

		
		public void set(T t, long i);
		
		
	}
	
	
	
}
