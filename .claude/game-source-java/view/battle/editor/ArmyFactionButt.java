package view.battle.editor;

import game.GAME;
import game.battle.util.DIV_SPEC;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER.GETTERE;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import view.main.VIEW;
import view.ui.profile.UIFactionBanner;

class ArmyFactionButt extends GuiSection{

	private final ArmySide divs;
	private final GETTERE<ArmySide> g;
	
	private static CharSequence ¤¤player = "player";
	private static CharSequence ¤¤enemy = "enemy";
	
	static {
		D.ts(ArmyFactionButt.class);
	}
	
	
	public ArmyFactionButt(Faction f, ArmySide divs, GETTERE<ArmySide> g) {
		
		if (f == FACTIONS.player()) {
			add(new GText(UI.FONT().H2, ¤¤player).normalify2(), 0, 0);
		}else {
			add(new GText(UI.FONT().H2, ¤¤enemy).errorify(), 0, 0);
		}
		
		CLICKABLE title = new GInput(new  StringInputSprite(24, UI.FONT().S) {
			@Override
			public void renAction() {
				text().clear().add(f.name);
			}
			
			@Override
			protected void change() {
				f.name.clear().add(text());
			};
		});
		
		addDown(4, title);
		
		{
			GuiSection s = new GuiSection();
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					int m = 0;
					for (DIV_SPEC s : divs.divs) {
						if (s != null)
							m += s.men();
					}
					GFORMAT.i(text, m);
				}
			}.hh(UI.icons().s.human));
			
			s.addRightC(68, new GStat() {
				
				@Override
				public void update(GText text) {
					
					int m = 0;
					for (DIV_SPEC s : divs.divs) {
						if (s != null)
							m += GAME.battle().power.get(s);
					}
					GFORMAT.i(text, m);
				}
			}.hh(UI.icons().s.fist));
			
			addRelBody(4, DIR.S, s);
		}
		
		CLICKABLE banner = new GButt.ButtPanel(f.banner().HUGE) {
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(new UIFactionBanner(f), this);
			}
			
		};
		
		
		addRelBody(8, DIR.W, banner);
		
		body().pad(16, 8);
		this.divs = divs;
		this.g = g;
		
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		GButt.ButtPanel.renderBG(r, true,  g.get() == divs, hoveredIs(), body());
		super.render(r, ds);
		GCOLOR.UI().border().renderFrame(r, body(), 0, 1);
	}
	
	@Override
	protected void clickA() {
		g.set(divs);
		super.clickA();
	}
	
	
}
