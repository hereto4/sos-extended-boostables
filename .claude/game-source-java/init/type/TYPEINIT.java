package init.type;

import java.io.IOException;

import init.INIT;
import init.INIT.InitResource;

public final class TYPEINIT extends InitResource{

	public TYPEINIT(INIT init) throws IOException {
		super(init);
		new HCLASSES();
		new CAUSE_LEAVES();
		new CAUSE_ARRIVES();
		new HTYPES(null);
		new CLIMATES();
		new TERRAINS();
		new TRAITS();
		new BUILDING_PREFS();
		WGROUP.init();
		HGROUP.init();
		POP_CL.init(null, null);
		new NEEDS();
		new DISEASES();

	}
	
	
}
