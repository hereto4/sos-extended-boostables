package settlement.entity.humanoid.ai.danger;

import static settlement.main.SETT.PATH;

import init.sprite.UI.UI;
import init.type.CAUSE_LEAVES;
import init.type.HTYPES;
import init.type.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.service.hygine.well.ROOM_WELL;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;

final class AIModule_Exposure extends AIModule {

	private final AIDataSuspender suspender = AI.suspender("Exposure");
	
	private static CharSequence ¤¤name = "Find Shelter";
	private static CharSequence ¤¤desc = "Find Shelter or services to alleviate heat or cold.";
	private static CharSequence ¤¤freezing = "Freezing!";
	private static CharSequence ¤¤cover = "Cooling down";
	private static CharSequence ¤¤nearDeath = "(Near Death!)";
	
	static {
		D.ts(AIModule_Exposure.class);
	}

	
	public AIModule_Exposure() {
		super(UI.icons().s.heat, ¤¤name, ¤¤desc);
	}

	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {

		if (STATS.NEEDS().EXPOSURE.COUNT.indu().get(a.indu()) == 0)
			return null;
		
		if (!suspender.is(d)) {
			if (!STATS.NEEDS().EXPOSURE.isCold(a.indu())) {
				AiPlanActivation p = getHot(a, d);
				if (p != null)
					return p;

					
			}else {
				AiPlanActivation p = getCold(a, d);
				if (p != null)
					return p;

			}
			suspender.suspend(d);
		}
		
		
		
		AiPlanActivation p = inside.activate(a, d);
		if (p != null)
			return p;
		
		return null;
	}

	public AiPlanActivation getHot(Humanoid a, AIManager d) {
		
		int dist = a.indu().hType() == HTYPES.DERANGED() ? Integer.MAX_VALUE : 200;
		
		AiPlanActivation p = AI.modules().needs.get(a, d, NEEDS.TYPES().SKINNYDIP, dist);
		if (p != null)
			return p;
		
		for (ROOM_WELL w : SETT.ROOMS().WELLS) {
			p = AI.modules().needs.get(a, d, w.service().need, dist);
			if (p != null)
				return p;
		}
		return null;
	}
	
	public AiPlanActivation getCold(Humanoid a, AIManager d) {
		int dist = a.indu().hType() == HTYPES.DERANGED() ? Integer.MAX_VALUE : 200;
		AiPlanActivation p = AI.modules().needs.get(a, d, SETT.ROOMS().HEARTH.service().need, dist);
		if (p != null)
			return p;
		return null;
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		suspender.update(d);
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {

		

		if (STATS.NEEDS().EXPOSURE.critical(a.indu())) {
			return (int) (10);			
		}
		
		if (!suspender.is(d) && STATS.NEEDS().EXPOSURE.COUNT.indu().get(a.indu()) > 0)
			return 8;
		

		return 0;

	}

	
	final AIPLAN inside = new AIPLAN.PLANRES("dangerExposeure") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return find.set(a, d);
		}
		
		private final Resumer find = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				exit.set(a, d);
				FINDABLE f = PATH().finders.indoor.getReservable(a.tc().x(), a.tc().y());
				d.planByte2 = 0;
				if (f != null) {
					d.planTile.set(a.tc());
					f.findableReserve();
					d.planByte2 = 1;
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				
				AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, PATH().finders.indoor, 64);
				if (s != null) {
					d.planTile.set(d.path.destX(), d.path.destY());
					d.planByte2 = 1;
					return s;
				}
				
				return AI.SUBS().STAND.activateRndDir(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				return exit.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				if (d.planByte2 == 1) {
					FINDABLE f = PATH().finders.indoor.getReserved(d.planTile.x(), d.planTile.y());
					if (f != null)
						f.findableReserveCancel();
					d.planByte2 = 0;
				}
			}
		};
		
		private final Resumer exit = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (5 + RND.rInt(5));
				return AI.SUBS().STAND.activateRndDir(a, d);
			};
			
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1--;
				
				
				
				if (d.planByte1 > 0 && STATS.NEEDS().EXPOSURE.COUNT.indu().get(a.indu()) > 0 && moduleCanContinue(a, d)) {
					if (STATS.NEEDS().EXPOSURE.critical(a.indu())) {
						return exit2.set(a, d);
					}
					
					if (RND.rBoolean()) {
						return AI.SUBS().STAND.activate(a, d, AI.STATES().anima.wave.activate(a, d, 2+RND.rFloat()*2));
					}
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				can(a, d);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				if (d.planByte2 == 1) {
					FINDABLE f = PATH().finders.indoor.getReserved(d.planTile.x(), d.planTile.y());
					if (f != null)
						f.findableReserveCancel();
					d.planByte2 = 0;
				}
			}
		};
		
		private final Resumer exit2 = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().LAY.activateTime(a, d, 8);
			};
			
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				if (STATS.NEEDS().EXPOSURE.COUNT.indu().isMax(a.indu()) && SETT.WEATHER().temp.getEntityTemp() != 0) {
					
					AIManager.dead = STATS.NEEDS().EXPOSURE.isCold(a.indu()) ? CAUSE_LEAVES.COLD() : CAUSE_LEAVES.HEAT();
					return AI.SUBS().LAY.activateTime(a, d, 8);
				}
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				if (d.planByte2 == 1) {
					FINDABLE f = PATH().finders.indoor.getReserved(d.planTile.x(), d.planTile.y());
					if (f != null)
						f.findableReserveCancel();
					d.planByte2 = 0;
				}
			}
			
			
		};

		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (STATS.NEEDS().EXPOSURE.isCold(a.indu())) {
				string.add(¤¤freezing);
			}else
				string.add(¤¤cover);
			if (STATS.NEEDS().EXPOSURE.critical(a.indu()))
				string.s().add(¤¤nearDeath);
			
		};
		
	};


}
