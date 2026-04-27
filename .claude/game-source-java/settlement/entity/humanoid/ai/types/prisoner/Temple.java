package settlement.entity.humanoid.ai.types.prisoner;

import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import util.text.D;

class Temple extends AIPLAN.PLANRES{

	public Temple() {
		super("prisTemple");
		// TODO Auto-generated constructor stub
	}

	private final CharSequence ¤¤name = "¤Being Sacrificed";
	
	private final ArrayList<ROOM_TEMPLE> temples = new ArrayList<>(SETT.ROOMS().TEMPLES.ALL.size());
	
	{
		for (ROOM_TEMPLE t : SETT.ROOMS().TEMPLES.ALL)
			if (t.sacrifices())
				temples.add(t);
		D.t(this);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		for (int ti = 0; ti < temples.size(); ti++) {
			ROOM_TEMPLE t = temples.get(ti);
			COORDINATE c = t.sacrificeReserve(a.race());
			if (c == null)
				continue;
			d.planByte1 = (byte) ti;
			d.planTile.set(c);
			return walk.set(a, d);
			
		}
		return null;
	}
	
	private final Resumer walk = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s != null)
				return s;
			cancel(a, d);
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return ready.set(a, d);
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
	
	private final Resumer ready = new Resumer(¤¤name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			a.speed.setDirCurrent(DIR.ALL.rnd());
			temple(a, d).sacrificeSetReady(d.planTile);
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			double k = temple(a, d).sacrificeKillAmount(d.planTile);
			if (k == 1)
				AIManager.dead = CAUSE_LEAVES.SACRIFICED();
			STATS.NEEDS().INJURIES.COUNT.indu().setD(a.indu(), k);
			return AI.SUBS().LAY.activateTime(a, d, 1);
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
	
	private ROOM_TEMPLE temple(Humanoid a, AIManager d) {
		return temples.get(d.planByte1);
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		temple(a, d).sacrificeUnreserve(d.planTile);
		super.cancel(a, d);
	}
	
	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		return temple(a, d).sacrificeReserved(d.planTile) && super.shouldContinue(a, d);
	}

}
