package view.battle;

import game.GAME;
import game.battle.Army;
import game.battle.div.Div;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.panel.GPanel;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;

public abstract class UIBattleResult extends GuiSection{

	
	private static CharSequence ¤¤casulties = "Casualties";
	private static CharSequence ¤¤kills = "Enemy Kills";
	
	static {
		D.ts(UIBattleResult.class);
	}
	
	public UIBattleResult(CharSequence title){
		
		
		
		add(side(GAME.ARMIES().player()));
		addRelBody(8, DIR.E, side(GAME.ARMIES().enemy()));
		
		GText tt = new GText(UI.FONT().H1, title).lablify();
		RENDEROBJ thing = new RENDEROBJ.RenderImp(tt.width(), UI.PANEL().titleBoxes[UI.PANEL().titleBoxes.length-1].height) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				UI.PANEL().titleBoxes[UI.PANEL().titleBoxes.length-1].renderCY(r, body.x1(), body.cY(), body().width());
				tt.renderC(r, body);
			}
		};
		addRelBody(16, DIR.N, thing);
		
		GuiSection s = new GuiSection();
		
		s.addRightC(0, new GButt.ButtPanel(Dic.¤¤Close) {
			@Override
			protected void clickA() {
				close();
			}
		});
		s.addRightC(0, new GButt.ButtPanel(BattlePanel.¤¤restart) {
			@Override
			protected void clickA() {
				VIEW.b().state().reloadBattle();
			}
		});
		addRelBody(16, DIR.S, s);
		
		add(new GPanel(this.body()));
		moveLastToBack();
		
		
		
		
	}
	
	private RENDEROBJ side(Army army) {
		
		
		GRows rr = new GRows(8);
		
		int deaths = 0;
		for (Div div : army.divisions()) {
			
			if (div.info.men() <= 0) {
				continue;
			}
			deaths += div.info.men()-div.menNrOf();
			rr.add(new Card(div));
		}
		GuiSection s = new GuiSection();
		int dd = deaths;
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, dd);
			}
		}.hh(UI.icons().s.death));
		
		s.addRelBody(8, DIR.S, new GScrollRows(rr.rowsCentered(VIEW.UI().div.normal.width()*8), new Card(GAME.ARMIES().division((short) 0)).body().height()*6).view());
		return s;
		
	}
	
	private static class Card extends HOVERABLE.HoverableAbs {
		

		int kills;
		
		private final Div div;
		
		Card(Div div){
			body.setDim(VIEW.UI().div.normal.width(), VIEW.UI().div.normal.height()+8);
			this.div = div;
			kills = GAME.ARMIES().factors.kills(div);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
			
			VIEW.UI().div.renderBasics(r, body.x1(), body.y1(), 1, div.info);
			
			double menTot = div.info.men();
			double menNow = div.menNrOf();
			
			GMeter.renderDelta(r, 1.0, (double)menNow/menTot, body.x1(), body.x2(), body.y2()-12, body.y2());
			
			GButt.ButtPanel.renderFrame(r, body);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			int menTot = div.info.men();
			int menNow = div.menNrOf();
			
			b.title(div.info.name());
			b.textLL(¤¤casulties);
			b.tab(6);
			b.add(GFORMAT.iofk(b.text(), menTot-menNow, menTot));
			b.NL();
			b.textLL(¤¤kills);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), kills));
			
			super.hoverInfoGet(text);
		}
		
	}
	
	
	protected abstract void close();
	
}
