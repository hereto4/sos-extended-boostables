package view.sett.ui.room;


import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.furnisher.Furnisher;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.keyboard.KEYS;
import view.main.VIEW;

public class UIRoom {

	final UIRoomTable table;
	final Detail detail;
	private final UIRoomModule[] appliers;
	final CLICKABLE clicker;
	
	private static CharSequence ¤¤delete = "¤Are you sure you want to delete this room?";
	private static CharSequence ¤¤ActivateDesc = "¤Turn on/off activity for this room.";
	private static CharSequence ¤¤Refurnish = "¤Refurnish Room. WARNING: All room progress will be lost and all employees fired.";
	
	static {
		D.ts(UIRoom.class);
	}
	
	@SafeVarargs
	public UIRoom(RoomBlueprint p, UIRoomModule... appliers) {
		this.appliers = appliers;
		if (p instanceof ROOM_ARTILLERY) {
			detail = null;
			table = null;
			clicker = null;
		}else if (p instanceof RoomBlueprintIns<?>) {
			detail = new Detail((RoomBlueprintIns<?>)p, appliers);
			table = new UIRoomTable((RoomBlueprintIns<?>)p, this, appliers);
			clicker = makeClicker((RoomBlueprintIns<?>)p, appliers);
		}else if(p instanceof RoomBlueprintImp) {
			detail = null;
			table = null;
			clicker = makeClicker((RoomBlueprintImp)p, appliers);
		}else {
			detail = null;
			table = null;
			clicker = null;
		}
		
		
	}
	
	public boolean can() {
		return detail != null;
	}
	
	public ISidePanel detail(RoomInstance ins) {
		detail.room = ins;
		detail.titleSet(ins.blueprintI().info.name);
		detail.panelTitle.text().clear().add(ins.name());
		return detail;
	}
	

	
	public RoomInstance detailIns() {
		return detail.room;
	}
	
	private static final ArrayList<CharSequence> errors = new ArrayList<>(128);
	private static final ArrayList<CharSequence> warnings = new ArrayList<>(128);
	private static final Stack<Str> free = new Stack<>(128);
	private static final ArrayList<Str> tmps = new ArrayList<Str>(128);
	static {
		while(tmps.hasRoom())
			tmps.add(new Str(64));
	}
	private static void errorPrep() {
		errors.clearSloppy();
		warnings.clearSloppy();
		free.clear();
		for (Str s : tmps) {
			s.clear();
			free.push(s);
		}
	}
	
	void hover(GBox box, Room i, int rx, int ry) {
		
		if (i.icon() == null)
			throw new RuntimeException(""+i);
		
		box.add(i.icon());
		box.add(box.text().add(i.name(rx, ry)));
		
		box.NL(2);
		
		for (UIRoomModule a : appliers) {
			a.hover(box, i, rx, ry);
		}
		
		box.NL(2);
		
		errorPrep();
		for (UIRoomModule a : appliers) {
			a.problem(free, errors, warnings, i, rx, ry);
		}
		
		
		for (CharSequence e : errors) {
			box.add(UI.icons().s.flag, GCOLOR.MAP().BAD);
			box.error(e);
			box.NL();
		}
		for (CharSequence e : warnings) {
			box.add(UI.icons().s.alert, GCOLOR.MAP().SOSO);
			box.error(e);
			box.NL();
		}
	}
	
	boolean problem(Room i, int rx, int ry) {
		errorPrep();
		for (UIRoomModule a : appliers) {
			a.problem(free, errors, warnings, i, rx, ry);
		}
		
		return errors.size() > 0;
	}
	
	boolean warning(Room i, int rx, int ry) {
		errorPrep();
		for (UIRoomModule a : appliers) {
			a.problem(free, errors, warnings, i, rx, ry);
		}
		
		return warnings.size() > 0;
	}
	
	CLICKABLE makeClicker(RoomBlueprintImp blueprint, UIRoomModule... appliers) {
		
		return new Row(blueprint);

	}
	
	private class Row extends UIPanelUtil.RoomRow {
		
		Row(RoomBlueprintImp p){
			super(p);
			body().setWidth(380);
			if (p instanceof RoomBlueprintIns<?>) {
				RoomBlueprintIns<?> pp = (RoomBlueprintIns<?>) p;
				addRightCAbs(200, new GStat() {
					@Override
					public void update(GText text) {
						GFORMAT.i(text, pp.instancesSize());
					}
				});
			if (pp.employmentExtra() != null)
				addRightCAbs(58, new GStat() {
					@Override
					public void update(GText text) {
						GFORMAT.iofkInv(text, pp.employmentExtra().target.get(), pp.employment().neededWorkers());
					}
				});
				
			}
			pad(2, 4);
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			if (p instanceof RoomBlueprintIns<?> &&  ((RoomBlueprintIns<?>)p).instancesSize() == 0){
				OPACITY.O25.bind();
				COLOR.BLACK.render(r, body(), -2);
				OPACITY.unbind();
			}
		}
		
		@Override
		protected void clickA() {
			if (hovered() == null || !(hovered() instanceof CLICKABLE))
				VIEW.s().panels.addDontRemove(VIEW.s().ui.rooms.main(), table.get());
		}
		
		@Override
		protected void renAction() {
			selectedSet(VIEW.s().panels.added(table));
		}
		

		
	}
	
	class Detail extends ISidePanel{

		private RoomInstance room;
		private final int detailWidth = 320;
		
		private final StringInputSprite panelTitle = new StringInputSprite(24, UI.FONT().S) {
			@Override
			protected void change() {
				room.name().clear().add(text());
			};
		};
		
		Detail(RoomBlueprintIns<?> p, UIRoomModule... appliers) {
			
			GETTER<RoomInstance> gg = new GETTER<RoomInstance>() {

				@Override
				public RoomInstance get() {
					return room;
				}
				
			};
			
			section = new GuiSection() {
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					if (!room.exists()) {
						last().remove(Detail.this);
						return;
					}
					if (room.blueprintI() != p) {
						last().remove(Detail.this);
						return;
					}
						
					SETT.OVERLAY().add(room.mX(), room.mY());
					super.render(r, ds);
				}
				
			};
			
			section.body().setWidth(detailWidth);
			
			
			
			{
				GuiSection section = new GuiSection() {
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						super.render(r, ds);
						GCOLOR.UI().border().render(r, Detail.this.section().body().x1()+8, Detail.this.section().body().x2()-8, body().y2()-1, body().y2());
					}
				};
				
				CLICKABLE wiki = new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
					@Override
					protected void clickA() {
						if (p.info.wiki != null)
							p.info.wiki.exe();;
					}
					
					@Override
					protected void renAction() {
						activeSet(p.info.wiki != null);
					};
					
				}.hoverInfoSet(Dic.¤¤Encyclopedia);
				section.add(wiki);
				
				CLICKABLE list = new GButt.ButtPanel(SPRITES.icons().m.menu) {
					@Override
					protected void clickA() {
						last().addDontRemove(VIEW.s().ui.rooms.main(), table.get());
						last().add(Detail.this, false);
					}
					
					@Override
					protected void renAction() {
						selectedSet(last().added(table.get()));
					};
				}.hoverInfoSet(Dic.¤¤List);
				section.addRightC(0, list);
				if (p.employment() != null || p == SETT.ROOMS().DUMP) {
					
					RENDEROBJ b = new GButt.ButtPanel(SPRITES.icons().m.lock) {
						@Override
						protected void clickA() {
							room.activate(!room.active());
						}
						@Override
						protected void renAction() {
							selectedSet(room.active());
						}
					}.hoverInfoSet(¤¤ActivateDesc);
					section.addRightC(0, b);
				}
				
				CLICKABLE expand = new GButt.ButtPanel(SPRITES.icons().m.building) {
					
					@Override
					protected void renAction() {
						visableSet(room.constructor() != null && room.constructor().usesArea());
					};
					
					@Override
					protected void clickA() {
						if (room.constructor() != null && room.constructor().usesArea()) {
							VIEW.s().misc.reconstruct(room);
						}
					}
				}.hoverInfoSet(¤¤Refurnish);
				section.addRightC(C.SG*4, expand);
				
				CLICKABLE delete = new GButt.ButtPanel(SPRITES.icons().m.trash) {
					@Override
					protected void clickA() {
						if (room.exists()) {
						
							if (room.area() == 1) {
								TmpArea a = room.remove(room.mX(), room.mY(), true, this, false);
								if (a != null)
									a.clear();
								return;
							}
							VIEW.inters().yesNo.activate(¤¤delete, 
									new ACTION() {
										
										@Override
										public void exe() {
											TmpArea a = room.remove(room.mX(), room.mY(), true, this, false);
											if (a != null)
												a.clear();
										}
									}, new ACTION() {
										
										@Override
										public void exe() {
											
											
										}
									}, true);
						}
					}
				}.hoverInfoSet(Dic.¤¤delete);
				section.addRightC(C.SG*4, delete);
				
				RENDEROBJ s = new GStat() {
					
					@Override
					public void update(GText text) {
						text.add(room.area());
						text.normalify2();
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(Dic.¤¤Area);
						
						if (room.constructor() != null) {
							b.textLL(Dic.¤¤Resources);
							b.NL(2);
							Furnisher f = room.constructor();
							for (int i = 0; i < f.resources(); i++) {
								b.add(f.resource(i).icon());
								b.textL(f.resource(i).name);
								b.tab(6);
								b.add(GFORMAT.i(b.text(), room.resAmount(i, room.upgrade())));
								b.NL();
							}
						}
					};
					
				}.hh(SPRITES.icons().m.expand).hoverInfoSet(Dic.¤¤Area);
				section.add(s, 0, section.body().y2()+4);
				
				if (p.degrades()) {
					LinkedList<RENDEROBJ> ss = new LinkedList<RENDEROBJ>();
					for (UIRoomModule a : appliers) {
						a.appendPanelIcon(ss, gg);
					}
					for (RENDEROBJ rr : ss) {
						section.addRightCAbs(96, rr);
					}
				}
				
				
				{
					HoverableAbs h = new HoverableAbs(Icon.HUGE) {
						
						@Override
						protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
							p.icon.huge.render(r, body);
							
							
							
							errorPrep();
							for (UIRoomModule a : appliers) {
								a.problem(free, errors, warnings, room, room.mX(), room.mY());
							}
							if (errors.isEmpty() && warnings.isEmpty()) {
								;
							}else {
								OPACITY.O25TO100.bind();
								if (warnings.isEmpty())
									GCOLOR.MAP().SOSO.bind();
								else
									GCOLOR.MAP().BAD.bind();
								SPRITES.icons().m.flag.render(r, body.x1(), body.x1()+Icon.M*2, body.y1(), body.y1()+Icon.M*2);
								OPACITY.unbind();
							}
							
							COLOR.unbind();
						}
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GBox box = (GBox) text;
							errorPrep();
							for (UIRoomModule a : appliers) {
								a.problem(free, errors, warnings, room, room.mX(), room.mY());
							}
							
							for (CharSequence e : errors) {
								box.add(UI.icons().s.flag, GCOLOR.MAP().BAD);
								box.error(e);
								box.NL();
							}
							for (CharSequence e : warnings) {
								box.add(UI.icons().s.alert, GCOLOR.MAP().SOSO);
								box.add(box.text().warnify().add(e));
								box.NL();
							}
						}
					};
					
					section.addRelBody(4, DIR.W, h);
				}
				
				
				
				GButt.ButtPanel b = new GButt.ButtPanel(SPRITES.icons().s.arrow_left) {
					
					@Override
					protected void clickA() {
						RoomInstance prev = room.blueprintI().getInstance(room.blueprintI().instancesSize()-1);
						for (int i = 0; i < room.blueprintI().instancesSize(); i++) {
							RoomInstance ins = room.blueprintI().getInstance(i);
							if (ins == room) {
								detail(prev);
								VIEW.s().getWindow().centererTile.set(room.body().cX(), room.body().cY());
								break;
							}
							prev = ins;
						}
					}
					
					@Override
					protected void renAction() {
						if (KEYS.MAIN().SHRINK.consumeClick()) {
							clickA();
						}
						super.renAction();
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(Dic.¤¤Previous);
						text.text(KEYS.MAIN().SHRINK.repr());
					}
					
				};
				section.addRelBody(4, DIR.W, b);
				b = new GButt.ButtPanel(SPRITES.icons().s.arrow_right) {
					
					@Override
					protected void clickA() {
						for (int i = 0; i < room.blueprintI().instancesSize(); i++) {
							RoomInstance ins = room.blueprintI().getInstance(i);
							if (ins == room) {
								room = room.blueprintI().getInstance((i+1)%room.blueprintI().instancesSize());
								detail(room);
								VIEW.s().getWindow().centererTile.set(room.body().cX(), room.body().cY());
								break;
							}
						}
					}
					
					@Override
					protected void renAction() {
						if (KEYS.MAIN().GROW.consumeClick()) {
							clickA();
						}
						super.renAction();
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(Dic.¤¤Next);
						text.text(KEYS.MAIN().GROW.repr());
					}
					
				};
				section.addRelBody(4, DIR.E, b);
				section.addRelBody(2, DIR.N, new GInput(panelTitle));
				
				section.body().moveCX(this.section.body().cX());
				section.body().moveY1(this.section.body().y1());
				section.body().incrH(8);
				this.section.add(section);
			}
			
			
			

			
			for (UIRoomModule a : appliers) {
				a.appendPanel(section, gg, 0, section.getLastY2()+10*C.SG);
			}
			
			
			
			
			
		}
		
	}

	
	
	
}
