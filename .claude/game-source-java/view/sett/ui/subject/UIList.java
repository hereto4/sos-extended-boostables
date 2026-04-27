package view.sett.ui.subject;

import game.time.TIME;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.settings.S;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.ENTETIES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.Dictionary;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GDropDown;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter;
import util.gui.table.GTableSorter.GTFilter;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;

final class UIList extends ISidePanel{
	

	private final List list = new List();
	
	private int selected = -1;
	private Humanoid current;
	private final FilterEmployment fWork = new FilterEmployment();
	
	public UIList() {
		
		titleSet(Dic.¤¤Subjects);
		section = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				list.sort();
				if (current != null) {
					VIEW.s().getWindow().centerer.set(current.body().cX(), current.body().cY());
					SETT.OVERLAY().add(current);
				}
				super.render(r, ds);
				if (selected != -1)
					current = list.get(selected);
				else
					current = null;
			}
		};
		
		section.body().setWidth(C.SG*270);
		
		section.addDownC(0, makeHeader());
		
		GuiSection n = makeFilter();

		section.addDownC(8, n);
		
		
		int y1 = n.body().y2()+10;
		
		n = makeList().createHeight(HEIGHT-8-section.body().y2(), true);
		n.body().moveY1(y1);
		n.body().moveX1(section.body().x1());
		section.add(n);
		section.pad(6, 0);
		
//		y1 = makeSeparator(s, n.body().y2()).body().y2();
//		n = detail();
//		n.body().moveY1(y1);
//		n.body().moveX1(s.body().x1()+10);
//		s.add(n);
		
	}
	
	public void show() {
		if (list.currentFilter() == fWork)
			list.setFilter(list.filters.get(0));
		list.sortForced();
		VIEW.s().panels.add(this, true);
	}
	
	public void show(Humanoid h) {
		if (list.currentFilter() == fWork)
			list.setFilter(list.filters.get(0));
		selected = -1;
		current = h;
		list.sortForced();
	}

	
	public void showProfession(RoomInstance work) {
		selected = -1;
		fWork.r = work;
		list.setFilter(fWork);
		list.sortForced();
		VIEW.s().panels.add(this, true);
	}
	
	private GuiSection makeHeader() {
		
		GuiSection s = new GuiSection();
		int i = 0;
		for (HTYPE t : HTYPES.ALL()) {
			if (!t.player || !t.visible)
				continue;
			s.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, STATS.POP().pop(t));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					int tab = 0;
					b.text(t.desc);
					b.NL();
					for (Race r : RACES.all()) {
						b.tab(tab);
						
						b.add(r.appearance().icon);
						b.add(GFORMAT.i(b.text(), STATS.POP().pop(r, t)));
						b.space();
						tab+=3;
						if (tab > 9) {
							b.NL();
							tab = 0;
						}
					}
				};
			}.hh(t.icon, t.names, 180), (i%1)*180, (i/1)*17);
			i++;
			
		}
		
		return s;
	}
	
	private GuiSection makeFilter() {
		
		GuiSection filter = new GuiSection();
		
		GDropDown<CLICKABLE> d = new GDropDown<CLICKABLE>(Dic.¤¤Sort);
		for (List.GTSort<Humanoid> s : list.sorts) {
			GButt.Glow c = new GButt.Glow((SPRITE)new Text(UI.FONT().S, s.name).setMaxWidth(150).setMultipleLines(false)) {
				@Override
				protected void clickA() {
					list.setSort(s);
				}
			};
			c.hoverTitleSet(s.name);
			c.body.setWidth(160);
			d.add(c);
		}
		d.init();
		filter.add(d);
		
		d = new GDropDown<CLICKABLE>(Dic.¤¤Filter);
		for (List.GTFilter<Humanoid> f : list.filters) {
			GButt.Glow c = new GButt.Glow((SPRITE)new Text(UI.FONT().S, f.name).setMaxWidth(150).setMultipleLines(false)) {
				@Override
				protected void clickA() {
					list.setFilter(f);
				}
			};
			c.hoverTitleSet(f.name);
			c.body.setWidth(160);
			d.add(c);
		}
		d.init();
		d.body.moveX2(filter.body().x2());
		d.body.moveY1(filter.body().y2());
		filter.add(d);
		
		RENDEROBJ r = new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, list.size(), STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null, 0));
				text.normalify();
			}
		}.hh(Dic.¤¤Showing);
		r.body().centerX(filter.body());
		r.body().moveY1(filter.body().y2()+5);
		filter.add(r);
		
		return filter;
		
	}
	
	private GTableBuilder makeList() {
		
		GTableBuilder builder = new GTableBuilder() {
			@Override
			public int nrOFEntries() {
				return list.size();
			}
			
			@Override
			public void click(int index) {
				selected = index;
				current = list.get(selected);
				if (current.canBeClicked())
					VIEW.s().ui.subjects.show(current);
			}
			
			@Override
			public void doubleClick(int index) {
//				VIEW.s().activate();
//				pop[index].click();
			}
			
			@Override
			public void hover(int index) {
				if (index >= 0) {
					current = list.get(index);
					current.hover(VIEW.hoverBox());
				}
			}
			
			@Override
			public boolean selectedIs(int index) {
				return VIEW.s().ui.subjects.current() == list.get(index);
			}
		};
		
		builder.column(null, 32, new GRowBuilder() {
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new RENDEROBJ.RenderImp(32, 24) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						Humanoid a = list.get(ier.get());
						if (a != null) {
							a.race().appearance().icon.renderCY(r, 0, body().cY());
							a.indu().clas().icon().renderCY(r, 18, body().cY());
						}
							
					}
				};
			}
		});
		
		final int ww = section.body().width();
		
		builder.column("Sort", ww, new GRowBuilder() {
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					@Override
					public void update(GText text) {
						Humanoid a = list.get(ier.get());
						if (a != null) {
							list.currentSort().format(a, text);
							text.setMaxWidth(ww-8);
							text.setMultipleLines(false);
						}
					}
				}.decrease().r();
			}
		});
		
		

		return builder;
		
	}


	private static final class FilterEmployment extends GTFilter<Humanoid>{
	
		RoomInstance r;
		
		public FilterEmployment() {
			super("profession");
		}
	
		@Override
		public boolean passes(Humanoid h) {
			return STATS.WORK().EMPLOYED.get(h.indu()) == r;
		}
		
		
	}

	private static final class List extends GTableSorter<Humanoid>{
			
		final LIST<GTSort<Humanoid>> sorts;
		final ArrayList<GTFilter<Humanoid>> filters;
		
		
		List(){
			super(ENTETIES.MM);
			
			ArrayList<GTSort<Humanoid>> sother2 = new ArrayList<GTSort<Humanoid>> (
				new GTSort<Humanoid>(Dic.¤¤name) {
					Str scurrent = new Str(32);
					Str scmp = new Str(32);
					@Override
					public int cmp(Humanoid current, Humanoid cmp) {
						scurrent.clear().add(STATS.APPEARANCE().name(current.indu()));
						scmp.clear().add(STATS.APPEARANCE().name(cmp.indu()));
						return Dictionary.compare(scurrent, scmp);
					}
	
					@Override
					public void format(Humanoid h, GText text) {
						text.normalify();
						text.add(STATS.APPEARANCE().name(h.indu()));
					}
				},
				new GTSort<Humanoid>(Dic.¤¤Age) {
					@Override
					public int cmp(Humanoid current, Humanoid cmp) {
						return STATS.POP().age.DAYS.get(current.indu()) - STATS.POP().age.DAYS.get(cmp.indu());
					}
	
					@Override
					public void format(Humanoid h, GText text) {
						text.normalify();
						text.add((double)STATS.POP().age.DAYS.get(h.indu())/TIME.years().bitConversion(TIME.days()), 2);
					}
				},
				new GTSort<Humanoid>(STATS.WORK().EMPLOYED.info.name) {
					@Override
					public int cmp(Humanoid current, Humanoid cmp) {
						
						return Dictionary.compare(current.title(), cmp.title());
					}
	
					@Override
					public void format(Humanoid h, GText text) {
						text.normalify();
						text.add(h.title());
					}
				}
				,
				new GTSort<Humanoid>(Dic.¤¤Occupation) {
					final Str string = new Str(64);
					final Str string2 = new Str(64);
					@Override
					public int cmp(Humanoid current, Humanoid cmp) {
						string.clear();
						string2.clear();
						current.ai().getOccupation(current, string);
						cmp.ai().getOccupation(cmp, string2);
						return Dictionary.compare(string, string2);
					}
	
					@Override
					public void format(Humanoid h, GText text) {
						text.normalify();
						h.ai().getOccupation(h, text);
					}
				}
			);
			
			ArrayList<GTSort<Humanoid>> sother = new ArrayList<GTSort<Humanoid>>(STATS.all().size());
			for (STAT s : STATS.createMatterList(true, false, null)) {
				if (s == STATS.POP().age.AGE_DAYS)
					continue;
				
				GTSort<Humanoid> h = new GTSort<Humanoid>(s.info().name) {
					
					@Override
					public int cmp(Humanoid current, Humanoid cmp) {
						return s.indu().get(current.indu()) - s.indu().get(cmp.indu());
					}
	
					@Override
					public void format(Humanoid h, GText text) {
						if (s.info().isInt()) {
							if (s.indu().max(h.indu()) == 1) {
								GFORMAT.bool(text, s.indu().get(h.indu()) == 1);
							}else
								GFORMAT.i(text, s.indu().get(h.indu()));
						}else {
							GFORMAT.perc(text, s.indu().getD(h.indu()));
						}
					}
				};
				sother.add(h);
			}
			
			sorts = sother2.join(sother);
			
			setSort(sorts.get(0));
			
			filters = new ArrayList<GTFilter<Humanoid>>(2+RACES.all().size()+HTYPES.ALL().size());
			filters.add(new GTFilter<Humanoid>(Dic.¤¤None) {
	
				@Override
				public boolean passes(Humanoid h) {
					return true;
				}
			});
			filters.add(new GTFilter<Humanoid>(Dic.¤¤Favourite) {
	
				@Override
				public boolean passes(Humanoid h) {
					return STATS.APPEARANCE().favo.get(h.indu()) == 1;
				}
			});
			
			for (int i = 0; i < RACES.all().size(); i++) {
				final int k = i;
				filters.add(new GTFilter<Humanoid>(RACES.all().get(k).info.name) {
	
					@Override
					public boolean passes(Humanoid h) {
						return h.indu().race().index == k;
					}
				
				});
			}
			
			for(HTYPE t : HTYPES.ALL()) {
				if (S.get().developer || !t.hostile)
					filters.add(new GTFilter<Humanoid>(t.names) {
		
						@Override
						public boolean passes(Humanoid h) {
							return h.indu().hType() == t;
						}
					
					});
			}
			setFilter(filters.get(0));
		}
	
		@Override
		protected Humanoid getUnsorted(int index) {
			ENTITY e = SETT.ENTITIES().getAllEnts()[index];
			if (e instanceof Humanoid) {
				if (S.get().developer || ((Humanoid)e).indu().player())
					return (Humanoid) e;
			}
			return null;
		}
	}
	
}
