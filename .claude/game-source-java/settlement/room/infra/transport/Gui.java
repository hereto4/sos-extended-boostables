


package settlement.room.infra.transport;

import static settlement.room.infra.logistics.MoveDic.¤¤fetch;
import static settlement.room.infra.logistics.MoveDic.¤¤fetchD;

import game.GAME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.infra.logistics.MoveDic;
import settlement.room.infra.logistics.MoveOrderPull;
import settlement.room.infra.logistics.MoveOrderPullUI;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentIns;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.common.UIPickerRes;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GGrid;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<TransportInstance, ROOM_TRANSPORT> {
	
	private static CharSequence ¤¤delivering = "Delivering";
	private static CharSequence ¤¤stored = "Loaded";
	private static CharSequence ¤¤loaded = "To be loaded";
	private static CharSequence ¤¤preparations = "Preparations:";
	
	static CharSequence ¤¤organise = "¤Loading Cart";
	static CharSequence ¤¤preparing = "¤Preparing Cart";

	private static CharSequence ¤¤prepared = "The cart is being prepared";
	private static CharSequence ¤¤loadedD = "The cart is being loaded";
	
	private static CharSequence ¤¤pNoDest = "There are no stations that can accept the selected resource!";
	private static CharSequence ¤¤pNoResource = "No resource has been set!";
	
	private static CharSequence ¤¤efficiency = "Efficiency";
	private static CharSequence ¤¤efficiencyD = "Efficiency is based on the number of workers. Each individual becomes more effective when they work together. Carry capacity also affects efficiency.";
	private static CharSequence ¤¤efficiencyD2 = "Current efficiency enables us to prepare {0} transports of {1} items per day, per worker. ({2} items / day per worker)";
	
	static {
		D.ts(Gui.class);
	}

	
	Gui(ROOM_TRANSPORT s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<TransportInstance> g, int x1, int y1) {

		{
			GuiSection s = new GuiSection();
			
			
			
			{
				
				GButt.ButtPanel p = new GButt.ButtPanel(UI.icons().m.wheel) {
					
					@Override
					protected void renAction() {
						selectedSet(g.get().fetching());
					}
					
					@Override
					protected void clickA() {
						g.get().fetchingSet(!g.get().fetching());
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {
						
						super.render(r, ds, isActive, isSelected, isHovered);
						if (g.get().fetching() && g.get().coolFetch > -1) {
							GCOLOR.UI().SOSO.hovered.bind();
							UI.icons().s.alert.render(r, body.x1()+6, body.y1()+6);
							COLOR.unbind();
						}
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(¤¤fetch);
						b.text(¤¤fetchD);
						b.NL();
						if (g.get().fetching() && g.get().coolFetch > -1) {
							b.add(b.text().warnify().add(MoveDic.¤¤fetchProblem));
						}
						super.hoverInfoGet(text);
					}
					
				};
				p.body.setDim(48);
				s.addRightC(0, p);
				
				p = new GButt.ButtPanel(UI.icons().m.priority) {
					
					@Override
					protected void renAction() {
						selectedSet(g.get().prio);
					}
					
					@Override
					protected void clickA() {
						g.get().prio = !g.get().prio;
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {
						
						super.render(r, ds, isActive, isSelected, isHovered);
						if (g.get().prio && g.get().coolFetch > -1) {
							GCOLOR.UI().SOSO.hovered.bind();
							UI.icons().s.alert.render(r, body.x1()+6, body.y1()+6);
							COLOR.unbind();
						}
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(MoveDic.¤¤prio);
						b.text(MoveDic.¤¤prioD);
						b.NL();
						if (g.get().prio && g.get().coolFetch > -1) {
							b.add(b.text().warnify().add(MoveDic.¤¤fetchProblem));
						}
						
					}
					
				};
				
				p.body.setDim(48);
				s.addRightC(0, p);
				
				

			
			}
			
			s.addRelBody(8, DIR.E, new MoveOrderPullUI(g, g, null, TransportInstance.ORDERS));
			
			GuiSection pop = new UIPickerRes(true) {
				
				@Override
				protected void select(RESOURCE r, int li) {
					g.get().data.resourceSet(r, g.get());
				}
				
				@Override
				protected RESOURCE getResource() {
					return g.get().data.resource();
				}
			};
			
			SPRITE sp = new SPRITE.Imp(Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					RESOURCE res = g.get().data.resource();
					if (res == null)
						UI.icons().m.cancel.render(r, X1, X2, Y1, Y2);
					else
						res.icon().render(r, X1, X2, Y1, Y2);
				}
			};
			
			s.addRelBody(8, DIR.S, new GHeader(Dic.¤¤Resource));
			
			GButt.ButtPanel b = new GButt.ButtPanel(sp) {
				
				@Override
				protected void clickA() {
					VIEW.inters().popup.show(pop, this, true);
				}
				
			};
			b.body.setDim(48);
			
			s.addRelBody(2, DIR.S, b);
			
			section.addRelBody(8, DIR.S, s);	
			
		}
		
		{
			GuiSection s = new GuiSection();
			
			s.addDown(0, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, g.get().efficiency()*bonus(g.get()));
					
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(¤¤efficiency);
					b.text(¤¤efficiencyD);
					b.NL();
					b.add(SETT.ROOMS().STOCKPILE.bonus().icon);
					b.textLL(SETT.ROOMS().STOCKPILE.bonus().name);
					b.tab(6);
					GText t = b.text();
					t.add('x').s();
					GFORMAT.f1(t, bonus(g.get()));
					b.add(t);
					b.NL(8);
					
					t = b.text();
					t.add(¤¤efficiencyD2);
					t.insert(0, g.get().efficiency()*bonus(g.get()), 2);
					t.insert(1, ROOM_TRANSPORT.MAX_LOAD);
					t.insert(2,  (int)(g.get().efficiency()*bonus(g.get())*ROOM_TRANSPORT.MAX_LOAD));
					b.add(t);
				};
				
			}.hh(¤¤efficiency, 200));
			
			s.addDown(16, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, g.get().data.prepD());
					
				}
			}.hh(¤¤preparations, 200));
			
			s.addDown(2, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, g.get().data.unloaded());
					
				}
			}.hh(¤¤loaded, 200));
			
			s.addDown(2, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.iofkInv(text, g.get().data.stored(), ROOM_TRANSPORT.MAX_LOAD);
					
				}
			}.hh(¤¤stored, 200));
			
			s.addDown(2, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, g.get().data.delivering());
					
				}
			}.hh(¤¤delivering, 200));
			
			if (S.get().developer) {
				s.addDown(8, new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, g.get().data.unloadedSpots());
						
					}
				}.hh(dCrate, 200));
			}

			s.addDown(16, new GStat() {
				
				@Override
				public void update(GText text) {
					if (g.get().data.prepD() >= 1) {
						text.add(¤¤prepared);
					}else {
						text.add(¤¤loadedD);
					}
					text.setMaxWidth(300);
					text.setMultipleLines(true);
				}
			});
			
			section.addRelBody(8, DIR.S, s);
		}
		
	}
	
	double bonusCache = 1;
	int bonusI = -10;
	
	private double bonus(TransportInstance i) {
		if (GAME.updateI() != bonusI) {
			bonusI = GAME.updateI();
			double b = 0;
			int am = 0;
			for (Humanoid a : RoomEmploymentIns.employees(i)) {
				am++;
				b += SETT.ROOMS().STOCKPILE.bonus().get(a.indu());
			}
			if (am > 0) {
				bonusCache = b/am;
				bonusCache /= SETT.ROOMS().STOCKPILE.bonus().baseValue;
			}else {
				bonusCache = 1;
			}
		}
		return bonusCache;
	}
	
	@Override
	protected void hover(GBox box, TransportInstance i) {
		super.hover(box, i);
		box.sep();
		if (i.resource() != null) {
			box.add(i.resource().icon());
			box.add(GFORMAT.iofkInv(box.text(), i.data.stored(), ROOM_TRANSPORT.MAX_LOAD));
		}
	}
	
	private final String dCrate = "crates to fetch to";
	

	@Override
	protected void problem(TransportInstance i, Stack<Str> free, LISTE<CharSequence> errors,
			LISTE<CharSequence> warnings) {
		if (i.employees().target() == 0)
			return;
		
		{

			boolean ok = false;
			boolean has = false;
			for (MoveOrderPull o : i.moveOrdersPull()) {
				if (o != null) {
					has = true;
					CharSequence p = o.problem(i);
					if (p != null) {
						errors.add(p);
						break;
					}else if (o.cooldown >= -1)
						ok = true;
				}
			}
			
			if (i.fetching() && i.coolFetch > -1 && (has && !ok)) {
				errors.add(MoveDic.¤¤pullProblem);
			}
		}

		if (i.resource() == null) {
			errors.add(¤¤pNoResource);
		}else if (SETT.ROOMS().STATION.tally(i.resource()).accepting() <= 0) {
			warnings.add(¤¤pNoDest);
		}
		
		super.problem(i, free, errors, warnings);
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		// TODO Auto-generated method stub
		super.appendTableFilters(filters, sorts, appliers);
		
		for (RESOURCE res : RESOURCES.ALL()) {
			filters.add(new GTFilter<RoomInstance>(res.names) {
				
				@Override
				public boolean passes(RoomInstance h) {
					TransportInstance i = (TransportInstance) h;
					if (i.data.resource() == res)
						return true;
					return false;
				}
			});
		}
	}
	
}
