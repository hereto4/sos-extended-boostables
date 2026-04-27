package settlement.room.service.nursery;

import game.time.TIME;
import init.sprite.UI.UI;
import init.type.HTYPES;
import settlement.entity.ENTETIES;
import settlement.room.industry.module.IndustryUtil;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GGrid;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<NurseryInstance, ROOM_NURSERY> {

	private static CharSequence ¤¤next = "Next child: (days)";
	private static CharSequence ¤¤capabilitityG = "Children (Max)";
	private static CharSequence ¤¤capabilitityGD = "Your global amount of infants and children and the amount your city can support at any given time. The support is based on the amount of free citizens of this species that reside in your city.";
	private static CharSequence ¤¤capabilitity = "Capacity (Local)";

	private static CharSequence ¤¤problemLimitG = "Your current child and infant population exceeds the global limit that is set. No new breeding can take place.";
	private static CharSequence ¤¤problemMax = "Your current child and infant population exceeds the global capacity. No new breeding can take place.";
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_NURSERY s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<NurseryInstance> getter, int x1, int y1) {
		
	
		GuiSection s = new GuiSection();
		int M = 240;
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, blueprint.babies+STATS.POP().pop(blueprint.race, HTYPES.CHILD()), blueprint.rmax());
			}
		}.hh(¤¤capabilitityG, ¤¤capabilitityGD, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, getter.get().getWork().size());
			}
		}.hh(¤¤capabilitity, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, getter.get().kidspotsUsed);
			}
		}.hh(HTYPES.CHILD().names, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, getter.get().infantspotsUsed);
			}
		}.hh(Dic.¤¤Babies, M));
		grid.NL();
		
		s.addDown(4, new GStat() {
			
			@Override
			public void update(GText text) {
				double min = 0;
				for (COORDINATE c : getter.get().body()) {
					if (getter.get().is(c) && blueprint.ss.init(c.x(), c.y())) {
						if (blueprint.ss.age.get() > min) {
							min = blueprint.ss.age.get();
						}
					}
				}
				if (min > 0)
					GFORMAT.i(text, (int)Math.ceil((blueprint.BABY_DAYS-min)/IndustryUtil.roomBonus(getter.get(), blueprint.productionData)));
				else
					text.normalify().add('-').add('-');
			}
		}.hh(¤¤next, M));
		
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return getter.get().getWork().size();
			}
			
			@Override
			public int get() {
				return getter.get().kidsAllowed;
			}
			
			@Override
			public void set(int t) {
				getter.get().kidsAllowed = (short) t;
			}
		};
		
		GSliderInt b = new GSliderInt(in, 164, true);
		b.addRelBody(8, DIR.W, new GHeader(Dic.¤¤limit, UI.FONT().S));
		
		s.addDown(4, b);
		
		
		s.body().incrW(64);
		
		section.addRelBody(8, DIR.S, s);
		

		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return GList.get(getter.get()).size();
			}
		};
		
		bu.column("", 32, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new RENDEROBJ.RenderImp(24, 24) {

					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						GList.get(getter.get()).get(ier.get()).icon.renderC(r, body);
					}
				};
			}
		}, DIR.C);
		
		bu.column(Dic.¤¤name, 200, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						text.lablify().add(GList.get(getter.get()).get(ier.get()).name);
						text.setMaxWidth(180);
						text.setMultipleLines(false);
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column(DicTime.¤¤Days, 60, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, GList.get(getter.get()).get(ier.get()).age);
					}
				}.r(DIR.NW);
			}
		});
		
		section.addRelBody(8, DIR.S, bu.create(8, false));
		
	}
	
	@Override
	protected void appendMain(GGrid icons, GGrid r, GuiSection sExtra) {
		
		GuiSection s = new GuiSection();
		int M = 280;
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.rmax());
			}
		}.hh(¤¤capabilitityG, ¤¤capabilitityGD, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.kidSpotsTotal);
			}
		}.hh(Dic.¤¤Capacity, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.kidSpotsUsed);
			}
		}.hh(HTYPES.CHILD().names, M));
		
		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, blueprint.babies);
			}
		}.hh(Dic.¤¤Babies, M));

		s.addDown(4, new GStat() {
			@Override
			public void update(GText text) {
				GFORMAT.i(text, (long) ((blueprint.race.physics.adultAt)/TIME.years().bitConversion(TIME.years())));
			}
		}.hh(Dic.¤¤AdultAge, M));
		
		INTE in = new INTE() {
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return ENTETIES.MAX;
			}
			
			@Override
			public int get() {
				return CLAMP.i(blueprint.limit, min(), max());
			}
			
			@Override
			public void set(int t) {
				CLAMP.i(t, min(), max());
				blueprint.limit = t;
			}
		};
		
		r.NL();
		
		r.section.addRelBody(0, DIR.S, s);
		
		r.section.addRelBody(8, DIR.S, new GHeader(Dic.¤¤limit));
		
		r.section.addRelBody(2, DIR.S, new GSliderInt(in, 200, true));
		
	}
	
	@Override
	protected void hover(GBox box, NurseryInstance i) {
		box.textL(Dic.¤¤Capacity);
		box.tab(5);
		box.add(GFORMAT.i(box.text(), i.getWork().size()));
		
		box.NL();
		box.textL(HTYPES.CHILD().names);
		box.tab(5);
		box.add(GFORMAT.i(box.text(), i.kidspotsUsed));
		
		box.NL();
		box.textL(Dic.¤¤Babies);
		box.tab(5);
		box.add(GFORMAT.i(box.text(), i.infantspotsUsed));
	}
	
	@Override
	protected void problem(NurseryInstance i, Stack<Str> free, LISTE<CharSequence> errors,
			LISTE<CharSequence> warnings) {

		
		
		
		if (i.kidspotsUsed + i.infantspotsUsed < blueprint.constructor.beds.get(i)) {
			int c = i.blueprintI().current();
			if (c > i.blueprintI().limit)
				errors.add(¤¤problemLimitG);
			if (i.blueprintI().kidSpotsUsed + i.blueprintI().babies > i.blueprintI().rmax())
				errors.add(¤¤problemMax);
		}
		

		
		super.problem(i, free, errors, warnings);
	}

}
