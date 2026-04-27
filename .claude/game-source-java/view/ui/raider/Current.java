package view.ui.raider;

import game.GAME;
import game.battle.util.DIV_SPEC;
import game.raiding.Raider;
import game.raiding.RaiderPortrait;
import init.constant.Config;
import init.settings.S;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GMeter.GMeterCol;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.main.VIEW;

final class Current extends GuiSection{

	private static CharSequence ¤¤atLargeD = "This bandit is currently at large, and is contemplating their next raid on us.";
	private static CharSequence ¤¤killed = "This bandit is but a memory and was brought to justice {0}.";
	private static CharSequence ¤¤hidingD = "This bandit does not have the strength to attack us currently and will leave us alone for now.";
	private static CharSequence ¤¤distantD = "We are too insignificant and poor for this bandit to bother us.";
	private static CharSequence ¤¤raidingD = "This bandit is currently raiding you.";
	
	
	private static CharSequence ¤¤Ransom = "Ransom";
	private static CharSequence ¤¤Raids = "Raids";
	
	static {
		D.ts(Current.class);
	}
	
	int ri = 0;
	
	Current(int height){
		
		addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				text.setFont(UI.FONT().H2);
				GCOLOR.T().H1.bind();
				text.add(rr().name);
			}
		}.r(DIR.NW));
		
		addDown(8, new GStat() {
			
			@Override
			public void update(GText text) {
				text.setMaxWidth(700);
				text.setMultipleLines(true);
				if (GAME.raiders().current.current() == rr()) {
					text.color(GCOLOR.T().IBAD).add(¤¤raidingD);
				}else if (rr().defeated) {
					Str.TMP.clear();
					DicTime.setDate(Str.TMP.clear(), (int)rr().secondDefeated);
					text.add(¤¤killed);
					text.insert(0, Str.TMP);
					text.color(GCOLOR.T().IGREAT);
				}else if (!rr().hasInterrest()) {
					text.color(GCOLOR.T().WARNING).add(¤¤distantD);
				}else if (rr().isScared()) {
					text.color(GCOLOR.T().WARNING).add(¤¤hidingD);
				}else {
					text.color(GCOLOR.T().IBAD).add(¤¤atLargeD);
				}
			}
		}.r(DIR.NW));
		

		
		{
			GuiSection s = new GuiSection();
			int dd = 150;
			int gi = 0;
			
			s.addGridD(new GStat() {
				
				@Override
				public void update(GText text) {
					if (UIRaiding.statsVisible(rr())) {
						GFORMAT.i(text, rr().army.men);
					}else {
						text.add('?');
					}
					
					
				}
			}.hv(Dic.¤¤Soldiers), gi++, dd, 100, dd, DIR.N);
			
			s.addGridD(new GStat() {
				
				@Override
				public void update(GText text) {
					if (UIRaiding.statsVisible(rr())) {
						GFORMAT.i(text, rr().army.power);
					}else {
						text.add('?');
					}
					
				}
			}.hv(Dic.¤¤Power), gi++, dd, 100, dd, DIR.N);
			
			s.addGridD(new GStat() {
				
				@Override
				public void update(GText text) {
					if (UIRaiding.statsVisible(rr())) {
						GFORMAT.i(text, (int)rr().worth);
					}else {
						text.add('?');
					}
				}
			}.hv(¤¤Ransom), gi++, dd, 100, dd, DIR.N);
			
			s.addGridD(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, (int)rr().raids);
					
				}
			}.hv(¤¤Raids), gi++, dd, 100, dd, DIR.N);
			
			add(s, 80, body().y2()+40);
		}
		
		
		{
			GuiSection ss = new GuiSection();
			
			RaiderPortrait p = new RaiderPortrait(4) {
				@Override
				protected Induvidual raider() {
					dead(rr().defeated);
					return rr().indu;
				}
			};
			
			SPRITE sp = new SPRITE.Imp(p.width(), p.height()) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					if (UIRaiding.portVisible(rr()) || S.get().developer) {
						p.render(r, X1, Y1);
					}else {
						UI.icons().m.questionmark.renderC(r, X1, X2, Y1, Y2);
					}
					
					GCOLOR.UI().border().renderFrame(r, X1, X2, Y1, Y2, 2, 2);
				}
			};
			
			ss.add(sp, 0, 0);
			
			int am = 10;
			
			ArrayList<GuiSection> rows = new ArrayList<GuiSection>((int)Math.ceil(Config.battle().DIVISIONS_PER_ARMY/(double)am));

			for (int i = 0; i < rows.max(); i++) {
				rows.add(new GuiSection());
			}
			
			
			for (int i = 0; i < Config.battle().DIVISIONS_PER_ARMY; i++) {
				
				GuiSection s = rows.get(i/am);
				s.addRightC(2, new Button(i));
			}
			
			ss.addRelBody(8, DIR.E, new GScrollRows(rows, sp.height()) {
				@Override
				protected boolean passesFilter(int i, RENDEROBJ o) {
					return i <= Math.ceil(rr().army.sdivs.size()/am);
					
				};
			}.view());
			
			add(ss, 0, body().y2()+16);
		}
		


		
		
	}
	
	private static int dim = 50;
	
	private class Button extends HOVERABLE.HoverableAbs {
		
		private final int ii;
		
		Button(int ii){
			this.ii = ii;
			body.setDim(dim);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {;
			if (ii >= rr().army.sdivs.size())
				return;
			

			DIV_SPEC d = rr().army.sdivs.get(ii);
			
			if (d == null)
				return;
			
			GButt.ButtPanel.renderBG(r, true, isHovered, false, body);
			
			if (rr().raids > 0 || UIRaiding.debug) {
				d.race().appearance().icon.renderC(r, body().cX(), body().cY()-6);

				VIEW.UI().div.renderPower(body.x2()-16, body.y1()+4, r,  GAME.battle().power.get(d));
				
				
				int w = (int) ((body.width()-8)*CLAMP.d((double)(d.men()+Config.battle().MEN_PER_DIVISION/5)/Config.battle().MEN_PER_DIVISION, 0, 1));
				
				GMeterCol col = GMeter.C_REDBLUE;
				
				
				GMeter.render(r, col, 1.0, body.x1()+4, body.x1()+4+w, body.y2()-14, body.y2()-6);
				OPACITY.unbind();
			}else {
				UI.icons().s.question.renderC(r, body().cX(), body.cY());
			}
			
			
			GButt.ButtPanel.renderFrame(r, body);
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (ii >= rr().army.sdivs.size())
				return;
			GBox b = (GBox) text;
			if (rr().raids > 0 || UIRaiding.debug) {
				DIV_SPEC d = rr().army.sdivs.get(ii);
				VIEW.UI().div.normal.hover(d, b);
			}
			else {
				b.add(b.text().add('?'));
			}
		}
		
	}
	
	public Raider rr() {
		return GAME.raiders().ALL().get(ri);
	}
	
}
