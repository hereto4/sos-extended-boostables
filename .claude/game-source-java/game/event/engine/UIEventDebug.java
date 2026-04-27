package game.event.engine;

import java.util.LinkedList;

import game.GAME;
import game.event.actions.EventAction;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.royalty.Royalty;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAIN;
import init.type.TERRAINS;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.panel.GPanel;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import world.map.regions.Region;

final class UIEventDebug extends GuiSection{

	public UIEventDebug(EVENT_HANDLER en) {
		
		addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				if (en.current() == null)
					text.add('-');
				else
					text.add(en.current().key);
			}
		}.hh("current"));
		
		addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				if (en.current() == null)
					return;
				text.add(en.timeElapsed());
				text.add('/');
				text.add(en.current().duration.seconds);
			}
		}.hh("time"));
		
		addDown(2, new GButt.ButtPanel("Expire") {
			@Override
			protected void clickA() {
				en.expire();
			}
			
			@Override
			protected void renAction() {
				activeSet(en.current() != null);
			}
		});
		
		addDown(2, new GButt.ButtPanel("#") {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {

				for (String s : en.tags.keys()) {
					if (en.tags.get(s) == Boolean.TRUE)
						text.text(s);
				}
			};
			
		});
		
		GInput in = new GInput(new StringInputSprite(16, UI.FONT().S));
		
		addDown(2, in);
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		

	
		
		for (Event a : Event.all) {
			
			
			
			SPRITE sp = new SPRITE.Imp(700, 24) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					int CY = Y1+(Y2-Y1)/2;
					(en.can(a) ? COLOR.GREEN100 : COLOR.RED100).bind();
					UI.icons().s.dot.renderCY(r,X1+6, CY);
					COLOR.unbind();
					UI.FONT().S.renderCY(r, X1+32, CY, a.key);
					GCOLOR.T().H1.bind();
					UI.FONT().S.renderCY(r, X1+382, CY, a.info.name);
					Str.TMP.clear().add(en.acc(a),2);
					COLOR.unbind();
					UI.FONT().S.renderCY(r, X2-64, CY, Str.TMP);
				}
			};
			
			GButt b = new GButt.ButtPanel(sp) {
				
				
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					
					UIEventDebug.this.hover(b, a);
					
				}
				
				@Override
				protected void clickA() {
					en.set(a, false, false, true, true);
					GuiSection pop = new GuiSection();
					EContext c = en.context();
					GText t = new GText(UI.FONT().S, "indu: ");
					t.add(c.indu.am).s();
					int am = 0;
					for (int i = 0; i < en.context().indu.max(); i++) {
						Induvidual tt = c.indu.get(i);
						if (tt != null &&  c.indu.eventGet(tt)) {
							if (am++<4) {
								t.add(STATS.APPEARANCE().name(tt)).s();
							}
							
						}
					}
					t.s().add(am);
					t.adjustWidth();
					pop.addDownC(8, t);
					
					t = new GText(UI.FONT().S, "regs: ");
					t.add(c.regs.am).s();
					am = 0;
					for (int i = 0; i < c.regs.max(); i++) {
						Region tt = c.regs.get(i);
						if (tt != null &&  c.regs.eventGet(tt)) {
							if (am++<4) {
								t.add(tt.info.name()).s();
							}
							
						}
					}
					t.s().add(am);
					t.adjustWidth();
					pop.addDownC(8, t);
					
					t = new GText(UI.FONT().S, "roys: ");
					t.add(c.royalty.am).s();
					am = 0;
					for (int i = 0; i < c.royalty.max(); i++) {
						Royalty tt = c.royalty.get(i);
						if (tt != null &&  c.royalty.eventGet(tt)) {
							if (am++<4) {
								t.add(tt.name() + " (" + tt.court.faction.name + ") ");
							}
						}
					}
					t.s().add(am);
					t.adjustWidth();
					pop.addDownC(8, t);
					
					t = new GText(UI.FONT().S, "fact: ");
					t.add(c.faction.am).s();
					am = 0;
					for (int i = 0; i < c.faction.max(); i++) {
						Faction tt = c.faction.get(i);
						if (tt != null &&  c.faction.eventGet(tt)) {
							if (am++<4) {
								t.add(tt.name).s();
							}
						}
					}
					t.s().add(am);
					t.adjustWidth();
					pop.addDownC(8, t);
					
					VIEW.inters().popup2.show(pop, this);
				}
				
			};
			rows.add(b);
			
		}
		
		addDown(2, new GScrollRows(rows, 400) {
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				
				if (in.text().length() == 0)
					return true;
				
				Event a = Event.all.get(i);
				return Str.containsText(a.key, in.text()) || Str.containsText(a.info.name, in.text());
			}
		}.view());
		
		add(new GPanel(body()));
		moveLastToBack();
		body().centerIn(C.DIM());

		IDebugPanel.add("event engine", new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().section.activate(UIEventDebug.this);
			}
		});
		
	}
	
	private void hover(GBox b, Event a) {
		b.title(a.info.name);
		if (a.info.icon != null)
			b.add(a.info.icon);
		b.NL();
		for (CharSequence s : a.info.messages) {
			b.text(s);
			b.NL();
		}
		b.add(b.text().warnify().add(a.info.desc));
		b.NL();
		b.add(b.text().errorify().add(a.info.subject));
		b.NL();
		{
			b.NL();
			b.textLL(Dic.¤¤Occurrence);
			b.NL();
			b.add(b.text().add(GAME.EVENT().can(a)));
			b.NL();
			int tt = 0;
			
			for (TERRAIN t : TERRAINS.ALL()) {
				if (tt > 6) {
					tt = 0;
					b.NL();
				}
				b.add(t.icon());
				b.add(GFORMAT.f0(b.text(), a.occurence.toccurence[t.index()]));
			}
			b.NL();
			for (Race rr : RACES.all()) {
				if (tt > 6) {
					tt = 0;
					b.NL();
				}
				b.add(rr.appearance().icon);
				b.add(GFORMAT.f0(b.text(), a.occurence.roccurence[rr.index()]));
			}
			b.NL();
			CLIMATE climate = SETT.ENV().climate();
			b.textLL(CLIMATES.INFO().name);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), a.occurence.coccurence[climate.index()]));
			b.NL();
			b.textSLL(Dic.¤¤Total);
			b.tab(6);
			b.add(GFORMAT.f0(b.text(), a.occurence.occurence()));
			b.NL();
			b.text(Dic.¤¤Current);
			b.add(GFORMAT.iofk(b.text(), GAME.EVENT().occ(a), a.occurence.maxSpawns));
			b.NL();
			
			
			a.occurence.plockable.hover(b, FACTIONS.player());
			b.NL();
			b.add(b.text().add(a.occurence.plockable.passes(FACTIONS.player())));

			b.sep();
			
			b.text("tags");
			b.add(b.text().add(a.tags.can(GAME.EVENT().tags)));
			
			b.add(UI.icons().s.plus);
			for (String k : a.tags.adds) {
				b.text(k);
			}
			b.NL();
			b.add(UI.icons().s.minus);
			for (String k : a.tags.removes) {
				b.text(k);
			}
			b.NL();
			
			for (String k : a.tags.allows) {
				if (!GAME.EVENT().tags.containsKey(k) || GAME.EVENT().tags.get(k) == Boolean.FALSE)
					b.add(b.text().errorify().add(k));
				else
					b.add(b.text().normalify2().add(k));
			}
			b.NL();
			for (String k : a.tags.allows_not) {
				if (GAME.EVENT().tags.containsKey(k) && GAME.EVENT().tags.get(k) == Boolean.TRUE)
					b.add(b.text().errorify().add(k));
				else
					b.add(b.text().normalify2().add(k));
			}
			b.NL();
			b.sep();
		}
		
		{
			b.add(UI.icons().s.cancel);
			for (EventAction c : a.duration.on_expire)
				b.text(c.key);
			b.NL();
			
			
			for (EChoice ch : a.choices) {
				b.add(UI.icons().s.question);
				for (EventAction c : ch.actions)
					b.text(c.key);
				b.NL();
			}
			

		}
		
		for (EventAction c : a.actions()) {
			b.text(c.key);
			b.NL();
		}
		
	}
	
}
