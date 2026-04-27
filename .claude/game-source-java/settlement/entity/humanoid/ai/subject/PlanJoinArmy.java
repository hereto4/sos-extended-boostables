package settlement.entity.humanoid.ai.subject;

import static settlement.main.SETT.PATH;

import game.battle.div.Div;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import util.text.D;
import world.army.AD;
import world.entity.army.WArmy;

class PlanJoinArmy extends AIPLAN.PLANRES{

	{
		D.t(this);
	}
	
	PlanJoinArmy(){
		super("subJoinArmy");
		
	}
	
	public int getPriority(Humanoid a) {
		
		return SETT.BATTLE().info.shouldJoinArmy(a) ? 10 : 0;
		
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		AISubActivation s = fetchGear.set(a, d);
		if (s != null)
			return s;
		return path.set(a, d);
		
	}
	
	private final Resumer path = new Resumer(D.g("Leaving", "Leaving for the Army")) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.entity.findExitNoEnemies(a, a.physics.tileC().x(), a.physics.tileC().y(), d.path, Integer.MAX_VALUE)) {
				
				return AI.SUBS().walkTo.pathFull(a, d);
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (STATS.BATTLE().DIV.get(a) == null)
				return null;
			dequip(a, d);
			AIManager.dead = CAUSE_LEAVES.ARMY();
			AD.cityDivs().add(a, STATS.BATTLE().DIV.get(a));
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return SETT.BATTLE().info.shouldJoinArmy(a);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			dequip(a, d);
		}
	};
	
	private final Resumer fetchGear = new Resumer("Getting Battlegear") {
		
		final RBITImp bi = new RBITImp();
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			bi.clear();
			Div div = STATS.BATTLE().DIV.get(a);
			if (div == null)
				return null;
			
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				if (amountNeeded(a, d, e) > 0) {
					bi.or(e.resource(a.indu()));
				}
			}
			
			if (bi.isClear())
				return null;
			
			return AI.SUBS().walkTo.resource(a, d, bi, Integer.MAX_VALUE);
		}
		
		private int amountNeeded(Humanoid a, AIManager d, EquipBattle e) {
			Div div = STATS.BATTLE().DIV.get(a);
			if (div == null)
				return 0;
			if (!SETT.BATTLE().info.shouldJoinArmy(a))
				return 0;
			int aa = e.target(div) - e.stat().indu().get(a.indu());
			if (aa <= 0)
				return 0;
			
			WArmy army = AD.cityDivs().attachedArmy(STATS.BATTLE().DIV.get(a));
			if (army == null)
				return 0;
			int am = (int) AD.supplies().equip.get(e.indexMilitary()).minimumTarget(army)-AD.supplies().equip.get(e.indexMilitary()).current().get(army);
			return Math.min(aa, am);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			RESOURCE r = d.resourceCarried();
			Div div = STATS.BATTLE().DIV.get(a);
			if (div == null) {
				can(a, d);
				return null;
			}
			WArmy army = AD.cityDivs().attachedArmy(STATS.BATTLE().DIV.get(a));
			if (army == null) {
				can(a, d);
				return null;
			}
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
				if (amountNeeded(a, d, e) > 0 && e.resource(a.indu()) == r) {
					e.inc(a.indu(), 1);
					d.resourceCarriedSet(null);
					AD.supplies().equip.get(e.indexMilitary()).current().inc(army, 1);
					break;
				}
			}
			d.resourceCarriedSet(null);
			AISubActivation s = set(a, d);
			if (s == null)
				return path.set(a, d);
			return s;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return STATS.BATTLE().DIV.get(a) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			dequip(a, d);
		}
		
	};
	
	private void dequip(Humanoid a, AIManager d) {
		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			e.set(a.indu(), 0);
			d.resourceCarriedSet(null);
		}
	}
	
}
