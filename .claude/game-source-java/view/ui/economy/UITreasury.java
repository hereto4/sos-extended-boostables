package view.ui.economy;

import game.faction.FACTIONS;
import game.faction.FWorth.WINT;
import game.faction.npc.FactionNPC;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.IntImp;
import util.gui.common.UIPickerRace;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.goods.UIGoodsExport;
import view.ui.goods.UIGoodsImport;
import view.ui.manage.IFullView;

public final class UITreasury extends IFullView {

	private static CharSequence ¤¤unused = "Show resources not actively traded";
	private static CharSequence ¤¤import = "Show resources that are imported.";
	private static CharSequence ¤¤export = "Show resources that are exported.";
	private static CharSequence ¤¤economy = "Economy & Trade";
	private static CharSequence ¤¤priceDesc = "The average global price, the total production rate without boosts, and the price multiplied with the production rate. This might give you a sense of what industries are profitable for you. The last two columns shows your current average bonus of selected race.";
	
	private GScrollRows ta;
	
	static {
		D.ts(UITreasury.class);
	}
	
	public UITreasury() {
		super(¤¤economy, UI.icons().l.coin);
		
		section.body().setWidth(WIDTH).setHeight(1);
		final IntImp ii = new IntImp();
		GETTER_IMP<RESOURCE> gres = new GETTER_IMP<>();
		
		
		GuiSection s = new GuiSection() {
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				ii.set(-1);
				gres.set(null);
				return super.hover(mCoo);
			}
			
		};
		s.addDownC(0, new MainChart(HEIGHT, ii, 10));
		
		s.addRight(32, new MainDetails(ii));
		
		GuiSection f = new GuiSection();
		
		GButt.ButtPanel oo = new GButt.ButtPanel(UI.icons().m.coins.resized(Icon.L)) {
			GuiSection s = new Prices();
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(s, this);
			};
		}.pad(2, 4);
		f.addDown(0, oo);
		GButt.ButtPanel unused = new GButt.ButtPanel(UI.icons().m.questionmark.resized(Icon.L)) {
			@Override
			protected void clickA() {
				selectedToggle();
			};
		}.pad(2, 4);
		unused.hoverInfoSet(¤¤unused);
		f.addDown(0, unused);
		GButt.ButtPanel impot = new GButt.ButtPanel(SETT.ROOMS().IMPORT.icon) {
			@Override
			protected void clickA() {
				selectedToggle();
			};
		}.pad(2, 4);
		impot.hoverInfoSet(¤¤import);
		impot.selectedSet(true);
		f.addDown(0, impot);
		GButt.ButtPanel export = new GButt.ButtPanel(SETT.ROOMS().EXPORT.icon) {
			@Override
			protected void clickA() {
				selectedToggle();
			};
		}.pad(2, 4);
		export.hoverInfoSet(¤¤export);
		export.selectedSet(true);
		f.addDown(0, export);
		
		s.add(f, s.body().x2() + 32, s.body().y2()-f.body().height());
		
		s.addRelBody(4, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, (int)FACTIONS.WORTH().faction());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				for (WINT d : FACTIONS.WORTH().faction) {
					b.add(d.icon);
					b.textL(d.info.name);
					b.tab(6);
					b.add(GFORMAT.iIncr(b.text(), d.player()));
					b.NL();
					b.text(d.info.desc);
					b.NL(5);
				}
			};
			
		}.hh(Dic.¤¤NetWorth));
		
		UIGoodsImport im = new UIGoodsImport();
		UIGoodsExport ex = new UIGoodsExport(true);
		ArrayList<RENDEROBJ> rows = new ArrayList<RENDEROBJ>(RESOURCES.ALL().size());
		for (RESOURCE res : RESOURCES.ALL())
			rows.add(new RRow(res, ii, gres, 8, im, ex));
		
		int height = HEIGHT-s.body().height()-16;
		height = height/rows.get(0).body().height();
		height *= rows.get(0).body().height();
		ta = new GScrollRows(rows, height) {
			
			@Override
			protected boolean passesFilter(int i, RENDEROBJ o) {
				RESOURCE res = RESOURCES.ALL().get(i);
				if (impot.selectedIs() && SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0)
					return true;
				if (export.selectedIs() && SETT.ROOMS().EXPORT.tally.capacity.get(res) > 0)
					return true;
				if (unused.selectedIs() && SETT.ROOMS().IMPORT.tally.capacity.get(res) == 0 && SETT.ROOMS().EXPORT.tally.capacity.get(res) == 0)
					return true;
				return false;
			};
		};
		s.add(ta.view(), s.body().x1()-58, s.body().y2()+8);
		
		s.add(new Factions(HEIGHT), s.body().x2()+16, s.body().y1());
		section.addRelBody(16, DIR.S, s);


		
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX box) {
		GBox b = (GBox) box;
		b.title(¤¤economy);
		
		b.textLL(Dic.¤¤Treasury);
		b.tab(6);
		b.add(GFORMAT.i(b.text(), (long) FACTIONS.player().credits().getD()));
		b.NL();
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (SETT.ROOMS().IMPORT.tally.capacity.get(res) > 0) {
				GText t = b.text();
				CharSequence p = SETT.ROOMS().IMPORT.tally.problem(res, false);
				if (p == null) {
					p = SETT.ROOMS().EXPORT.tally.problem(res, false);
				}
				
				if (p != null) {
					b.add(res.icon());
					b.add(t.errorify().add(p));
					b.NL();
				}else {
					p = SETT.ROOMS().IMPORT.tally.warning(res, false);
					if (p == null) {
						p = SETT.ROOMS().IMPORT.tally.problem(res, false);
					}
					if (p != null) {
						b.add(res.icon());
						b.add(t.warnify().add(p));
						b.NL();
					}
				}
			}
			
			
		}
	}

	private static class Prices extends GuiSection{
		
		Prices(){
			
			UIPickerRace pick = new UIPickerRace();
			pick.set(FACTIONS.player().race().index);
			
			GTableBuilder bu = new GTableBuilder() {
				
				@Override
				public int nrOFEntries() {
					return RESOURCES.ALL().size();
				}
			};
			
			bu.column("", Icon.M, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new RENDEROBJ.RenderImp(Icon.M) {
						
						@Override
						public void render(SPRITE_RENDERER r, float ds) {
							RESOURCES.ALL().get(ier.get()).icon().render(r, body);
						}
					};
				}
			});
			
			bu.column(Dic.¤¤Price, 120, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, FACTIONS.PRICE().get(RESOURCES.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});
			
			bu.column(Dic.¤¤Rate, 120, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f(text, 1.0/SETT.ROOMS().industries.vanillaRate(RESOURCES.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});
			
			if (S.get().developer) {
				bu.column("dRate", 120, new GRowBuilder() {
					
					@Override
					public RENDEROBJ build(GETTER<Integer> ier) {
						return new GStat() {
							
							@Override
							public void update(GText text) {
								RESOURCE res = RESOURCES.ALL().get(ier.get());
								double rr = 0;
								double p = 0;
								for (FactionNPC f : FACTIONS.NPCs()) {
									p += f.citizens(null);
									rr += f.stockpile.prodRate(res)*f.citizens(null);
								}
								rr /= p;
								double r = SETT.ROOMS().industries.vanillaRate(res)/(1.0/rr);
								GFORMAT.f(text, r);
							}
						}.r(DIR.NW);
					}
				});
			}
			
			bu.column(Dic.¤¤Rate + " x " + Dic.¤¤Price, 120, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f(text, FACTIONS.PRICE().get(RESOURCES.ALL().get(ier.get()))/SETT.ROOMS().industries.vanillaRate(RESOURCES.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});
			
			bu.column(Dic.¤¤Rate + "*", 120, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f(text, 1.0/SETT.ROOMS().industries.rate(POP_CL.clP(pick.race()), RESOURCES.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});
			
			bu.column(Dic.¤¤Rate + " x " + Dic.¤¤Price + "*", 120, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					return new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.f(text, FACTIONS.PRICE().get(RESOURCES.ALL().get(ier.get()))/SETT.ROOMS().industries.rate(POP_CL.clP(pick.race()), RESOURCES.ALL().get(ier.get())));
						}
					}.r(DIR.NW);
				}
			});
			
			add(bu.create(16, true));
			
			addRelBody(16, DIR.N, pick.section);
			
			GText t = new GText(UI.FONT().S, ¤¤priceDesc);
			t.setMaxWidth(400);
			t.setMultipleLines(true);
			
			
			
			addRelBody(4, DIR.N, t);
		}
		
		
	}
	
}
