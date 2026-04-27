package settlement.entity.humanoid.ai.consume;

import init.race.RACES;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.type.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanResourceMany;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.equip.WearableResource;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.text.D;

final class M_PlanEquip extends AIPLAN.PLANRES{
	
	private static CharSequence ¤¤name = "Getting equipment";
	static {
		D.ts(M_PlanEquip.class);
	}
	
	
	private final RBITImp bits = new RBITImp();
	
	public M_PlanEquip() {
		super("serEquip");
	}
	
	static {
		D.ts(M_PlanEquip.class);
	}
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		bits.clear();
		for (WearableResource e : RACES.res().all(a.indu().popCL())) {
			int needed = e.needed(a.indu());
			if (needed > 0) {
				bits.or(e.resource(a.indu()));
			}
		}
		
		if (!bits.isClear()) {
			RESOURCE res = SETT.PATH().finders.resource.find(bits, a.tc().x(), a.tc().y(), d.path, 100);
			if (res != null) {
				
				int nn = 0;
				for (WearableResource rr : RACES.res().get(a.indu().popCL(), res)) {
					if (rr.resource(a.indu()) == res) {
						int n = rr.needed(a.indu());
						if (n > 0)
							nn += n;
					}
				}
					
				if (nn <= 0) {
					throw new RuntimeException(res + " " + RACES.res().get(a.indu().popCL(), res).size());
				}
				AISubActivation s = fetch.activateFound(a, d, res, nn, true, true);
				return s;
			}
		}
		
		NEEDS.TYPES().SHOPPING.stat().fixMax(a.indu());
			
		
		for (WearableResource e : RACES.res().all(a.indu().popCL())) {
			e.wearOut(a.indu());
		}
		return null;
	}

	@Override
	protected void name(Humanoid a, AIManager d, Str string) {
		string.add(¤¤name);
	}
	
	private final AIPlanResourceMany fetch = new AIPlanResourceMany(this, 64) {
		
		@Override
		public AISubActivation next(Humanoid a, AIManager d) {
			RESOURCE res = d.resourceCarried();
			int am = d.resourceA();
			Induvidual i = a.indu();
			
			if (res == null || am <= 0)
				return null;
			
			for (WearableResource r : RACES.res().get(i.popCL(), res)) {
				r.wearOut(i);
				int dam = CLAMP.i(am, 0, r.needed(a.indu()));
				r.inc(i, dam);
				am -= dam;
				d.resourceAInc(-dam);
				if (am <= 0)
					break;
			}
			
			if (AIModules.current(d).moduleCanContinue(a, d))
				return init(a, d);
			return null;
		}
		
		@Override
		public void cancel(Humanoid a, AIManager d) {
			
		}
	};
	
}
