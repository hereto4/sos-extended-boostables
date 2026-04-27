package settlement.entity.humanoid.ai.types.insane;

import static settlement.main.SETT.PATH;

import game.time.TIME;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.type.CAUSE_ARRIVES;
import init.type.CAUSE_LEAVES;
import init.type.HTYPE;
import init.type.HTYPES;
import init.type.NEEDS;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.health.asylum.ROOM_ASYLUM;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.D;

public final class AIModule_Insane extends AIModule{
	


	private static CharSequence ¤¤treatment = "¤In Treatment";
	private static CharSequence ¤¤insane = "¤Being Insane";
	private static CharSequence ¤¤name = "¤Be Insane";
	
	public AIModule_Insane() {
		super(UI.icons().s.crazy, ¤¤name, null);

	}
	
	static{
		D.ts(AIModule_Insane.class);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {

		AiPlanActivation p = asylum.activate(a, d);
		
		if (p != null) {
			return p;
		}
		
		return crazy.activate(a, d);
		
	}
	
	@Override
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		AI.modules().coo(d).set(-1, -1);
	}
	
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		if (newDay && SETT.ROOMS().ASYLUM.isreserved(AI.modules().coo(d))) {
			int chance = (int)(0.5*TIME.years().bitConversion(TIME.days())/SETT.ROOMS().ASYLUM.treatmentFactor(AI.modules().coo(d)));
			if (RND.oneIn(chance)){
				a.HTypeSet(HTYPES.SUBJECT(), null, CAUSE_ARRIVES.CURED());
			}
		}
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		return 8;
	}
	
	private final AIPLAN asylum = new AIPLAN.PLANRES("instaneAsylum") {
		
		private final ROOM_ASYLUM A = SETT.ROOMS().ASYLUM;
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			AI.modules().coo(d).set(-1, -1);
			COORDINATE c = A.registerPrisoner(a);
			if (c != null) {
				AI.modules().coo(d).set(c);
				d.planByte1 = 8;
				return init.set(a, d);
			}
			return null;
		}
		
		@Override
		protected AISubActivation resume(Humanoid a, AIManager d) {
			AISubActivation s = super.resume(a, d);
			if (s == null) {
				SETT.ROOMS().ASYLUM.unregisterPrisoner(AI.modules().coo(d));
				AI.modules().coo(d).set(-1, -1);
			}
			return s;
		};
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			SETT.ROOMS().ASYLUM.unregisterPrisoner(AI.modules().coo(d));
			AI.modules().coo(d).set(-1, -1);
			super.cancel(a, d);
		}
		
		private final Resumer init = new Resumer(¤¤treatment) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				
				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				if (!A.isreserved(AI.modules().coo(d)))
					return null;
				
				if (d.planByte1 <= 0) {
					if (!SETT.PATH().connectivity.is(a.tc())) {
						return unfuck.set(a, d);
					}
					cancel(a, d);;
					return null;
				}
				
				if (!A.isWithinCell(a.tc().x(), a.tc().y(), AI.modules().coo(d))) {
					
					return walkToDoor.set(a, d);
				}
				
				if (NEEDS.TYPES().HUNGER.stat().getPrio(a.indu()) > 0) {
					if (A.eatFood(AI.modules().coo(d))) {
						NEEDS.TYPES().HUNGER.stat().fix(a.indu());
					}
				}
				
				if (TIME.light().nightIs()) {
					return AI.SUBS().subSleep.activate(a, d);
				}
				
				//hunger, popo
				if (RND.oneIn(5)) {
					AISubActivation s = changeSpot.set(a, d);
					if (s != null)
						return s;
				}
				return crazySubsA[RND.rInt(crazySubsA.length)].activate(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return A.isreserved(AI.modules().coo(d));
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
				
			}
		};
		
		private final Resumer walkToDoor = new Resumer(¤¤treatment) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.cooFull(a, d, AI.modules().coo(d));
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return init.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
				
			}
		};
		
		private final Resumer unfuck = new Resumer(¤¤treatment) {
			
			final AISUB untrapp = new AISUB.Simple("insanetrapped") {

				@Override
				public AISTATE resume(Humanoid a, AIManager d) {
					d.subByte ++;
					if (d.subByte != 1)
						return null;
					for (int di = 0; di < DIR.ALL.size(); di++) {
						DIR dir = DIR.ALL.get(di);
						if (PATH().connectivity.is(a.tc(), dir) 
								&& SETT.ROOMS().ASYLUM.isWithinCell(a.tc().x()+dir.x(), a.tc().y()+dir.y(), AI.modules().coo(d))) 
						{
							return AI.STATES().WALK2.dirTile(a, d, dir);
						}
					}
					return AI.STATES().STAND.activate(a, d, 1);
				}
				
			};
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return untrapp.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return init.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
				
			}
		};
		
		private final Resumer changeSpot = new Resumer(¤¤treatment) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				DIR dir = DIR.ORTHO.rnd();
				int dx = a.tc().x() + dir.x();
				int dy = a.tc().y() + dir.y();
				if (A.isWithinCell(dx, dy, AI.modules().coo(d))) {
					if (!SETT.ENTITIES().hasAtTile(dx, dy))
						return AI.SUBS().walkTo.cooFull(a, d, dx, dy);
				}
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return init.set(a, d);
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
		public boolean event(Humanoid a, AIManager d, HEvent.HEventData e) {
			if (e.event == HEvent.ROOM_REMOVED && e.room.is(AI.modules().coo(d))) {
				SETT.ROOMS().ASYLUM.unregisterPrisoner(AI.modules().coo(d));
				AI.modules().coo(d).set(-1, -1);
			}
			return super.event(a, d, e);
		};
	};
	
	private final AIPLAN crazy = new AIPLAN.PLANRES("insaneCrazy") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer(¤¤insane) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (NEEDS.TYPES().HUNGER.stat().getPrio(a) > 0) {
					AISubActivation s = eat.set(a, d);
					if (s != null)
						return s;
					if (STATS.FOOD().STARVATION.indu().getD(a.indu()) > 0) {
						AIManager.dead = CAUSE_LEAVES.STARVED();
					}
				}
				
				if (TIME.light().nightIs())
					return sleep.set(a, d);
				d.planByte1 = (byte) (10 + RND.rInt(10));
				if (SETT.PATH().finders.randomDistanceAway.find(a.tc().x(), a.tc().y(), d.path, 70)) {
					return AI.SUBS().walkTo.pathRun(a, d);
				}
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return res.set(a, d);
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
		
		private final Resumer res = new Resumer(¤¤insane) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 --;
				if (d.planByte1 <= 0)
					return null;
				if (RND.oneIn(10)) {
					return AI.SUBS().walkTo.run_arround_crazy(a, d, 2);
				}
				return crazySubs[RND.rInt(crazySubs.length)].activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return setAction(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, settlement.entity.humanoid.HEvent.HEventData e) {
				if (e.event == HEvent.COLLISION_UNREACHABLE) {
					DIR dd = a.speed.dir();
					if (!dd.isOrtho())
						dd = dd.next(1);
					for (int i = 0; i < 4; i++) {
						if (SETT.PATH().connectivity.is(a.tc(), dd)) {
							break;
						}
						dd = dd.next(2);
						//a.speed.turn90();
					}
					if (SETT.PATH().connectivity.is(a.tc(), dd)) {
						a.speed.setRaw(dd, 0.5);
					}else
						a.speed.magnitudeTargetSet(0);
					
				}
				return super.event(a, d, e);
			};
		};
		
		private final Resumer sleep = new Resumer(¤¤insane) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (TIME.light().nightIs()) {
					return AI.SUBS().subSleep.activate(a, d);
				}
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
		
		private final Resumer eat = new Resumer(¤¤insane) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.resource(a, d, RESOURCES.EDI().mask, Integer.MAX_VALUE);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (d.resourceCarried() != null) {
					STATS.FOOD().eat(a, 1, 0);
					d.resourceAInc(-1);
					return AI.SUBS().STAND.activate(a, d, AI.STATES().anima.box.activate(a, d, 2));
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
		


	};
	
	private final AISUB[] crazySubsA = new AISUB[] {
		new AISUB.Simple("crazy1") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte > 5 && RND.oneIn(5))
					return null;
				a.speed.setDirCurrent(a.speed.dir().next(1));
				return AI.STATES().STAND.activate(a, d, 0.25);
			}
		},
		new AISUB.Simple("crazy2") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte > 5 && RND.oneIn(5))
					return null;
				a.speed.setDirCurrent(a.speed.dir().next(1));
				return AI.STATES().anima.box.activate(a, d, 0.25);
			}
		},
		new AISUB.Simple("crazy3") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte > 5 && RND.oneIn(5))
					return null;
				a.speed.setDirCurrent(a.speed.dir().next(1));
				return AI.STATES().anima.dance.activate(a, d, 0.25);
			}
		},
		new AISUB.Simple("crazy4") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte > 5 && RND.oneIn(5))
					return null;
				a.speed.setDirCurrent(a.speed.dir().next(1));
				return AI.STATES().anima.danceE.activate(a, d, 0.25);
			}
		},
		new AISUB.Simple("crazy5") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte > 5 && RND.oneIn(5))
					return null;
				return AI.STATES().anima.box.activate(a, d, 0.25);
			}
		},
		new AISUB.Simple("crazy6") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte > 5 && RND.oneIn(5))
					return null;
				a.speed.setDirCurrent(a.speed.dir().next(-1+RND.rInt(3)));
				return AI.STATES().anima.armsOut.activate(a, d, 0.25);
			}
		},
		new AISUB.Simple("crazy7") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte > 5 && RND.oneIn(5))
					return null;
				return AI.STATES().anima.lay.activate(a, d, 0.25);
			}
		},

	};
	
	private final AISUB[] crazySubs = new AISUB[crazySubsA.length+1];
	{
		for (int i = 0; i < crazySubsA.length; i++) {
			crazySubs[i] = crazySubsA[i];
		}
		crazySubs[crazySubs.length-1] = new AISUB.Simple("crazyRun") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte > 10 && RND.oneIn(5))
					return null;
				DIR dir = DIR.ORTHO.getC(d.subByte);
				if (SETT.PATH().solidity.is(a.tc(), dir))
					return AI.STATES().anima.lay.activate(a, d, 0.25);
				return AI.STATES().RUN2.dirTile(a,d,dir);
			}
		};
		
	}


	

}
