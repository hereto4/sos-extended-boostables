package snake2d.util.misc;

public interface ACTION {
	
	public void exe();
	
	public static final ACTION NOP = new ACTION() {
		@Override
		public void exe() {
			
		}
	};

	
	public interface ACTION_O<T> {
		
		public void exe(T t);
		
	}
	
}
