package game.boosting;

import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LIST;

public final class BUtil {
	
	private BUtil() {
		
	}
	
	public static <T> double min(LIST<? extends BoosterAbs<T>> all, Class<?> b, double baseValue) {
		double m = 1;
		double a = baseValue;
		for (int i = 0; i < all.size(); i++) {
			BoosterAbs<T> bb = all.get(i);
			if (bb.isMul) {
				m *= bb.min();
			}else {
				a += bb.min();
			}
				
		}
		return m * a;
	}

	public static <T> double max(LIST<? extends BoosterAbs<T>> all, Class<?> b, double baseValue) {
		double m = 1;
		double a = baseValue;
		for (int i = 0; i < all.size(); i++) {
			BoosterAbs<T> bb = all.get(i);
			if (bb.isMul) {
				m *= bb.max();
			}else {
				a += bb.max();
			}
				
		}
		return m * a;
	}
	
	public static double value(LIST<? extends BoosterAbs<?>> all, double input, double add, double mul, double minValue){
		double padd = add > 0 ? add : 0;
		double sub = add < 0 ? add : 0;
		for (int si = 0; si < all.size(); si++) {
			BoosterAbs<?> s = all.get(si);
			if (s.isMul)
				mul *= s.getValue(input);
			else {
				double a = s.getValue(input);
				if (a < 0)
					sub += a;
				else
					padd += a;
			}
		}
		return CLAMP.d(padd*mul + sub, minValue, Double.MAX_VALUE);
	}
	
	public static <T> double value(LIST<? extends BoosterAbs<T>> all, T t, double add, double mul, double minValue){
		double padd = add > 0 ? add : 0;
		double sub = add < 0 ? add : 0;
		for (int si = 0; si < all.size(); si++) {
			BoosterAbs<T> s = all.get(si);
			if (s.isMul)
				mul *= s.get(t);
			else {
				double a = s.get(t);
				if (a < 0)
					sub += a;
				else
					padd += a;
			}
			
		}
		return CLAMP.d(padd*mul + sub, minValue, Double.MAX_VALUE);
	}
	
	public static <T> double value(LIST<? extends BoosterAbs<T>> all, T t){
		return value(all, t, 1,1,0);
	}
	
	
}
