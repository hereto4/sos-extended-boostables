package util.data;

import util.info.INFO;

public interface GETTER_TRANS<F, T> {
	
	public T get(F f);
	
	public default INFO info() {
		return null;
	}
	
	public interface GETTER_TRANSE<F, T> extends GETTER_TRANS<F, T>{
		public void set(F f, T t);
	}
	
}