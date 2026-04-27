package settlement.job;

import game.GAME;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.TmpArea;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.util.datatypes.AREA;
import util.text.D;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMessages;
import view.tool.PlacableMulti;

public class PlacerRemoveAll extends PlacableMulti{

	private static CharSequence ¤¤remove = "¤Dismantle";
	private static CharSequence ¤¤desc = "¤Dismantles structures and rooms.";
	static {
		D.ts(PlacerRemoveAll.class);
	}
	
	public PlacerRemoveAll() {
		super(¤¤remove, ¤¤desc, SPRITES.icons().l.demolish);
	}
	
	@Override
	public PLACABLE getUndo() {
		return SETT.JOBS().tool_clear;
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r instanceof ArtilleryInstance) {
			return ((ArtilleryInstance)r).army() == GAME.ARMIES().player() ? null : PlacableMessages.¤¤ROOM_MUST;
		}
		CharSequence ro = r != null &&  !SETT.ROOMS().THRONE.is(tx, ty) ? null : PlacableMessages.¤¤ROOM_MUST;
		CharSequence st = SETT.JOBS().clearss.structure.problem(tx, ty, false);
		if (ro == null || st == null)
			return null;
		return PlacableMessages.¤¤ROOM_OR_STRUCTURE_MUST;
		
	}

	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (SETT.ROOMS().map.is(tx, ty)) {
			TmpArea aa = SETT.ROOMS().map.get(tx, ty).remove(tx, ty, true, this, false);
			if (aa != null)
				aa.clear();
		}
		if (SETT.JOBS().clearss.structure.problem(tx, ty, false) == null)
			SETT.JOBS().clearss.structure.placer().place(tx, ty, a, t);
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		Room r = SETT.ROOMS().map.get(fromX, fromY);
		if (r == null)
			return false;
		if (SETT.ROOMS().THRONE.is(fromX, fromY))
			return false;
		return r.isSame(fromX, fromY, toX, toY);
	}

}
