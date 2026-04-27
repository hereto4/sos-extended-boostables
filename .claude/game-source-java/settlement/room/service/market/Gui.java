package settlement.room.service.market;

import init.race.RACES;
import init.race.Race;
import init.race.RaceResources.RaceResource;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.stats.STATS;
import settlement.stats.equip.EquipCivic;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<MarketInstance, ROOM_MARKET> {
	
	private static CharSequence ¤¤Food = "¤Wares";
	private static CharSequence ¤¤Amount = "¤Amount";
	private static CharSequence ¤¤Incoming = "¤Incoming";
	
	private static CharSequence ¤¤uses = "¤Some or all markets are selling this resource. Click to disable this resource for all.";
	private static CharSequence ¤¤usesN = "¤No markets are selling this resource. Click to enable it for all";
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_MARKET s) {
		super(s);
	}

	
	
	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<MarketInstance> g, int x1, int y1) {
		
		GuiSection s = new GuiSection();
		int i = 0;
		for (RaceResource e : RACES.res().ALL) {
			
			GButt.BSection ss = new GButt.BSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.res.name);
					b.textLL(¤¤Amount).add(GFORMAT.iofkInv(b.text(), g.get().amount(e), g.get().maxAmount));
					b.NL();
					b.textLL(¤¤Incoming).add(GFORMAT.i(b.text(), g.get().jobReserved(e)));
					b.NL();
					
					b.sep();
					
					for (EquipCivic ee : STATS.EQUIP().civics()) {
						if (ee.resource == e.res) {
							b.textL(Dic.¤¤Equipped);
							b.NL();
							for (Race r : RACES.all()) {
								for (HCLASS cl : HCLASSES.ALL()) {
									if (ee.target(cl, r) > 0 && STATS.POP().POP.data(cl).get(r) > 0) {
										b.add(r.appearance().icon);
										b.rewind(8);
										if (cl.iconSmall() != null)
											b.add(cl.iconSmall());
										
									}
								}
								
							}
							b.NL();
						}
					}
					
					
					b.textL(STATS.HOME().materials.info().name);
					b.NL();
					for (Race r : RACES.all()) {
						for (HCLASS cl : HCLASSES.ALL()) {
							if (r.home().clas(cl).amount(e.res) > 0 && STATS.POP().POP.data(cl).get(r) > 0) {
								b.add(r.appearance().icon);
								b.rewind(8);
								b.add(cl.iconSmall());
							}
						}
					}
					
				}
				
				@Override
				protected void renAction() {
					selectedSet(g.get().uses(e));
				}
				
				@Override
				protected void clickA() {
					g.get().usesToggle(e);
				}
			};
			
			
			ss.addRightC(4, e.res.icon());
			
			ss.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, g.get().amount(e));
				}
				
			});
			
			ss.body().incrW(48);
			ss.pad(4);
			
			s.add(ss, (i%3)*ss.body().width(), (i/3)*ss.body().height());
			i++;
			
		}
		
		s.addRelBody(2, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, g.get().amountTotal());
				
			}
		}.hh(¤¤Food));
		
		section.addRelBody(8, DIR.S, s);
		
	}
	
	@Override
	protected void hover(GBox b, MarketInstance i) {
		b.NL();
		b.textLL(¤¤Food).add(GFORMAT.i(b.text(), i.amountTotal()));	
		b.NL();
	}


	
	@Override
	protected void appendMain(GGrid gg, GGrid text, GuiSection sExtra) {
		GuiSection s = new GuiSection();
		int i = 0;
		int m = 5;
		
		
		
		for (RaceResource e : RACES.res().ALL) {
			
			SPRITE sp = new SPRITE.Imp(70, 24) {
				
				GText t = new GText(UI.FONT().S, 6);
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					t.clear();
					e.res.icon().render(r, X1, Y1);
					GFORMAT.i(t, blueprint.amount(e.res));
					t.renderCY(r, X1+26, Y1+(Y2-Y1)/2);
				}
			};
			
			
			RENDEROBJ r = new GButt.ButtPanel(sp) {
				@Override
				protected void clickA() {
					if (blueprint.uses(e)) {
						for (int i = 0; i < blueprint.instancesSize(); i++) {
							MarketInstance ii = blueprint.getInstance(i);
							if (ii.uses(e)) {
								ii.usesToggle(e);
							}
						}
					}else {
						for (int i = 0; i < blueprint.instancesSize(); i++) {
							MarketInstance ii = blueprint.getInstance(i);
							if (!ii.uses(e)) {
								ii.usesToggle(e);
							}
						}
					}
					super.clickA();
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.res.name);
					if (blueprint.uses(e)) {
						b.text(¤¤uses);
					}else {
						b.text(¤¤usesN);
					}
					b.NL();
					
					b.textLL(Dic.¤¤Stored).add(GFORMAT.i(b.text(), blueprint.amount(e.res)));
					b.NL();

					b.sep();
					
					for (EquipCivic ee : STATS.EQUIP().civics()) {
						if (ee.resource == e.res) {
							b.textL(Dic.¤¤Equipped);
							b.NL();
							for (Race r : RACES.all()) {
								for (HCLASS cl : HCLASSES.ALL()) {
									if (ee.target(cl, r) > 0 && STATS.POP().POP.data(cl).get(r) > 0) {
										b.add(r.appearance().icon);
										b.rewind(8);
										if (cl.iconSmall() != null)
											b.add(cl.iconSmall());
									}
								}
								
							}
							b.NL();
						}
					}
					
					
					b.textL(STATS.HOME().materials.info().name);
					b.NL();
					for (Race r : RACES.all()) {
						for (HCLASS cl : HCLASSES.ALL()) {
							if (r.home().clas(cl).amount(e.res) > 0 && STATS.POP().POP.data(cl).get(r) > 0) {
								b.add(r.appearance().icon);
								b.rewind(8);
								if (cl.iconSmall() != null)
									b.add(cl.iconSmall());
							}
						}
					}
					
				}
				
				@Override
				protected void renAction() {
					selectedSet(blueprint.uses(e));
				}
				
			};
			
			
			s.add(r, (i%m)*r.body().width(), (i/m)*r.body().height());
			i++;
			
		}
		
		s.add(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.total);
				
			}
		}.hh(¤¤Food), 0, s.body().y1()-16);
		
		text.add(s);

		
	}
	
	

}
