package snake2d.util.sets;

public interface Valued<T> {

	public T t();
	public double value();
	
	public class ValuedImp<T> implements Valued<T> {
		
		public final T t;
		public double value;
		
		public ValuedImp(T t){
			this.t = t;
		}
		
		@Override
		public T t() {
			return t;
		}

		@Override
		public double value() {
			return value;
		}
		
		public ValuedImp<T> set(double d) {
			value = d;
			return this;
		}
		
	}
	
}
