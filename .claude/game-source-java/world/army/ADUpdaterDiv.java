package world.army;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;
import util.updating.IUpdater;
import view.ui.message.MessageText;
import world.entity.army.WArmy;

final class ADUpdaterDiv extends IUpdater{
	
	private static CharSequence ¤¤Desertion = "¤Desertion!";
	private static CharSequence ¤¤DesertionD = "¤Army supplies are low, and as a result {0} soldiers have deserted from {1}.";
	
	static {
		D.ts(ADUpdaterDiv.class);
	}
	
	public ADUpdaterDiv(ADInit init){
		super(FACTIONS.MAX(), TIME.secondsPerDay());
	}
	
	@Override
	protected void update(int i, double timeSinceLast) {
		
		Faction f = i == 0 ? null : FACTIONS.getByIndex(i-1);
		
		ADArmies as = AD.army(f);
		for (int ai = 0; ai < as.all().size(); ai++) {
			WArmy a = as.all().get(ai);
			if (a.faction() != FACTIONS.player() || AD.supplies().health(a) >= 1) {
				train(a);
			}else {
				starve(a);
			}
			if (!a.added())
				ai--;
		}
		
	}
	
	void train(WArmy a) {
		for (int di = 0; di < a.divs().size(); di++) {
			WDIV div = a.divs().get(di);
			if (div instanceof WDivRegional) {
				WDivRegional d = (WDivRegional) div;
				d.updateDay();
			}else if (div instanceof WDivStored) {
				((WDivStored) div).age();
			}
		}
	}

	void starve(WArmy a) {
		
		double health = AD.supplies().health(a);
		int am = 0;
		for (int di = 0; di < a.divs().size(); di++) {
			ADDiv div = a.divs().get(di);
			if (health < RND.rFloat()) {
				if (div.needSupplies()) {
					int aa = (int)(div.men()*(1.0-health)*(0.5+0.5*RND.rFloat()));
					am += aa;
					div.menSet(div.men()-aa);
				}
			}
		}
		
		if (am > 0) {
			Str.TMP.clear();
			Str.TMP.add(¤¤DesertionD).insert(0, am).insert(1, a.name);
			new MessageText(¤¤Desertion, Str.TMP).send();
			if (AD.men(null).get(a) <= 0)
				a.stop();
		}
		
		
	}
	
}
