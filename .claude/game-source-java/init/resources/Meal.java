package init.resources;

import snake2d.util.misc.CLAMP;

public final class Meal {

	private Meal() {
		
	}
	
	public static int make(ResG e, int amount, double pref) {
		return (e.index() << 16 | (amount<<8) | CLAMP.i((int)(255*pref), 0, 255));
	}
	
	public static ResG get(int data) {
		return RESOURCES.EDI().all().get((data>>16)&0x0FF);
	}
	
	public static int amount(int data) {
		return (data>>8) & 0x0FF;
	}
	
	public static double pref(int data) {
		return (data & 0x0FF)/255.0;
	}
	
}
