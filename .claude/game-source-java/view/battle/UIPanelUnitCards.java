package view.battle;

import game.battle.Army;
import game.battle.div.Div;
import init.constant.Config;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;

final class UIPanelUnitCards extends ISidePanel{

	
	private static int xs = 5;
	private final ArrayList<DivButton> cards = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private final ArrayList<DivButton> current = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private DivButton clicked;
	private boolean dragging;
	private final Army army;
	private final DivSelection selection;
	


	public UIPanelUnitCards(Army army, DivSelection selection) {
		titleSet(Dic.¤¤Army);
		this.army = army;
		this.selection = selection;
		this.section = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				init();
				super.render(r, ds);
				if (!MButt.LEFT.isDown()) {
					dragging = false;
				}
			};
		};
		for (Div d : army.divisions()) {
			cards.add(new DivButton(d, selection));
		}
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				int am = CLAMP.i(current.size(), 0, Config.battle().DIVISIONS_PER_ARMY);
				return (int) Math.ceil((double)am/xs);
			}
		};
		
		bu.column(null, xs*VIEW.UI().div.battle.width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return row(ier);
			}
		});
		
		section.add(bu.createHeight(HEIGHT, false));
		
		
	}

	private RENDEROBJ row(GETTER<Integer> ier) {
		GuiSection ss = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				
				int x1 = body().x1();
				int y1 = body().y1();
				clear();
				for (int i = 0; i < xs; i++) {
					int k = ier.get()*xs + i;
					if (k >= current.size()){
						break;
					}else {
						addRightC(0, current.get(k));
					}
				}
				body().moveX1Y1(x1, y1);
				body().setWidth(VIEW.UI().div.battle.width*xs);
				body().setHeight(VIEW.UI().div.battle.height);
				super.render(r, ds);
			}
			
		};
		ss.body().setWidth(VIEW.UI().div.battle.width*xs);
		ss.body().setHeight(VIEW.UI().div.battle.height);
		return ss;
	}
	
	private void init() {
		current.clearSloppy();
		for (Div d : army.ordered()) {
			if (d.menNrOf() > 0) {
				current.add(cards.get(d.indexArmy()));
			}else {
				selection.deSelect(d);
			}
			
		}
		//selection.clearHover();
	}
	
	
	private class DivButton extends ClickableAbs{

		private final Div div;
		private final DivSelection selection;
		
		DivButton(Div div, DivSelection selection){
			body.setDim(VIEW.UI().div.battle);
			this.div = div;
			this.selection = selection;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			isSelected = selection.selected(div);
			isHovered |= selection.hovered(div);
			VIEW.UI().div.battle.render(div, body().x1(), body().y1(), 1, r, isActive, isSelected, isHovered);
			
			if (dragging && isHovered && clicked != null && clicked != this && !KEYS.MAIN().UNDO.isPressed() && !KEYS.MAIN().MOD.isPressed()) {
				
				COLOR.GREEN100.render(r, body().x1()-2, body().x1()+2, body().y1(), body().y2());
				if (!MButt.LEFT.isDown()) {
					
					army.setDivAtOrderedIndex(div, clicked.div);
					selection.deSelect(clicked.div);
					selection.select(div);
					clicked = this;
				}
			}
			
		}

		@Override
		protected void clickA() {
			if (KEYS.MAIN().UNDO.isPressed() && clicked != null) {
				int ci = current.indexOf(this);
				int di = current.indexOf(clicked);
				int f = Math.min(ci, di);
				int t = Math.max(ci, di);
				
				for (int i = 0; i < current.size(); i++) {
					if (i >= f && i <= t)
						selection.select(current.get(i).div);
					else
						selection.deSelect(current.get(i).div);
				}
				
				
			}else if(KEYS.MAIN().MOD.isPressed()) {
				selection.sToggle(div);
			}else {
				for (int i = 0; i < current.size(); i++) {
					selection.deSelect(current.get(i).div);
				}
				selection.select(div);
				clicked = this;
				dragging = true;
				if (MButt.LEFT.isDouble() && div.menNrOf() > 0) {
					VIEW.s().battle.getWindow().centerer.set(div.reporter.body().cX(), div.reporter.body().cY());
					VIEW.b().getWindow().centerer.set(div.reporter.body().cX(), div.reporter.body().cY());
					VIEW.inters().popup.show(VIEW.UI().div.battle.hovBox(div), this);
					
					
				}
				
				
				
			}
			
			

		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (VIEW.inters().popup.showing())
				return;
			div.hoverInfo((GBox)text);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				selection.hover(div);
				return true;
			}
			return false;
		}
		
	}
}
