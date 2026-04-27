package view.ui.profile;

import game.GAME;
import game.boosting.BoostSpec;
import game.faction.FACTIONS;
import game.faction.player.PLevels;
import game.faction.player.PTitles.PTitle;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.value.Lock;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.AD;
import world.region.RD;


final class Level extends GuiSection{

	private final CharSequence ¤¤Title = "¤{0} {1} of {2}";
	
	public Level(int height) {
		D.t(this);
		
		GTextR title = new GTextR(new GText(init.sprite.UI.UI.FONT().H1, 64).lablify()) {
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				text().clear();
				text().add(¤¤Title);
				text().insert(0, FACTIONS.player().level().current().name());
				text().insert(1, FACTIONS.player().rulerName());
				text().insert(2, FACTIONS.player().name);
				text().adjustWidth();
				super.render(r, ds, isHovered);
			};
		}.setAlign(DIR.N);
		add(title);
		
		RENDEROBJ desc = new RENDEROBJ.RenderImp(720, 64) {
			
			private final GText text = new GText(init.sprite.UI.UI.FONT().M, 200);
			private final GText tmp = new GText(init.sprite.UI.UI.FONT().M, 200);
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				text.clear();
				
				int y1 = body().y1()+ text.height()/2;
				
				for (PTitle t : FACTIONS.player().titles.all()) {
					if (t.selected()) {
						tmp.set(text);
						if (text.width() > 0 && tmp.width() + text.width() > body().width()) {
							text.renderC(r, body().cX(), y1);
							text.clear();
							text.add(t.name);
							text.adjustWidth();
							y1 += text.height();
						}else {
							if (text.width() > 0)
								text.add(',').s();
							text.add(t.name);
							text.adjustWidth();
						}
					}
					
					
					
						
				}
				
				if (text.width() > 0) {
					text.renderC(r, body().cX(), y1+text.height()/2);
					
				}
				
			}
		};
		
		addDownC(8, desc);
		
		GuiSection stats = new GuiSection();
		
		stats.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().POP.data(null).get(null) + AD.cityDivs().total());
			}
		}.hv(Dic.¤¤Population));
		
		stats.addRightCAbs(120, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null) + AD.cityDivs().total());
			}
		}.hv(HCLASSES.CITIZEN().names));
		
		stats.addRightCAbs(120, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, STATS.POP().POP.data(null).get(null) + AD.cityDivs().total() + RD.RACES().population.faction().get(FACTIONS.player()) -RD.RACES().population.get(FACTIONS.player().realm().capitol()));
			}
		}.hv(Dic.¤¤Subjects));
		
		addRelBody(0, DIR.S, stats);
		
		
		RENDEROBJ[] rens = new RENDEROBJ[GAME.player().level().all().size()];
		for (int i = 0; i < rens.length; i++) {
			rens[i] = new TRow(GAME.player().level().all().get(i));
		}
		
		int h = height-getLastY2()-16;
		int am = h /rens[0].body().height();
		h = am *rens[0].body().height();
		
		RENDEROBJ r = new GScrollRows(rens, h, 0).view();
		
		addDownC(8, r);
	}
	
	private class TRow extends GuiSection {
		
		PLevels.Level l;
		
		TRow(PLevels.Level l){
			this.l = l;

			int w = 500;

			add(new GHeader(l.name()));
			
			body().setWidth(w);
			
			GuiSection s = new GuiSection();
			
			for (BoostSpec b : l.boosters.all()) {
				s.addRightC(2, b.boostable.icon);
				if (s.body().width() + s.getLast().width() >= w-body().width())
					break;
			}
			s.body().moveX2(w);
			s.body().moveCY(body().cY());
			absorb(s);
			
			
			s = new GuiSection();
			s.body().setHeight(32);
			for (Lock<?> b : l.lockers.all()) {
				s.addRightC(8, b.lockable.icon);
				if (s.body().width() + s.getLast().width() >= w)
					break;
			}
			
			boolean f = true;
			for (BoostSpec b : l.boosters.all()) {
				s.addRightC(f ? 8: 64, new GStat() {
					
					@Override
					public void update(GText text) {
						b.booster.format(text, b.booster.to());
					}
				}.hh(b.boostable.icon.big));
				f = false;
				if (s.body().width() + s.getLast().width() >= w)
					break;
			}
			
			s.body().moveX1(body().x1());
			s.body().moveY1(body().y2()+8);
			absorb(s);

			SPRITE nn = new GText(UI.FONT().H2, GFORMAT.toNumeral(l.index()+1));
			add(nn, -16*4, 0);
			
			pad(8);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			GCOLOR.UI().border().render(r, body());
			GCOLOR.UI().bg().render(r, body(), -1);
			super.render(r, ds);
			if (l.index() > GAME.player().level().current().index()) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body(), -1);
				OPACITY.unbind();
			}
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			l.hoverInfoGet(text);
		}
		
	}
	
}
