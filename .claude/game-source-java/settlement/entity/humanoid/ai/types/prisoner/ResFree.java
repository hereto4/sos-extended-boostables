package settlement.entity.humanoid.ai.types.prisoner;

import init.type.CAUSE_ARRIVES;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.PLANRES.Resumer;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.stats.STATS;
import util.text.Dic;

class ResFree {
	
	public static Resumer make(AIPLAN.PLANRES res) {
		return res .new Resumer(Dic.¤¤Free) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {	
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				a.HTypeSet(HTYPES.SUBJECT(), null, CAUSE_ARRIVES.PAROLE());
				STATS.LAW().EX_CON.indu().setD(a.indu(), 1.0);
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
	}
	
}
