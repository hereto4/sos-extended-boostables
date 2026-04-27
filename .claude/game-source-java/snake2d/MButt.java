package snake2d;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public enum MButt {

	LEFT, RIGHT, WHEEL, 
	
	WHEEL_SPIN;
	
	static double delta;
	static float wheelDy;
	boolean isDown = false;
	long nanoNow = -1;
	boolean isDouble;
	public int clicks;
	public static LIST<MButt> ALL = new ArrayList<>(values());
	
//	public static float getWheelSpin(){
//		return wheelDy;
//	}
	
	public static float clearWheelSpin(){
		float f = wheelDy;
		wheelDy = 0;
		return f;
	}
	
	public static float peekWheel() {
		return wheelDy;
	}
	
	public boolean isDown(){
		return isDown;
	}
	
	public boolean consumeClick() {
		if (clicks > 0) {
			clicks --;
			return true;
		}
		return false;
	}
	
	public boolean consumeAllClick() {
		if (clicks > 0) {
			clicks = 0;
			return true;
		}
		return false;
	}
	
	public boolean isDouble() {
		return isDouble;
	}
	
}
