package settlement.entity.humanoid.ai.crime;

import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.STATS;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.util.rnd.RND;
import util.text.D;

final class Flasher extends AIPLAN.PLANRES{

	private static CharSequence ¤¤verb = "¤Streaking";
	
	static{
		D.ts(Flasher.class);
	}

	final AIModule_Crime m;
	
	public Flasher(String key, AIModule_Crime m) {
		super(key);
		this.m = m;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return go.set(a, d);
	}

	private final Resumer go = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			m.commitCrime(a, d, true, CRIME.FLASHING);
			STATS.POP().NAKED.set(a.indu(), 1);
			return AI.SUBS().walkTo.run_arround_crazy(a, d, 1);
			

		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (!RND.oneIn(4)) {
				STATS.POP().NAKED.set(a.indu(), 0);
				return null;
				
			}
			AIModule_Crime.notify(a);
			return AI.SUBS().walkTo.run_arround_crazy(a, d, 1);
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
	};
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.NOTIFY_CRIME)
			return false;
		return super.event(a, d, e);
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		STATS.POP().NAKED.set(a.indu(), 0);
		super.cancel(a, d);
	}
	
}
