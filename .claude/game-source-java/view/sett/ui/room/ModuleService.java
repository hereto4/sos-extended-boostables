package view.sett.ui.room;

import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomInstance;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.ROOM_SPECTATOR.ROOM_SPECTATOR_HASER;
import settlement.room.service.module.RoomService.ROOM_SERVICE_HASER;
import settlement.room.service.module.RoomServiceAccess;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.data.DOUBLE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GGrid;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.text.D;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleService implements ModuleMaker {

	private static CharSequence ¤¤NO = "¤No Available Services";
	private static CharSequence ¤¤AVAILABLE = "¤Available Services";
	private static CharSequence ¤¤USED = "¤Currently Used";
	private static CharSequence ¤¤NEEDS = "¤Needs Work";
	private static CharSequence ¤¤TOTAL = "¤Total";
	private static CharSequence ¤¤QUALITY = "¤Quality";
	private static CharSequence ¤¤ACCESS = "¤The overall access of your minions";
	private static CharSequence ¤¤USAGE = "¤Service";
	private static CharSequence ¤¤Load = "¤Load";
	private static CharSequence ¤¤Capacity = "¤Capacity";
	private static CharSequence ¤¤CapacityD = "¤An rough estimate of how many subjects that can be served.";
	
	
	private static CharSequence ¤¤USAGE_DESC = "¤The highest load of this service during a day. Once full, it means there aren't enough services to meet your subjects demands. If low, it's an indication you can cut down on this service.";
	private static CharSequence ¤¤RADIUS = "¤Radius";
	private static CharSequence ¤¤RADIUSD = "¤All services operate within a radius. The radius is the max distance a subject is prepared to walk to get to a service.";
	
	static {
		D.ts(ModuleService.class);
	}
	
	
	public ModuleService(Init init) {
		
	}
	
	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		if (p instanceof ROOM_SERVICE_HASER)
			l.add(new I((ROOM_SERVICE_HASER) p));
	}

	
	private final class I extends UIRoomModule {
		
		private final ROOM_SERVICE_HASER p;
		
		I(ROOM_SERVICE_HASER p){
			this.p = p;
		}
		
		@Override
		public void appendManageScr(GGrid grid, GGrid text, GuiSection sExta) {
			
			if (p instanceof ROOM_SPECTATOR_HASER) {
				
			}else {
				
				SPRITE s = new SPRITE.Imp(58, 14) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						double d = 1.0-p.service().load();
						GMeter.render(r, GMeter.C_REDGREEN, d, X1,X2,Y1,Y2);
					}
				};
				
				RENDEROBJ h = new GHeader.HeaderHorizontal(SPRITES.icons().s.citizen, s) {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(¤¤USAGE);
						
						b.textLL(¤¤Load);
						b.tab(6);
						b.add(GFORMAT.percInv(b.text(), p.service().load()));
						b.NL();
						b.text(¤¤USAGE_DESC);
						b.NL(8);
						
						b.textL(¤¤AVAILABLE);
						b.add(GFORMAT.i(b.text(), p.service().available()));
						b.NL();
						b.textL(¤¤TOTAL);
						b.add(GFORMAT.i(b.text(), p.service().total()));
						
						b.NL(8);
						b.textLL(¤¤Capacity);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), (int) (p.service().total()*p.service().totalMultiplier())));
						b.NL();
						b.text(¤¤CapacityD);
						
						b.NL(8);
						b.textLL(¤¤RADIUS);
						b.add(GFORMAT.i(b.text(), p.service().radius));
						b.NL();
						b.text(¤¤RADIUSD);
					}
					
				};
				
				grid.add(h);
			}
			
			
			
			if (p instanceof ROOM_SERVICE_ACCESS_HASER) {
				
				grid.add(new GStat() {
					@Override
					public void update(GText text) {
						RoomServiceAccess a = ((ROOM_SERVICE_ACCESS_HASER)p).service();
						GFORMAT.perc(text, a.cityAccess());
					}
				}.hh(SPRITES.icons().s.arrowUp).hoverInfoSet(¤¤ACCESS));
			}
			
		}

		@Override
		public void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
				LISTE<UIRoomBulkApplier> appliers) {

		
			
		}

		@Override
		public void appendButt(GuiSection s, GETTER<RoomInstance> ins) {
			
			DOUBLE d = new DOUBLE() {
				
				@Override
				public double getD() {
					return 1.0-((ROOM_SERVICER) ins.get()).service().load();
				}
			};
			
			s.addRelBody(16, DIR.E, SPRITES.icons().s.human);
			
			s.addRightC(2, new GMeter.GMeterSprite(GMeter.C_REDGREEN, d, 48, 12));
			
			
		}

		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			
			ROOM_SERVICER i = (ROOM_SERVICER)room;
			box.textL(¤¤AVAILABLE).add(GFORMAT.iofkInv(box.text(), i.service().available(), i.service().total()));
			box.NL();
			if (p instanceof ROOM_SPECTATOR_HASER)
				return;
			box.textL(¤¤Load);
			box.add(GFORMAT.percInv(box.text(), i.service().load()));
		}
		
		@Override
		public void problem(Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings, Room room, int rx, int ry) {
			if (((ROOM_SERVICER)room).service().available() == 0)
				errors.add(¤¤NO);
		}

		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			
//			if (p instanceof ROOM_SPECTATOR_HASER) {
//				section.addRelBody(8, DIR.S, new GStat() {
//					
//					@Override
//					public void update(GText text) {
//						RoomServiceInstance i = g(get).service();
//						GFORMAT.iofk(text, i.available(), i.total());
//						SETT.OVERLAY().service((ROOM_SERVICE_ACCESS_HASER)(get.get().blueprintI()));
//						
//					}
//				}.hh(¤¤USAGE));
//				return;
//			}
			
			SPRITE s = new SPRITE.Imp(48, 16) {

				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					if (get.get().blueprintI() instanceof ROOM_SERVICE_HASER) {
						SETT.OVERLAY().service((ROOM_SERVICE_HASER)(get.get().blueprintI()));
					}
					
					double d = 1.0-g(get).service().load();
					GMeter.render(r, GMeter.C_REDGREEN, d, X1, X2, Y1, Y2);
				}
			};
			
			RENDEROBJ r = new GHeader.HeaderHorizontal(¤¤USAGE, s) {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					RoomServiceInstance i = g(get).service();
					GBox b = (GBox) text;
					
					b.textLL(¤¤Load);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), g(get).service().load()));
					b.NL();
					text.text(¤¤USAGE_DESC);
					text.NL(8);
					
					
					b.textL(¤¤AVAILABLE);
					b.tab(6);
					text.add(GFORMAT.i(b.text(), i.available()));
					text.NL();
					b.textL(¤¤USED);
					b.tab(6);
					text.add(GFORMAT.i(b.text(), i.total()-i.available()));
					text.NL();
					
					if (get.get() instanceof RoomInstance && ((RoomInstance)get.get()).blueprintI().employment() != null) {
						b.textL(¤¤NEEDS);
						b.tab(6);
						text.add(GFORMAT.i(b.text(), i.total()-(i.available()-i.reserved())));
						text.NL();
					}
					
					b.textL(¤¤TOTAL);
					b.tab(6);
					text.add(GFORMAT.i(b.text(), i.total()));
					b.NL(8);
					
					b.textL(¤¤QUALITY);
					b.tab(6);
					text.add(GFORMAT.perc(b.text(), g(get).quality()));
					b.NL(8);
					
					b.textLL(¤¤Capacity);
					b.tab(6);
					text.add(GFORMAT.i(b.text(), (int)(i.total()*p.service().totalMultiplier())));
					b.NL();
					b.text(¤¤CapacityD);
					
					b.NL(8);
					b.textLL(¤¤RADIUS);
					b.add(GFORMAT.i(b.text(), p.service().radius));
					b.NL();
					b.text(¤¤RADIUSD);
				
				}
			};
			
			section.addRelBody(8, DIR.S, r);
			
		}

		private ROOM_SERVICER g(GETTER<RoomInstance> g) {
			return (ROOM_SERVICER) g.get();
		}



		
	}
	
	

	
}
