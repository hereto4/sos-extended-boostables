package view.battle.editor;

import game.battle.util.DIV_SPEC;
import game.raiding.RaiderArmy;
import init.constant.Config;
import init.race.RACES;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import world.army.AD;

final class ArmySide {

	public ArrayList<DIV_SPEC> divs = new ArrayList<DIV_SPEC>(Config.battle().DIVISIONS_PER_ARMY);
	public int[] artillery = new int[AD.supplies().arts().size()];
	
	void generate(double power) {
		RaiderArmy p = new RaiderArmy(RACES.playable().rnd(), power, RND.rFloat());
		divs.clearSloppy();
		for (DIV_SPEC d : p.sdivs)
			divs.add(d);
		
		for (int i = 0; i < artillery.length; i++)
			artillery[i] = p.artillery[i];
	}
	
	void clear() {
		divs.clearSloppy();
		for (int i = 0; i < artillery.length; i++)
			artillery[i] = 0;
	}
	
}
