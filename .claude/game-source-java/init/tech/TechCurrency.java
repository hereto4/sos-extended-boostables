package init.tech;

import game.boosting.BOOSTING;
import game.boosting.Boostable;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public final class TechCurrency {

	public final Boostable bo;
	public final int index;
	
	TechCurrency(Boostable bo, int index){
		this.bo = bo;
		this.index = index;
	}
	
	static class TechCurrencies {

		public final ArrayListGrower<TechCurrency> all = new ArrayListGrower<>();
		
		public LIST<TechCost> read(Json json){
			
			ArrayListGrower<TechCost> cc = new ArrayListGrower<>(); 
			
			json = json.json("COSTS");
			
			
			
			for (String k : json.keys()) {
				Boostable bo = BOOSTING.MAP().tryGet(k);
				if (bo == null) {
					json.error("The boostable: " + k + "does not exist in this context. The boostable in question must be predefined in the game and can not be dynamic.", k);
				}
				double am = json.d(k, 0, 10000000);
				cc.add(add(bo, am));				
			}
			return cc;
		}
		
		private TechCost add(Boostable bo, double am) {
			for (TechCurrency c : all) {
				if (c.bo == bo) {
					return new TechCost(c, am);
				}
			}
			TechCurrency c = new TechCurrency(bo, all.size());
			all.add(c);
			return new TechCost(c, am);
			
		}
		
	}
}
