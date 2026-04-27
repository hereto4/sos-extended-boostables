package view.sett.ui.room.copy;

import static settlement.main.SETT.ROOMS;

import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import util.text.Dic;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class First extends PlacableMulti{

	private final Source source;
	
	public First(Source source) {
		super(Dic.¤¤Copy, "", SPRITES.icons().m.expand);
		this.source = source;
	}

	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		if (!SETT.IN_BOUNDS(tx, ty))
			return E;
		if (Jobs.get(tx, ty) == null && !SETT.ROOMS().copy.copier.canCopy(tx, ty))
			return E;
		return null;
	}

	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		source.set(tx, ty, true);
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return ROOMS().copy.copier.canCopy(fromX, fromY) && ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
	}
	
	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area,
			PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
		if (source.is(tx, ty))
			return;
		if (isPlacable)
			super.renderPlaceHolder(r, mask, x, y, tx, ty, area, type, isPlacable, areaIsPlacable);
		else
			SPRITES.cons().BIG.dashed_hollow.render(r, mask, x, y);
	}
	
	private final PLACABLE undo = new PlacableMulti(Dic.¤¤Undo, "", SPRITES.icons().m.cancel) {
		
		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			source.set(tx, ty, false);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			if (source.is(tx, ty))
				return null;
			return E;
		}
		
		@Override
		public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
			return source.is(fromX, fromY) && ROOMS().copy.copier.canCopy(fromX, fromY) && ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY);
		}
	};
	
	@Override
	public PLACABLE getUndo() {
		return undo;
	}
	
	

}
