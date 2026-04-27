package snake2d.util.misc;

public final class CLAMP {

	private CLAMP() {
		
	}
	
	public static int i (int v, int min, int max) {
		if (v < min)
			return min;
		if (v > max)
			return max;
		return v;
	}
	
	public static byte b (byte v, int min, int max) {
		if (v < min)
			return (byte) min;
		if (v > max)
			return (byte) max;
		return v;
	}
	
	public static double d (double v, double min, double max) {
		if (Double.isNaN(v))
			return 0;
		if (v == Double.NEGATIVE_INFINITY)
			return min;
		if (v == Double.POSITIVE_INFINITY)
			return max;
		if (v < min)
			return min;
		if (v > max)
			return max;
		return v;
	}
	
	public static double c (double v, double max) {
		
		if (v < max) {
			return v;
		}
			
		if (v > max) {
			
			double d = v%max;
			
			
			int i = (int) (v/max);
			if ((i & 1) == 1) {
				return max-d;
			}
			return d;
		}
		
		return v;
	}

	
}
