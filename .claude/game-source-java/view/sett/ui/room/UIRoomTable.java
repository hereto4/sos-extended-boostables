package view.sett.ui.room;

import game.boosting.Booster;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEquip;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.Dictionary;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GDropDown;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GAllocator;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.interrupter.ISidePanel;
import view.main.VIEW;

final class UIRoomTable extends ISidePanel {

	private static final TableSorter tableSort = new TableSorter();
	private final RoomBlueprintIns<?> blueprint;
	private GTFilter<RoomInstance> filterCurrent;
	private GTSort<RoomInstance> sortCurrent;
	private RoomInstance hovered;
	private boolean wasHovering = false;
	private Coo oldC = new Coo();

	private static CharSequence ¤¤NrOfRooms = "Number of Rooms";
	private static CharSequence ¤¤Bulk = "¤Bulk";
	private static CharSequence ¤¤Showing = "¤Showing";
	private static CharSequence ¤¤level = "Current Available Max Level:";
	
	
	static {
		D.ts(UIRoomTable.class);
	}

	ISidePanel get() {
		wasHovering = false;
		tableSort.set(blueprint, filterCurrent, sortCurrent);
		return this;
	}

	UIRoomTable(RoomBlueprintIns<?> b, UIRoom gui, UIRoomModule... appliers) {
		this.blueprint = b;

		titleSet(blueprint.info.names);

		section = new GuiSection() {
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				tableSort.sort();
				super.render(r, ds);
				if (hovered != null) {
					SETT.OVERLAY().add(hovered.mX(), hovered.mY());
					VIEW.s().getWindow().centerAtTile(hovered.body().cX(), hovered.body().cY());
					wasHovering = true;
					hovered = null;
				} else {
					if (wasHovering)
						VIEW.s().getWindow().centerAt(oldC);
					wasHovering = false;
				}
			}
		};
		
		int width = 200;
		RENDEROBJ o = makeRow(gui, new GETTER_IMP<Integer>(), appliers);
		if (o.body().width() > width)
			width = o.body().width();
		
		section.body().setWidth(width).setHeight(1);

		GuiSection sExtra = new GuiSection();
		
		int y1 = 0;

		
		{
			GuiSection s = new GuiSection();

			GGrid grid = new GGrid(s, section.body().width(), 1, 0, 0).setAlignment(DIR.W);
			GGrid text = new GGrid(new GuiSection(), 1, 1, 0, 0).setAlignment(DIR.C);
			grid.add(new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.i(text, blueprint.instancesSize());
				}
			}.hh(SPRITES.icons().s.house).hoverInfoSet(¤¤NrOfRooms));

			
			
			for (UIRoomModule m : appliers) {
				m.appendManageScr(grid, text, sExtra);
			}

			int k = 0;
			for(RENDEROBJ r : s.elements()) {
				r.body().moveX1Y1(8 + (k%2)*section.body().width()/2, y1 + (k/2)*24);
				section.add(r);
				k++;
			}
			
			for(RENDEROBJ r : text.section.elements())
				section.addRelBody(4, DIR.S, r);

			y1 = section.body().y2();

		}

		{

			final ArrayListGrower<GTFilter<RoomInstance>> filters = new ArrayListGrower<>();
			filters.add(new GTFilter<RoomInstance>(Dic.¤¤None) {

				@Override
				public boolean passes(RoomInstance h) {
					return true;
				}

			});

			final ArrayListGrower<GTSort<RoomInstance>> sorts = new ArrayListGrower<>();
			sorts.add(new GTSort<RoomInstance>(Dic.¤¤name) {

				@Override
				public int cmp(RoomInstance current, RoomInstance cmp) {
					return Dictionary.compare(current.name(), cmp.name());
				}

				@Override
				public void format(RoomInstance h, GText text) {
					text.add(h.name());
					text.normalify();
				}

			});

			final ArrayListGrower<UIRoomBulkApplier> apps = new ArrayListGrower<UIRoomBulkApplier>();

			for (UIRoomModule m : appliers) {
				m.appendTableFilters(filters, sorts, apps);
			}

			GuiSection filter = new GuiSection() {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					UI.PANEL().butt.render(r, body(), 0);
					super.render(r, ds);
				}
				
			};
			
			GDropDown<CLICKABLE> d = new GDropDown<CLICKABLE>(Dic.¤¤Sort, 100);
			for (GTSort<RoomInstance> s : sorts) {
				SPRITE sp = (SPRITE)new Text(UI.FONT().S, s.name).setMaxWidth(120).setMultipleLines(false);
				CLICKABLE c = new GButt.Glow(sp) {

					{
						if (sortCurrent == null) {
							sortCurrent = s;
						}
					}

					@Override
					protected void clickA() {
						sortCurrent = s;
						tableSort.setSort(s);
					};
				}.hoverInfoSet(s.name);
				d.add(c);
			}
			d.init();
			d.body.moveY1(filter.body().y1() + 4);
			d.body.moveX1(filter.body().x1() + 26);
			filter.add(d);

			d = new GDropDown<CLICKABLE>(Dic.¤¤Filter, 100);
			for (GTFilter<RoomInstance> f : filters) {
				SPRITE sp = (SPRITE)new Text(UI.FONT().S, f.name).setMaxWidth(120).setMultipleLines(false);
				CLICKABLE c = new GButt.Glow(sp) {
					{
						if (filterCurrent == null)
							filterCurrent = f;
					}

					@Override
					protected void clickA() {
						tableSort.setFilter(f);
						filterCurrent = f;

					};
				}.hoverInfoSet(f.name);
				d.add(c);
			}
			d.init();
			filter.addDown(2, d);

			if (apps.size() > 0) {
				final GDropDown<CLICKABLE> bulk = new GDropDown<CLICKABLE>(¤¤Bulk, 100);
				for (UIRoomBulkApplier a : apps) {
					SPRITE sp = (SPRITE)new Text(UI.FONT().S, a.name).setMaxWidth(120).setMultipleLines(false);
					CLICKABLE c = new GButt.Glow(sp) {

						@Override
						protected void clickA() {
							for (int i = 0; i < tableSort.size(); i++) {
								RoomInstance t = tableSort.get(i);
								if (t != null) {
									a.apply(t);
								}

							}
							bulk.setSelected(null);

						};
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							a.hover((GBox) text);
						}
					}.hoverInfoSet(a.name);
					bulk.add(c);
				}
				bulk.setSelected(null);
				d = bulk;
				d.init();
				d.body.moveX1(24);
				d.body.moveY2(filter.body().y2() - 4);

				filter.addDown(2, d);
			}

			RENDEROBJ r = new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.iofk(text, tableSort.size(), blueprint.all().size());
					text.normalify();
				}
			}.hh(¤¤Showing);
			r.body().moveX1(100);
			r.body().moveY1(filter.body().y2() + 5);
			filter.addRelBody(6, DIR.S, r);

			filter.body().centerX(section);
			filter.body().moveY1(y1 + C.SG * 8);
			section.add(filter);
			y1 = filter.body().y2();
		}

		{
			GuiSection s = table(y1, gui, appliers);
			s.body().moveX1Y1(0, y1 + 10);
			section.addRelBody(10, DIR.S, s);
		}
		
		if (sExtra.body().width() > 0)
			section.addRelBody(8, DIR.E, sExtra);
		
		if (b.employment() != null){
			GuiSection equip = new GuiSection();
			int i = 0;
			for (RoomEquip e : SETT.ROOMS().employment.equip.ALL){
				if (e.has(b.employment())) {
					
					GButt.BSection s = new GButt.BSection() {
						
						@Override
						protected void hoverInfoSelf(GUI_BOX box) {
							GBox bo = (GBox) box;
							
							bo.add(e.info);
							bo.NL(8);
							
							bo.textL(¤¤level);
							bo.add(GFORMAT.i(bo.text(), e.target(b.employment()).availableMax()));
							bo.NL();
							if (e.target(b.employment()).boost() != null) {
								e.target(b.employment()).boost().hoverDetailed(bo, POP_CL.clP(), null, true);
							}
							bo.sep();
							
							bo.textLL(Dic.¤¤Target);
							bo.tab(7);
							bo.add(GFORMAT.iofk(bo.text(), e.target(b.employment()).get(), e.target(b.employment()).max()));
							bo.NL();;
							
							bo.textLL(Dic.¤¤Target);
							bo.add(bo.text().para(Dic.¤¤Total));
							bo.tab(7);
							bo.add(GFORMAT.i(bo.text(), e.targetI(b.employment())));
							bo.NL();
							
							bo.textLL(Dic.¤¤Current);
							bo.tab(7);
							bo.add(GFORMAT.i(bo.text(), e.current(b.employment())));
							bo.NL();
							
							bo.textLL(Dic.¤¤Degrade);
							bo.add(bo.text().para(Dic.¤¤Total));
							bo.add(bo.text().para(DicTime.¤¤Day));
							bo.tab(7);
							bo.add(GFORMAT.f0(bo.text(), -e.targetI(b.employment())*e.degradePerDay));
							
							bo.sep();
							Booster bbb = e.boost(blueprint.employment()).booster;
							bbb.hover(bo, bbb.getValue(e.value(b.employment())));
							bbb.hoverSpan(bo, bbb.getValue(e.value(b.employment())));
							bo.NL();
						}
						
					};
					
					s.add(e.resource.icon(), 0, 0);
					
					s.addRelBody(8, DIR.E, new GAllocator(COLOR.ORANGE100.makeSaturated(0.7), e.target(b.employment()), 6, 16));
					s.pad(8, 4);
					equip.addGrid(s, i++, 3, 0, 0);
					
					
				}
				
			}
			if (equip.elements().size() > 0) {
				section.addRelBody(4, DIR.N, equip);
			}
		}

	}

	private GuiSection table(int y1, UIRoom gui, UIRoomModule... appliers) {
		GTableBuilder builder = new GTableBuilder() {

			@Override
			public int nrOFEntries() {
				return tableSort.size();
			}

			@Override
			public void hover(int index) {

			}

			@Override
			public void click(int index) {

			}

			@Override
			public boolean selectedIs(int index) {
				RoomInstance t = tableSort.get(index);
				return gui.detailIns() == t && VIEW.s().panels.added(gui.detail(t));
			}
		};

		// for (UIRoomModule m : appliers)
		// m.appendTableRow(builder);

		RENDEROBJ o = makeRow(gui, new GETTER_IMP<Integer>(), appliers);
		
		builder.column(o.body().width(), new GRowBuilder() {
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return makeRow(gui, ier, appliers);
			}
		}, DIR.NW);

		return builder.createHeight(HEIGHT - y1 - C.SG * 16, false);
	}

	private GuiSection makeRow(UIRoom gui, GETTER<Integer> ier, UIRoomModule... appliers) {
		
		GuiSection s = new GButt.BSection(100, 0) {
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (!isHoveringAHoverElement()) {
					RoomInstance t = tableSort.get(ier.get());
					gui.hover(VIEW.hoverBox(), t, t.mX(), t.mY());
				} else
					super.hoverInfoGet(text);
			}
			
			@Override
			public boolean click() {
				if (super.click())
					return true;
				RoomInstance t = tableSort.get(ier.get());
				ISidePanel d = gui.detail(t);
				VIEW.s().panels.add(d, false);
				VIEW.s().getWindow().centererTile.set(t.body().cX(), t.body().cY());
				oldC.set(VIEW.s().getWindow().pixels().cX(), VIEW.s().getWindow().pixels().cY());
				return true;
			}
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				selectOnlythis(VIEW.s().panels.added(VIEW.s().ui.rooms.rooms[blueprint.index()].detail) && VIEW.s().ui.rooms.rooms[blueprint.index()].detailIns() == tableSort.get(ier.get()));
				super.render(r, ds);
			}

		};



		final int mW = UI.FONT().S.height()*12;
		
		if (blueprint.upgrades().max() > 0) {
			SPRITE sp = new SPRITE.Imp(Icon.S+Icon.S, Icon.S) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					RoomInstance ins = get(ier);
					int up = ins.upgrade();
					ColorImp.TMP.interpolate(GCOLOR.T().H2, GCOLOR.T().H1, (double)up/blueprint.upgrades().max());
					ColorImp.TMP.bind();
					for (int i = 0; i <= up; i++) {
						int x = i / 2;
						int y = i % 2;
						SPRITES.icons().s.plus.render(r, X1+x*Icon.S/2, Y1+y*Icon.S/2);
					}
					COLOR.unbind();
				}
			};
			s.add(sp, 0, 0);
			s.addRightC(4, new GStat() {
				@Override
				public void update(GText text) {
					if (tableSort.currentSort() != null)
						tableSort.currentSort().format(get(ier), text);
					text.setMaxWidth(s.body().width()-24);
					text.setMultipleLines(false);
					text.lablify();
				}
			}.decrease());
		}else {
			s.add(new GStat() {
				@Override
				public void update(GText text) {
					if (tableSort.currentSort() != null)
						tableSort.currentSort().format(get(ier), text);
					text.setMaxWidth(s.body().width()-24);
					text.setMultipleLines(false);
					text.lablify();
				}
			}.decrease(), 0, 0);
		}
		
		s.body().incrW(mW);

		
		
		GuiSection pButts = new GuiSection();
		
//		if (blueprint.employment() != null) {
//			pButts.add(new GButt.Checkbox() {
//				@Override
//				protected void renAction() {
//					selectedSet(get(ier).active());
//				}
//	
//				@Override
//				protected void clickA() {
//					if (get(ier).active()) {
//						get(ier).deactivate();
//					} else {
//						get(ier).activate();
//					}
//				}
//			});
//		}
//		
//		pButts.addRightC(8, new GButt.Glow(SPRITES.icons().s.crossheir) {
//
//			@Override
//			public boolean hover(COORDINATE mCoo) {
//				if (super.hover(mCoo)) {
//					int index = ier.get();
//					if (index < 0)
//						return true;
//					RoomInstance t = tableSort.get(index);
//					if (!wasHovering) {
//						oldC.set(VIEW.s().getWindow().pixels().cX(), VIEW.s().getWindow().pixels().cY());
//						wasHovering = true;
//					}
//
//					hovered = t;
//					return true;
//				}
//				return false;
//			}
//		});
//
//		pButts.addRightC(4, new GButt.Glow(SPRITES.icons().s.cancel) {
//			ACTION a = new ACTION() {
//
//				@Override
//				public void exe() {
//					RoomInstance t = get(ier);
//					TmpArea a = get(ier).remove(t.mX(), t.mY(), true, this, false);
//					if (a != null)
//						a.clear();
//					tableSort.sortForced();
//				}
//			};
//
//			@Override
//			protected void clickA() {
//				dCount--;
//				if (dCount < 0) {
//					VIEW.inters().yesNo.activate(¤¤ReallyDelete, a, ACTION.NOP, true);
//					dCount = 10;
//				} else {
//					a.exe();
//				}
//
//			}
//		}.hoverInfoSet(¤¤DeleteRoom));

		GuiSection butts = new GuiSection();
		
		GETTER<RoomInstance> getter = new GETTER<RoomInstance>() {

			@Override
			public RoomInstance get() {
				return UIRoomTable.this.get(ier);
			}
			
		};
		
		for (UIRoomModule m : appliers)
			m.appendButt(butts, getter);
		
		butts.body().moveX1(0);
		pButts.body().moveY1(butts.body().y1());
		if (butts.body().width() + pButts.body().width() > s.body().width()) {
			pButts.body().moveX1(butts.body().x2()+16);
		}else {
			pButts.body().moveX2(s.body().width());
		}
		for (RENDEROBJ o : pButts.elements())
			butts.add(o);
		
		butts.body().centerY(s);
		butts.body().moveY1(s.body().y2()+2);
		
		for (RENDEROBJ o : butts.elements())
			s.add(o);
		
		s.pad(8, 8);

		return s;

	}

	protected RoomInstance get(GETTER<Integer> ier) {
		return tableSort.get(ier.get());
	}

}

class TableSorter extends GTableSorter<RoomInstance> {

	private RoomBlueprintIns<?> b;

	public TableSorter() {
		super(10000);
	}

	@Override
	protected RoomInstance getUnsorted(int index) {
		if (b == null)
			return null;
		if (index < b.all().size()) {
			return b.getInstance(index);
		}
		return null;
	}

	public void set(RoomBlueprintIns<?> b, GTFilter<RoomInstance> filterCurrent, GTSort<RoomInstance> sortCurrent) {
		if (this.b != b || filterCurrent != filter || sort != sortCurrent) {
			this.b = b;
			this.sort = sortCurrent;
			this.filter = filterCurrent;
			sortForced();
		}
	}

}
