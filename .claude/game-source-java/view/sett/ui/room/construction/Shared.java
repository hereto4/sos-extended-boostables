package view.sett.ui.room.construction;

import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.tilemap.terrain.TBuilding;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;
import view.main.VIEW;

public class Shared {
	
	private final GuiSection buttonsIndoor = new GuiSection();
	
	{
		D.gInit(this);
		
		for (TBuilding t : SETT.TERRAIN().BUILDINGS.all()) {
			
			
			CLICKABLE c = new GButt.Panel(t.iconCombo, t.structure.desc) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(t.structure.name);
					buttonIndoor.hoverInfoGet(text);
					b.NL();
					b.text(t.structure.desc);
					b.setResource(t.structure.resource, t.structure.resAmount);
				}
				
				@Override
				protected void clickA() {
					SETT.ROOMS().placement.placer.structure.set(t);
					VIEW.inters().popup.close();
				}
				
				@Override
				protected void renAction() {
					selectedSet(SETT.ROOMS().placement.placer.structure.get() == t);
				}
			};
			buttonsIndoor.addDownC(0, c);
		}
	}
	
	private final CLICKABLE buttonIndoor = new GButt.ButtPanel(SPRITES.icons().m.cancel) {
		@Override
		protected void clickA() {
			VIEW.inters().popup.show(buttonsIndoor, this);
		}
		@Override
		protected void renAction() {
			replaceLabel(SETT.ROOMS().placement.placer.structure.get().iconCombo, DIR.C);
		}
	}.hoverInfoSet(D.g("indoor", "This room requires to be built indoors and you must pick a structure type."));
	

	Shared(){
		
	}
	
}
