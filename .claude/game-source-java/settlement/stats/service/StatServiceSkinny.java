package settlement.stats.service;

import init.type.NEEDS;
import settlement.main.SETT;
import settlement.stats.StatsInit;
import snake2d.util.sets.LISTE;
import util.text.D;

final class StatServiceSkinny extends StatServiceSimple{

	private static CharSequence ¤¤name = "Skinnydipping";
	private static CharSequence ¤¤desc = "When the weather allows for it, subjects might want to have a dip in a pool of water.";

	static {
		D.ts(StatServiceSkinny.class);
	}
	
	StatServiceSkinny(LISTE<StatServiceImp> all, StatsInit init) {
		super("MISC_SKINNYDIP", all, init, ¤¤name, ¤¤desc, SETT.ROOMS().POOLS.get(0).icon, NEEDS.TYPES().SKINNYDIP);
	}

}