package view.sett.ui.standing;

import game.boosting.BoostSpec;
import game.boosting.BoostableCat;
import game.boosting.BoosterAbs;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.religion.Religion;
import init.religion.RELIGIONS;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.NEEDS;
import init.type.POP_CL;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBurial;
import settlement.stats.colls.StatsBurial.StatGrave;
import settlement.stats.colls.StatsReligion.ReligionTot;
import settlement.stats.colls.StatsReligion.StatReligion;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.sett.ui.standing.Cats.Cat;

final class CatReligion extends Cat{

	private static CharSequence ¤¤Burrial = "¤Burial";
	private static CharSequence ¤¤AllowRace = "¤Allow/deny access for Species";
	private static CharSequence ¤¤Allow = "¤Allow/deny access for whole class";
	private static CharSequence ¤¤TempleBoost = "¤Temple Boosts";
	private static CharSequence ¤¤Conversion = "¤Conversion";
	static {
		D.ts(CatReligion.class);
	}
	
	CatReligion(HCLASS cl) {
		super(new StatCollection[] { STATS.RELIGION(), STATS.BURIAL()});
		titleSet(cs[0].info.name);
		
		section.add(dvision(cl));
		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		

		
		for (StatReligion r : STATS.RELIGION().ALL){
			rens.add(temple(r, cl));
		}
		
		
		

		rens.add(access(STATS.RELIGION().SHRINE, cl));
		rens.add(access(STATS.RELIGION().TEMPLE, cl));
		
		{
			rens.add(new GHeader(¤¤TempleBoost));
			GRows rows = new GRows(6);
			GText t = new GText(UI.FONT().S, 8);
			for (BoostSpec ss : STATS.RELIGION().TEMPLE.TOTAL.boosters.all()) {
				rows.add(new HOVERABLE.HoverableAbs(85, 32) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
						ss.boostable.icon.renderCY(r, body().x1()+8, body().cY());
						t.clear();
						GFORMAT.f0(t, ss.inc(POP_CL.clP(CitizenMain.current, cl)));
						t.renderCY(r, body().x1()+28, body().cY());
						GButt.ButtPanel.renderFrame(r, body);
					}
					@Override
					public void hoverInfoGet(GUI_BOX box) {
						GBox bb = (GBox) box;
						
						
						bb.title(ss.tName);
						double d = 0;
						for (Religion rr : RELIGIONS.ALL()) {
							for (BoostSpec sb : rr.boosts.all()) {
								if (sb.boostable == ss.boostable) {
									sb.booster.hover(box, sb.booster.get(POP_CL.clP(CitizenMain.current, cl)));
									BoosterAbs.hoverSpan(bb, sb.booster.from(), sb.booster.to());
									bb.NL();
								}
							};
						}
						bb.NL(8);
						
						bb.textLL(Dic.¤¤Boosts);
						bb.tab(7);
						bb.add(GFORMAT.f0(bb.text(), d));
						
					};
				});
				
				
			}
			rens.add(rows.rows());
		}
		
		{
			STAT s = STATS.RELIGION().OPPOSITION;
			rens.add(new StatRow(s, cl));
		}

		{
			StatsBurial s = STATS.BURIAL();
			new StatRowGrave(cl, rens);
			for (STAT ss : s.others())
				rens.add(new StatRow(ss, cl));
			
			
		}
		section.add(new GScrollRows(rens, HEIGHT-section.body().height()-8, 0).view(), 0, section.body().y2()+4);

	}
	
	private static RENDEROBJ access(ReligionTot tot, HCLASS cl) {
		return new StatRow(tot.TOTAL, cl);
	}
	
	private static RENDEROBJ dvision(HCLASS cl) {
		GStaples s = new GStaples(STATS.DAYS_SAVED) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				box.title(STATS.RELIGION().ALL.get(0).followers.info().name);
				int i = STATS.DAYS_SAVED - stapleI - 1;
				box.add(box.text().add(-i).s().add(TIME.days().cycleName()));
				box.NL(8);
				
				for (StatReligion s : STATS.RELIGION().ALL) {
					box.add(s.religion.icon);
					box.textLL(s.religion.info.name);
					box.tab(6);
					box.add(GFORMAT.i(box.text(), s.followers.data(cl).get(CitizenMain.current, i)));
					box.tab(8);
					if (i < STATS.DAYS_SAVED-1)
						box.add(GFORMAT.iIncr(box.text(), s.followers.data(cl).get(CitizenMain.current, i)-s.followers.data(cl).get(CitizenMain.current, i+1)));
					box.NL();
				}
				
				box.add(NEEDS.TYPES().SHRINE.rate.icon);
				box.textLL(STATS.RELIGION(). SHRINE.TOTAL.info().name);
				box.tab(6);
				box.add(GFORMAT.perc(box.text(), STATS.RELIGION().SHRINE.TOTAL.data(cl).getD(CitizenMain.current), i));
				if (i < STATS.DAYS_SAVED-1)
					box.add(GFORMAT.percInc(box.text(), STATS.RELIGION().SHRINE.TOTAL.data(cl).getD(CitizenMain.current, i)-STATS.RELIGION().SHRINE.TOTAL.data(cl).getD(CitizenMain.current, i+1)));
				box.NL();
				
				box.add(NEEDS.TYPES().TEMPLE.rate.icon);
				box.textLL(STATS.RELIGION().TEMPLE.TOTAL.info().name);
				box.tab(6);
				box.add(GFORMAT.perc(box.text(), STATS.RELIGION().TEMPLE.TOTAL.data(cl).getD(CitizenMain.current), i));
				if (i < STATS.DAYS_SAVED-1)
					box.add(GFORMAT.percInc(box.text(), STATS.RELIGION().TEMPLE.TOTAL.data(cl).getD(CitizenMain.current, i)-STATS.RELIGION().TEMPLE.TOTAL.data(cl).getD(CitizenMain.current, i+1)));
				box.NL();
			}
			
			@Override
			protected double getValue(int stapleI) {
				int i = STATS.DAYS_SAVED - stapleI - 1;
				return STATS.POP().POP.data(cl).get(CitizenMain.current, i);
			}
			
			@Override
			protected void renderExtra(SPRITE_RENDERER r, COLOR color, int stapleI, boolean hovered, double value,
					int x1, int x2, int y1, int y2) {
				
				
				int i = STATS.DAYS_SAVED-stapleI-1;
				
				int h = y2-y1;
				if (h <= 0)
					h = 1;
				
				for (StatReligion s : STATS.RELIGION().ALL) {
					int hh = (int) Math.ceil(h*s.followers.data(cl).getD(CitizenMain.current, i));
					if (hh > 0) {
						ColorImp c = ColorImp.TMP;
						c.set(s.religion.color);
						c.shadeSelf(hovered ? 0.75 : 0.55);
						c.render(r, x1, x2, y2-hh, y2);
						c.set(s.religion.color);
						c.shadeSelf(hovered ? 1 : 0.80);
						c.render(r, x1+1, x2-1, y2-hh+1, y2-1);
						y2-= hh;
					}
				}
			}
		};
		
		s.body().setWidth(StatRow.Width);
		s.body().setHeight(80);
		return s;
	}
	
	private static RENDEROBJ temple(StatReligion ss, HCLASS cl) {
		GuiSection s = new GuiSection() {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (!isHoveringAHoverElement()) {
					
					GBox b = (GBox) text;
					b.title(ss.info.name);
					b.text(ss.info.desc);
					b.NL(8);
					
					b.textL(ss.followers.info().name);
					b.tab(8);
					b.add(GFORMAT.i(b.text(), ss.followers.data(cl).get(CitizenMain.current)));
					b.NL();
					
					b.textLL(STATS.RELIGION().TEMPLE.name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), STATS.RELIGION().TEMPLE.access(ss.religion).data(cl).getD(CitizenMain.current)));
					b.add(GFORMAT.perc(b.text(), STATS.RELIGION().TEMPLE.quality(ss.religion).data(cl).getD(CitizenMain.current)));
					b.NL();
					
					b.textLL(STATS.RELIGION().SHRINE.name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), STATS.RELIGION().SHRINE.access(ss.religion).data(cl).getD(CitizenMain.current)));
					b.add(GFORMAT.perc(b.text(), STATS.RELIGION().SHRINE.quality(ss.religion).data(cl).getD(CitizenMain.current)));
					b.NL();
					
					b.textLL(¤¤Conversion);
					b.tab(6);
					b.add(GFORMAT.f0(b.text(), ss.religion.conversionCity.get(cl.get(CitizenMain.current))));
					
					b.NL(8);
				
					
					ss.religion.boosts.hover(text, POP_CL.clP(CitizenMain.current, cl), BoostableCat.TYPE_SETT);
					
				
				}
				super.hoverInfoGet(text);
			}
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				GCOLOR.UI().border().render(r, body());
				GCOLOR.UI().bg().render(r, body(), -1);
				super.render(r, ds);
			}
		};
		
		s.addRightC(4, new RENDEROBJ.RenderImp(16, 16) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				ColorImp.TMP.set(ss.religion.color);
				ColorImp.TMP.shadeSelf(0.75);
				ColorImp.TMP.render(r, body);
				ColorImp.TMP.set(ss.religion.color);
				ColorImp.TMP.render(r, body, -2);
			}
		});
		
		s.addRightC(16, new GButt.Checkbox() {
			@Override
			protected void clickA() {
				ss.permission.toggle(cl, CitizenMain.current);
			}
			
			@Override
			protected void renAction() {
				selectedSet(is());
			}
			
			private boolean is() {
				return ss.permission.get(cl, CitizenMain.current);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (CitizenMain.current != null)
					text.text(¤¤AllowRace);
				else
					text.text(¤¤Allow);
			}
		});
		
		s.addRightC(16, ss.religion.icon);
		
		s.addRightC(16, UI.icons().s.human);
		s.addRightC(4, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, ss.followers.data(cl).get(CitizenMain.current));
			}
		});
		
		s.addRightC(64, NEEDS.TYPES().SHRINE.rate.icon);
		s.addRightC(4, new RENDEROBJ.RenderImp(100, 16) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				double v = STATS.RELIGION().SHRINE.access(ss.religion).data(cl).getD(CitizenMain.current)*STATS.RELIGION().SHRINE.quality(ss.religion).data(cl).getD(CitizenMain.current);
				GMeter.render(r, GMeter.C_BLUE, v, body());
			}
		});
		
		s.addRightC(16, NEEDS.TYPES().TEMPLE.rate.icon);
		s.addRightC(4, new RENDEROBJ.RenderImp(100, 16) {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				double v = STATS.RELIGION().TEMPLE.access(ss.religion).data(cl).getD(CitizenMain.current)*STATS.RELIGION().TEMPLE.quality(ss.religion).data(cl).getD(CitizenMain.current);
				GMeter.render(r, GMeter.C_BLUE, v, body());
			}
		});
		
		s.pad(10, 6);
		s.body().setWidth(520);
		return s;
	}
	
	final static class StatRowGrave {

		private final HCLASS cl;
		
		StatRowGrave(HCLASS cl, LinkedList<RENDEROBJ> rens){
			
			this.cl = cl;
			
			boolean has = false;
			for (StatGrave ss : STATS.BURIAL().graves()) {
				for (Race r : RACES.all()) {
					if (ss.standing().definition(r).get(cl).max > 0) {
						has = true;
						break;
					}
				}
			}
			if (!has)
				return;
			
			GuiSection s = new GuiSection();
			
			s.add(new GText(UI.FONT().H2, ¤¤Burrial).lablify(), 0, 0);
			s.addRightCAbs(StatRow.StatX, new GStat() {
				
				@Override
				public void update(GText text) {
					double d = 0;
					for (StatGrave ss : STATS.BURIAL().graves()) {
						d = Math.max(d, ss.data(cl).getD(CitizenMain.current));
					}
					GFORMAT.perc(text,d);
				}
			});
			
			s.addCentredY(new RENDEROBJ.RenderImp(StatRow.MeterW, 20) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double max = 0;
					double now = 0;
					double nor = 0;
					double prev = 0;
					for (StatGrave s : STATS.BURIAL().graves()) {
						max = Math.max(max, s.standing().max(cl, CitizenMain.current));
						now = Math.max(now, s.standing().get(cl, CitizenMain.current));
						prev = Math.max(prev, s.standing().getPrev(cl, CitizenMain.current, 8));
						nor = Math.max(nor, s.standing().normalized(cl, CitizenMain.current));
					}
					
					GMeter.renderDelta(r, prev/max, now/max, body.x1(), (int) (body().x1() + body().width()*nor), body().y1(), body().y2());
				}
			}, StatRow.MeterX);
			s.pad(4, 0);
			rens.add(s);
			
			for (StatGrave ss : STATS.BURIAL().graves()) {
				for (Race r : RACES.all()) {
					if (ss.standing().definition(r).get(cl).max > 0) {
						
						rens.add(service(ss));
						break;
					}
				}
				
			}
	
		}

		
		private RENDEROBJ service(StatGrave ss) {
			GuiSection s = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (!isHoveringAHoverElement()) {
						ss.hover(text, cl, CitizenMain.current);
					}
					super.hoverInfoGet(text);
				}
			};
			s.add(new StatRow.Arrow(ss, cl));
			s.addRightC(4, new GButt.Checkbox() {
				@Override
				protected void clickA() {
					ss.grave().permission().toggle(cl, CitizenMain.current);
				}
				
				@Override
				protected void renAction() {
					selectedSet(is());
				}
				
				private boolean is() {
					return ss.grave().permission().get(cl, CitizenMain.current);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (CitizenMain.current != null)
						text.text(¤¤AllowRace);
					else
						text.text(¤¤Allow);
				}
			});
			s.addRightC(4, ss.grave().blueprint().iconBig());
			s.addRightC(4, new GText(UI.FONT().S, ss.grave().blueprint().info.names).lablifySub());
			s.addCentredY(new GStat() {
				
				@Override
				public void update(GText text) {
					text.setFont(UI.FONT().S);
					
					StatRow.format(text, ss, ss.data(cl).getD(CitizenMain.current), cl);
				}
			}, StatRow.StatX);
			
			s.addCentredY(new RENDEROBJ.RenderImp(StatRow.MeterW, 12) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double max = ss.standing().max(cl, CitizenMain.current);
					double now = ss.standing().get(cl, CitizenMain.current);
					double nor = ss.standing().normalized(cl, CitizenMain.current);
					GMeter.render(r, GMeter.C_BLUE, now/max, body.x1(), (int) (body().x1() + body().width()*nor), body().y1(), body().y2());
				}
			}, StatRow.MeterX);
			

			return s;
		}
		
	}

}
