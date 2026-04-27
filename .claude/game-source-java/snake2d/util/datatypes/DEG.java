package snake2d.util.datatypes;

import snake2d.util.rnd.RND;

public class DEG {

	private static double[][] PRE = new double[360][2];
	static{
		for (int i = 0; i < PRE.length; i++){
			PRE[i][0] = Math.cos(Math.toRadians(i));
			PRE[i][1] = Math.sin(Math.toRadians(i));
		}
	}
	
	private static int currentI;
	private static int tmpI;
	
	private DEG(){
		
	}
	
	public static void set(COORDINATE coo){
		set(coo.x(), coo.y());
	}
	
	public static void set(VECTOR vec){
		set(vec.x(), vec.y());
	}
	
	public static void set(double x, double y){
		currentI = (int) Math.toDegrees(Math.atan2(y,x));
		if (currentI < 0)
			currentI+= 360;
	}
	
	public static double getCurrentX(){
		return PRE[currentI][0];
	}
	
	public static double getCurrentY(){
		return PRE[currentI][1];
	}
	
	public static double getTmpX(){
		return PRE[tmpI][0];
	}
	
	public static double getTmpY(){
		return PRE[tmpI][1];
	}
	
	public static void moveTmp(int deg){
		tmpI = currentI + deg;
		if (tmpI < 0)
			tmpI+= 360;
		else if (tmpI >= 360)
			tmpI -= 360;
		
	}

	public static void setRandom() {
		currentI = RND.rInt(360);
		
	}
}
