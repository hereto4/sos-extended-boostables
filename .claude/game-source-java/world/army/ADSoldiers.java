package world.army;

import init.race.RACES;
import init.race.Race;
import snake2d.util.sets.ArrayList;
import util.text.Dic;
import world.army.ADInit.Register;
import world.army.ADInt.ADIntImp;
import world.entity.army.WArmy;

final class ADSoldiers {

	private final ArrayList<ADIntImp> current = new ArrayList<ADIntImp>(RACES.all().size());
	private final ArrayList<ADIntImp> target = new ArrayList<ADIntImp>(RACES.all().size());
	private final ADIntImp currentTot;
	private final ADIntImp targetTot;
	
	ADSoldiers(ADInit init){
		
		currentTot = new ADIntImp(init, "SOLDIERS", Dic.¤¤Soldiers, "");
		targetTot = new ADIntImp(init, "SOLDIERS_TARGET", Dic.¤¤SoldiersTarget, "");
		
		for (Race r : RACES.all()) {
			ADIntImp ii = new ADIntImp(init, "SOLDIERS_" + r.key, Dic.¤¤Soldiers + ": " + r.info.names, "") {
				
				@Override
				public void count(WArmy t, int delta) {
					currentTot.inc(t, get(t)*delta);
					super.count(t, delta);
				}
				
			};
			current.add(ii);
			ii = new ADIntImp(init, "SOLDIERS_TAR_" + r.key, Dic.¤¤SoldiersTarget + ": " + r.info.names, "") {
				
				@Override
				public void count(WArmy t, int delta) {
					targetTot.inc(t, get(t)*delta);
					super.count(t, delta);
				}
				
			};
			target.add(ii);
			
		}
		
		init.registers.add(new Register() {
			
			@Override
			public void register(ADDiv div, int d) {
				current.get(div.race().index()).inc(div.army(), d*div.men());
				target.get(div.race().index()).inc(div.army(), d*div.menTarget());
			}
		});
		
	}

	public ADInt current(Race race) {
		if (race == null)
			return currentTot;
		return current.get(race.index);
	}
	
	public ADInt target(Race race) {
		if (race == null)
			return targetTot;
		return target.get(race.index);
	}
	
}
