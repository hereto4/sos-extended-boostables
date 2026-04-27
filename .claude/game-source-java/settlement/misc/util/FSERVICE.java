package settlement.misc.util;

public interface FSERVICE extends FINDABLE{

	public void consume();
	
	public default void startUsing() {
		
	}
	
	public default boolean hasQueue() {
		return false;
	}
	
}
