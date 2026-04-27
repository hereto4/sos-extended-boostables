package view.ui.profile;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PTitles.PTitle;
import init.race.RACES;
import init.race.Race;
import init.settings.S;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GMeter;
import util.gui.misc.GMeter.GMeterCol;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;

final class Titles extends GuiSection{

	private static CharSequence ¤¤spent = "¤Chosen";
	private static CharSequence ¤¤NotAchiving = "¤Titles can not be unlocked.";
	private static CharSequence ¤¤spentD = "¤Whenever you start a new game, you get to choose 5 unlocked titles.";
	
	private static CharSequence ¤¤Locked = "¤Title is currently unavailable.";
	private static CharSequence ¤¤Active = "¤Title is currently unlocked and active.";
	private static CharSequence ¤¤Unlocked = "¤Title is currently unlocked. You can select it when starting a new game.";
	static {
		D.ts(Titles.class);
	}
	
	Titles(int HEIGHT){
		
		addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, FACTIONS.player().titles.selected(), 5);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(¤¤spentD);
			};
		}.hh(¤¤spent));
		
		if (!GAME.achieving()) {
			addRelBody(4, DIR.S, new GStat() {
				
				@Override
				public void update(GText text) {
					if (!GAME.achieving())
						text.errorify().add(¤¤NotAchiving);
				}
				
			});
		}
		
		StringInputSprite filter = new StringInputSprite(18, UI.FONT().M).placeHolder(Dic.¤¤Search);
		
		addRelBody(8, DIR.S, new GInput(filter));
		
		
		ArrayList<RENDEROBJ> rows = new ArrayList<>(FACTIONS.player().titles.all().size());
		
		
		for (PTitle t : FACTIONS.player().titles.all()) {
			rows.add(new Butt(t));
		}
		
		int hi = HEIGHT-body().height()-16;
		hi = rows.get(0).body().height()*(hi/rows.get(0).body().height());
		
		addRelBody(8, DIR.S, new GScrollRows(rows, hi) {
			
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				if (filter.text().length() > 0)
					return Str.containsText(((Butt)o).title.name,  filter.text());
				return true;
			};
			
		}.view());
		
		
		
		
		

		
	}
	
	private static final class Butt extends HOVERABLE.HoverableAbs {

		private final PTitle title;
		private final GuiSection sec = new GuiSection();
		
		
		private GText t = new GText(UI.FONT().H2, 24);
		Butt(PTitle title){
			body.setDim(800, title.icon.height()*2 + 16);
			this.title = title;
			int wi = 800-16-title.icon.width()*2-8;
			t.setMaxWidth(wi);
			t.set(title.name);
			
			sec.add(t, 0, 0);
			sec.addDown(4, new RENDEROBJ.RenderImp(wi, 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GMeterCol cc = GMeter.C_ORANGE;
					if (title.selected())
						cc = GMeter.C_BLUE;
					else if (title.unlocked())
						cc = GMeter.C_GREEN;
					double d = 0;
					if (title.unlocked() || title.race(FACTIONS.player().race())) {
						d = 1;
					}else {
						d = title.lockable.progress(FACTIONS.player());
					}
					GMeter.render(r, cc, d,  body());
				}
			});
			
			sec.addRelBody(8, DIR.W, new RENDEROBJ.RenderImp(title.icon.width()*2, title.icon.height()*2) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (!title.selected() && !(title.unlocked() && title.race(FACTIONS.player().race())))
						GCOLOR.T().INACTIVE.bind();
					title.icon.render(r, body);
					COLOR.unbind();
					
					int x1 = Butt.this.body.x2()-40;
					int y1 = Butt.this.body.y1()+16;
					
					for (Race ra : RACES.playable()) {
						if (title.race(ra)) {
							ra.appearance().icon.render(r, x1, y1);
							x1-= 38;
						}
					}
					
				}
			});
			
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			GButt.ButtPanel.renderBG(r, title.unlocked(), title.isNew(), title.selected(), body);
			GButt.ButtPanel.renderFrame(r, body);
			
			sec.body().centerIn(body);
			sec.render(r, ds);
			
			
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			title.consumeNew();
			GBox b = (GBox) text;
			b.title(title.name);
			b.text(title.desc);
			b.NL(6);
			
			if (title.selected()) {
				b.text(¤¤Active);
			}else if(title.unlocked()) {
				b.text(¤¤Unlocked);
			}else {
				b.text(¤¤Locked);
			}
			b.NL(6);
			title.lockable.hover(text, FACTIONS.player());
			
			b.sep();
			
			title.lockers.hover(text);
			b.NL(8);
			
			title.boosters.hover(text, Math.max(0.5, title.boosterValue()), -1);
			
			if (S.get().developer && MButt.WHEEL.consumeClick()) {
				FACTIONS.player().titles.unlock(title);
			}
			
		}
		
	}
	
}
