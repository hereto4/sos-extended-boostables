package settlement.entity.humanoid.ai.service;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import init.resources.RES_AMOUNT;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.service.barber.ROOM_BARBER;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class M_PlanBarber extends MPlan<ROOM_BARBER>{

	public M_PlanBarber() {
		super("Barber", SETT.ROOMS().BARBERS, true);
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	final Resumer first = new Resumer("") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte2 = (byte) (5 + RND.rInt(15));
			get(a, d).startUsing();
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (d.planByte2-- < 0) {
				get(a, d).consume();
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					if (!SETT.PATH().solidity.is(d.planTile, DIR.ORTHO.get(di))) {
						for (RES_AMOUNT ra : a.race().resourcesGroom()) {
							FACTIONS.player().res().inc(ra.resource(), RTYPE.PRODUCED, ra.amount());
							SETT.THINGS().resources.create(d.planTile.x(), d.planTile.y(), ra.resource(), ra.amount());
						}
					}
				}
				return null;
			}
			
			DIR dir = blue(d).dir(d.planTile.x(), d.planTile.y());
			
			if (blue(d).service().usageSound != null && RND.oneIn(5))
				blue(d).service().usageSound.rnd(a);
			
			if (RND.rBoolean()) {
				a.speed.setDirCurrent(dir);
				if (RND.rBoolean()) {
					return AI.SUBS().STAND.activateRndDir(a, d, 6);
				}else {
					return AI.SUBS().STAND.activate(a, d, AI.STATES().anima.box.activate(a, d, 3));
				}
			}else {
				dir = dir.next((int) RND.rSign());
				return AI.SUBS().STAND.activateRndDir(a, d, 6);
			}
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			FINDABLE s = get(a, d);
			return s != null && s.findableReservedIs();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FINDABLE s = get(a, d);
			if (s != null)
				s.findableReserveCancel();
		}
	};


	
}
