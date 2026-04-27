package settlement.job;

import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.Room;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.AREA;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public interface ROOM_JOBBER extends AREA {

	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return false job will dissapear before this. Must place anew if continue you wish (yoda)
	 */
	public void jobFinsih(int tx, int ty, RESOURCE r, int ram);
	
	public void jobToggle(boolean toggle);
	
	public boolean jobToggleIs();
	
	public default void jobSet(int tx, int ty, boolean active, RESOURCE res) {
		
		boolean dd = SETT.JOBS().planMode.is();
		
		SETT.JOBS().planMode.set(active);
		
		if (res == null)
			Placer.place(tx, ty, SETT.JOBS().room);
		else
			Placer.place(tx, ty, SETT.JOBS().rooms[res.bIndex()]);
		if (!active)
			PlacerDormant.place(tx, ty);
		else
			PlacerActivate.place(tx, ty);
		
		SETT.JOBS().planMode.set(dd);
	}
	
	public static void render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it, boolean repairing) {
		if (repairing) {
			it.lit();
			GCOLOR.MAP().JOB_ACTIVE.bind();
			SPRITES.cons().BIG.dashed.render(r, 0x0F, it.x(), it.y());
			COLOR.unbind();
		}
	}
	
	public default void jobClear(int tx, int ty) {
		PlacerDelete.place(tx, ty);
	}
	
	static ROOM_JOBBER get(int tx, int ty) {
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null && r instanceof ROOM_JOBBER)
			return (ROOM_JOBBER) r;
		return null;
	}
	
	public boolean needsFertilityToBeCleared(int tx, int ty);
	
	public default boolean needsTerrainToBeCleared(int tx, int ty) {
		return true;
	}
	
	public boolean becomesSolid(int tx, int ty);

	public int totalResourcesNeeded(int x, int y);
	
	public boolean isJobActive();
	
}
