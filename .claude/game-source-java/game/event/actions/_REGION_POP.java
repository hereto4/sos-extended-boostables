package game.event.actions;

import game.event.engine.EContext;
import game.event.engine.Event;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;
import world.region.pop.RDRace;

final class _REGION_POP extends EventActionConstructor{
	
	_REGION_POP() {
		super("REGION_POP");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final LIST<Race> races;
		private final double amountRel;
		private final int amountAbs;
		private final CInt amount;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			races = RACES.map().readMany(data);
			amountRel = data.dTry("AMOUNT_REL", -1, 1, 0);
			amountAbs = data.i("AMOUNT_ABS", Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
			amount = new CInt("AMOUNT");
			data.checkUnused();
		}

		@Override
		public void setContext(Event event, EContext data) {

			amount.set(event, data, 0);
			for (Region reg : WORLD.REGIONS().active()) {
				if (RD.event().ii.get(reg) == 1) {
					for (Race rr : races) {
						RDRace r = RD.RACE(rr);
						if (r == null)
							continue;
						int am = r.pop.get(reg);
						am = (int) (am*amountRel);
						am += amountAbs;
						amount.set(event, data, amount.get(event, data)+am);
					}
				}
			}
		}
		

		@Override
		public void exe(Event event, EContext data) {

			
			for (Region reg : WORLD.REGIONS().active()) {
				if (RD.event().ii.get(reg) == 1) {
					for (Race rr : races) {
						RDRace r = RD.RACE(rr);
						if (r == null)
							continue;
						int am = r.pop.get(reg);
						am = (int) (am*amountRel);
						am += amountAbs;
						r.pop.inc(reg, am);
					}
				}
			}
			
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext data, RECTANGLE messBody) {
			
			rows.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, amount.get(event, data));
				}
			}.hh(UI.icons().s.death));
			
		}
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
			
			
		}
		
	}
	

	
}
