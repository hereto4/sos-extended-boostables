package world.map.regions;

import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

class Test {

	public static void main(String[] args) {
		
		final int tot = 500;
		Trans tr = null;
		T[] all = new T[tot];
		for (int i = 0; i < all.length; i++)
			all[i] = new T(RND.rFloatP(2));
		{
			double ave = 0;
			double max = 0;
			double mi = Double.MAX_VALUE;
			for (T t : all) {
				ave += t.value;
				max = Math.max(max, t.value);
				mi = Math.min(t.value, mi);
			}
			ave /= tot;
			
			tr = new Trans(ave, mi, max);
		}
		{
			double a = 0;
			double mi = Double.MAX_VALUE;
			double ma = 0;
			for (T t : all) {
				double v = tr.d(t.value);
				a += v;
				mi = Math.min(mi, v);
				ma = Math.max(v, ma);
			}
			System.out.println(a/tot + " " + mi + " " + ma);
		}
		
		
	}
	
	private static class T {
		
		public double value;
		
		T(double value){
			this.value = value;
		}
		
	}
	
	private static class Trans {
		
		public final double weight;
		public final double ave;
		public final double max;
		
		public Trans(double ave, double min, double max) {
			this.ave = ave;
			this.max = max;

			double w = 1 - ave/max;
			this.weight = 1.0/w;
			
		}
		
		private double d(double v) {

			double m = weight*ave/max;
			double d = (1-m + weight*v/max)/2.0;
			return CLAMP.d(d, 0, 1);
			
		}
		
	}
	
	
}
