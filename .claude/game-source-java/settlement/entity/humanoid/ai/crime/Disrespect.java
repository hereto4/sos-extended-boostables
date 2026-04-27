package settlement.entity.humanoid.ai.crime;

import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.util.rnd.RND;
import util.text.D;

final class Disrespect extends AIPLAN.PLANRES{

	private static CharSequence ¤¤verb = "¤Disrespecting the ruler!";
	
	static {
		D.ts(Disrespect.class);
	}
	
	final AIModule_Crime m;
	
	public Disrespect(String key, AIModule_Crime m) {
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
			d.planByte1 = (byte) (1 + RND.rInt(16));
			d.planByte2 = 0;
			return AI.SUBS().walkTo.around(a, d, THRONE.coo().x(), THRONE.coo().y());
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte2 ++;
			if (d.planByte2 == 1)
				m.commitCrime(a, d, false, CRIME.DISRESPECT);
			if (d.planByte1-- >= 0) {
				a.speed.turn2(a.tc().x(), a.tc().y(), THRONE.coo().x(), THRONE.coo().y());
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.fist.activate(a, d));
			}
			return null;
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
