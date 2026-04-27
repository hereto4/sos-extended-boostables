package snake2d.util.misc;

public class IntChecker {

	private final short[] check;
	private int checkI = 1;
	
	
	public IntChecker(int size) {
		check = new short[size];
	}
	
	public IntChecker init(){
		
		checkI ++;
		if (checkI == 0x0FFFF){
			for (int i = 0; i < check.length; i++)
				check[i] = 0;
			checkI = 1;
		}
		return this;
	}
	
	/**
	 * 
	 * @param c
	 * @return true if component previously has been set
	 */
	public boolean isSet(int i){
		return (check[i] & 0x0FFFF) == checkI;
	}
	
	public void unset(int i) {
		check[i] = 0;
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param c
	 * @return true if component previously has been set
	 */
	public boolean isSetAndSet(int i){
		if (!isSet(i)) {
			check[i] = (short) (checkI);
			return false;
		}
		return true;
	}
	
	public int size() {
		return check.length;
	}
	
	
}