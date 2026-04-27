package settlement.job;

import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TWIDTH;

import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.job.StateManager.State;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import util.text.D;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class PlacerActivate extends PlacableMulti{

	private static CharSequence ¤¤name = "¤Activate Job";
	private static CharSequence ¤¤desc = "¤Activates suspended jobs.";
	static {
		D.ts(PlacerActivate.class);
	}
	
	public PlacerActivate() {
		super(¤¤name, ¤¤desc, SPRITES.icons().l.suspend.twin(UI.icons().m.anti, DIR.C, 1));
	}
	
	static void place(int tx, int ty) {
		int i = tx +ty*TWIDTH;
		if (JOBS().getter.is(i)) {
			JOBS().state.activate(i, JOBS().getter.get(i));
		}
		ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
		if (j != null && !j.jobToggleIs())
			j.jobToggle(true);
	}
	
	@Override
	public PLACABLE getUndo() {
		return JOBS().tool_dormant;
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		ROOM_JOBBER j = ROOM_JOBBER.get(fromX, fromY);
		return j != null && j.is(toX, toY);
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		int i = tx +ty*TWIDTH;
		
		ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
		
		if (ROOMS().map.is(i)) {
			
			if (j != null)
				return null;
		}else if (JOBS().getter.is(i)) {
			if (JOBS().state.is(i, State.DORMANT))
				return null;
		}
		return "";
	}

	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		int i = tx +ty*TWIDTH;
		if (JOBS().getter.is(i)) {
			JOBS().state.activate(i, JOBS().getter.get(i));
		}
		ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
		if (j != null && !j.jobToggleIs())
			j.jobToggle(true);
	}
	

}
