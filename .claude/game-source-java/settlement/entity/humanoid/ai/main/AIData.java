package settlement.entity.humanoid.ai.main;

import util.data.BOOLEANO.BOOLEAN_OE;
import util.data.DataO;

public final class AIData extends DataO<AIManager>{


	AIData() {
		super("AI_DATA");
	}
	
	
	@Override
	protected long[] data(AIManager t) {
		return t.longs;
	}
	
	public class AIDataBit extends DataO<AIManager>.DataBit implements BOOLEAN_OE<AIManager>{
		
		
		public AIDataBit(String key){
			super(key + "_BIT");
		}
		
		@Override
		public boolean is(AIManager d) {
			return get(d) == 1;
		}
		
		@Override
		public BOOLEAN_OE<AIManager> set(AIManager d, boolean s) {
			set(d, s ? 1 : 0);
			return this;
		}

	}
	
	public final class AIDataSuspender extends util.data.DataO<AIManager>.DataCrumb {
		
		public AIDataSuspender(String key){
			super(key + "_sus");
		}
		
		public boolean is(AIManager d) {
			return get(d) != 0;
		}
		
		public void suspend(AIManager d) {
			set(d, 2);
		}
		
		public void update(AIManager d) {
			if (is(d)) {
				set(d, get(d)-1);
			}
		}
		
	}

}