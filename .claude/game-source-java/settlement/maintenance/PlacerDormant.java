package settlement.maintenance;

import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.Room;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.SPRITE;
import util.text.D;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

final class PlacerDormant extends PlacableMulti{
	
	
	private static CharSequence ¤¤name = "¤Enable maintenance";
	private static CharSequence ¤¤nameAnti = "¤Disable maintenance";
	private static CharSequence ¤¤desc = "¤Enables maintenance performed by janitors in on area.";
	private static CharSequence ¤¤descAnti = "¤Disables maintenance performed by janitors on an area.";
	static {
		D.ts(PlacerDormant.class);
	}
	
	
	public PlacerDormant() {
		super(¤¤name, ¤¤desc
				,null);
	}
	
	@Override
	public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
		if (SETT.MAINTENANCE().disabled.is(tx, ty))
			return null;
		return E;
	}

	@Override
	public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
		SETT.MAINTENANCE().disabled.set(tx, ty, false);
		
	}
	
	@Override
	public PLACABLE getUndo() {
		return undo;
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		Room r = SETT.ROOMS().map.get(fromX, fromY);
		if (r != null && r.degrader(fromX, fromY) != null && r.isSame(fromX, fromY, toX, toY))
			return true;
		return false;
	}
	
	@Override
	public void updateRegardless(GameWindow window, AREA selected) {
		SETT.OVERLAY().MAINTENANCE.add();
		super.updateRegardless(window, selected);
	}
	
	private SPRITE icon;
	
	@Override
	public SPRITE getIcon() {
		if (icon == null)
			icon = SETT.ROOMS().JANITOR.icon.twin(UI.icons().s.cog, DIR.NE, 1);
		return icon;
	}
	
	public final PlacableMulti undo = new PlacableMulti(¤¤nameAnti, ¤¤descAnti, null) {
		
		private SPRITE icon;
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
			if (!SETT.MAINTENANCE().disabled.is(tx, ty))
				return null;
			return E;
		}

		@Override
		public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
			SETT.MAINTENANCE().disabled.set(tx, ty, true);
		}
		
		@Override
		public SPRITE getIcon() {
			if (icon == null)
				icon = SETT.ROOMS().JANITOR.icon.twin(UI.icons().m.anti, DIR.C, 1);
			return icon;
		}
		
		@Override
		public void updateRegardless(GameWindow window, AREA selected) {
			SETT.OVERLAY().MAINTENANCE.add();
			super.updateRegardless(window, selected);
		}
		
		
		@Override
		public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
			Room r = SETT.ROOMS().map.get(fromX, fromY);
			if (r != null && r.degrader(fromX, fromY) != null && r.isSame(fromX, fromY, toX, toY))
				return true;
			return false;
		}
		
		@Override
		public PLACABLE getUndo() {
			return PlacerDormant.this;
		}
	};

}