package settlement.entity.humanoid.ai.subject;

import game.GAME;
import init.type.CAUSE_LEAVES;
import init.type.HCLASSES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import util.text.D;
import world.WORLD;

class PlanEmmigrate extends AIPLAN.PLANRES{

	private static CharSequence ¤¤name = "¤Fed up with this dump and your failing rule.";
	
	public PlanEmmigrate() {
		super("SubEmigrate");
	}
	
	static {
		D.ts(PlanEmmigrate.class);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		GAME.events().riot.emigrate(a);
		return walk.set(a, d);
	}

	boolean shouldEmmigrate(Humanoid a) {
		if (a.indu().clas() != HCLASSES.CITIZEN())
			return false;

		
		if (SETT.ENTRY().isClosed())
			return false;
		
		if (WORLD.camps().available(a.race()) && WORLD.camps().current(a.indu().faction(), a.race()) < STATS.POP().POP.data(HCLASSES.CITIZEN()).get(a.race()))
			return true;
		
		
		if (!GAME.events().riot.shouldEmigrate(a))
			return false;
//		if ((a.indu().randomness() & 0x01F) != (TIME.days().bitsSinceStart()&0x01F)) {
//			return false;
//		}
//		
//		double dd = STANDINGS.CITIZEN().main.getD(a.race());
//		
//		if (dd >= ET)
//			return false;
//		
//		int d = (int) (dd*ETI * 0x0FFFF);
//		
//		if ((a.indu().randomness() & 0x0FFFF) < d) {
//			return false;
//		}
		
		return true;
	}
	
	private final Resumer walk = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (SETT.PATH().finders.entryPoints.find(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)) {
				STATS.POP().EMMIGRATING.indu().set(a.indu(), 1);
				STATS.WORK().EMPLOYED.set(a, null);
				STATS.BATTLE().RECRUIT.set(a, null);
				STATS.BATTLE().DIV.set(a, null);
				return AI.SUBS().walkTo.path(a, d);
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			HumanoidResource.dead = CAUSE_LEAVES.EMMIGRATED();
			STATS.POP().EMMIGRATING.indu().set(a.indu(), 0);
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			STATS.POP().EMMIGRATING.indu().set(a.indu(), 0);
		}
	};
	
}
