package settlement.entity.humanoid.ai.battle;

import game.GAME;
import game.battle.div.Div;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIEventListeners;
import settlement.entity.humanoid.ai.main.AIEventListeners.HEventListener;
import settlement.entity.humanoid.ai.main.AIManager;

public class InterBattle {
	
	public final static HEventListener listener = new HEventListener() {

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			switch(e.event) {
			case MEET_HARMLESS:
				return false;
			case COLLISION_SOFT:
				d.interrupt(a, e);
				d.overwrite(a, AI.modules().battle.subSoft.initReady(d, a, e.other, e.norX, e.norY, e.facingDot, e.momentum));
				break;
			case MEET_ENEMY:
				d.interrupt(a, e);
				AI.modules().battle.soundSword.rnd(a);
				d.overwrite(a, AI.modules().battle.subSoft.initReady(d, a, e.other, e.norX, e.norY, e.facingDot, e.momentum));
				break;
				
			case CHECK_MORALE:
				Div div = a.division();
				if (div == null) {
					return AIEventListeners.def.event(a, d, e);
				} else {
					if (div.settings().mustering() && div.morale() <= 0) {
						d.overwrite(a, AI.modules().battle.dessert);
					}
				}
				break;
			case ALERT_DANGER:
				break;
			case COLLISION_TILE:
				if (AI.modules().battle.tile.shouldattackTile(d, a, e.tx, e.ty)) {
					d.overwrite(a, AI.modules().battle.tile.init(d, a, e.tx, e.ty));
					break;
				}else {
					d.interrupt(a, e);
					d.overwrite(a, AI.SUBS().STAND.activateTime(a, d, 1));
				}
				return true;
			case NOTIFY_CRIME:
				break;
			default:
				return AIEventListeners.def.event(a, d, e);
			}
			return false;
		}

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.DEFENCE_SKILL) {
				return GAME.battle().fight.valueDefenceSkill(a, e.facingDot, e.adx, e.ady);
			}
			if (e.type == HPoll.PARRY_SKILL) {
				return GAME.battle().fight.valueParrySkill(a, e.facingDot, e.adx, e.ady);
			}
			return AIEventListeners.def.poll(a, d, e);
		};
		
		
		
		
	};
	
	public static double pollReady(Humanoid a, AIManager d, HPollData e) {
		
		if (e.type == HPoll.IMPACT_DAMAGE) {
			if (e.isEnemy) {
				GAME.battle().fight.setImpactDamage(a, e.colli, e.damage);
			}else {
				e.damage.damagetileStrength = 0;
			}
			return 0;
		}
		if (e.type == HPoll.DEFENCE_SKILL) {
			return GAME.battle().fight.valueDefenceSkill(a, e.facingDot, e.adx, e.ady);
		}
		if (e.type == HPoll.PARRY_SKILL) {
			return GAME.battle().fight.valueParrySkill(a, e.facingDot, e.adx, e.ady);
		}
		
		
		return AIEventListeners.def.poll(a, d, e);
	};
	


	
	
}
