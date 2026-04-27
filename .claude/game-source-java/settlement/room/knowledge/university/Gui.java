package settlement.room.knowledge.university;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import init.type.POP_CL;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GScrollRows;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.text.D;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<UniversityInstance, ROOM_UNIVERSITY> {

	private static CharSequence ¤¤daysToEducate = "¤Days to Educate:";
	private static CharSequence ¤¤limit = "¤Education Limit:";
	private static CharSequence ¤¤type = "¤Education type:";
	
	private static CharSequence ¤¤learningRate = "¤Base Learning Rate";
	private static CharSequence ¤¤learningRateI = "¤{0} days";

	static {
		D.ts(Gui.class);
	}
	
	public Gui(ROOM_UNIVERSITY s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<UniversityInstance> getter, int x1, int y1) {
		grid.NL();
		grid.add(new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(¤¤learningRateI);
				text.insert(0, (int)(1.0/blueprint.learningSpeed(getter.get(), FACTIONS.player())));
			}
		}.hv(¤¤learningRate));
		grid.NL();

		
	}
	
	@Override
	protected void appendTableFilters(LISTE<GTFilter<RoomInstance>> filters, LISTE<GTSort<RoomInstance>> sorts,
			LISTE<UIRoomBulkApplier> appliers) {
		
	}
	



	@Override
	protected void appendMain(GGrid grid, GGrid text, GuiSection sExtra) {
		

		LinkedList<RENDEROBJ> rows = new LinkedList<RENDEROBJ>();
		
		for (Race r : RACES.all()) {
			rows.add(new RaceRow(FACTIONS.player().races.get(r.index)));
		}
		
		text.add(new GScrollRows(rows, rows.get(0).body().height()*5).view());
		
	}
	
	private class RaceRow extends GuiSection{
		
		private final Race race;
		
		RaceRow(Race race){
			this.race = race;
			addRightC(0, race.appearance().icon);
			addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f(text, 1.0/(blueprint.learningSpeed*blueprint.bonus().get(POP_CL.clP(race))), 1);
				}
			});
			INTE in = new INTE() {
				
				@Override
				public int get() {
					return (int) (blueprint.limit.getD(race)*20);
				}

				@Override
				public int min() {
					return 0;
				}

				@Override
				public int max() {
					return 20;
				}

				@Override
				public void set(int t) {
					blueprint.limit.setD(race, t/20.0);
				}
			};
			addRightC(80, new GSliderInt(in, 120, true, false));
			
			addRightC(8, new GButt.ButtPanel(STATS.EDUCATION().EDUCATION.info().icon.resized(Icon.S)) {
				
				@Override
				protected void renAction() {
					selectedSet(!STATS.EDUCATION().policyIndoctor.is(race));
				}
				
				@Override
				protected void clickA() {
					STATS.EDUCATION().policyIndoctor.set(race, false);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					// TODO Auto-generated method stub
					super.hoverInfoGet(text);
				}
			});
			addRightC(0, new GButt.ButtPanel(STATS.EDUCATION().INDOCTRINATION.info().icon.resized(Icon.S)) {
				
				@Override
				protected void renAction() {
					selectedSet(STATS.EDUCATION().policyIndoctor.is(race));
				}
				
				@Override
				protected void clickA() {
					STATS.EDUCATION().policyIndoctor.set(race, true);
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					// TODO Auto-generated method stub
					super.hoverInfoGet(text);
				}
			});
			
			pad(4, 4);
		}
		
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(race.info.names);
			
			b.textLL(¤¤daysToEducate);
			b.tab(7);
			double am = 1.0/(blueprint.learningSpeed*blueprint.bonus().get(POP_CL.clP(race)));
			b.add(GFORMAT.f(b.text(), am));
			b.NL();
			
			b.textLL(¤¤limit);
			b.tab(7);
			b.add(GFORMAT.perc(b.text(), blueprint.limit.getD(race)));
			b.NL();
			
			b.textLL(¤¤type);
			b.tab(7);
			STAT s = STATS.EDUCATION().policyIndoctor.is(race) ? STATS.EDUCATION().INDOCTRINATION : STATS.EDUCATION().EDUCATION;
			b.textLL(s.info().name);
			b.NL();
			b.text(s.info().desc);
		}

		
	}
	
}
