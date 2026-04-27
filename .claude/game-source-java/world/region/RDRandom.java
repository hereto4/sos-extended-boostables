package world.region;

import init.sprite.UI.UI;
import init.value.GVALUES;
import util.data.DOUBLE_O;
import util.data.DataRandom;
import world.map.regions.Region;
import world.region.RD.RDInit;

public final class RDRandom extends DataRandom<Region>{



	RDRandom(RDInit init){
		super(init.count, 4);

		for (int i = 0; i < 8; i++) {
			int bit = 16*i;
			DOUBLE_O<Region> vv = new DOUBLE_O<Region>() {
				
				@Override
				public double getD(Region reg) {
					return RDRandom.this.get(reg, bit, 16);
				}
				
			};
			GVALUES.REGION.push("RANDOM_" + i, "Random: " + i, UI.icons().s.question, vv, false);
		}
		
	}

	
}
