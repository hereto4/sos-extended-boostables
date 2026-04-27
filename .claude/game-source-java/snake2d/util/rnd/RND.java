package snake2d.util.rnd;

import java.util.Random;

import snake2d.Printer;


public class RND {

	private static Random rnd;
	private static int seed;
	
	static {
		rnd = new Random();
		seed = rnd.nextInt();
		rnd = new Random(seed);
		long seed = rnd.nextLong();
		Printer.ln("[RND] ---Initiating random, seed: " + seed);
		//rnd.setSeed(System.nanoTime());
		//seed stuff
	}
	
	public static int rInt(){
		return rnd.nextInt();
	}

	public static boolean rBoolean() {
		return rnd.nextBoolean();
	}
	
	public static int rInt(int max) {
		return rnd.nextInt(max);
	}

	public static int rInt0(int dist) {
		if (dist == 0)
			return 0;
		return -dist + rnd.nextInt(dist*2 + 1);
	}
	
	public static float rFloat() {
		return rnd.nextFloat();
	}
	
	public static float rFloatP(float exponent){
		float res = RND.rFloat();
		while (exponent > 1){
			res *= res;
			exponent --;
		}
		return res;
	}
	
	public static float rFloat(double d) {
		return (float) (rnd.nextFloat()*d);
	}

	/**
	 * 
	 * @param d
	 * @return get a float from 1-d to 1+d
	 */
	public static float rFloat1(double d) {
		return (float) ((1.0 -d ) + rnd.nextFloat()*d*2.0);
	}
	
	/**
	 * 
	 * @param d
	 * @return a float f, f >= -d, f < d
	 */
	public static float rFloat0(double d) {
		return (float) (-d + rnd.nextFloat()*d*2.0);
	}
	
	public static boolean oneIn(int what){
		if (what <= 1)
			return true;
		return rnd.nextInt(what) == what-1;
	}
	
	public static boolean oneIn(double what){
		return oneIn((int) what);
	}
	
	public static boolean oneInD(double what){
		return rFloat()*what < 1;
	}

	public static short rShort(int upperBound) {
		return (short) rInt(upperBound);
	}
	
	public static short rShort() {
		return rShort(Short.MAX_VALUE);
	}

	public static long rLong() {
		return rnd.nextLong();
	}
	
	public static float rExpo(){
		float f = rFloat();
		return f*f;
	}
	
	public static double rSign() {
		if (rBoolean())
			return 1.0;
		return -1.0;
	}

	public static int seed() {
		return seed;
	}
	
	public static void setSeed(int seed) {
		RND.seed = seed;
		rnd.setSeed(seed);
	}
	
		
}
