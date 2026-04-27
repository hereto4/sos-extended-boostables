package settlement.room.service.food.eatery;

import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.resources.ResGEat;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GChart;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.DicTime;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<EateryInstance, ROOM_EATERY> {
	
	private static CharSequence ¤¤Food = "¤Food";
	private static CharSequence ¤¤Amount = "¤Amount";
	private static CharSequence ¤¤Incoming = "¤Incoming";
	private static CharSequence ¤¤Consumed = "¤Consumed";
	private static CharSequence ¤¤uses = "¤Some or all stands are distributing this food. Click to disable this resource for all.";
	private static CharSequence ¤¤usesN = "¤No stands are distributing this food. Click to enable it for all";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_EATERY s) {
		super(s);
		D.t(this);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<EateryInstance> g, int x1, int y1) {
		
		GuiSection s = new GuiSection();
		int i = 0;
		for (ResG e : RESOURCES.EDI().all()) {
			
			GButt.BSection ss = new GButt.BSection() {
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(e.resource.name);
					b.textLL(¤¤Amount).add(GFORMAT.iofkInv(b.text(), g.get().amount(e), g.get().maxAmount));
					b.NL();
					b.textLL(¤¤Incoming).add(GFORMAT.i(b.text(), g.get().jobReserved(e)));
					b.NL();
					b.textLL(¤¤Consumed + "(" + DicTime.¤¤Year + ")").add(GFORMAT.i(b.text(), (int) -blueprint.industry().ins().get(e.index()).year.get(g.get())));
					b.NL();
					
					b.sep();
					b.textLL(STATS.FOOD().FOOD_PREFFERENCE.info().name);
					b.NL();
					for (Race r : RACES.all()) {
						if (r.pref().foodMask.has(e.resource))
							b.add(r.appearance().icon);
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
			
			
			ss.addRightC(4, e.resource.icon());
			
			ss.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, g.get().amount(e));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(e.resource.name);
					b.textLL(¤¤Food).add(GFORMAT.i(b.text(), g.get().amount(e)));					
				};
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
	protected void hover(GBox b, EateryInstance ins) {
		b.NL();
		b.textLL(¤¤Food).add(GFORMAT.i(b.text(), ins.amountTotal()));	
		b.NL();
		for (int i = 0; i < RESOURCES.EDI().all().size(); i++) {
			ResG r = RESOURCES.EDI().all().get(i);
			b.add(r.resource.icon());
			GText t = b.text();
			GFORMAT.i(t, ins.amount(r));
			if (!ins.uses(r))
				t.errorify();
			b.add(t);
			b.space();
			if (i % 6 == 5) {
				b.NL();
			}
				
		}
		
		
		b.NL();
	}

	@Override
	protected void appendMain(GGrid gg, GGrid text, GuiSection sExtra) {
		GuiSection s = new GuiSection();
		GChart cc = new GChart();
		
		int i = 0;
		int m = 5;
		
		
		
		for (ResGEat e : RESOURCES.EDI().all()) {
			
			SPRITE sp = new SPRITE.Imp(70, 24) {
				
				GText t = new GText(UI.FONT().S, 6);
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					e.resource.icon().render(r, X1, Y1);
					t.clear();
					GFORMAT.i(t, blueprint.amount(e));
					t.renderCY(r, X1+26, Y1+(Y2-Y1)/2);
				}
			};
			
			RENDEROBJ r = new GButt.ButtPanel(sp) {
				@Override
				protected void clickA() {
					if (blueprint.uses(e)) {
						for (int i = 0; i < blueprint.instancesSize(); i++) {
							EateryInstance ii = blueprint.getInstance(i);
							if (ii.uses(e)) {
								ii.usesToggle(e);
							}
						}
					}else {
						for (int i = 0; i < blueprint.instancesSize(); i++) {
							EateryInstance ii = blueprint.getInstance(i);
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
					b.title(e.resource.name);
					if (blueprint.uses(e)) {
						b.text(¤¤uses);
					}else {
						b.text(¤¤usesN);
					}
					b.NL();
					
					b.title(e.resource.name);
					b.textLL(¤¤Amount).add(GFORMAT.i(b.text(), blueprint.amount(e)));
					b.NL(8);
					b.textLL(¤¤Consumed).add(GFORMAT.i(b.text(), (int) -blueprint.industry().ins().get(e.index()).history().get()));
					b.NL();
					cc.clear();
					cc.add(blueprint.industry().ins().get(e.index()).history());
					b.add(cc);
					
					b.sep();
					b.textLL(STATS.FOOD().FOOD_PREFFERENCE.info().name);
					b.NL();
					for (Race r : RACES.all()) {
						if (r.pref().foodMask.has(e.resource))
							b.add(r.appearance().icon);
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
