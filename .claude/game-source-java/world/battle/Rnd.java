package world.battle;

import java.io.IOException;

import snake2d.util.MATH;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;

final class Rnd implements SAVABLE{

	private static final int[] rnds = new int[2024];
	private static int ri = 0;
	
	public static double f() {
		double d = inc()& Integer.MAX_VALUE;
		return d/Integer.MAX_VALUE;
	}
	
	public static int i() {
		return inc();
	}
	
	public static int i(int max) {
		return MATH.mod(inc(), max);
	}
	
	public static boolean oneIn(int am) {
		int d = MATH.mod(inc(), am);
		return d == 0;
	}
	
	private static int inc() {
		int r = rnds[ri];
		rnds[ri] = RND.rInt();
		ri ++;
		if (ri >= rnds.length)
			ri = 0;
		return r;
	}
	
	@Override
	public void save(FilePutter file) {
		file.is(rnds);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		file.is(rnds);
	}
	
	@Override
	public void clear() {
		for (int i = 0; i < rnds.length; i++)
			rnds[i] = RND.rInt();
		ri = 0;
		
	}
	
}
