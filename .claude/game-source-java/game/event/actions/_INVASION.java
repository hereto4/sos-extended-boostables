package game.event.actions;

import game.GAME;
import game.event.engine.EContext;
import game.event.engine.Event;
import game.faction.FACTIONS;
import game.raiding.Raider;
import game.raiding.RaidingMap.RaidRegion;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.text.Str;
import util.text.D;
import world.army.AD;
import world.region.RD;

final class _INVASION extends EventActionConstructor{
	
	private static CharSequence ¤¤arrive = "The army of {0} has now arrived.";
	
	static {
		D.ts(_INVASION.class);
	}
	
	_INVASION() {
		super("INVASION");
		
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final Race race;
		private final double amountFrom;
		private final double amountTo;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			race = RACES.map().readTry("RACE", data);
			amountFrom = data.d("AMOUNT_FROM", 0, 1000);
			amountTo = data.d("AMOUNT_TO", amountFrom, 1000);
			data.checkUnused();
		}

		@Override
		public void exe(Event event, EContext data) {
			if (race == null)
				return;
			
			double pow = 0;
			LIST<RaidRegion> vv = GAME.raiders().entry.entryRegions();
			if (vv.size() > 0) {
				pow = Double.MAX_VALUE;
				for (RaidRegion reg : vv) {
					
					double p = RD.MILITARY().power.getD(reg.r());
					if (p < pow)
						pow = p;
				}
			}
			pow += AD.power().get(FACTIONS.player());
			pow *= (amountFrom + RND.rFloat()*(amountTo-amountFrom));
			Raider rr = new Raider(race, pow);
			rr.text.set(rr, true);
			
			GAME.raiders().current.raid(rr, "" + Str.TMP.clear().add(¤¤arrive).insert(0, rr.name));
		}
		
	}
	
}
