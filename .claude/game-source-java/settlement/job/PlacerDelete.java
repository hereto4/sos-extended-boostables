package settlement.job;

import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.TERRAIN;

import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.tilemap.terrain.TGrowable;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.datatypes.AREA;
import util.text.D;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMessages;
import view.tool.PlacableMulti;

final class PlacerDelete extends PlacableMulti{

	private static CharSequence ¤¤name = "Cancel Jobs";
	private static CharSequence ¤¤desc = "Cancels all jobs and room plans.";
	static {
		D.ts(PlacerDelete.class);
	}
	
	public PlacerDelete() {
		super(¤¤name, ¤¤desc, SPRITES.icons().m.cancel);
	}
	
	static void place(int tx, int ty) {
		
		if (JOBS().getter.is(tx, ty))
			JOBS().state.clear(tx, ty);
		
		TerrainTile t = TERRAIN().get(tx, ty);
		if (t instanceof TGrowable) {
			TGrowable b = (TGrowable) t;
			b.job.set(tx, ty, false);
		}
		
	}

	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		if (SETT.ROOMS().construction.isser.is(fromX, fromY)) {
			return SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
		}
		return false;
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		
		if (SETT.JOBS().clearss.huntundo.isPlacable(tx, ty, a, t) == null) {
			return null;
		}
		
		if (SETT.ROOMS().construction.isser.is(tx, ty))
			return null;
		ROOM_JOBBER j = ROOM_JOBBER.get(tx, ty);
		if (j != null)
			return PlacableMessages.¤¤JOB_MUST;
		if (!JOBS().getter.is(tx, ty)) {
			if (!SETT.TERRAIN().GROWABLES.get(0).job.is(tx, ty))
				return PlacableMessages.¤¤JOB_MUST;
		}
		return null;
	}


	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		
		if (SETT.JOBS().clearss.huntundo.isPlacable(tx, ty, a, t) == null) {
			SETT.JOBS().clearss.huntundo.place(tx, ty, a, t);
		}
		
		if (SETT.ROOMS().construction.isser.is(tx, ty)) {
			SETT.ROOMS().map.get(tx, ty).remove(tx, ty, true, this, false).clear();
		}
		Job j = JOBS().getter.get(tx, ty);
		if (j != null)
			j.cancel(tx, ty);
		place(tx, ty);
		if (SETT.TERRAIN().GROWABLES.get(0).job.is(tx, ty))
			SETT.TERRAIN().GROWABLES.get(0).job.set(tx, ty, false);
		
	}
	
	@Override
	public boolean canBePlacedAs(PLACER_TYPE t) {
		return true;
	}
	

}
