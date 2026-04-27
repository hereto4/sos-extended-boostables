package game.boosting.superb;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.faction.royalty.Royalty;

public class SuperBoostables {

	public SuperBoostable<Royalty> OPINION = new SuperBoostable<Royalty>(BOOSTABLES.CIVICS().bOpinion);
	
	public SuperBoostables(GAME game) {
		
	}
	
}
