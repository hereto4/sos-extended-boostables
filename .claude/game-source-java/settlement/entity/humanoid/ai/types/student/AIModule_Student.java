package settlement.entity.humanoid.ai.types.student;

import init.sprite.UI.UI;
import init.type.HTYPE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.room.knowledge.university.ROOM_UNIVERSITY;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import util.text.D;

public final class AIModule_Student extends AIModule{

	private final Plan plan = new Plan(this);

	private static CharSequence ¤¤name = "Study";
	private static CharSequence ¤¤desc = "Gain some education or indoctrination in a learning facility";
	static {
		D.ts(AIModule_Student.class);
	}
	
	public AIModule_Student(){
		super(UI.icons().s.admin, ¤¤name, ¤¤desc);
		
		
	}
	
	public boolean tryInit(Humanoid h, AIManager d) {
		return getFirstUni(h, d) != null;
	}
	
	public static boolean shouldContinue(Humanoid h, AIManager d) {
		ROOM_UNIVERSITY uu = uni(h);
		if (uu == null)
			return false;
		
		if (!checkUni(h, d, uu))
			return false;
		if (STATS.WORK().EMPLOYED.get(h).employees().isOverstaffed())
			return false;
		if (uu.bonus().get(h.indu()) <= 0)
			return false;
		return true;
	}
	
	static ROOM_UNIVERSITY uni(Humanoid h) {
		RoomInstance ii = STATS.WORK().EMPLOYED.get(h);
		if (ii != null && ii.blueprintI() instanceof ROOM_UNIVERSITY)
			return (ROOM_UNIVERSITY) ii.blueprintI();
		return null;
	}
	
	private ROOM_UNIVERSITY getFirstUni(Humanoid h, AIManager d) {
		
		ROOM_UNIVERSITY best = null;
		double bv = 0;
		for (ROOM_UNIVERSITY u : SETT.ROOMS().UNIVERSITIES) {
			if (u.emp.employable() > 0 && checkUni(h, d, u)) {
				double r = u.learningSpeed*u.bonus().get(h.indu());
				if (r > bv) {
					bv = r;
					best = u;
				}
			}
		}
		return best;
	}
	
	static boolean checkUni(Humanoid h, AIManager d, ROOM_UNIVERSITY u) {
		
//		return u.emp.employable() >= 0 && u.limit.getD(h.race()) > STATS.EDUCATION().TOTAL().getD(h.indu());
//		
		if (u.bonus().get(h.indu()) <= 0)
			return false;
		
		double lim = u.limit.getD(h.race());
		if (lim <= 0)
			return false;
		
		if (lim > STATS.EDUCATION().TOTAL().getD(h.indu()))
			return true;
		
		if (STATS.EDUCATION().policyIndoctor.is(h.race())) {
			return STATS.EDUCATION().EDUCATION.indu().get(h.indu()) > 0;
		}else
			return STATS.EDUCATION().INDOCTRINATION.indu().get(h.indu()) > 0;
		
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		return plan.activate(a, d);
	}

	@Override
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		ROOM_UNIVERSITY u = getFirstUni(a, d);
		u.emp.employ(a);
	}

	private static double lls = 1/16.0;
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		RoomInstance in = STATS.WORK().EMPLOYED.get(a);
		if (in != null && in.blueprintI() instanceof ROOM_UNIVERSITY) {
			ROOM_UNIVERSITY u = (ROOM_UNIVERSITY) in.blueprintI();
			double ls = u.learningSpeed(in, a.indu());
			STATS.EDUCATION().educate(a.indu(), ls*lls);
		}
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (!shouldContinue(a, d))
			return 0;
		return ((ROOM_UNIVERSITY) STATS.WORK().EMPLOYED.get(a).blueprintI()).isTime.is() ? 5 : 0;
	}

}
