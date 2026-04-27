package settlement.stats.colls;

import game.faction.FACTIONS;
import game.faction.npc.stockpile.NPCStockpile;
import game.tourism.TOURISM;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATImp;
import settlement.stats.stat.StatCollection;
import snake2d.util.misc.CLAMP;
import util.text.D;

public class StatsGovern extends StatCollection{
	
	public final STAT tourismFriend;
	public final STAT tourismEnemy;
	public final STAT RICHES;
	
	private static CharSequence ¤¤name = "Government";
	private static CharSequence ¤¤desc = "Government stats";
	
	static {
		D.ts(StatsGovern.class);
	}
	
	public StatsGovern(StatsInit init){
		super(init, "GOVERN", ¤¤name, ¤¤desc);
		
		tourismFriend = new STATImp("TOURISM_FRIEND", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				double res = 0;
				double tot = 0;
				for (Race other : RACES.all()) {
					tot += TOURISM.race(other);
					res += TOURISM.race(other)*r.pref().race(other);
				};
				if (tot == 0)
					return 0;
				
				return (int) ((res/tot)*pdivider(s, r, 0));
			}
		};
		tourismFriend.info().icon = UI.icons().m.citizen;
		
		tourismEnemy = new STATImp("TOURISM_ENEMY", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				
				double res = 0;
				double tot = 0;
				for (Race other : RACES.all()) {
					tot += TOURISM.race(other);
					res += TOURISM.race(other)*(1.0-r.pref().race(other));
				};
				if (tot == 0)
					return 0;
				
				return (int) ((res/tot)*pdivider(s, r, 0));
				
			}
		};
		tourismEnemy.info().icon = UI.icons().m.citizen.twin(UI.icons().m.anti);
		
		RICHES = new STATImp("RICHES", init) {

			@Override
			protected int getDD(HCLASS s, Race r) {
				double d = FACTIONS.player().credits().credits()/(NPCStockpile.AVERAGE_PRICE*4);
				d /= STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null) + STATS.POP().POP.data(HCLASSES.NOBLE()).get(null);
				return (int) (CLAMP.d(d, 0, 1)*pdivider(s, r, 0));
			}
			
		};
		RICHES.info().setMatters(true, false);
		RICHES.info().icon = UI.icons().m.coins;
		
	}
	
	
	
}
