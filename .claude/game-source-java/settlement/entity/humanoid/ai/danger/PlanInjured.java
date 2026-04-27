package settlement.entity.humanoid.ai.danger;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import util.text.D;

class PlanInjured extends AIPLAN.PLANRES{

	private static CharSequence ¤¤name = "Bleeding out";
	private final SubPlanSeekHospital ho = new SubPlanSeekHospital(this);
	
	public final SoundRace sound = AUDIO.race("IN_PAIN_MOAN");
	
	static {
		D.ts(PlanInjured.class);
	}
	
	public PlanInjured(String key) {
		super(key);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		AISubActivation s = ho.init(a, d);
		if (s != null)
			return s;
		return res.set(a, d);
	}
	
	private final Resumer res = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if (RND.oneIn(10))
				sound.rnd(a);
			return AI.SUBS().LAY.activate(a, d);
			
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (STATS.NEEDS().INJURIES.COUNT.indu().isMax(a.indu())) {
				HumanoidResource.dead = a.lastLeaveCause() != null ? a.lastLeaveCause() : CAUSE_LEAVES.getAccident();
			}else 
			
			if (!STATS.NEEDS().INJURIES.inDanger(a.indu())) {
				return null;
			}
			
			AISubActivation s = ho.init(a, d);
			if (s != null)
				return s;
		
			return set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	



}
