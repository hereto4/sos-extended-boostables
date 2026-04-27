package view.sett.ui.room.prints;

import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.ROOMA;
import settlement.room.main.Room;
import settlement.room.main.copy.SavedPrints.SavedPrint;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.text.D;
import view.main.VIEW;
import view.tool.PlacableMessages;
import view.tool.PlacableSingle;
import view.tool.ToolConfig;

class PlacerSave extends PlacableSingle{

	private static CharSequence ¤¤not = "¤A room that can be furnished must be selected.";
	
	static {
		D.ts(PlacerSave.class);
	}
	
	private final UISavedPrints pp;
	
	

	final ToolConfig config = new ToolConfig() {
	
		@Override
		public void addUI(LISTE<RENDEROBJ> uis) {
			
		}
		
		@Override
		public void activateAction() {

		};
		
		@Override
		public void update(boolean UIHovered) {
			if (!VIEW.s().panels.added(pp))
				VIEW.s().tools.place(null, null, false);
		};
		
		@Override
		public boolean back() {
			if (pp.placing != null) {
				pp.placing = null;
				return false;
			}
			VIEW.s().panels.remove(pp);
			return true;
		};
		
	};
	
	PlacerSave(UISavedPrints panel){
		super("");
		this.pp = panel;
	}

	@Override
	public CharSequence isPlacable(int tx, int ty) {
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r == null)
			return PlacableMessages.¤¤ROOM_MUST;
		if (!(r instanceof ROOMA))
			return ¤¤not;
		
		if (!SETT.ROOMS().copy.prints.canAdd(r))
			return ¤¤not;
		return null;
	}

	@Override
	public void placeFirst(int tx, int ty) {
		Room r = SETT.ROOMS().map.get(tx, ty);
		SavedPrint p = SETT.ROOMS().copy.prints.push(r, tx, ty);
		if (p != null)
			pp.set(p);
	}
	
	@Override
	public SPRITE getIcon() {
		return SPRITES.icons().m.crossair;
	}
	
	@Override
	public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
		return isPlacable(toX, toY) == null && SETT.ROOMS().map.get(fromX, fromY).isSame(fromX, fromY, toX, toY); 
	}
}