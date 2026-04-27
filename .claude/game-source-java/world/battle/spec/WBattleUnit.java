package world.battle.spec;

import snake2d.util.gui.GUI_BOX;
import snake2d.util.sprite.SPRITE;

public interface WBattleUnit {

	public CharSequence name();
	public int men();
	public int losses();
	public int lossesRetreat();
	public SPRITE icon();	
	public void hover(GUI_BOX box);
	public double defences();
}