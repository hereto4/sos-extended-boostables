package view.battle;

import game.GAME;
import game.battle.Army;
import init.sprite.SPRITES;
import settlement.room.military.artillery.ArtilleryInstance;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListResize;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;

final class UIPanelArtillery extends ISidePanel{

	
	private final CatSelection selection;
	private final ArrayListResize<ArtilleryInstance> all = new ArrayListResize<>(256);
	
	private static int width = 160;

	public UIPanelArtillery(Army army, CatSelection selection) {
		titleSet(Dic.¤¤Artillery);
		this.selection = selection;
		GTableBuilder builder = new GTableBuilder() {
			
			private int upI = -1;
			
			@Override
			public int nrOFEntries() {
				if (upI != GAME.updateI()) {
					upI = GAME.updateI();
					all.clearSoft();
					for (ArtilleryInstance bb : selection.all()) {
						if (bb.army() == army) {
							all.add(bb);
						}
					}
				}
				return all.size();
			}
		};
		
		builder.column(null, width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new CatButton(ier, selection);
			}
		});
		
		section.add(builder.createHeight(HEIGHT, false));
		
		
	}
	
	private static int lastClicked;
	
	
	class CatButton extends ClickableAbs{

		private final GETTER<Integer> ier;

		CatButton(GETTER<Integer> ier, CatSelection selection){
			body.setDim(width, 28);
			this.ier = ier;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			ArtilleryInstance ins = get();
			if (ins == null)
				return;
			
			isSelected = ins.selected;
			isHovered = ins.hovered;
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
			ins.blueprintI().iconBig().medium.render(r, body().x1()+3, body().y1()+3);
			
			if (ins.mustered()) {
				if (ins.targetDivGet() != null || ins.targetCooGet() != null) {
					SPRITES.icons().s.bow.render(r, body().x2()-40, body().y1()+6);
				}
				if (ins.menMustering() == 0)
					GCOLOR.T().IBAD.bind();
				else if (ins.menMustering() == 1)
					GCOLOR.T().IGREAT.bind();
				else
					GCOLOR.T().IGOOD.bind();
				SPRITES.icons().s.human.render(r, body().x2()-20, body().y1()+6);
				COLOR.unbind();
			}
		}
		
		@Override
		protected void clickA() {
			ArtilleryInstance ins = get();
			if (ins == null)
				return;
			
			if (KEYS.MAIN().MOD.isPressed()) {
				selection.toggle(ins);
				lastClicked = -1;
			}else if(KEYS.MAIN().UNDO.isPressed() && lastClicked != -1) {
				int s = lastClicked;
				int e = ier.get();
				
				if (e < s) {
					int k = s;
					s = e;
					e = k;
				}
				for (; s <= e; s++) {
					if (s >= 0 && s < selection.all().size())
						selection.select(selection.all().get(s));
				}
			}else {
				selection.clear();
				selection.toggle(ins);
				lastClicked = ier.get();
			}
			
			
			if (MButt.LEFT.isDouble()) {
				VIEW.s().battle.getWindow().centererTile.set(ins.body().cX(), ins.body().cY());
			}
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			ArtilleryInstance ins = get();
			if (ins == null)
				return;
			ins.hover(text);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				ArtilleryInstance ins = get();
				if (ins != null)
					ins.hovered = true;
				return true;
			}
			return false;
		}
		
		private ArtilleryInstance get() {
			int i = ier.get();
			if (i < 0 || i >= all.size())
				return null;
			return all.get(i);
		}
		
	}
}
