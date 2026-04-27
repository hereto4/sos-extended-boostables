package game.battle.formation;

import snake2d.util.datatypes.Rec;

public class FormationBody extends Rec{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public boolean init(DivPosition d) {
		clear();
		
		
		int cx = 0;
		int cy = 0;
		int am = 0;
		for (int i = 0; i < d.deployed(); i++) {
			int x = d.px(i);
			int y = d.py(i);
			cx += x;
			cy += y;
			am++;
		}
		
		if (am == 0)
			return false;
		
		cx/=am;
		cy/=am;
		
		int width = 0;
		int height = 0;
		am = 0;
		for (int i = 0; i < d.deployed(); i++) {
			int x = d.px(i);
			int y = d.py(i);
			
			width += Math.abs(cx-x);
			height += Math.abs(cy-y);
			am++;

			
		}
		
		if (am == 0)
			return false;
		
		width/=am;
		height/=am;
		width*=4;
		height*=4;
		setDim(width, height);
		moveC(cx, cy);
		
		
		return true;
	}
	
}
