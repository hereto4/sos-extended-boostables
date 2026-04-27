package settlement.entity.humanoid.ai.idle;

import static settlement.main.SETT.PATH;

import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIEventListeners;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class SubStand extends AISUB.Simple{

	private final Inter inter = new Inter();
	private final AIPLAN plan;
	
	public SubStand(AIPLAN plan, String key) {
		super(key);
		this.plan = plan;
	}
	
	@Override
	public AISTATE resume(Humanoid a, AIManager d) {
		
		if (!a.speed.isZero())
			return AI.STATES().STOP.instant(a, d);
		
		switch(d.subByte) {
		case 0: 
			d.subByte = (1);
			return AI.STATES().WALK2.cTile(a, d);
		case 1:
			d.subByte = (2);
			return AI.STATES().STAND.activate(a, d, 1.0f + RND.rFloat(2.0));
		case 2:
			d.subByte = (100);
			a.speed.setRaw(a.speed.dir().next(1*(RND.rBoolean()?1:-1)), 0);
			return AI.STATES().STAND.activate(a, d, 1.0f + RND.rFloat(2.0));
		default:
			return null;
		}

	}
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		return inter.poll(a, d, e);
	}
	
	@Override
	public boolean event(Humanoid a, AIManager ai, HEventData e) {
		if (e.event == HEvent.MEET_HARMLESS) {
			if (e.other instanceof Humanoid) {
				Humanoid o = (Humanoid) e.other;
				AIManager oai = (AIManager) o.ai();
				if (oai.plan() == plan) {
					return false;
				}
			}
			COORDINATE c = a.physics.tileC();
			DIR d = e.other.speed.dir();
			int dd = RND.rBoolean() ? 1 : -1;
			d = d.next(-2*dd);
			for (int i = 0; i < 5; i++) {
				if (PATH().coster.player.getCost(c.x(), c.y(), c.x()+d.x(), c.y()+d.y()) > 0 && PATH().finders.isGoodTileToStandOn(c.x()+d.x(), c.y()+d.y(), a) && !SETT.ROOMS().map.is(c, d)) {
					a.speed.setDirCurrent(d);
					ai.subPathByte = (byte) d.id();
					ai.interrupt(a, e);
					ai.overwrite(a, sub.activate(a, ai));
					
					return false;
				}
				d = d.next(-dd);
			}
			ai.interrupt(a, e);
			ai.overwrite(a, subMoveaway.activate(a, ai));
		}
		return AIEventListeners.def.event(a, ai, e);
	}

	private final AISUB sub = new AISUB.Simple("standMakeWay", "making way") {
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			d.subByte ++;
			switch (d.subByte) {
			case 1: return AI.STATES().STOP.instant(a, d);
			case 2: return AI.STATES().WALK2.cTile(a, d);
			case 3: return AI.STATES().STAND.activate(a, d, 0.5f);
			case 4: 
				if (d.subPathByte >= DIR.ALL.size() || d.subPathByte < 0)
					return AI.STATES().STOP.activate(a, d);
				a.speed.setDirCurrent(DIR.ALL.get(d.subPathByte));
				return AI.STATES().WALK2.dirTile(a, d, a.speed.dir());
			case 5: a.speed.magnitudeInit(0); return AI.STATES().STOP.activate(a, d);
			case 6: return AI.STATES().STAND.activate(a, d, 15f + RND.rFloat(5f));
			case 7: return AI.STATES().STOP.instant(a, d);
			case 8: return null;
			}
			return null;
		}
		
	};
	
	private final AISUB subMoveaway = new AISUB.Simple("standMoveAway", "making way") {
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			d.subByte ++;
			switch (d.subByte) {
			case 1: 
				PATH().finders.getOutofWay.request(a, d.path);
				return AI.STATES().STOP.instant(a, d);
			case 2: return AI.STATES().STAND.activate(a, d, 1f);
			case 3: 
				if (PATH().finders.getOutofWay.checkAndSetRequest(a.tc().x(), a.tc().y(), d.path)) {
					if (d.path.isSuccessful()) {
						d.overwrite(a, AI.SUBS().walkTo.path(a, d));
					}
				}
			case 4: return AI.STATES().STOP.activate(a, d);
			case 5 :return AI.STATES().STAND.activate(a, d, 3f + RND.rFloat(3f));
			}
			return null;
		}
		
	};

	
}
