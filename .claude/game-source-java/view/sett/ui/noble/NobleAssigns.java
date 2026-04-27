package view.sett.ui.noble;

import game.GAME;
import game.nobility.Noble;
import game.nobility.NobleOffice;
import init.constant.C;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GButt;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import view.main.VIEW;

class NobleAssigns extends GuiSection{

	Noble n;
	
	NobleAssigns(){
		
		GRows rows = new GRows(8);
		
		for (NobleOffice o : GAME.NOBLE().OFFICES) {
			
			if (!o.special)
				rows.add(new BB(o));
			
			
		}
		rows.nl();
		for (NobleOffice o : GAME.NOBLE().OFFICES) {
			
			if (o.special)
				rows.add(new BB(o));
			
			
		}
		
		GScrollRows rr = new GScrollRows(rows.rows(), C.HEIGHT()-300);
		
		add(rr.view());
		
		
	}
	
	private class BB extends GButt.ButtPanel{
		
		private final NobleOffice o;
		
		BB(NobleOffice o){
			super(o.icon.huge);
			this.o = o;
			pad(4, 4);
		}
		
		@Override
		protected void renAction() {
			selectedSet(n.office() == o);
			//activeSet(GAME.NOBLE().allocations(o) == 0 || n.office() == o);
		}
		
		@Override
		protected void clickA() {
			GAME.NOBLE().setOffice(n, o);
			VIEW.inters().popup.close();
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			o.hover(text);
		}
	}

}
