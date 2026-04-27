package settlement.entity.humanoid.ai.service;

import static settlement.main.SETT.PATH;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.stats.STATS;
import settlement.stats.service.StatServiceSimple;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.D;

class PlanSkinny extends S_Plan{

	private static CharSequence ¤¤verb = "¤Skinny dipping";
	
	static {
		D.ts(PlanSkinny.class);
	}
	
	private final StatServiceSimple stat = STATS.SERVICE().skinnyDip;
	
	public PlanSkinny() {
		super(STATS.SERVICE().skinnyDip, 1.0);
	}
	
	@Override
	public boolean hasAccess(Humanoid a, AIManager d) {
		return stat.access(a);
	}

	@Override
	public boolean allowed(Humanoid a, AIManager d) {
		return stat.permission().is(a.indu().popCL());
	}

	@Override
	public boolean goodTime(Humanoid a, AIManager d) {
		if (SETT.WEATHER().ice.canBatheOutside()) {
			return true;
		}
		return false;
	}
	
	private int dist;
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		dist = 128;
		return skinnydip.activate(a, d);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d, int dist) {
		this.dist = dist;
		return skinnydip.activate(a, d);
	}
	
	public final AIPLAN skinnydip = new AIPLAN.PLANRES("ser" + stat.total().key()) {

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			if (SETT.WEATHER().ice.canBatheOutside() && PATH().finders.water.has(a.tc())) {
				return walkToWater.set(a, d);
			}
			return null;
		}
		
		private final AISUB sub = new AISUB.Simple("") {

			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {

				d.subByte++;

				if (!a.speed.isZero())
					a.speed.magnitudeInit(0);

				if (d.subByte > 1) {
					STATS.NEEDS().EXPOSURE.fix(a.indu());
					STATS.NEEDS().DIRTINESS.set(a.indu(), 0);
				}

				if (d.subByte > 20)
					return null;

				if (RND.oneIn(5)) {

					DIR dir = DIR.ALL.get(RND.rInt(DIR.ALL.size()));

					for (int i = 0; i < 8; i++) {
						int x = a.physics.tileC().x() + dir.x();
						int y = a.physics.tileC().y() + dir.y();
						if (SETT.PATH().coster.player.getCost(a.tc().x(), a.tc().y(), x, y) > 0 && PATH().finders.water.get(x, y) != null) {
							return AI.STATES().WALK2.dirTile(a, d, dir);
						}
						dir = dir.next(1);
					}

				}

				if (RND.oneIn(3))
					return AI.STATES().STAND.aDirRND(a, d, 1 + RND.rFloat(2));
				return AI.STATES().LAY.activate(a, d, 1 + RND.rFloat(5));
			}
		};

		private final Resumer walkToWater = new Resumer(¤¤verb) {

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				
				if (PATH().finders.water.reserve(a.physics.tileC(), d.path, dist)) {
					AISubActivation ss = AI.SUBS().walkTo.pathFull(a, d);
					if (ss != null) {
						stat.setAccess(a, true);
						return ss;
					}
					can(a, d);
				}
				stat.setAccess(a, false);
				return null;
				
			}

			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				FINDABLE s = PATH().finders.water.get(d.path.destX(), d.path.destY());
				if (s == null)
					return null;
				if (!s.findableReservedIs()) {
					if (!s.findableReservedCanBe())
						return null;
					s.findableReserve();
				}
				return bathe.set(a, d);
			}

			@Override
			public boolean con(Humanoid a, AIManager d) {
				
				return true;
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE s = PATH().finders.water.getReserved(d.path.destX(), d.path.destY());
				if (s != null)
					s.findableReserveCancel();
				STATS.POP().NAKED.set(a.indu(), 0);
			}
		};

		private final Resumer bathe = new Resumer(¤¤verb) {

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				STATS.POP().NAKED.set(a.indu(), 1);
				STATS.NEEDS().EXPOSURE.fix(a.indu());
				d.planByte1 = (byte) (5 + RND.rInt(10));
				return sub.activate(a, d);
			}

			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				STATS.NEEDS().EXPOSURE.fix(a.indu());
				STATS.NEEDS().DIRTINESS.set(a.indu(), 0);
				
				if (!conn(a, d)) {
					can(a, d);
					return null;
					
				}
				
				if (d.planByte1-- > 0 &&AIModules.current(d) != null && AIModules.current(d).moduleCanContinue(a, d) && SETT.WEATHER().ice.canBatheOutside()) {
					return sub.activate(a, d);
				}
				can(a, d);
				return null;
			}

			private boolean conn(Humanoid a, AIManager d) {
				FINDABLE s = PATH().finders.water.getReserved(d.path.destX(), d.path.destY());
				return s != null && s.findableReservedIs();
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE s = PATH().finders.water.getReserved(d.path.destX(), d.path.destY());
				if (s != null)
					s.findableReserveCancel();
				STATS.POP().NAKED.set(a.indu(), 0);
			}
		};


	};



}
