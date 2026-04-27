package view.sett.ui.home;

import game.faction.FACTIONS;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.gui.misc.GButt;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.keyboard.KeyButt;
import view.main.VIEW;
import view.sett.ui.bottom.RoomUpgrader;
import view.tool.PlacableMulti;

public class UIHomes extends ISidePanel{

	private final UIHomesTable table;
	private boolean overlay = true;
	
	public UIHomes() {
		
		{
			PlacableMulti pp = new UIHomeAssign();
			
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.s().tools.place(pp);
				}
			};
			
			GButt.ButtPanel c = new GButt.ButtPanel(pp.getIcon()){
				
				@Override
				protected void clickA() {
					VIEW.s().tools.place(pp);
				};
				
			};
			
			c.setDim(60, 40);
			CLICKABLE cc = KeyButt.wrap(a, c, KEYS.SETT(), "SET_HOMES", pp.name(), pp.desc);
			section.add(cc);
		}
		{
		
			PlacableMulti pp = new UIHomeOdd();
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(pp);
				}
			};
			
			GButt.ButtPanel c = new GButt.ButtPanel(pp.getIcon()){
				
				
				
				@Override
				protected void clickA() {
					a.exe();
				};
				
			};
			
			c.setDim(60, 40);
			
			CLICKABLE cc = KeyButt.wrap(a, c, KEYS.SETT(), "MOVE_HOMES", pp.name(), pp.desc);
			section.addRightC(8, cc);
			
		}
		
		{
			
			PlacableMulti pp = new RoomUpgrader();
			ACTION a = new ACTION() {
				
				@Override
				public void exe() {
					VIEW.inters().popup.close();
					VIEW.s().tools.place(pp);
				}
			};
			
			GButt.ButtPanel c = new GButt.ButtPanel(pp.getIcon()){

				@Override
				protected void clickA() {
					a.exe();
				};
				
				@Override
				protected void renAction() {
					activeSet(SETT.ROOMS().HOME.reqs.passes(FACTIONS.player()));
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					text.NL(8);
					if (!SETT.ROOMS().HOME.reqs.passes(FACTIONS.player()))
						SETT.ROOMS().HOME.reqs.hover(text, FACTIONS.player());
				}
				
			};
			
			c.setDim(60, 40);
			
			CLICKABLE cc = KeyButt.wrap(a, c, KEYS.SETT(), "UPGRADE_HOMES", pp.name(), pp.desc);
			section.addRightC(8, cc);
			
		}
		
		if (false){
			//oddjobber mover seems to move different species into houses
		}
		
		{
			GButt.ButtPanel c = new GButt.ButtPanel(SPRITES.icons().m.place_brush){
				
				@Override
				protected void clickA() {
					overlay = overlay || SETT.OVERLAY().HOMELESS.added();
					overlay = !overlay;
					if (!overlay)
						VIEW.s().overlayThing.set(null);
				};
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					text.title(Dic.¤¤Overlay);
					text.text(SETT.OVERLAY().HOMELESS.desc);
				}
				
				@Override
				protected void renAction() {
					selectedSet(overlay || SETT.OVERLAY().HOMELESS.added());
					if (overlay)
						SETT.OVERLAY().HOMELESS.add();
				};
				
			};
			c.setDim(60, 40);
			section.addRightC(8, c);
		}
		
		titleSet(Dic.¤¤Housing);
		
		table = new UIHomesTable(ISidePanel.HEIGHT-400);
		section.addRelBody(8, DIR.S, table);
		section.addRelBody(8, DIR.S, new UIHomesFurniture(300));
	}

	@Override
	protected void addAction() {
		table.subject = null;
	}
	
	@Override
	protected boolean back() {
		if (table.subject != null) {
			table.subject = null;
			return true;
		}
		return false;
	}


	
}
