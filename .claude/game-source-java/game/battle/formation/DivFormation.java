package game.battle.formation;

import game.battle.util.DIV_SPEC;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface DivFormation extends DivPosition, BODY_HOLDER{


	


	public DIR dir();
	
	public DIV_FORMATION formation();
	
	public COORDINATE start();
	
	public double dx();
	
	public double dy();
	
	public int width();
	public int height(DIV_SPEC spec);
	
	public int dirMaskOrtho(int i);
	
	public DIR dir(int i);
	
	public boolean isEdge(int i);
	
	


	public default boolean isSameAs(DivFormation o) {
		return start().isSameAs(o.start()) && dx() == o.dx() && dy() == o.dy() && deployed() == o.deployed() && width() == o.width() && formation() == o.formation() && centrePixel().isSameAs(o.centrePixel());
	}
	
	public COORDINATE centreTile();
	
	public COORDINATE centrePixel();
	
	public boolean isCoherent();
	public boolean hasExtraRoom();
}
