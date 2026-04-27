package settlement.room.knowledge.school;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.sett.ui.room.UIRoomBulkApplier;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<SchoolInstance, ROOM_SCHOOL> {

	private static CharSequence ¤¤daysToEducate = "¤Days to Educate:";
	private static CharSequence ¤¤type = "¤Education type:";
	
	private static CharSequence ¤¤learningRate = "¤Base Learning Rate";
	private static CharSequence ¤¤learningRateI = "¤{0} days";

	static {
		D.ts(Gui.class);
	}
	
	public Gui(ROOM_SCHOOL s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<SchoolInstance> getter, int x1, int y1) {
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
	protected void appendMain(GGrid rrr, GGrid text, GuiSection sExtra) {
		LinkedList<RENDEROBJ> rows = new LinkedList<RENDEROBJ>();
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = FACTIONS.player().races.get(ri);
			if (r.physics.adultAt > 0)
				rows.add(new RaceRow(r));
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
					GFORMAT.f(text, 1.0/(blueprint.learningSpeed), 1);
				}
			});
			
			CLICKABLE check = new GButt.Checkbox() {
				
				@Override
				protected void renAction() {
					selectedSet(blueprint.access(race));
				}
				
				@Override
				protected void clickA() {
					blueprint.accessToggle(race);;
				}
				
			};
			
			addRightC(80,check);
			
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
			double am = 1.0/(blueprint.learningSpeed);
			b.add(GFORMAT.f(b.text(), am));
			b.NL();
			
			b.textLL(Dic.¤¤Access);
			b.tab(7);
			b.add(b.text().add(blueprint.access(race)));
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
