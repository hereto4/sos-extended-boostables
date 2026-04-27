package view.sett.ui.room.construction;

import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;

final class SShape{

	
	private static CharSequence ¤¤Expand = "Expand room. Items can only be placed on the designated room area.";
	private static CharSequence ¤¤ExpandOver = "Expand room over existing structures";
	private static CharSequence ¤¤Overlay = "Toggle overlay";
	private static CharSequence ¤¤Shrink = "Shrink Room";
	
	static {
		D.ts(SShape.class);
	}
	
	private final GuiSection ss = new GuiSection() {
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			boolean b = VIEW.s().tools.placer.getCurrent() == s.placement.placer.area() || VIEW.s().tools.placer.getCurrent() == s.placement.placer.area().getUndo();
			if (b)
				SETT.ROOMS().placement.placer.renderExpense();
		};
	};
	private final State s;
	private final GuiSection pButts = new GuiSection();
	private final GuiSection butts = new GuiSection();

	private final GHeader title = new GHeader(Dic.¤¤Shape).subify();
	

	
	private final CLICKABLE buttExpand = new GButt.ButtPanel(SPRITES.icons().m.expand) {
		
		@Override
		protected void clickA() {
			s.placement.placer.buildOnWalls.set(false);
			VIEW.s().tools.place(s.placement.placer.area(), s.config);
		}
		
		@Override
		protected void renAction() {
			selectedSet(VIEW.s().tools.placer.getCurrent() == s.placement.placer.area() && !s.placement.placer.buildOnWalls.is());
		}
	}.hoverInfoSet(¤¤Expand);
	
	final CLICKABLE buttExpandWalls = new GButt.ButtPanel(new SPRITE.Twin(SPRITES.icons().m.expand, SPRITES.icons().m.plus)) {
		
		@Override
		protected void clickA() {
			s.placement.placer.buildOnWalls.set(true);
			VIEW.s().tools.place(s.placement.placer.area(), s.config);
		}
		
		@Override
		protected void renAction() {
			selectedSet(VIEW.s().tools.placer.getCurrent() == s.placement.placer.area() && s.placement.placer.buildOnWalls.is());
		}
	}.hoverInfoSet(¤¤ExpandOver);
	
	final CLICKABLE buttOverlay = new GButt.ButtPanel(UI.icons().s.eye.sized(Icon.M)) {
		
		@Override
		protected void clickA() {
			s.placement.placer.showOverlay.toggle();
		}
		
		@Override
		protected void renAction() {
			selectedSet(s.placement.placer.showOverlay.is());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			text.title(¤¤Overlay);
			if (s.placement.placer.blueprint().constructor().overlay() != null && s.placement.placer.blueprint().constructor().overlay().desc != null) {
				text.text(s.placement.placer.blueprint().constructor().overlay().desc);
			}
		};
	};
	
	private final CLICKABLE buttShrink = new GButt.ButtPanel(SPRITES.icons().m.shrink) {
		
		@Override
		protected void clickA() {
			VIEW.s().tools.place(s.placement.placer.area().getUndo(), s.config);
		}
		
		@Override
		protected void renAction() {
			bg(GCOLOR.UI().BAD.normal);
			selectedSet(VIEW.s().tools.placer.getCurrent() == s.placement.placer.area().getUndo());
		}
		
	}.hoverInfoSet(¤¤Shrink);
	

	SShape(State s){
		this.s = s;
	}
	
	GuiSection get(){
		ss.clear();
		
		ss.add(title);
		
		butts.clear();
		butts.add(buttExpand, 0, 0);
		butts.addRightC(2, buttExpandWalls);
		if (SETT.ROOMS().placement.placer.blueprint().constructor().overlay() != null)
			butts.addRightC(2, buttOverlay);
		butts.addRightC(2, buttShrink);
		
		butts.body().incrW(buttExpand.body().width()+10);
		
		ss.addRelBody(4, DIR.S, butts);
		
		
		pButts.clear();
		pButts.body().setDim(1, 32);
		if (VIEW.s().tools.placer.getCurrent() == s.placement.placer.area() || VIEW.s().tools.placer.getCurrent() == s.placement.placer.area().getUndo())
			VIEW.s().tools.placer.stealButtons(pButts, true);
		ss.addRelBody(0, DIR.S, pButts);
		
		
		return ss;
	}
	
	
	
}
