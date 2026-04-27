package snake2d.util.sets;

public interface Tuple<A, B> {

	public A a();
	public B b();
	
	public static class TupleImp<A, B> implements Tuple<A, B> {
		
		public A a;
		public B b;
		
		public TupleImp() {
			
		}
		
		public TupleImp(A a, B b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public A a() {
			return a;
		}

		@Override
		public B b() {
			return b;
		}
		
		
	}
	
	public static class TupleD<A> {
		
		public final A a;
		public double d;
		
		public TupleD(A a) {
			this.a = a;
		}
		
		
	}
}
