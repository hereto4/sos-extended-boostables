package view.sett.ui.standing;

import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HTYPES;
import init.type.POP_CL;
import settlement.main.SETT;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.knowledge.school.ROOM_SCHOOL;
import settlement.room.knowledge.university.ROOM_UNIVERSITY;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.sett.ui.standing.Cats.Cat;

class CatOccupation extends Cat {
	
	private static CharSequence ¤¤lSpeed = "Learning Days";
	private static CharSequence ¤¤lSpeedD = "How many days are needed to fully educate a subject. Depends on race and other factors.";
	private static CharSequence ¤¤educate = "Educate";
	private static CharSequence ¤¤workPrio = "Work Priorities";
	private static CharSequence ¤¤indoctor = "Indoctrinate";
	
	static {
		D.ts(CatOccupation.class);
	}

	CatOccupation(HCLASS cl){
		super(new StatCollection[] {STATS.WORK(), STATS.EDUCATION()});
		titleSet(Dic.¤¤Occupation);
		LinkedList<RENDEROBJ> rens = new LinkedList<>();

		
		rens.add(new StatRow.Title(STATS.WORK().info));
		for (STAT s : STATS.WORK().workStats) {
			rens.add(new StatRow(s, cl));
		}
		
		rens.add(new GButt.ButtPanel(¤¤workPrio) {
			@Override
			protected void clickA() {
				VIEW.s().ui.rooms.prio(cl, CitizenMain.current, this);
			}
		}.pad(16, 2));
		
		if (cl != HCLASSES.SLAVE()) {
			rens.add(new StatRow(STATS.WORK().RET.RETIREMENT_AGE, cl));
			rens.add(new StatRow(STATS.WORK().RET.RETIREMENT_HOME, cl));
		}
		
		if (cl != HCLASSES.SLAVE()) {
			
			rens.add(new StatRow.Title(STATS.EDUCATION().info));
			rens.add(new StatRow(STATS.EDUCATION().EDUCATION, cl));
			rens.add(new StatRow(STATS.EDUCATION().INDOCTRINATION, cl));
			
			
			
			{
				GuiSection s = new GuiSection();
				
				GButt.ButtPanel p = new GButt.ButtPanel(¤¤educate) {
					
					@Override
					protected void clickA() {
						STATS.EDUCATION().policyIndoctor.set(CitizenMain.current, false);
					}
					
					@Override
					protected void renAction() {
						selectedSet(!STATS.EDUCATION().policyIndoctor.is(CitizenMain.current));
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(¤¤educate);
						b.text(STATS.EDUCATION().EDUCATION.info().desc);
						
						b.NL(8);
						
						STATS.EDUCATION().EDUCATION.boosters.hover(text, 1.0, Dic.¤¤Boosts, null, -1);
						
					}
					
				};
				p.pad(32, 4);
				s.body().incrW(48).incrH(1);
				s.addRightC(0, p);
				p = new GButt.ButtPanel(¤¤indoctor) {
					
					@Override
					protected void clickA() {
						STATS.EDUCATION().policyIndoctor.toggle(CitizenMain.current);
					}
					
					@Override
					protected void renAction() {
						selectedSet(STATS.EDUCATION().policyIndoctor.is(CitizenMain.current));
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(¤¤indoctor);
						b.text(STATS.EDUCATION().INDOCTRINATION.info().desc);
						
						b.NL(8);
						STATS.EDUCATION().INDOCTRINATION.boosters.hoverDetailed(text, STATS.EDUCATION().INDOCTRINATION.data(cl).getD(CitizenMain.current), Dic.¤¤Boosts, null, -1);
						
					}
					
				};
				p.pad(32, 4);
				s.body().incrW(48).incrH(1);
				s.addRightC(0, p);
				
				
				rens.add(s);
			}
			
			{
				GuiSection s = new GuiSection();

				
				GRows rows = new GRows(2);
				
				rows.add(s);
				
				for (ROOM_UNIVERSITY u : SETT.ROOMS().UNIVERSITIES) {
										
					s = new GuiSection();
					
					s.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.iofk(text, u.employment().employed(), u.employment().employedMax());
						}
					}, 0, 0);
					s.addDown(4, new SPRITE.Imp(90, 8) {
						
						@Override
						public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
							GMeter.render(r, GMeter.C_BLUE, u.limit.getD(CitizenMain.current), X1, X2, Y1, Y2);
							
						}
					});
					
					s.addRelBody(8, DIR.W, u.iconBig());
					s.body().setWidth(135);
					s.body().pad(0, 2);
					
					GButt.ButtPanel b = new GButt.ButtPanel(s.asSprite()) {
						
						@Override
						public void hoverInfoGet(GUI_BOX box) {
							GBox b = (GBox) box;
							b.title(u.info.names);
							b.text(u.info.desc);
							b.NL();
							
							b.textLL(u.employment().title);
							b.tab(6);
							b.add(GFORMAT.iofk(b.text(), u.employment().employed(), u.employment().employedMax()));
							b.NL();
							
							b.textLL(¤¤lSpeed);
							b.tab(6);
							b.add(GFORMAT.i(b.text(), (long) (1.0/(u.learningSpeed*u.bonus().get(POP_CL.clP(CitizenMain.current, HCLASSES.CITIZEN()))))));
							b.NL();
							b.text(¤¤lSpeedD);
							b.NL();
							
							
							b.textLL(u.limit.info().name);
							b.tab(6);
							b.add(GFORMAT.perc(b.text(), u.limit.getD(CitizenMain.current)));
							b.NL();
							b.text(u.limit.info().desc);
							b.NL();
							
						}
						
						@Override
						protected void clickA() {
							VIEW.s().panels.add(VIEW.s().ui.rooms.open(u), true);
						}
					};
					rows.add(b);
				}
				
				rens.add(rows.rows());
				
				
			}
			
			{
				GuiSection s = new GuiSection();
				s.add(new GHeader(HTYPES.CHILD().names));
				s.addRightC(8, new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, STATS.POP().pop(HTYPES.CHILD()));
					}
				});
				rens.add(s);
				
				GRows rows = new GRows(2);
				
				for (ROOM_SCHOOL u : SETT.ROOMS().SCHOOLS) {
					s = new GuiSection() {
						@Override
						public void hoverInfoGet(GUI_BOX box) {
							GBox b = (GBox) box;
							b.title(u.info.names);
							b.text(u.info.desc);
							b.NL();
							
							b.textLL(Dic.¤¤load);
							b.tab(6);
							b.add(GFORMAT.perc(b.text(), u.service().load()));
							b.NL();
							
							b.textLL(¤¤lSpeed);
							b.tab(6);
							b.add(GFORMAT.i(b.text(), (long) (1.0/(u.learningSpeed))));
							b.NL();
							b.text(¤¤lSpeedD);
							b.NL();
							
							b.textLL(Dic.¤¤Access);
							b.tab(6);
							b.add(b.text().add(u.access(CitizenMain.current)));
							b.NL();
							
							super.hoverInfoSelf(box);
						}
						
					};
					
					s.addRightC(0, u.iconBig());
					
					s.addRightC(8, new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.perc(text, u.service().load());
						}
					});
					
					s.addRightC(64, new SPRITE.Imp(Icon.S) {

						@Override
						public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
							if (u.access(CitizenMain.current)) {
								GCOLOR.T().IGOOD.bind();
								UI.icons().s.allRight.render(r, X1, Y1);
							}else {
								GCOLOR.T().IBAD.bind();
								UI.icons().s.cancel.render(r, X1, Y1);
							}
							
						}
						
					});
					
					s.body().setWidth(120);
					s.body().pad(16, 0);
					
					GButt.ButtPanel b = new GButt.ButtPanel(s.asSprite()) {
						@Override
						public void hoverInfoGet(GUI_BOX box) {
							GBox b = (GBox) box;
							b.title(u.info.names);
							b.text(u.info.desc);
							b.NL();
							
							b.textLL(Dic.¤¤load);
							b.tab(6);
							b.add(GFORMAT.perc(b.text(), u.service().load()));
							b.NL();
							
							b.textLL(¤¤lSpeed);
							b.tab(6);
							b.add(GFORMAT.i(b.text(), (long) (1.0/(u.learningSpeed))));
							b.NL();
							b.text(¤¤lSpeedD);
							b.NL();
						}
						
						@Override
						protected void clickA() {
							VIEW.s().panels.add(VIEW.s().ui.rooms.open(u), true);
						}
					};
					
					rows.add(b);
					
				}
				
				rens.add(rows.rows());
				
				
			}

		}
		
		
		section.add(new GScrollRows(rens, HEIGHT, 0).view());
		
	}
	
	GuiSection makeHomes(HCLASS c) {
		GuiSection s = new GuiSection();
		
		int i = 0;
		
		for (ROOM_RESTHOME hh : SETT.ROOMS().RESTHOMES) {
			
			final ROOM_RESTHOME h = hh;
			
			
			SPRITE icon = new SPRITE.Imp(Icon.L) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (CitizenMain.current != null && CitizenMain.current.pref().getWork(h.employment()) <= 0) {
						OPACITY.O50.bind();
						COLOR.BLACK.render(r, X1, X2, Y1, Y2);
						OPACITY.unbind();
					}
					h.iconBig().render(r, X1, X2, Y1, Y2);
				}
			};
			
			RENDEROBJ r = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iIncr(text, h.employment().neededWorkers()-h.employment().employed());
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(h.info.names);
					b.text(h.info.desc);
					b.NL(8);
					b.textLL(HTYPES.RETIREE().names);
					b.tab(5);
					b.add(GFORMAT.iofk(b.text(), h.employment().employed(), h.employment().neededWorkers()));
					b.NL();
					b.textLL(Dic.¤¤Quality);
					b.tab(5);
					b.add(GFORMAT.perc(b.text(), h.quality()));
					b.NL();
					if (CitizenMain.current != null) {
						b.textLL(STANDINGS.CITIZEN().fullfillment.info().name);
						b.tab(5);
						b.add(GFORMAT.perc(b.text(), CitizenMain.current.pref().getWork(h.employment())));
					}
						
				};
				
			}.hh(icon);
			
			s.add(r, 150*(i%4), 40*(i/4));
			i++;
			
		}
		
		s.pad(8);
		return s;
		
	}
	
	
	
}
