package settlement.entity.humanoid.ai.consume;

import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.equip.WearableResource;
import util.text.D;

final class M_PlanReturn extends AIPLAN.PLANRES{

	private static CharSequence ¤¤sName = "Returning equipment";

	static {
		D.ts(M_PlanReturn.class);
	}
	
	public M_PlanReturn() {
		super("SerEquip");
		// TODO Auto-generated constructor stub
	}

	private final RBITImp bits = new RBITImp();
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		Induvidual i = a.indu();
		bits.clear();
		for (WearableResource e : STATS.EQUIP().allE()) {
			if (e.needed(a.indu()) < 0) {
				bits.or(e.resource(i));
			}
				
		}	
		
		if (bits.isClear())
			return null;
		
		RESOURCE res = SETT.PATH().finders.storage.reserve(a.tc(), bits, d.path, 200);
		
		if (res == null) {
			dump(a, d);
			return null;
		}else {
			d.planByte1 = res.bIndex();
			remOne(a, d, res);
			return walk.set(a, d);
			
		}
		
	}
	
	final Resumer walk = new Resumer(¤¤sName) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.depositInited(a, d, RESOURCES.ALL().get(d.planByte1));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (AIModules.current(d).moduleCanContinue(a, d)) {
				return init(a, d);
			}
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			//Dump
			
		}
	};
	
	private void remOne(Humanoid a, AIManager d, RESOURCE res) {
		for (WearableResource e : STATS.EQUIP().allE()) {
			if (e.needed(a.indu()) < 0 && e.resource(a.indu()) == res) {
				e.inc(a.indu(), -1);	
				return;
			}
		}
	}
	
	private void dump(Humanoid a, AIManager d) {
		Induvidual i = a.indu();
		
		for (WearableResource e : STATS.EQUIP().allE()) {
			int toDump = -e.needed(a.indu());
			if (toDump > 0) {
				e.inc(i, -toDump);
				SETT.THINGS().resources.create(a.physics.tileC(), e.resource(i), toDump);
				
			}
		}
		
	}
	
}
