package world.army;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import util.updating.IUpdater;
import world.entity.army.WArmy;

final class ADUpdater extends IUpdater{
	
	
	public ADUpdater(ADInit init){
		super(FACTIONS.MAX(), TIME.secondsPerDay()*0.25);
	}
	
	@Override
	protected void update(int i, double timeSinceLast) {
		
		Faction f = i == 0 ? null : FACTIONS.getByIndex(i-1);
		
		for (ADInit.Updater u : AD.iinit().updaters)
			u.update(f, timeSinceLast);
		
		ADArmies as = AD.army(f);
		
		for (int ai = 0; ai < as.all().size(); ai++) {
			WArmy a = as.all().get(ai);
			for (ADInit.Updater u : AD.iinit().updaters)
				u.update(a, timeSinceLast);
		}
		
	}

}
