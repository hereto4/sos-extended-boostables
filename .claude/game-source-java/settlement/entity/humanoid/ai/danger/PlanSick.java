package settlement.entity.humanoid.ai.danger;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import util.text.D;

class PlanSick extends AIPLAN.PLANRES{

	private static CharSequence ¤¤name = "Being sick";
	private final SubPlanSeekHospital ho = new SubPlanSeekHospital(this);
	
	public final SoundRace sound = AUDIO.race("SICK_MOAN");
	
	static {
		D.ts(PlanSick.class);
	}
	
	public PlanSick(String key) {
		super(key);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		if (STATS.DISEASE().shouldHospital(a)) {
			AISubActivation s = ho.init(a, d);
			if (s != null)
				return s;
		}
		if (a.indu().hType().works)
			STATS.WORK().EMPLOYED.set(a, null);
		return res.set(a, d);
	}
	
	private final Resumer res = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if ((STATS.RAN().get(a.indu(), 0) & 0x0FF) > 200) {
				
				HOME h = STATS.HOME().GETTER.get(a, this);
				if (h != null) {
					if (!h.is(a.tc().x(), a.tc().y())) {
						int sx= h.serviceX();
						int sy = h.serviceY();
						return AI.SUBS().walkTo.cooFull(a, d, sx, sy);
						
					}else if (SETT.ENTITIES().hasAtTileHigher(a, a.tc().x(), a.tc().y())){
						for (DIR dir : DIR.ORTHO) {
							int dx = a.tc().x()+dir.x();
							int dy = a.tc().y()+dir.y();
							if (h.is(dx, dy) && !SETT.PATH().solidity.is(dx, dy) && !SETT.ENTITIES().hasAtTileHigher(a, dx, dy)) {
								return AI.SUBS().walkTo.cooFull(a, d, dx, dy);
							}
						}
					}
				}
				
			}
			
			sound.rnd(a);
			
			return AI.SUBS().LAY.activateTime(a, d, 60);
			
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (STATS.DISEASE().diseaseIsDone(a, 0)) {
				if (STATS.DISEASE().shouldDie(a)) {
					AIManager.dead = CAUSE_LEAVES.DISEASE();
					AIManager.deadGore = false;
					return AI.SUBS().STAND.activate(a, d);
				}
				STATS.DISEASE().cure(a.indu(), false);
				return null;
			}
			if (STATS.DISEASE().shouldHospital(a)) {
				AISubActivation s = ho.init(a, d);
				if (s != null)
					return s;
			}
			
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
