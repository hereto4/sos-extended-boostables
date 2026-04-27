package game.battle.formation;

import snake2d.util.datatypes.COORDINATE;

public interface DivPosition {
	
	public COORDINATE tile(int i);
	public COORDINATE pixel(int i);
	public int px(int i);
	public int py(int i);
	public int tx(int i);
	public int ty(int i);
	public int deployed();
	
}
