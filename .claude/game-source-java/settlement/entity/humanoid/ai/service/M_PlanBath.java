package settlement.entity.humanoid.ai.service;

import static settlement.main.SETT.ENTITIES;

import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.service.hygine.bath.Bath;
import settlement.room.service.hygine.bath.BathInstance;
import settlement.room.service.hygine.bath.ROOM_BATH;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class M_PlanBath extends MPlan<ROOM_BATH>{

	public M_PlanBath() {
		super("Bath", SETT.ROOMS().BATHS, true);
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	private final AISUB sub = new AISUB.Simple("Bathing") {

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {

			if (!a.speed.isZero())
				a.speed.magnitudeInit(0);

			d.subByte++;

			if (d.subByte > 20) {
				cancel(a, d);
				return null;
			}

			if (d.subByte == 1) {
				for (DIR dir : DIR.ORTHO) {
					int x = a.physics.tileC().x() + dir.x();
					int y = a.physics.tileC().y() + dir.y();
					if (ROOM_BATH.isPool(x, y))
						return AI.STATES().WALK2.dirTile(a, d, dir);
				}
				d.debug(a, "No bath!");
				return AI.STATES().STAND.aDirRND(a, d, 1 + RND.rFloat(2));
			}

			if (d.subByte > 1) {
				STATS.POP().NAKED.set(a.indu(), 0);
			}

			return AI.STATES().STAND.aDirRND(a, d, 1 + RND.rFloat(2));

		}
		
		@Override
		public boolean event(Humanoid a, AIManager ai, HEventData e) {
			if (e.event == HEvent.MEET_HARMLESS) {
				if (a.speed.isZero()) {
					DIR d = DIR.ORTHO.get(RND.rInt(4));
					for (int i = 0; i < DIR.ORTHO.size(); i++) {
						int x = a.physics.tileC().x() + d.x();
						int y = a.physics.tileC().y() + d.y();
						if (ROOM_BATH.isPool(x, y) && !ENTITIES().hasAtTile(a, x, y)) {
							ai.overwrite(a, AI.STATES().WALK2.tile(a, ai, x, y));
						}
						d = d.next(2);
					}

				}
				return false;
			}
			return super.event(a, ai, e);
		};

	};

	private final Resumer first = new Resumer("1") {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			STATS.POP().NAKED.set(a.indu(), 1);
			return sub.activate(a, d);
		}

		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			can(a, d);
			
			STATS.NEEDS().EXPOSURE.fix(a.indu());
			STATS.NEEDS().DIRTINESS.set(a.indu(), 0);
			return walk2Bench.set(a, d);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			FINDABLE s = blue(d).bath(d.planTile.x(), d.planTile.y());
			return s != null && s.findableReservedIs();
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			Bath s = blue(d).bath(d.planTile.x(), d.planTile.y());
			if (s != null && s.findableReservedIs()) {
				s.consume();
			}
			STATS.POP().NAKED.set(a.indu(), 0);
		}
	};
	private final Resumer walk2Bench = new Resumer("2") {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			BathInstance b = blue(d).get(a.physics.tileC().x(), a.physics.tileC().y());
			if (b != null) {
				COORDINATE c = b.getBench();
				if (c != null) {
					STATS.POP().NAKED.set(a.indu(), 1);
					return trySub(a, d, AI.SUBS().walkTo.cooFull(a, d, c), null);
				}
			}
			return null;
		}

		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return relax.set(a, d);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return blue(d).isBench(d.path.destX(), d.path.destY());
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			BathInstance b = blue(d).get(a.physics.tileC().x(), a.physics.tileC().y());
			if (b != null) {
				b.returnBench(d.path.destX(), d.path.destY());
			}

			STATS.POP().NAKED.set(a.indu(), 0);
		}
	};
	private final Resumer relax = new Resumer("3") {

		final AISUB sub = new AISUB.Simple("Relaxing") {

			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte++;
				if (d.subByte == 1)
					return AI.STATES().anima.layoff.activate(a, d, 10 + RND.rInt(20));
				return null;
			}
		};

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			DIR dir = blue(d).getBenchDir(a.physics.tileC().x(), a.physics.tileC().y());
			a.speed.setDirCurrent(dir.perpendicular());
			return sub.activate(a, d);
		}

		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			can(a, d);
			return null;
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return blue(d).isBench(d.path.destX(), d.path.destY());
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			BathInstance b = blue(d).get(a.physics.tileC().x(), a.physics.tileC().y());
			if (b != null) {
				b.returnBench(d.path.destX(), d.path.destY());
			}
			STATS.POP().NAKED.set(a.indu(), 0);
		}
	
	};
	
}
