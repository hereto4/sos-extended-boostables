package settlement.entity.humanoid.ai.consume;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.STATS;
import util.text.D;

final class F_PlanEat extends AIPLAN.PLANRES{

	private final AISUB sub;
	private static CharSequence ¤¤name = "Finding food";
	
	static{
		D.ts(F_PlanEat.class);
	}
	
	public F_PlanEat(AISUB sub) {
		super("SerEat");
		this.sub = sub;
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return fetchRaw.set(a, d);
	}
	
	private final Resumer fetchRaw = new Resumer(¤¤name) {
		
		final RBITImp bits = new RBITImp();
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			bits.clearSet(RESOURCES.EDI().mask);
			if (STATS.FOOD().STARVATION.indu().get(a.indu()) <= 0) {
				bits.and(STATS.FOOD().fetchMask(a));
			}
			return AI.SUBS().walkTo.resource(a, d, bits, Integer.MAX_VALUE);
		};
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			STATS.FOOD().eat(a, 1, 0);
			return eat.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			d.resourceCarriedSet(null);
		}
	};
	
	private final Resumer eat = new Resumer(¤¤name) {
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			if (d.resourceCarried() != null && RESOURCES.EDI().is(d.resourceCarried())) {
				FACTIONS.player().res().inc(d.resourceCarried(), RTYPE.CONSUMED, -1);
			}
			d.resourceCarriedSet(null);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			d.resourceCarriedSet(null);
		}

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return sub.activate(a, d);
		}
	};
	
}
