package settlement.entity.humanoid.ai.idle;

import static settlement.main.SETT.PATH;

import game.boosting.BOOSTABLES;
import game.time.TIME;
import init.sprite.UI.UI;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.main.Room;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.D;

public final class AIModule_Idle extends AIModule{



	static CharSequence ¤¤name = "Biding Time";
	static CharSequence ¤¤name2 = "Bide Time";
	static {
		D.ts(AIModule_Idle.class);
	}
	
	public AIModule_Idle() {
		super(UI.icons().s.cancel, ¤¤name2, ¤¤name);
		// TODO Auto-generated constructor stub
	}
	
	private final PlanInterract inter = new PlanInterract();
	
	public AIPLAN interract() {
		return inter.interract;
	}
	
	private final AIPLAN plan = new AIPLAN.PLANRES("idlePlan") {
		
		private final SubStand sub = new SubStand(this, "idlestand");
		private final SubMove walk = new SubMove("idlewalk");
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}

		
		private final Resumer start = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				
				
				
				if (PATH().finders.getOutofWay.shouldFind(a)) {
					PATH().finders.getOutofWay.request(a, d.path);
					return walking.set(a, d);
				}
				
//				if (PATH().finders.getOutofWay.find(a.physics.tileC(), d.path, a)) {
//					return AI.SUBS().walkTo.path(a, d);
//				}
				if (RND.oneIn(10))
					return walk.activate(a, d);
				return sub.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d)) {
					if (RND.oneIn(15))
						return walk.activate(a, d);
					return sub.activate(a, d);
				}
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.CAN_INTERRACT && a.speed.isZero())
					return 1.0;
				return super.poll(a, d, e);
			};
		};
		
		private final Resumer walking = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().STAND.activateRndDir(a, d, 1+RND.rInt(4));
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d)) {
					if (PATH().finders.getOutofWay.checkAndSetRequest(a.tc().x(), a.tc().y(), d.path)) {
						return exit.set(a, d);
					}
					return AI.SUBS().STAND.activateRndDir(a, d, 1+RND.rInt(4));
				}
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
		
		private final Resumer exit = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (d.path.isSuccessful()) {
					return AI.SUBS().walkTo.pathFull(a, d);
				}else {
					if (RND.oneIn(15))
						return walk.activate(a, d);
					return sub.activate(a, d);
				}
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				return null;
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
	};

	private final AIPLAN bench = new AIPLAN.PLANRES("idleBench") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			int r = (int) (STATS.RAN().get(a.indu(), 39) + TIME.hours().bitsSinceStart());
			r &= 0x0FF;
			
			
			
			if (r > (0xFF>>2)) {
				FINDABLE ff = SETT.ROOMS().BENCH.finder.getReservable(a.tc().x(), a.tc().y());
				if (ff != null) {
					ff.findableReserve();
					walk.set(a, d);
					return AI.SUBS().STAND.activateTime(a, d, 1);
				}
				AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, SETT.ROOMS().BENCH.finder, SETT.ROOMS().BENCH.radius());
				if (s != null) {
					d.planTile.set(d.path.destX(), d.path.destY());
					walk.set(a, d);
					return s;
				}
				if (!SETT.ROOMS().map.is(a.tc()) && SETT.FLOOR().getter.is(a.tc())) {
					STATS.SERVICE().bench.clearAccess(a.indu());
				}
			
			}
			return null;
		}

		
		private final Resumer walk = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 16;
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (SETT.ROOMS().BENCH.finder.getReserved(d.planTile.x(), d.planTile.y()) == null) {
					return null;
				}
				STATS.SERVICE().bench.access.indu().set(a.indu(), 1);
				Room bb = (SETT.ROOMS().BENCH.get(d.planTile.x(), d.planTile.y()));
				STATS.SERVICE().bench.setAccess(a.indu(), true, 1.0-bb.getDegrade(d.planTile.x(), d.planTile.y()), bb.upgrade(d.planTile.x(), d.planTile.y()));
				if (d.planByte1 -- < 0 || !moduleCanContinue(a, d)) {
					can(a, d);
					return null;
				}
					
				DIR dir = SETT.ROOMS().BENCH.benchDir(d.planTile.x(), d.planTile.y(), a.speed.dir());
				if (RND.oneIn(4))
					dir = dir.next((int) RND.rSign());
				a.speed.setDirCurrent(dir);
				return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(10));
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FINDABLE s = SETT.ROOMS().BENCH.finder.getReserved(d.planTile.x(), d.planTile.y());
				if (s != null)
					s.findableReserveCancel();
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.CAN_INTERRACT && a.speed.isZero())
					return 1.0;
				return super.poll(a, d, e);
			};
		};
		
	};
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		AiPlanActivation p = bench.activate(a, d);
		if (p != null)
			return p;
		
		
		if (PATH().finders.getOutofWay.shouldFind(a))
			return plan.activate(a, d);
		double b = BOOSTABLES.ACTIVITY().SOCIAL.get(a.indu());
		if (b > 0 && RND.oneIn(5/b)) {
			p = inter.lookForFriend.activate(a, d);
			if (p != null)
				return p;
		}
		return plan.activate(a, d);
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		return 0;
	}

}
