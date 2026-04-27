package settlement.room.infra.admin;

import game.faction.FACTIONS;
import game.time.TIME;
import init.settings.S;
import init.sprite.UI.UI;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;
import view.ui.top.UINotifications.UINotification;

class Gui extends UIRoomModuleImp<AdminInstance, ROOM_ADMIN> {
	

	private static CharSequence ¤¤projection = "{0} administration workers are currently producing {1} administration. A single administration worker produces roughly {2} administration points.";
	private static CharSequence ¤¤nName = "Administration Problem";
	private static CharSequence ¤¤needed = "Overhead will start to affect your city at {0} subject workers. Your currently have a total of {1} workers, resulting in a need of {2} administration to function optimally.";
	private static CharSequence ¤¤effects = "The current effect your administrators have on your city. Overhead only becomes important at {0} workers.";
	private static CharSequence ¤¤auto = "Automate";
	private static CharSequence ¤¤autoD = "Automate employment of your administrations, so that they'll strive to always produce the needed value for your city.";
	
	private static CharSequence ¤¤pEmpMissing = "There are not enough available workers to employ enough administrators to upkeep your admin levels. Free up some workforce and/or increase work priority for administrations.";
	private static CharSequence ¤¤pEmpFull = "Your admin rooms are at full capacity. Bigger/more admin rooms are needed to produce enough admin to have your city work optimally.";
	private static CharSequence ¤¤pPaper = "Some of our administrators are not functioning properly. You should investigate why.";
	private static CharSequence ¤¤p = "We are missing admin in our city, and as a result our industries are suffering from a {0}% penalty.";
	private static CharSequence ¤¤pno = "Our administrators have no problems";
	static {
		D.ts(Gui.class);
	}
	
	public Gui(ROOM_ADMIN s) {
		super(s);
		
		new UINotification(s.icon.small, COLOR.WHITE100, true) {

			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(¤¤nName);
				if (prob() != null)
					b.error(prob());
				super.hoverInfoGet(text);
			}
			
			@Override
			public int get() {
				if (s.reqs.passes(FACTIONS.player()) && prob() != null)
					return (int) (s.needed()-s.data.projection());
				return 0;
			}
			
		};
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<AdminInstance> getter, int x1, int y1) {
		
		AdminData data = blueprint.data;
		
		section.addRelBody(8, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				double p = getter.get().employees().employed()*IndustryUtil.calcProductionRate(data.knowledgePerStation, blueprint.industry, data.boost, getter.get());
				GFORMAT.f0(text, p);
			}
			@Override
			public void hoverInfoGet(GBox b) {
				double p = getter.get().employees().employed()*IndustryUtil.calcProductionRate(data.knowledgePerStation, blueprint.industry, data.boost, getter.get());
				b.text(Str.TMP.clear().add(¤¤projection).insert(0, getter.get().employees().employed()).insert(1, (int)p).insert(2, getter.get().employees().employed() == 0 ? 0 :p/getter.get().employees().employed(), 1));
				b.NL(8);
				IndustryUtil.hoverProductionRate(b, data.knowledgePerStation, blueprint.industry, data.boost, getter.get());
			};
			
		}.hv(Dic.¤¤Target));
		
		
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		
	}
	
	@Override
	protected void hover(GBox box, AdminInstance i) {
		super.hover(box, i);

	}

	@Override
	protected void appendMain(GGrid r, GGrid text, GuiSection sExtra) {
		GuiSection ss = new GuiSection();
		
		AdminData data = blueprint.data;
		
		int i = 0;
		
		ss.addGrid(sExtra, i, 2, 180, 0);
		

		ss.addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f0(text, data.projection(), 2);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(Str.TMP.clear().add(¤¤projection).insert(0, blueprint.employment().employed()).insert(1, (int)data.projection()).insert(2, data.perEmployee(), 1));
			}
			
		}.hh(Dic.¤¤Produced), i++, 2, 180, 20, DIR.NW);
		
		ss.addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f0(text, blueprint.needed(), 2);
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				Str.TMP.clear().add(¤¤needed);
				Str.TMP.insert(0, (int)blueprint.POP_MIN);
				Str.TMP.insert(1, STATS.WORK().workforce()-blueprint.employment().employed());
				Str.TMP.insert(2, (int)blueprint.needed());
				b.text(Str.TMP);
			};
			
		}.hh(Dic.¤¤Needed), i++, 2, 180, 20, DIR.NW);
		
		ss.addGridD(new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.perc(text, blueprint.value());
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.text(Str.TMP.clear().add(¤¤effects).insert(0, (int)blueprint.POP_MIN));
				b.NL();
				blueprint.boosts.hover(b, CLAMP.d(blueprint.value(), blueprint.BOOST_FROM, blueprint.BOOST_TO), -1);
			}
			
		}.hh(Dic.¤¤Value), i++, 2, 180, 20, DIR.NW);
		ss.pad(8, 0);
		
		
		ss.body().incrW(64);
		
		{
			GuiSection s = new GuiSection();
			
			CLICKABLE b = new GButt.ButtPanel(¤¤auto) {
				
				@Override
				protected void renAction() {
					selectedSet(blueprint.automate);
				}
				
				@Override
				protected void clickA() {
					blueprint.automate();
				}
				
				
			}.hoverInfoSet(¤¤autoD);
			
			s.add(new HOVERABLE.HoverableAbs(b.body().height(), b.body().height()) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
					GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
					if (prob() == null) {
						UI.icons().m.ok.renderC(r, body());
					}else {
						UI.icons().m.cancel.renderC(r, body());
					}
					GButt.ButtPanel.renderFrame(r, body);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					if (prob() == null) {
						b.text(¤¤pno);
					}else {
						b.error(prob());
						
					}
				}
			});
			
			s.addRightC(0, b);
			ss.addRelBody(8, DIR.S, s);
		}
		
		
		if (S.get().developer) {
			ss.addDown(2, new GButt.ButtPanel("++") {
				@Override
				protected void clickA() {
					data.cheatAdd(50);
					super.clickA();
				}
			});
			ss.addDown(0, new GButt.ButtPanel("--") {
				@Override
				protected void clickA() {
					data.clear();
					super.clickA();
				}
			});
		}
		
		text.add(ss);
		
	}
	
	double probTime = -1;
	
	CharSequence prob() {
		Str.TMP.clear();
		if (!blueprint.automate && blueprint.data.projection() < blueprint.needed()) {
			Str.TMP.add(¤¤p);
			Str.TMP.insert(0, (int)(100*(blueprint.BOOST_TO-blueprint.value())));
			Str.TMP.NL();
		}
		if (blueprint.automate) {
			
			if (blueprint.employmentExtra().target.get() < blueprint.employmentExtra().neededWorkers()) {
				if (probTime == -1) {
					probTime = TIME.currentSecond();
				}
				if (TIME.currentSecond()-probTime > TIME.secondsPerDay()*0.4)
					Str.TMP.add(¤¤pEmpMissing);
			}else {
				probTime = -1;
			}
			if (blueprint.employmentExtra().employed() >= blueprint.employmentExtra().employedMax())
				Str.TMP.add(¤¤pEmpFull);
			if (blueprint.missingPaper)
				Str.TMP.add(¤¤pPaper);
			
			
		}
		return Str.TMP.length() > 0 ? Str.TMP : null;
	}
	
	@Override
	protected void problem(AdminInstance i, Stack<Str> free, LISTE<CharSequence> errors,
			LISTE<CharSequence> warnings) {
		
		super.problem(i, free, errors, warnings);
	}
	
	

}
