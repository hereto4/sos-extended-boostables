package view.sett.ui.health;

import java.util.Comparator;

import game.boosting.BOOSTABLES;
import game.boosting.Boostable;
import game.boosting.Booster;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.DISEASE;
import init.type.DISEASES;
import init.type.POP_CL;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.D;
import util.text.DicTime;
import view.interrupter.ISidePanel;
import view.main.VIEW;

public class UIHealth extends ISidePanel{
	
	private static CharSequence ¤¤Diseases = "Known Diseases";
	private static CharSequence ¤¤Epidemic = "Current Epidemic";
	
	static {
		D.ts(UIHealth.class);
	}
	
	public UIHealth() {
		Boostable bo = BOOSTABLES.PHYSICS().HEALTH;
		titleSet(bo.name);
		
	
		
		{
			
			
			section.add(new GStat() {
				
				@Override
				public void update(GText text) {
					if (STATS.DISEASE().currentEpidemic() == null)
						text.add('-').add('-').add('-');
					else
						text.add(STATS.DISEASE().currentEpidemic().info.name);
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					if (STATS.DISEASE().currentEpidemic() != null)
						STATS.DISEASE().currentEpidemic().hover(b);
				};
				
			}.increase().hv(¤¤Epidemic));
			
			GuiSection s = new GuiSection();
			
			if (S.get().developer)
				s.add(stat(STATS.DISEASE().incubating()), 0, s.body().y2()+6);
			s.add(stat(STATS.DISEASE().sick()), 0, s.body().y2()+6);
			
			
			
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, SETT.ROOMS().HOSPITAL.service().available(), SETT.ROOMS().HOSPITAL.service().total());
				}
				
			}.increase().hh(SETT.ROOMS().HOSPITAL.info.names, 250), 0, s.body().y2()+6);
			
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f1(text, bo.get(POP_CL.clP()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					bo.hoverDetailed(b, POP_CL.clP(), null, true);
				};
				
			}.increase().hh(bo.name, 250), 0, s.body().y2()+6);
			

			
			section.addRelBody(8, DIR.S, s);
			
		}
		
		{
			double m = 0;
			for (Booster b : BOOSTABLES.PHYSICS().HEALTH.all()) {
				
				m = Math.max(m, b.max());
			}
			
			ArrayList<RENDEROBJ> rows = new ArrayList<RENDEROBJ>(BOOSTABLES.PHYSICS().HEALTH.all().size());
			
			
			ArrayList<Booster> bb = new ArrayList<Booster>(BOOSTABLES.PHYSICS().HEALTH.all());
			bb.sort(new Comparator<Booster>() {
				
				@Override
				public int compare(Booster o1, Booster o2) {
					return o1.max() == o2.max() ? 0 : o1.max() > o2.max() ? -1 : 1;
				}
			});
			
			
			for (Booster b : bb) {
				
				GuiSection s = new GuiSection();
				s.addRightC(0, b.info.icon.resized(Icon.L));
				s.addRightC(8, new GText(UI.FONT().H2, b.info.name).setMaxWidth(200));
				


				s.addRightCAbs(200, new GStat() {
					
					@Override
					public void update(GText text) {
						b.format(text, b.get(POP_CL.clP()));
					}
				});
				rows.add(s);
				
				s.body().incrW(100);
				s.body().pad(2, 2);
			}
			
			section.addRelBody(8, DIR.S, new GScrollRows(rows, rows.get(0).body().height()*8).view());
			
		}
		
		{
			GuiSection s = new GuiSection();
			
			GStaples chart = new GStaples(STATS.DAYS_SAVED) {

				@Override
				protected double getValue(int stapleI) {
					int i = STATS.DAYS_SAVED-stapleI-1;
					return STATS.DISEASE().healthHistory.getD(i);
				}

				@Override
				protected void hover(GBox box, int stapleI) {
					int i = STATS.DAYS_SAVED-stapleI-1;
					box.title(bo.name);
					GText t = box.text();
					DicTime.setDaysAgo(t, i);
					box.add(t);
					box.tab(6);
					box.add(GFORMAT.perc(box.text(), STATS.DISEASE().healthHistory.getD(i)));
					
					
				}
				
				@Override
				protected void setColor(ColorImp c, int stapleI, double value) {
					if (value < 0.5) {
						c.interpolate(GCOLOR.UI().SOSO.normal, GCOLOR.UI().BAD.normal, 1.0-value*2);
					}else {
						c.interpolate(GCOLOR.UI().SOSO.normal, GCOLOR.UI().GOOD.normal, (value-0.5)*2);
					}
				}
				
			};
			
			chart.body().setDim(400, 90);

			s.addRelBody(4, DIR.S, chart);
			
			section.addDown(16, s);
		}
		
		{
			GuiSection s = new GuiSection();
			s.add(new GHeader(¤¤Diseases));
			
			LinkedList<RENDEROBJ> rows = new LinkedList<>();
			
			for (DISEASE d : DISEASES.all()) {
				GuiSection ss = new GuiSection();
				
				GButt.ButtPanel b = new GButt.ButtPanel(UI.FONT().M.getText(d.info.name)) {
					
					@Override
					protected void clickA() {
						if (S.get().developer) {
							STATS.DISEASE().outbreak(0.25, d);
						}
						super.clickA();
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						d.hover(text);
					}
				};
				
				b.body.setWidth(280);
				ss.add(b);
				if (S.get().developer) {
					ss.addRightC(0, new GButt.ButtPanel(UI.icons().s.death.resized(10)) {
						@Override
						protected void clickA() {
							STATS.DISEASE().outbreak(d.infectRate, d);
						}
					});
					ss.addRightC(0, new GButt.ButtPanel(UI.icons().s.death) {
						@Override
						protected void clickA() {
							STATS.DISEASE().outbreak(1, d);
						}
					});
					
				}
				
				rows.add(ss);
			}
			
			s.addDown(2, new GScrollRows(rows, HEIGHT-section.body().height()-80, section.body().width()).view());
			
			section.addDown(16, s);
			
		}
		
		
		
		
	}
	
	private static RENDEROBJ stat(STAT st) {
		
		GuiSection s = new GuiSection();
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, st.data().get(null));
			}
		}.increase().hh(st.info().name, st.info().desc, 250));
		
		s.addRightC(80, new GButt.ButtPanel(UI.icons().s.crossheir) {
			
			int ie = 0;
			
			@Override
			protected void clickA() {
				for (int i = 0; i < SETT.ENTITIES().getAllEnts().length; i++) {
					ie ++;
					if (ie >= SETT.ENTITIES().getAllEnts().length)
						ie = 0;
					ENTITY e = SETT.ENTITIES().getAllEnts()[ie];
					if (e != null && e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (st.indu().get(a.indu()) > 0) {
							VIEW.s().getWindow().centererTile.set(a.tc());
							break;
						}
					}
					
				}
			}
			
			@Override
			protected void renAction() {
				activeSet(st.data().get(null) > 0);
			}
			
		});

		return s;
	}
	
}
