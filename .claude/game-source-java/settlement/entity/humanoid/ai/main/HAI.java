package settlement.entity.humanoid.ai.main;

import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;

public interface HAI {

	public RESOURCE resourceCarried();
	public int resourceA();
	public void getOccupation(Humanoid a, Str string);
	public COORDINATE getDestination();
	public void hoverInfoSet(Humanoid a, GBox text);

}
