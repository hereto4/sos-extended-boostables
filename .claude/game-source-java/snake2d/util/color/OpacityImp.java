package snake2d.util.color;

import snake2d.CORE;


public class OpacityImp implements OPACITY{
	
	public static final OpacityImp TMP = new OpacityImp(0);
	
	private byte opacity;
	
	public OpacityImp(int op){
		set(op);
	}
	
	public OpacityImp(OPACITY o){
		this.opacity = o.get();
	}
	
	public void set(int op){
		if (op < 0)
			op = 0;
		else if (op > 255)
			op = 255;
		opacity = (byte) op;
	}
	
	public void set(double op){
		set((int)(op*255));
	}
	
	
	public void set(OPACITY o){
		this.opacity = o.get();
	}
	
	@Override
	public byte get(){
		return opacity;
	}
	
	public void increase(float factor){
		set((int) (opacity *factor));
	}
	
	public void increase(int amount){
		set(opacity + amount);
	}
	
	public static void unBind(){
		CORE.renderer().setNormalOpacity();
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + " " + opacity;
	}
	
}
