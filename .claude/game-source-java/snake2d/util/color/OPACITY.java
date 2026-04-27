package snake2d.util.color;

import snake2d.CORE;


public interface OPACITY {
	
	public static final OPACITY O100 = new OpacityImp(255); 
	public static final OPACITY O50 = new OpacityImp(128); 
	public static final OPACITY O35 = new OpacityImp(85); 
	public static final OPACITY O25 = new OpacityImp(64); 
	public static final OPACITY O018 = new OpacityImp(45); 
	public static final OPACITY O012 = new OpacityImp(32); 
	public static final OPACITY O005 = new OpacityImp(16); 
	public static final OPACITY O66 = new OpacityImp(170);
	public static final OPACITY O75 = new OpacityImp(191);
	public static final OPACITY O85 = new OpacityImp(216);
	public static final OPACITY O99 = new OpacityImp(254);
	public static final OPACITY O0 = new OpacityImp(0); 
	
	public static final OPACITY O25TO100 = new OpaPuls(255/4, 254);
	public static final OPACITY O75TO100 = new OpaPuls(3*255/4, 254);
	public static final OPACITY O12To25 = new OpaPuls(255/8, 254/4);
	public static final OPACITY O0To25 = new OpaPuls(0, 254/4);
	public static final OPACITY O25To50 = new OpaPuls(255/4, 254/2);
	
	public byte get();
	
	public default void bind(){
		CORE.renderer().setOpacity(this);
	}
	
	public static void unbind(){
		CORE.renderer().setNormalOpacity();
	}
}
