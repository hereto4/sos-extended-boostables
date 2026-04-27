package view.ui.raider;

import java.util.ArrayList;

import game.GAME;
import game.raiding.Raider;
import game.raiding.RaiderPortrait;
import init.race.appearence.RPortrait;
import init.settings.S;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.table.GScrollRows;
import util.text.D;

final class List extends GuiSection{

	private static CharSequence ¤¤name = "Raiders";
	private static CharSequence ¤¤raiding = "Raiding";
	private static CharSequence ¤¤atLarge = "At Large!";
	private static CharSequence ¤¤killed = "R.I.P.";
	private static CharSequence ¤¤hiding = "In Hiding";
	private static CharSequence ¤¤distant = "Distant";
	
	
	private Current c;
	private boolean all = true;
	
	static{
		D.ts(List.class);
	}
	
	public List(Current c, int height){
		this.c = c;
		add(new GHeader(¤¤name));
		
		addRightC(16, new GButt.ButtPanel(UI.icons().s.question) {
			@Override
			protected void clickA() {
				all = !all;
			}
			@Override
			protected void renAction() {
				selectedSet(all);
			}
		});
		
		
		if (S.get().developer){
			addRightC(8, new GButt.ButtPanel(UI.icons().s.question) {
				@Override
				protected void clickA() {
					UIRaiding.debug = !UIRaiding.debug;
				}
				@Override
				protected void renAction() {
					selectedSet(UIRaiding.debug);
				}
			});
			
			addRightC(8, new GButt.ButtPanel(UI.icons().s.arrow_right) {
				@Override
				protected void clickA() {
					GAME.raiders().raid();
				}
			});
			
			addRightC(8, new GButt.ButtPanel(UI.icons().s.fish) {
				@Override
				protected void clickA() {
					GAME.raiders().reset();
				}
			});
		}
		
		{
			ArrayList<RENDEROBJ> rows = new ArrayList<>();
			
			for (int ri = 0; ri < GAME.raiders().AMOUNT; ri++) {
				
				rows.add(new RR(ri));
			}
			
			GScrollRows rr = new GScrollRows(rows, 70*((height-body().height()-16)/70)) {
				
				@Override
				protected boolean passesFilter(int i, RENDEROBJ o) {
					if (all)
						return true;
					final Raider rr = GAME.raiders().ALL().get(i); 
					if (UIRaiding.statsVisible(rr))
						return true;
					return false;
				}
				
			};
			
			addRelBody(8, DIR.S, rr.view());
		}
		
		
	}
	

	
	private class RR extends ClickableAbs{

		private final int ri;
		
		RR(int ri){
			this.ri = ri;
			body.setDim(450, 70);
		}
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			
			
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			final Raider rr = GAME.raiders().ALL().get(ri); 
			
			boolean active = true;
			isSelected = ri == c.ri;
			
			COLOR c = COLOR.WHITE100;
			Str.TMP.clear();
			
			if (GAME.raiders().current.current() == rr) {
				c = GCOLOR.T().IBAD;
				Str.TMP.add(¤¤raiding);
			}else if (rr.defeated) {
				active = false;
				c = GCOLOR.T().IGREAT;
				Str.TMP.add(¤¤killed);
			}else if (!rr.hasInterrest()) {
				active = false;
				c = GCOLOR.T().WARNING;
				Str.TMP.add(¤¤distant);
			}else if (rr.isScared()) {
				active = false;
				c = GCOLOR.T().WARNING;
				Str.TMP.add(¤¤hiding);
			}else {
				c = GCOLOR.T().IBAD;
				Str.TMP.add(¤¤atLarge);
			}
			
			
			
			GButt.ButtPanel.renderBG(r, active, isHovered, isSelected, body);
			
			if (UIRaiding.portVisible(rr)) {
				RaiderPortrait.render(r, body.x1()+8, body.y1()+6, 1, rr.indu, rr.defeated);
				if (rr.defeated) {
					UI.icons().m.anti.render(r,  body.x1()+8, body.x1()+8+RPortrait.P_WIDTH, body.y1()+6,  body.y1()+6+RPortrait.P_WIDTH);
				}
			}else {
				UI.icons().m.questionmark.renderC(r,  body.x1()+8+RPortrait.P_WIDTH/2, body.y1()+6+RPortrait.P_WIDTH/2);
			}
			

			
			
			
			c.bind();
			UI.FONT().S.render(r, Str.TMP, body.x1()+64+16, body.y1()+8+32);
			COLOR.unbind();
			
			(active ? OPACITY.O100 : OPACITY.O50).bind();
			UI.FONT().H2.renderCropped(r, rr.name, body.x1()+64, body.y1()+8, 370);
			
			
			UI.icons().s.money.render(r, body.x2()-200, body.y1()+8+32);
			if (UIRaiding.statsVisible(rr))
				Str.TMP.clear().add((int)rr.worth);
			else
				Str.TMP.clear().add('?');
			UI.FONT().S.render(r, Str.TMP, body.x2()-200+24, body.y1()+8+32);
			
			UI.icons().s.fist.render(r, body.x2()-100, body.y1()+8+32);
			if (UIRaiding.statsVisible(rr))
				Str.TMP.clear().add((int)rr.army.power);
			else
				Str.TMP.clear().add('?');
			
			UI.FONT().S.render(r, Str.TMP, body.x2()-100+24, body.y1()+8+32);
			COLOR.unbind();
			OPACITY.unbind();
//			
//			if (!active) {
//				OPACITY.O25.bind();
//				COLOR.WHITE50.render(r, body, -2);
//				OPACITY.unbind();
//			}
			
			
			GButt.ButtPanel.renderFrame(r, body);;
			
		}
		
		@Override
		protected void clickA() {
			c.ri = ri;
			
		}
		
		
	}
	

	
}
