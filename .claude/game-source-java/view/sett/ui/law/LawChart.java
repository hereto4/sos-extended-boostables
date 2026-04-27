package view.sett.ui.law;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.time.TIME;
import init.settings.S;
import init.type.POP_CL;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.LawRate;
import settlement.stats.law.Processing.Extra;
import settlement.stats.law.Processing.Punishment;
import settlement.stats.law.Processing.PunishmentImp;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GuiSection;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.Dic;
import util.text.DicTime;
import view.main.VIEW;

final class LawChart extends GuiSection{
	
	final GuiSection pop = new GuiSection();

	public LawChart(int sw){
		
		for (Punishment t : LAW.process().punishments) {
			pop.add(new GButt.Glow(t.name + " +1") {
				@Override
				protected void clickA() {
					t.inc(FACTIONS.player().race());
				}
			}, pop.body().x1(), pop.body().y2());
			pop.add(new GButt.Glow(t.name + " -1") {
				@Override
				protected void clickA() {
					t.inc(FACTIONS.player().race());
				}
			}, pop.body().x1(), pop.body().y2());
			
		}
		for (Extra t : LAW.process().extras.join(LAW.process().arrests, LAW.process().prosecute)) {
			pop.add(new GButt.Glow(t.name + " +1") {
				@Override
				protected void clickA() {
					t.inc(FACTIONS.player().race(), true);;
				}
			}, pop.body().x1(), pop.body().y2());
			pop.add(new GButt.Glow(t.name + " -1") {
				@Override
				protected void clickA() {
					t.inc(FACTIONS.player().race(), false);
				}
			}, pop.body().x1(), pop.body().y2());
			
		}
		
		add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text,  LAW.law().rate().getD(0));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤Law);
				b.text(LawRate.¤¤desc);
				
				b.NL(8);
				b.textLL(Dic.¤¤Target);
				b.add(GFORMAT.percInv(b.text(), LAW.law().today()));
			};
			
		}.increase().hh(Dic.¤¤Law, 180));
		
		GStaples chart = new GStaples(STATS.DAYS_SAVED) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int i = STATS.DAYS_SAVED-stapleI-1;
				hoverrr(i, box);
			
			}
			
			@Override
			protected double getValue(int stapleI) {
				int i = STATS.DAYS_SAVED-stapleI-1;
				return LAW.law().rate().getD(i);
			}
			
			@Override
			protected void setColor(ColorImp c, int stapleI, double value) {
				c.interpolate(GCOLOR.UI().SOSO.hovered, GCOLOR.UI().GOOD.hovered, value);
				int i = STATS.DAYS_SAVED-stapleI-1;
				int am = 0;
				for (Extra e : LAW.process().extras)
					am += e.total(null, i);
				if (LAW.process().punishTotal(null).get(i) == 0  && am == 0) {
					c.shadeSelf(0.7);
				}
			}
			

			
		};
		chart.normalize(false);
		chart.body().setDim(STATS.DAYS_SAVED*sw, 80);
		add(chart, body().x1()-40, body().y2()+4);
	}
	
	private void hoverrr(int i, GBox box) {
		
		box.textLL(DicTime.setAgo(box.text(), i*TIME.secondsPerDay()));

		
		BOOSTABLES.CIVICS().LAW.hoverDetailedHistoric(box, POP_CL.clP(), null, true, i);
		
		box.sep();
		
		box.NL(8);
		box.tab(5);
		box.textLL(Dic.¤¤Count);
		box.tab(7);
		box.textLL(Dic.¤¤Rate);
		box.tab(9);
		box.textLL(Settings.¤¤Deter);
		box.tab(12);
		box.textLL(Dic.¤¤Max);
		box.NL();
		
		
		box.NL(4);
		hoverrr(i, box, LAW.process().arrests);
		box.NL(8);
		
		for (Punishment s : LAW.process().punishments)
			hoverrr(i, box, s);
		
		for (Extra s : LAW.process().extras) {
			hoverrr(i, box, s);
		}
		
		box.NL(32);
		box.textLL(Dic.¤¤Law);
		box.tab(5);
		box.add(GFORMAT.perc(box.text().increase(), LAW.law().rate().getD(i)));
		box.tab(7);
		{
			GText t = box.text();
			t.add('(').s().add(LAW.process().arrests.rate(null).getD(i), 1).s().add('*').s().add('(');
			boolean f = true;
			for (Punishment s : LAW.process().punishments) {
				if (!f) {
					t.add('+').s();
				}
				f = false;
				t.add(s.rate(null).getD(i)*s.multiplier, 1).s();
			}
			t.add(')').add(')');
			box.add(t);
		}
		box.NL(8);
		
	}
	
	private void hoverrr(int i, GBox box, PunishmentImp s) {
		
		box.textLL(s.name);
		box.tab(5);
		box.add(GFORMAT.iofkInv(box.text(), s.history(null).get(i), s.total(null, i)));
		box.tab(7);
		box.add(GFORMAT.perc(box.text(), s.rate(null).getD(i)));
		box.tab(9);
		
		double m = 1 + s.multiplier;
		box.add(GFORMAT.f(box.text(), s.rate(null).getD(i)*m));
		GText t = box.text();
		t.add('(');
		t.add(m, 1);
		t.add(')');
		box.tab(12);
		box.add(t);
		box.NL();
	}
	
	@Override
	protected void clickA() {
		if (S.get().developer)
			VIEW.inters().popup.show(pop, this);
	}


	
	
}
