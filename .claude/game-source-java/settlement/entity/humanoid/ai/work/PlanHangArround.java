package settlement.entity.humanoid.ai.work;

import static settlement.main.SETT.PATH;

import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.RoomInstance;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.D;

final class PlanHangArround extends PlanWork{

	public PlanHangArround(String key) {
		super(key);
		// TODO Auto-generated constructor stub
	}

	private static CharSequence ¤¤wait = "¤Waiting for work";
	
	static {
		D.ts(PlanHangArround.class);
	}
	
	@Override
	public AISubActivation init(Humanoid a, AIManager d) {
	
		
		
		if (!PATH().reachability.is(work(a).mX(), work(a).mY()))
			return null;
		
		if (work(a).is(a.physics.tileC())) {
			if (RND.rInt(25) != 0)
				return stand.set(a, d);
			return walkIn.set(a, d);
		}
		
		if (work(a).body().width() <= 1 && work(a).body().height() <= 1) {
			if (a.physics.tileC().tileDistanceTo(work(a).mX(), work(a).mY()) < 10) {
				if (RND.rInt(15) != 0)
					return stand.set(a, d);
				return walk.set(a, d);
			}
		}
		
		return walk.set(a, d);
		 
	}
	
	private final Resumer stand = new Resumer(¤¤wait) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (RND.rBoolean())
				return AI.SUBS().STAND.activate(a, d, AI.STATES().anima.wave.activate(a, d, 2));
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
		}
	};
	
	private final Resumer walk = new Resumer(¤¤wait) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.room(a, d, work(a));
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return stand.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
		}
	};
	
	private final Resumer walkIn = new Resumer(¤¤wait) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			RoomInstance r = work(a);
			int di = RND.rInt(DIR.ORTHO.size());
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR dd = DIR.ALL.getC(di+i);
				int dx = a.tc().x()+dd.x();
				int dy = a.tc().y()+dd.y();
				if (r.is(dx, dy)) {
					AVAILABILITY av = SETT.PATH().availability.get(dx, dy);
					if (av.player >= 0 && av.player < AVAILABILITY.Penalty && av.from == 0) {
						return AI.SUBS().walkTo.cooFull(a, d, dx, dy);
					}
				}
			}
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return stand.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
		}
	};
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.WORKING) {
			return 1.0;
		}
		return super.poll(a, d, e);
	}
	
}