package util.data;

import snake2d.util.map.MAP_INT;
import snake2d.util.rnd.RND;

public final class RANMAP implements MAP_INT{

	private final int SIZE = 128;
	private final int yScroll = Integer.numberOfTrailingZeros(SIZE);
	private final int tMaskX = SIZE-1;
	private final int tMask = SIZE*SIZE-1;
	
	private final int[] ran = new int[SIZE*SIZE];
	
	public RANMAP(){
		for (int i = 0; i < ran.length; i++)
			ran[i] = RND.rInt()&0x7FFFFFFF;
	}
	
	@Override
	public int get(int tile) {
		return ran[tile&tMask];
	}

	@Override
	public int get(int tx, int ty) {
		tx &= tMaskX;
		ty &= tMaskX;
		return ran[tx + (ty<<yScroll)];
	}

}
