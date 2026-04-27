package view.sett.ui.room;

import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.text.Dic;
import view.interrupter.ISidePanel;

class ConstructionList extends ISidePanel{

	private static int WIDTH = 120;
	private static int XX = 4;
	
	
	public ConstructionList() {
		titleSet(Dic.¤¤construction);
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return (int)Math.ceil((double)SETT.ROOMS().construction.instances()/XX);
			}
		};
		
		for (int i = 0; i < XX; i++) {
			final int k = i;
			bu.column(null, WIDTH, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new Entry(ier, k);
				}
			});
		}
		
		
		section.add(bu.createHeight(HEIGHT-32, false));
		
	}
	
	private static class Entry extends ClickableAbs {

		private final int col;
		private GETTER<Integer> ier;
		
		Entry(GETTER<Integer> ier, int col){
			this.ier = ier;
			this.col = col;
			body.setDim(WIDTH, 48);
		}
		
		@Override
		protected void clickA() {
			int i = ier.get()*XX + col;
			if (i >= SETT.ROOMS().construction.instances())
				return;
			
			SETT.ROOMS().construction.clickButt(i);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			int i = ier.get()*XX + col;
			if (i >= SETT.ROOMS().construction.instances())
				return;
			
			SETT.ROOMS().construction.hoverButt((GBox) text, i);
			
			super.hoverInfoGet(text);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			int i = ier.get()*XX + col;
			if (i >= SETT.ROOMS().construction.instances())
				return;
			
			GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
			SETT.ROOMS().construction.renderButt(r, body.x1()+8, body.cY(), i);
			GButt.ButtPanel.renderFrame(r, body);
		}
		
	}
	
	
}
