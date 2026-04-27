package view.world.ui.region;

import util.gui.misc.GBox;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import world.map.regions.Region;

interface RV {

	public ISidePanel get(Region reg);
	public void hover(GBox box, Region reg);
	public void hoverGarrison(GBox box, Region reg);
	public boolean added(ISidePanels pans, Region reg);
}
