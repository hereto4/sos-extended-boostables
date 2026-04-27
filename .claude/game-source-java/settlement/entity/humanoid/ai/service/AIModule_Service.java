package settlement.entity.humanoid.ai.service;

import game.time.TIME;
import init.sprite.UI.UI;
import init.type.HTYPE;
import init.type.NEED;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.stats.Induvidual;
import settlement.stats.service.StatService;
import snake2d.util.rnd.RND;
import util.data.INT_O.INT_OE;
import util.text.D;

public final class AIModule_Service extends AIModule{

	private final INT_OE<AIManager> remaining;
	final S_Plans plans = new S_Plans();

	private static CharSequence ¤¤name = "Service";
	private static CharSequence ¤¤desc = "Do an activity that is on offer in your city.";
	static {
		D.ts(AIModule_Service.class);
	}
	
	public AIModule_Service() {
		super(UI.icons().s.trade, ¤¤name, ¤¤desc);
		remaining = AI.data().new DataNibble("SERVICE_REM");
		
		
		
	}

	public AiPlanActivation get(Humanoid a, AIManager d, NEED need, int dist) {
		for (S_Plan p : plans.needMap.get(need.index())) {
			AiPlanActivation pp = p.getPlan(a, d, dist);
			if (p != null)
				return pp;
		}
		
		return null;
	}
	
	public AiPlanActivation plan(Humanoid a, AIManager d, NEED need, double ran) {
		
		S_Plan s = pservice(a.indu(), need, ran);
		if (s != null)
			return s.getPlan(a, d);
		return null;
	}
	
	public StatService service(Induvidual a, NEED need, double ran) {
		
		S_Plan s = pservice(a, need, ran);
		if (s != null)
			return s.service;
		return null;
		
	}
	
	private S_Plan pservice(Induvidual a, NEED need, double ran) {
		
		if (need == null)
			return null;
		
		double pm = 0;
		
		for (S_Plan p : plans.needMap.get(need.index())) {
			pm += p.usage;
		}
		
		pm *= ran;
		
		for (S_Plan p : plans.needMap.get(need.index())) {
			pm -= p.usage;
			if (pm <= 0)
				return p;
		}
		
		return null;
	}
	
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (remaining.get(d) <= 0)
			remaining.set(d, 1);
		
		while(remaining.get(d) > 0) {
			remaining.inc(d, -1);
			AiPlanActivation p = plans.getPlan(a, d);
			if (p != null)
				return p;
		}
		
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		if (newDay) {
			reset(a, d);
		}
		
	}
	
	@Override
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		reset(a, d);
	}
	
	private void reset(Humanoid a, AIManager d) {
		int am = remaining.get(d);
		int n = RND.rInt(TIME.servicePerDay());
		am += n;
		if (am > TIME.servicePerDay()*2)
			am = TIME.servicePerDay()*2;
		remaining.set(d, am);
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (remaining.get(d) > 0)
			return 3;
		return 0;
	}
	
	
}
