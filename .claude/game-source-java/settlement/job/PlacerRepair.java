package settlement.job;

import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TERRAIN;

import init.sprite.SPRITES;
import settlement.room.main.Room;
import settlement.tilemap.TILE_FIXABLE;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import util.text.D;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMessages;
import view.tool.PlacableMulti;

class PlacerRepair extends PlacableMulti{
	
	
	private static CharSequence ¤¤name = "Repair";
	private static CharSequence ¤¤desc = "Repair damaged structures and rooms.";
	
	static {
		D.ts(PlacerRepair.class);
	}
	
	public PlacerRepair() {
		super(¤¤name, ¤¤desc, SPRITES.icons().l.repair);
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (ROOMS().construction.isRepair(tx, ty))
			return null;
		TerrainTile tt = TERRAIN().get(tx, ty);
		if (!(tt instanceof TILE_FIXABLE))
			return PlacableMessages.¤¤BROKEN_MUST;
		Job j = ((TILE_FIXABLE)tt).fixJob(tx, ty);
		if (j == null)
			return PlacableMessages.¤¤BROKEN_MUST;
		return null;
	}

	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (ROOMS().construction.isRepair(tx, ty)) {
			PlacerActivate.place(tx, ty);
		}else {
			TerrainTile tt = TERRAIN().get(tx, ty);
			Job j = ((TILE_FIXABLE)tt).fixJob(tx, ty);
			j.placer().place(tx, ty, a, t);
		}
		
	}
	
	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA a, PLACER_TYPE t,
			boolean isPlacable, boolean areaIsPlacable) {
		SPRITES.cons().ICO.repair.render(r, x, y);
	}
	
	@Override
	public PLACABLE getUndo() {
		return JOBS().tool_clear;
	}

	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		Room r = ROOMS().map.get(fromX, fromY);
		return (ROOMS().construction.isRepair(fromX, fromY) && r.isSame(fromX, fromY, toX, toY));
	}
	

	
}
