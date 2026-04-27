package settlement.entity.humanoid.ai.crime;

import static settlement.main.SETT.PATH;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.resources.RBIT;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.util.rnd.RND;
import util.text.D;

final class Theft extends AIPLAN.PLANRES{

	private static CharSequence ¤¤verb = "¤Stealing!";
	
	static{
		D.ts(Theft.class);
	}
	
	final AIModule_Crime m;
	
	public Theft(String key, AIModule_Crime m) {
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
			return AI.SUBS().walkTo.resource(a, d, RBIT.ALL, 100);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			m.commitCrime(a, d, false, CRIME.THEFT);
			GAME.player().res().inc(d.resourceCarried(), RTYPE.THEFT, -1);
			int x = d.path.destX();
			int y = d.path.destY();
			int e = PATH().finders.resource.normal.reserveExtra(d.resourceCarried(), x,y, 4+RND.rInt(10));
			PATH().finders.resource.pickup(d.resourceCarried(), x, y, e);
			GAME.player().res().inc(d.resourceCarried(), RTYPE.THEFT, -e);
			d.resourceCarriedSet(null);
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
	
}
