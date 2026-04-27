package view.battle.editor;

import game.battle.util.DIV_SPEC;
import init.constant.Config;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.gui.slider.GTarget;
import util.gui.table.GMatrix;
import util.text.Dic;
import view.main.VIEW;
import view.ui.div.UIDivEditor;
import world.army.AD;
import world.army.ADSupplies;
import world.army.ADSupplies.ADArtillery;

class ArmyDivs extends GuiSection{

	private GETTER_IMP<ArmySide> current;
	private ArrayList<RDiv> all = new ArrayList<RDiv>(Config.battle().DIVISIONS_PER_ARMY);
	private final UIDivEditor editor;
	
	
	
	ArmyDivs(GETTER_IMP<ArmySide> current, UIDivEditor editor){
		this.editor = editor;
		this.current = current;
		while(all.hasRoom())
			all.add(new RDiv());
		
		
		
		GMatrix m = new GMatrix(4, 9, VIEW.UI().div.normal.width(), VIEW.UI().div.normal.height()) {
			
			@Override
			public int nrOFEntries() {
				return current.get().divs.size();
			}
			
			@Override
			public RENDEROBJ get(int i, int columnI) {
				 all.get(i).div = current.get().divs.get(i);
				
				return all.get(i);
			}

			@Override
			public void move(int oldI, int newI) {

				DIV_SPEC after = current.get().divs.get(newI);
				DIV_SPEC dd = current.get().divs.removeOrdered(oldI);
				int ii = current.get().divs.indexOf(after);
				current.get().divs.insert(ii, dd);
				
			}
		};
		add(m);
		
		GuiSection as = new GuiSection();
		
		for (ADArtillery a : AD.supplies().arts()) {
			
			INTE ii = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return ADSupplies.artilleryMax;
				}
				
				@Override
				public int get() {
					return current.get().artillery[a.index()];
				}
				
				@Override
				public void set(int t) {
					current.get().artillery[a.index()] = t;
				}
			};
			
			GuiSection s = new GuiSection();
			s.hoverInfoSet(a.art.info.names);
			s.add(a.art.icon, 0, 0);
			s.addRightC(8, new GTarget(48, false, true, ii));
			
			as.addRightC(16, s);
			
		}
		
		addRelBody(6, DIR.N, as);
	}

	private class RDiv extends ClickableAbs{

		
		
		private DIV_SPEC div;
		private boolean exitHovered;
		
		RDiv(){
			body.setDim(VIEW.UI().div.normal.width(), VIEW.UI().div.normal.height());
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			VIEW.UI().div.normal.render(r, body.x1(), body.y1(), 1, div, isActive, isSelected, isHovered);
			if (isHovered) {
				if (!exitHovered)
					OPACITY.O66.bind();
				UI.icons().s.cancel.render(r, body.x2()-16, body.y1());
				OPACITY.unbind();
			}
		}
		
		@Override
		protected void clickA() {
			if (exitHovered) {
				current.get().divs.removeOrdered(div);
			}else
			editor.div().copyFrom(div);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (exitHovered) {
				text.title(Dic.¤¤remove);
				text.text(Dic.¤¤RightClick);
			}else
				VIEW.UI().div.normal.hover(div, text);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			exitHovered = false;
			if (super.hover(mCoo)) {
				if (mCoo.isWithin(body.x2()-16, body.x2(), body.y1(), body.y1()+16))
					exitHovered = true;
				if (MButt.RIGHT.consumeClick())
					current.get().divs.removeOrdered(div);
				return true;
			}
			return false;
		}
		
		
	}

}
