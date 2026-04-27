package settlement.job;

import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.TWIDTH;

import init.sprite.SPRITES;
import settlement.job.StateManager.State;
import snake2d.util.datatypes.AREA;
import util.text.D;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class PlacerDormant extends PlacableMulti{
	private static CharSequence ¤¤name = "¤Suspend Jobs";
	private static CharSequence ¤¤desc = "¤Suspended jobs will not be performed. Useful for planning out your city and controlling which areas will be worked first.";
	static {
		D.ts(PlacerDormant.class);
	}
	
	
	public PlacerDormant() {
		super(¤¤name, ¤¤desc
				,SPRITES.icons().l.suspend);
	}
	
	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
		if (j != null) {
			return null;
		}
		int i = tx+ty*TWIDTH;
		if (JOBS().getter.is(i) && !JOBS().state.is(i, State.DORMANT))
			return null;
		return "";
	}

	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		place(tx,ty);
	}
	
	static void place(int tx, int ty) {		
		ROOM_JOBBER r = ROOM_JOBBER.get(tx, ty);
		if (r != null && r.jobToggleIs())
			r.jobToggle(false);
		int i = tx+ty*TWIDTH;
		Job j = JOBS().getter.get(i);
		if (j == null)
			return;
		JOBS().state.set(State.DORMANT, j);
		
	}
	
	@Override
	public PLACABLE getUndo() {
		return JOBS().tool_activate;
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		ROOM_JOBBER j = ROOM_JOBBER.get(fromX, fromY);
		return j != null && j.is(toX, toY);
	}

}
