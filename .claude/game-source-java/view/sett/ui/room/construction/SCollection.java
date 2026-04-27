package view.sett.ui.room.construction;

import game.faction.FACTIONS;
import settlement.room.main.RoomBlueprintImp;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GButt;
import view.main.VIEW;
import view.sett.ui.bottom.UIRoomBuild;

final class SCollection {

	private final State state;
	private final IButt[] butts = new IButt[32];
	private GuiSection section = new GuiSection();
	
	SCollection(State state){
		this.state = state;
		
		for (int i = 0; i < butts.length; i++) {
			butts[i] = new IButt(state, i);
		}
	}
	
	GuiSection get() {
		section.clear();
		if (state.collection == null || state.collection.rooms().size() == 0)
			return section;
		for (int i = 0; i < state.collection.rooms().size(); i++) {
			section.addGrid(butts[i], i, 8, 0, 0);
		}
		return section;
	}
	
	static class IButt extends GButt.ButtPanel {
		
		private final State state;
		private final int k;
		IButt(State state, int k){
			super(new SPRITE.Imp(init.sprite.UI.Icon.L){

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					RoomBlueprintImp b = state.collection.rooms().get(k);
					b.iconBig().render(r, X1, X2, Y1, Y2);
				}
				
			});
			this.state = state;
			this.k = k;
		}
		
		@Override
		protected void renAction() {
			RoomBlueprintImp b = state.collection.rooms().get(k);
			activeSet(b != null && b.reqs.passes(FACTIONS.player()));
			selectedSet(state.b == state.collection.rooms().get(k));
		}
		
		@Override
		protected void clickA() {
			VIEW.s().misc.placer.init(state.collection.rooms().get(k), state.collection);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			UIRoomBuild.hoverRoomBuild(state.collection.rooms().get(k), text);
		}
		
	}
	
	
}
