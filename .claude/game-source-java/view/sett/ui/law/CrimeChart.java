package view.sett.ui.law;

import game.boosting.BOOSTABLES;
import game.boosting.BValue.PopTime;
import game.faction.FACTIONS;
import game.time.TIME;
import init.settings.S;
import init.sprite.SPRITES;
import init.type.POP_CL;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.PRISONER_TYPE;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.Dic;
import util.text.DicTime;
import view.main.VIEW;

final class CrimeChart extends GuiSection{
	
	final GuiSection pop = new GuiSection();
	private final PopTime ptime = new PopTime();
	
	public CrimeChart(int sw){
		
		
		for (CRIME t : PRISONER_TYPE.CRIME.CRIMES) {
			pop.add(new GButt.Glow(t.name + " +1") {
				@Override
				protected void clickA() {
					LAW.crimes().register(FACTIONS.player().race(), t);
				}
			}, pop.body().x1(), pop.body().y2());
			pop.addRightC(20, new GButt.Glow(t.name + " +10") {
				@Override
				protected void clickA() {
					for (int i = 0; i < 10; i++)
						LAW.crimes().register(FACTIONS.player().race(), t);
				}
			});
			
		}
		
		
		addRelBody(0, DIR.S, new GHeader(Dic.¤¤Crime));
		GStaples chart = new GStaples(STATS.DAYS_SAVED) {
			
			@Override
			protected void renderExtra(SPRITE_RENDERER r, COLOR color, int stapleI, boolean hovered, double value,
					int x1, int x2, int y1, int y2) {
				x1 += 1;
				x2 -= 1;
				int ii = STATS.DAYS_SAVED -stapleI-1;
				double tot = LAW.crimes().crimes(null).get(ii);
				ColorImp c = ColorImp.TMP;
				double dy1 = y1;
				double dy2 = y1;
				double dy = y2-y1;
				if (dy <= 0)
					return;
				
				for (int i = 0; i < PRISONER_TYPE.CRIMES.size(); i++) {
					double d = dy*LAW.crimes().crimes(PRISONER_TYPE.CRIMES.get(i)).get(ii)/tot;
					dy2 += d;
					c.set(COLOR.UNIQUE.getC(i));
					if (hovered) {
						c.shadeSelf(1.2);
					}
					c.render(r, x1, x2, (int)dy1, (int)dy2);
					dy1 = dy2;
				}
			}
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int ii = STATS.DAYS_SAVED -stapleI-1;
				
				box.textL(DicTime.setAgo(box.text(), ii*TIME.secondsPerDay()));
				box.NL(4);
				
				for (int i = 0; i < PRISONER_TYPE.CRIMES.size(); i++) {
					box.add(SPRITES.icons().s.circle, COLOR.UNIQUE.getC(i));
					box.text(PRISONER_TYPE.CRIMES.get(i).names);
					box.tab(6);
					box.add(GFORMAT.i(box.text(), LAW.crimes().crimes(PRISONER_TYPE.CRIMES.get(i)).get(ii)));
					box.NL();
				}
				

				box.sep();
				BOOSTABLES.BEHAVIOUR().LAWFULNESS.hover(box, ptime.get(POP_CL.clP(), ii), true);
				
//				box.NL(2);
//				box.textL(Dic.¤¤Population);
//				box.tab(6);
//				box.add(GFORMAT.i(box.text(), STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null, ii)));
//				box.NL(2);
//	
//				box.NL();
//				box.textL(Dic.¤¤Law);
//				box.tab(6);
//				box.add(GFORMAT.perc(box.text(), LAW.law().rate().getD(ii)));
//				box.NL(2);
			}
			
			@Override
			protected double getValue(int stapleI) {
				int ii = STATS.DAYS_SAVED -stapleI-1;
				return LAW.crimes().crimes(null).get(ii);
			}
			
			@Override
			protected void setColor(ColorImp c, int stapleI, double value) {
				c.set(COLOR.YELLOW100).saturateSelf(0.5);
			}
			
		};
		chart.body().setWidth(STATS.DAYS_SAVED*sw);
		chart.body().setHeight(80);
		
		
		add(chart, body().x1()-40, body().y2()+4);
	}
	
	@Override
	protected void clickA() {
		if (S.get().developer)
			VIEW.inters().popup.show(pop, this);
	}


	
	
}
