package settlement.entity.humanoid.ai.service;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.service.lavatory.Lavatory;
import settlement.room.service.lavatory.LavatoryInstance;
import settlement.room.service.lavatory.ROOM_LAVATORY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;

class M_PlanLavatory extends MPlan<ROOM_LAVATORY>{

	public M_PlanLavatory() {
		super("Lav", SETT.ROOMS().LAVATORIES, true);
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return takingDump.set(a, d);
	}
	
	private final Resumer takingDump = new Resumer("PlanDischarging") {
		
		private final AISUB sub = new AISUB.Simple("taking a dump") {
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte++;
				
				if (d.subByte > 4 + RND.rInt(5))
					return null;

				if (blue(d).service().usageSound != null && RND.oneIn(2))
					blue(d).service().usageSound.rnd(a);
				
				return AI.STATES().STAND.activate(a, d, 5f);
			}
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			Lavatory l = get(a, d);
			a.speed.setDirCurrent(l.getDir());
			return sub.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			get(a, d).consume();
			return walk2Water.set(a, d);
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
	
	private final Resumer walk2Water = new Resumer("Washing up") {
		
		
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			LavatoryInstance b = blue(d).get(a.physics.tileC().x(), a.physics.tileC().y());
			if (b != null) {
				COORDINATE c = b.getExtra();
				
				if (c != null) {
					AISubActivation s = AI.SUBS().walkTo.coo(a, d, c);
					if (s == null) {
						can(a, d);
						return null;
					}
					return s;
				}
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return washing.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return blue(d).isExtra(d.path.destX(), d.path.destY());
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			if (blue(d).isExtra(d.path.destX(), d.path.destY())) {
				LavatoryInstance b = blue(d).get(d.path.destX(), d.path.destY());
				b.returnExtra(d.path.destX(), d.path.destY());
			}
		}
	};
	
	private final Resumer washing = new Resumer("Washing up") {
		
		private final AISUB sub = new AISUB.Simple("washing") {
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte++;
				
				if (d.subByte > 1)
					return null;
				
				return AI.STATES().anima.box.activate(a, d, 15f);
			}
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return sub.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			can(a, d);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return blue(d).isExtra(d.path.destX(), d.path.destY());
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			if (blue(d).isExtra(d.path.destX(), d.path.destY())) {
				LavatoryInstance b = blue(d).get(d.path.destX(), d.path.destY());
				b.returnExtra(d.path.destX(), d.path.destY());
			}
		}
	};
	
	@Override
	protected Lavatory get(Humanoid a, AIManager d) {
		return blue(d).getService(d.planTile.x(), d.planTile.y());
	}
}
