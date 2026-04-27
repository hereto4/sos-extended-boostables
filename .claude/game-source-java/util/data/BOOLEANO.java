package util.data;

import util.info.INFO;

public interface BOOLEANO<T> {

	public boolean is(T t);
	
	public default INFO info() {
		return null;
	}
	
	public interface BOOLEAN_OE<T> extends BOOLEANO<T>{
		
		public BOOLEAN_OE<T> set(T t, boolean b);
		
		public default BOOLEAN_OE<T> toggle(T t) {
			return set(t, !is(t));
		}
		
		public default BOOLEAN_OE<T> setOn(T t) {
			return set(t, true);
		}
		
		public default BOOLEAN_OE<T> setOff(T t) {
			return set(t, false);
		}
	}
	
}
