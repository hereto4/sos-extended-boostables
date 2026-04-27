package settlement.misc.placers;

import snake2d.util.sets.ArrayList;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PlacableFixed;

public class ComplexPlacers {

	public final ArrayList<PLACABLE> ALL;
	public final PlacableFixed landingParty;
	
	public ComplexPlacers() {
		
		new PlacerLanding();
		landingParty = PlacerLanding.get();
		ALL = new ArrayList<>(landingParty);
		
		IDebugPanelSett.add("complex", ALL);
	}
	
}
