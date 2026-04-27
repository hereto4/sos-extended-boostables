package settlement.entity.humanoid.ai.work;

import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanResourceMany;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.room.main.employment.RoomEquip;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.text.D;

final class PlanFetchEquip extends PlanWork{

	public PlanFetchEquip(String key) {
		super(key);
		// TODO Auto-generated constructor stub
	}

	private final RBITImp bit = new RBITImp();
	private static CharSequence ¤¤fetch = "Fetching Work Equipment";
	
	static {
		D.ts(PlanFetchEquip.class);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		RoomInstance ins = work(a);
		if (ins == null)
			return null;
		
		RoomEmploymentSimple ee = ins.blueprintI().employment();
		if (ee == null)
			return null;
		
		if (ee.tools().size() == 0)
			return null;
		
		bit.clear();
		for (RoomEquip w : ee.tools()) {
			if (ins.employees().toolsNeeded(w) > 0) {
				bit.or(w.resource);
			}
		}
		
		RESOURCE r = SETT.PATH().finders.resource.find(bit, a.tc(), d.path, Integer.MAX_VALUE);

		if (r == null)
			return null;
		
		for (RoomEquip w : ee.tools()) {
			int am = ins.employees().toolsNeeded(w);
			if (am > 0 && r == w.resource) {
				am = CLAMP.i(am, 0, 15);
				ins.employees().toolReserve(w, am);
				return fetch.activateFound(a, d, r, am, true, true);
			}
		}
		
		throw new RuntimeException();
		
	}
	
	private RoomEquip eq(RESOURCE res, RoomInstance ins) {
		for (RoomEquip w : ins.blueprint().employment().tools()) {
			if (res == w.resource) {
				return w;
			}
		}
		throw new RuntimeException();
	}
	
	private final AIPlanResourceMany fetch = new AIPlanResourceMany(this, 64) {
		
		@Override
		public AISubActivation next(Humanoid a, AIManager d) {
			AISubActivation s = toRoom.set(a, d);
			return s;
		}
		
		@Override
		public void cancel(Humanoid a, AIManager d) {
			RoomInstance ins = work(a);
			if (ins == null)
				return;
			RESOURCE res = resource(a, d);
			if (res == null)
				return;
			RoomEquip w = eq(res, ins);
			ins.employees().toolReserve(w, -target(a, d));
			
			
		}
	};
	
	private final Resumer toRoom = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			RoomInstance ins = work(a);
			if (ins == null) {
				can(a, d);
				return null;
			}
			AISubActivation s = AI.SUBS().walkTo.room(a, d, ins);
			if (s != null) {
				return s;
			}else {
				res(a, d);
				return null;
			}
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			fetch.cancel(a, d);
			RoomInstance ins = work(a);
			RoomEquip w = eq(d.resourceCarried(), ins);
			ins.employees().toolDeliver(w, d.resourceA());
			d.resourceCarriedSet(null);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			fetch.cancel(a, d);
			d.resourceDrop(a);
			
		}
	};

	@Override
	protected void name(Humanoid a, AIManager d, Str string) {
		string.add(¤¤fetch);
	}
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.WORKING) {
			return 1.0;
		}
		return super.poll(a, d, e);
	}

	
}
