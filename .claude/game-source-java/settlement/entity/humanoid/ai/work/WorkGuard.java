package settlement.entity.humanoid.ai.work;
import game.GAME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.guard.GuardInstance;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.rnd.RND;

final class WorkGuard extends PlanBlueprint {
	
	protected WorkGuard(AIModule_Work module, PlanBlueprint[] map) {
		super(module, SETT.ROOMS().GUARD, map);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return walk.set(a, d);
	}
	
	final Resumer walk = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			GuardInstance ins = (GuardInstance) work(a);
			
			ins.blueprintI().equip().stat().indu().set(a.indu(), 2);
			
			if (!ins.guardSpot(d.planTile)) {
				GAME.Notify("WEIRD!!!" + ins.mX() + " " + ins.mY());
				return null;
			}
			
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			
			if (s == null)
				ins.guardSpotReturn(d.planTile.x(), d.planTile.y());
			
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return guard.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return hasEmployment(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			GuardInstance ins = (GuardInstance) work(a);
			if (ins != null)
				ins.guardSpotReturn(d.planTile.x(), d.planTile.y());
		}
	};
	
	private final Resumer guard = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			GuardInstance ins = (GuardInstance) work(a);
			a.speed.turn2(ins.body().cX(), ins.body().cY(), a.tc().x(), a.tc().y());
			d.planByte1 = (byte) (2 + RND.rInt(5));
			return AI.SUBS().STAND.activateTime(a, d, 5 + RND.rInt(5));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			
			GuardInstance ins = (GuardInstance) work(a);
			
			Humanoid c = ins.pollCriminal();
			
			if (c != null) {
				can(a, d);
				return d.resumeOtherPlan(a, AI.listeners().catchCriminal(c));
			}
			
			d.planByte1 --;
			
			if (d.planByte1 <= 0) {
				can(a, d);
				if (SETT.ROOMS().GUARD.instancesSize() > 1 && RND.oneIn(10) && STATS.WORK().WORK_TIME.indu().get(a.indu()) <= 0.5)
					return patrol.set(a, d);
				return null;
			}
			a.speed.setRaw(a.speed.dir().next(1*(RND.rBoolean()? 1:- 1)), 0);
			return AI.SUBS().STAND.activateTime(a, d, 5 + RND.rInt(5));
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return hasEmployment(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			GuardInstance ins = (GuardInstance) work(a);
			if (ins != null)
				ins.guardSpotReturn(d.planTile.x(), d.planTile.y());
		}
	};
	
	private final Resumer patrol = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			int i = RND.rInt(SETT.ROOMS().GUARD.instancesSize());
			if (SETT.ROOMS().GUARD.getInstance(i) == work(a)) {
				i++;
				i %= SETT.ROOMS().GUARD.instancesSize();
			}
			
			d.planByte1 = (byte) (2 + RND.rInt(5));
			return AI.SUBS().walkTo.room(a, d, SETT.ROOMS().GUARD.getInstance(i));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 --;
			if (d.planByte1 <= 0) {
				can(a, d);
				return null;
			}
			a.speed.setRaw(a.speed.dir().next(1*(RND.rBoolean()? 1:- 1)), 0);
			return AI.SUBS().STAND.activateTime(a, d, 5 + RND.rInt(5));
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return hasEmployment(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			e.stat().indu().set(a.indu(), 0);
		}
		super.cancel(a, d);
	}
	
	@Override
	protected AISubActivation resume(Humanoid a, AIManager d) {
		AISubActivation s = super.resume(a, d);
		if (s == null)
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				e.stat().indu().set(a.indu(), 0);
			}
		return s;
	}

}