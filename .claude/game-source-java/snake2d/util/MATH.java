package snake2d.util;

public final class MATH {

	public final static QuickPOW pow15 = new QuickPOW(1.5, 64);
	
	private MATH() {
		
	}
	
	public static int mod(int a, int m) {
		int remainder = (a % m);
		a = ((remainder >> 31) & m) + remainder;
		return a;
	}
	
	public static double mod(double a, double b) {
		if (a < 0) {
			a = -a;
			a %= b;
			a = b-a;
			return a;
		}else if (a > b)
			return a % b;
		return a;
	}
	
	public static double distance(double from, double to, double max) {
		if (to < from)
			return max-from + to;
		return to-from;
	}
	
	public static int distance(int start, int current, int max) {
		if (current < start)
			return max-start + current;
		return current-start;
	}
	
	public static double distanceC(double from, double to, double max) {
		if (max <= 0)
			return 0;
		
		from = mod(from, max);
		to = mod(to, max);
		
		if (to < from) {
			return from-to;
		}
		return to-from;
	}
	
	public static int distanceC(int from, int to, int max) {
		from = mod(from, max);
		to = mod(to, max);
		
		if (to < from) {
			return from-to;
		}
		return to-from;
	}
		
	public static boolean isWithin(double point, double from, double to) {
		
		if (from < to)
			return point >= from && point < to;
		return point >= from || point < to;
	
		
	}
	
	public static int ETA(int now, int target, int timeCycle) {
		if (now <= target) {
			return target-now;
		}else {
			return timeCycle-now+target;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(MATH.mod(-1.0, 10));
		System.out.println(MATH.distanceC(-1, 5, 10));
		System.out.println(MATH.distanceC(5, -1, 10));
		
		System.out.println(MATH.distance(5, 4, 16));
		System.out.println(MATH.distance(5, -1, 10));
		
		
		System.out.println(MATH.ETA(12, 9, 16));
		
	}
	
	/**
	 * Math.pow(x,2) is quicker than this fix
	 * Math.sqrt is almost as quick
	 * @param d
	 * @param pow2
	 * @return
	 */
	public final static class QuickPOW {
		
		public final double pow;
		
		private final double[] pows; 
		
		public QuickPOW(double pow, int precistion){
			pows = new double[precistion];
			this.pow = pow;
			for (int i = 0; i < precistion; i++) {
				double d = (double)i/precistion;
				pows[i] = Math.pow(d, pow);
			}
		}
		
		public double pow(double d) {
			int prec = pows.length-1;
			double ii = (d*prec);
			int i = (int) ii;
			if (i < 0)
				return 0;
			if (i >= prec)
				return 1.0;
			
			ii-= i;
			double res = pows[i]*(1-ii);
			res += pows[i+1]*ii;
			
			return res;
		}
		
	}

	
}
