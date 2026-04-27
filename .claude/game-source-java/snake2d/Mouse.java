package snake2d;

import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import snake2d.util.datatypes.Coo;
import snake2d.util.gui.clickable.CLICKABLE;

public class Mouse{
	
	public static CLICKABLE currentClicked = null;
	
	private float mXC;
	private float mYC;
	private final DoubleBuffer mX = BufferUtils.createDoubleBuffer(1);
	private final DoubleBuffer mY = BufferUtils.createDoubleBuffer(1);
	private final Coo MOUSE_COO = new Coo();
	
	private final int clickMax = 100;
	private final MButt[] clicks = new MButt[clickMax];
	private volatile int clickCurrent = 0;
	
	
	private final long window;
	
	private final GLFWMouseButtonCallback callback;
	private final GLFWScrollCallback sCallback;
	
	Mouse(long window) {
		
		this.window = window;
		
		callback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				
				if (button > 2){
					return;
				}
				if (MButt.ALL.get(button).isDown = action == GLFW.GLFW_PRESS){
					
					long nanoOld = MButt.ALL.get(button).nanoNow;
					MButt.ALL.get(button).nanoNow = Input.nanoNow;
					
					if (Input.nanoNow - nanoOld < 250000000 && clickCurrent < clickMax){
						MButt.ALL.get(button).isDouble = true;
					}
					if (clickCurrent < clickMax)
						clicks[clickCurrent++] = MButt.ALL.get(button);
				}
				
			}
		};
		glfwSetMouseButtonCallback(window, callback);
		
		sCallback = new GLFWScrollCallback() {
			
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				MButt.delta += yoffset;
				MButt.wheelDy += (int) MButt.delta;
				MButt.delta -= (int) MButt.delta;
				
				
				if ((int)MButt.wheelDy != 0 && clickCurrent < clickMax)
					clicks[clickCurrent++] = MButt.WHEEL_SPIN;
				
			}
		};
		glfwSetScrollCallback(window, sCallback);
		
	}
	
	void applySettings(SETTINGS sett) {
		mXC = (float)sett.getNativeWidth()/(float)CORE.getGraphics().displayWidth;
       mYC = (float)sett.getNativeHeight()/(float)CORE.getGraphics().displayHeight;
	}
	

	boolean update(){
		
		if (!CORE.getGraphics().isFocused())
			return false;
		
		GLFW.glfwGetCursorPos(window, mX, mY);
        float newX = (float) Math.ceil((mX.get()*mXC));
        float newY = (float) Math.ceil((mY.get()*mYC));
        boolean ret = false;
        
        if (newX >= 0 && newX <= CORE.getGraphics().nativeWidth && newY >= 0 && newY <= CORE.getGraphics().nativeHeight){
        	if (newY != MOUSE_COO.y() || newX != MOUSE_COO.x()){
	        	ret = true;
	        	MOUSE_COO.ySet(newY);
	        	MOUSE_COO.xSet(newX);
	        }
        }
        mX.clear();
        mY.clear();
		return ret;
	}
	
	void poll (CORE_STATE current){
		update();
		for (MButt b : MButt.ALL)
			b.clicks = 0;
		if (clickCurrent > 0)
			currentClicked = null;
		for (int i = 0; i < clickCurrent; i++) {
			clicks[i].clicks ++;
			current.mouseClick(clicks[i]);
		}
		for (MButt b : MButt.ALL)
			b.isDouble = false;
		clickCurrent = 0;
	}
	
	void clear(){
		clickCurrent = 0;
		MButt.wheelDy = 0;
		for (MButt b : MButt.ALL) {
			b.isDouble = false;
			b.isDown = false;
			b.clicks = 0;
		}
	}
	
	public void showCusor(boolean yes){
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, 
				yes ? GLFW.GLFW_CURSOR_NORMAL : GLFW.GLFW_CURSOR_HIDDEN);
	}
	
	public Coo getCoo(){
		return MOUSE_COO;
	}
	
	public void setMousePoss(float x, float y){
		GLFW.glfwSetCursorPos(window, x, y);
	}
	
	void release(){
		callback.close();
		sCallback.close();
	}
	
}
