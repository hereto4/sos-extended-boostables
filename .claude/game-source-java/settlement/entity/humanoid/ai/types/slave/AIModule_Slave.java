package settlement.entity.humanoid.ai.types.slave;

import game.GAME;
import game.faction.FACTIONS;
import init.sprite.UI.UI;
import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import util.text.D;

public final class AIModule_Slave extends AIModule{

	private PlanUprise uprise = new PlanUprise();
	private static CharSequence ¤¤leave = "Leaving city for another master.";
	private static CharSequence ¤¤name = "rise up";
	static {
		D.ts(AIModule_Slave.class);
	}
	
	public AIModule_Slave() {
		super(UI.icons().s.slave, ¤¤name, null);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		if (GAME.events().uprising.spots.shouldSignUpUpriser(a))
			return uprise.activate(a, d);
		if (!SETT.ENTRY().isClosed() && FACTIONS.player().slaves().shouldLeave(a))
			return leave.activate(a, d);
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (GAME.events().uprising.spots.shouldSignUpUpriser(a))
			return 8;
		if (!SETT.ENTRY().isClosed() && FACTIONS.player().slaves().shouldLeave(a))
			return 9;
		return 0;
	}
	
	public final AIPLAN leave = new AIPLAN.PLANRES("SLAVE_SOLD") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}
		
		private final Resumer start = new Resumer(¤¤leave) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (SETT.PATH().finders.entryPoints.find(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)) {
					FACTIONS.player().slaves().reserveLeave(a);
					return AI.SUBS().walkTo.path(a, d);
				}
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return fin.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FACTIONS.player().slaves().reserveLeaveCancel(a);
			}
		}; 
		
		private final Resumer fin = new Resumer(¤¤leave) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				HumanoidResource.dead = CAUSE_LEAVES.SOLD();
				FACTIONS.player().slaves().leave(a);
				return AI.SUBS().STAND.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
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
	};

}
