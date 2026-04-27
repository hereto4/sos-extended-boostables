package settlement.job;

import static settlement.main.SETT.JOBS;

import init.resources.RBIT;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.datatypes.AREA;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

class Debug {


	static boolean showRoom = false;
	
	Debug(){
		
		PLACABLE p;
		
		p = new PlacableMulti("reservePerform") {
			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				Job j =  JOBS().getter.get(tx, ty);
				if (j == null)
					return;
				
				RBIT bb = j.jobResourceBitToFetch();
				RESOURCE res = null;
				if (bb != null) {
					for (RESOURCE r : RESOURCES.ALL())
						if (bb.has(r)) {
							res = r;
							break;
						}
				}
				
				if (j.jobReserveCanBe()) {
					j.jobReserve(res);
				}else if (j.jobReservedIs(res)) {
					j.jobPerform(null, res, 1);
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				Job j =  JOBS().getter.get(tx, ty);
				if (j != null) {
					
					RBIT bb = j.jobResourceBitToFetch();
					RESOURCE res = null;
					if (bb != null) {
						for (RESOURCE r : RESOURCES.ALL())
							if (bb.has(r)) {
								res = r;
								break;
							}
					}
					
					if (j.jobReserveCanBe() || j.jobReservedIs(res)) {
						return null;
					}
				}
				return "";
			}
		};
		

		

		
		IDebugPanelSett.add("job", p);
		
		BOOLEAN_MUTABLE roomJobs = new BOOLEAN_MUTABLE() {
			
			@Override
			public boolean is() {
				// TODO Auto-generated method stub
				return showRoom;
			}

			@Override
			public BOOLEAN_MUTABLE set(boolean bool) {
				showRoom = bool;
				return this;
			}
		};
		
		IDebugPanelSett.add("Show roomjobs", roomJobs);
		
	}
	
}
