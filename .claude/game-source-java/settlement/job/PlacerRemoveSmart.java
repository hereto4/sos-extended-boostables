package settlement.job;

import static settlement.main.SETT.JOBS;

import init.sprite.SPRITES;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import util.text.D;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class PlacerRemoveSmart extends PlacableMulti{

	private static CharSequence ¤¤remove = "¤Smart Remove";
	private static CharSequence ¤¤desc = "¤Removes jobs if any selected, else removes structures and rooms.";
	static {
		D.ts(PlacerRemoveSmart.class);
	}
	
	private int stage = -1;
	
	PlacerRemoveSmart() {
		super(¤¤remove, ¤¤desc, SPRITES.icons().l.demolish.twin(UI.icons().s.allRight, DIR.NE, 2));
	}
	
	@Override
	public PLACABLE getUndo() {
		return SETT.JOBS().tool_clear;
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (stage == 1) {
			if (SETT.ROOMS().map.get(tx, ty) == null && SETT.JOBS().getter.get(tx, ty) != null) {
				return null;
			}
			if (SETT.ROOMS().construction.isser.is(tx, ty) && SETT.ROOMS().DELETE.isPlacable(tx,ty, null, null) == null) {
				return null;
			}
		}else if (stage == 2) {

			if (SETT.ROOMS().DELETE.isPlacable(tx, ty, a, t) == null)
				return null;
			if (SETT.JOBS().clearss.structure.problem(tx, ty, false) == null) {
				return null;
			}
		}
		
		return E;
		
	}

	@Override
	public CharSequence isPlacable(AREA area, PLACER_TYPE type) {
		stage = -1;
		for (COORDINATE c : area.body()) {
			if (area.is(c)) {
				if (SETT.ROOMS().map.get(c) == null && SETT.JOBS().getter.get(c) != null) {
					stage = 1;
					return null;
				}
				if (SETT.ROOMS().construction.isser.is(c.x(), c.y()) && SETT.ROOMS().DELETE.isPlacable(c.x(), c.y(), null, null) == null) {
					stage = 1;
					return null;
				}
				if (SETT.JOBS().clearss.structure.problem(c.x(), c.y(), false) == null) {
					stage = 2;
					return null;
				}
				if (SETT.ROOMS().DELETE.isPlacable(c.x(), c.y(), null, null) == null) {
					stage = 2;
					return null;
				}
			}
		}
		return E;
	}
	
	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (stage == 1) {
			if (SETT.ROOMS().map.get(tx, ty) == null && SETT.JOBS().getter.get(tx, ty) != null) {
				Job j = JOBS().getter.get(tx, ty);
				if (j != null)
					j.cancel(tx, ty);
				SETT.JOBS().clearer.set(tx, ty);
			}
			if (SETT.ROOMS().construction.isser.is(tx, ty) && SETT.ROOMS().DELETE.isPlacable(tx, ty, null, null) == null) {
				SETT.ROOMS().DELETE.place(tx, ty, a, t);
			}
		}else if (stage == 2) {
			if (SETT.ROOMS().DELETE.isPlacable(tx, ty, a, t) == null){
				SETT.ROOMS().DELETE.place(tx, ty, a, t);
			}
			if (SETT.JOBS().clearss.structure.problem(tx, ty, false) == null) {
				SETT.JOBS().clearss.structure.placer().place(tx, ty, a, t);
			}
			
		}
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		if (stage == 1) {
			return SETT.ROOMS().construction.isser.is(fromX, fromY) && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
		}
		if (stage == 2) {
			return SETT.ROOMS().DELETE.isPlacable(fromX, fromY, null, null) == null && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
		}
		return false;
	}

}
