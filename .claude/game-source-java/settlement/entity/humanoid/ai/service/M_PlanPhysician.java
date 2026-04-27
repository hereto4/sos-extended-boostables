package settlement.entity.humanoid.ai.service;

import init.constant.C;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.health.physician.ROOM_PHYSICIAN;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;

final class M_PlanPhysician extends MPlan<ROOM_PHYSICIAN>{

	public M_PlanPhysician() {
		super("Phys", SETT.ROOMS().PHYSICIANS, false);
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	private final Resumer first = new Resumer("2") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			STATS.POP().NAKED.set(a.indu(), 1);
			DIR dir = blue(d).getLayDir(d.planTile.x(), d.planTile.y());
			a.speed.setDirCurrent(dir);
			int x = d.planTile.x()*C.TILE_SIZE + C.TILE_SIZEH + dir.x()*(C.TILE_SIZEH-2);
			int y = d.planTile.y()*C.TILE_SIZE + C.TILE_SIZEH + dir.y()*(C.TILE_SIZEH-2);
			a.physics.body().moveC(x, y);
			return AI.SUBS().LAY.activateTime(a, d, 25);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			get(a, d).consume();
			can(a, d);
			for (DIR dir : DIR.ORTHO) {
				if (!SETT.PATH().solidity.is(a.tc(), dir)) {
					int x = a.tc().x()*C.TILE_SIZE + C.TILE_SIZEH + dir.x()*(C.TILE_SIZE);
					int y = a.tc().y()*C.TILE_SIZE + C.TILE_SIZEH + dir.y()*(C.TILE_SIZE);
					a.physics.body().moveC(x, y);
					break;
					
				}
			}
			
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return get(a, d) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			STATS.POP().NAKED.set(a.indu(), 0);
			if (blue(d).service().finder.getReserved(d.planTile.x(), d.planTile.y()) != null)
				blue(d).service().finder.getReserved(d.planTile.x(), d.planTile.y()).findableReserveCancel();
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_UNREACHABLE)
				return true;
			return super.event(a, d, e);
		}
	};


	
}
