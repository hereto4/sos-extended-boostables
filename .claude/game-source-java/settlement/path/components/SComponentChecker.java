package settlement.path.components;

import util.data.BOOLEANO;

public final class SComponentChecker implements BOOLEANO<SComponent>{

	private final SComponentLevel level;
	private short[] neighbourcheck;
	private short neigbourcheckI = 0;
	
	public SComponentChecker(SComponentLevel level) {
		this.level = level;
		neighbourcheck = new short[level.componentsMax()+100];
	}
	
	public SComponentChecker init(){
		
		if (neighbourcheck.length < level.componentsMax()) {
			neighbourcheck = new short[level.componentsMax()+100];
			neigbourcheckI = 0;
		}
		neigbourcheckI ++;
		if (neigbourcheckI == 0){
			for (int i = 0; i < neighbourcheck.length; i++)
				neighbourcheck[i] = 0;
			neigbourcheckI = 1;
		}
		return this;
	}
	
	/**
	 * 
	 * @param c
	 * @return true if component previously has been set
	 */
	@Override
	public boolean is(SComponent c){
		return isSet(c.index());
	}
	
	public boolean inbounds(SComponent c) {
		if (c.index() < 0 || c.index() >= neighbourcheck.length)
			return false;
		return true;
	}
	
	/**
	 * 
	 * @param c
	 * @return true if component previously has been set
	 */
	public boolean isSet(int c){
		if (c < 0 || c >= neighbourcheck.length)
			return false;
		return neighbourcheck[c] == neigbourcheckI;
	}
	
	public void unset(SComponent c) {
		neighbourcheck[c.index()] = (short) (neigbourcheckI-1);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param c
	 * @return true if component previously has been set
	 */
	public boolean isSetAndSet(SComponent c){
		if (!is(c)) {
			neighbourcheck[c.index()] = neigbourcheckI;
			return false;
		}
		return true;
	}
	
	
	
}
