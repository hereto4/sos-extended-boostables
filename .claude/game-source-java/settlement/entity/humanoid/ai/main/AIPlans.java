package settlement.entity.humanoid.ai.main;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TERRAIN;

import game.GAME;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.NEEDS;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.D;

public final class AIPlans {

	private static CharSequence ¤¤swimming = "¤Swimming";
	private static CharSequence ¤¤trapped = "¤Cut off from the throne";
	private static CharSequence ¤¤WalkingToCentre = "¤Walking to the center of the city";
	private static CharSequence ¤¤Fleeing = "¤Fleeing";
	
	static {
		D.ts(AIPlans.class);
	}
	
	
	public final AIPLAN unreachable = new AIPLAN.PLANRES("planUnreach") {
		
		@Override
		public AISubActivation init(Humanoid a, AIManager d) {
			
			if (NEEDS.TYPES().HUNGER.stat().stat().indu().isMax(a.indu()))
				AIManager.dead = CAUSE_LEAVES.STARVED();
			
			if (SETT.PATH().finders().reachable.find(a.tc(), d.path, 8)) {
				return path.set(a, d);
			}
			
			STATS.POP().TRAPPED.indu().set(a.indu(), 1);
			if (a.division() != null) {
				a.division().reporter.reportReachable(a, false);
			}
			if (TERRAIN().WATER.DEEP.is(a.tc()))
				return drowning.set(a, d);
			else {
				return start.set(a, d);
			}

			
		}

		private final Resumer path = new Resumer(¤¤trapped) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.path(a, d);
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				if (TERRAIN().WATER.DEEP.is(a.tc()))
					return drowning.set(a, d);
				if (!PATH().connectivity.is(a.physics.tileC()))
					AIManager.dead = CAUSE_LEAVES.DROWNED();
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
		
		private final Resumer drowning = new Resumer(¤¤swimming) {

			private AISUB sub = new AISUB.Simple("UNR_DROWN") {
				
				@Override
				public AISTATE resume(Humanoid a, AIManager d) {
					
					
					
					if (d.subByte > 20 && RND.oneIn(3))
						return null;
					else
						d.subByte++;
						
					return AI.STATES().STAND.aDirRND(a, d, (float) (0.2+RND.rFloat(0.3)));
				}
			};
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return sub.activate(a, d);
			}

			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				AIManager.dead = CAUSE_LEAVES.DROWNED();
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
		
		private AISUB sub = new AISUB.Simple("UNR_DROWN2") {

			@Override
			public AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte != 1)
					return null;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR dir = DIR.ALL.get(di);
					if (PATH().connectivity.is(a.tc(), dir)) {
						if (!dir.isOrtho() 
								&& !PATH().availability.get(a.tc().x()+dir.x(), a.tc().y()).tileCollide
							&& !PATH().availability.get(a.tc().x(), a.tc().y()+dir.y()).tileCollide) {
							return AI.STATES().WALK2.dirTile(a, d, dir);
						}else if(dir.isOrtho() && !PATH().availability.get(a.tc().x()+dir.x(), a.tc().y()+dir.y()).tileCollide){
							return AI.STATES().WALK2.dirTile(a, d, dir);
						}
					}
					
				}
				switch(RND.rInt(3)) {
				case 0:return AI.STATES().STAND.activate(a, d, 0.5f+RND.rFloat(5));
				case 1:return AI.STATES().anima.wave.activate(a, d, 0.5f+RND.rFloat(5));
				}
				if (a.division() != null) {
					a.setDivision(null);
					
				}
				if (a.indu().hType().hostile)
					STATS.BATTLE().ROUTING.indu().set(a.indu(), 1);
				return AI.STATES().anima.box.activate(a, d, 0.5f+RND.rFloat(5));
			}
			
		};

		private final Resumer start = new Resumer(¤¤trapped) {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 0;
				return AI.SUBS().STAND.activate(a, d);
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				
				d.planByte1++;
				if (d.planByte1 > 5)
					return null;
				if (PATH().connectivity.is(a.physics.tileC())) {
//					STATS.POP().TRAPPED.indu().set(a.indu(), 0);
//					if (a.division() != null)
//						a.division().reporter.reportReachable(a.divSpot(), true);
					return null;
				}
//				
//				if (a.division() != null)
//					a.division().reporter.reportReachable(a.divSpot(), false);
				return sub.activate(a, d);
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
		protected void cancel(Humanoid a, AIManager d) {
			super.cancel(a, d);
			//STATS.POP().TRAPPED.indu().set(a.indu(), 0);
		};
	}; 
	
	
	public final AIPLAN NOP = new AIPLAN.PLANRES("planNop") {


		private final Resumer start = new Resumer("") {

			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().STAND.activateTime(a, d, 0);
			}

			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
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

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return false;
		};
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return 0;
		};
	};
	
	final AIPLAN dead = new AIPLAN.PLANRES("planDead") {
		
		private final AISUB dead = new AISUB.Simple("plandead") {
			
			@Override
			public AISTATE resume(Humanoid a, AIManager d) {
				return AI.STATES().layStop.activate(a, d, 100);
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return false;
			};
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return 0;
			};
		};
		
		final Resumer r = new Resumer("dead") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return dead.activate(a, d);
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				return dead.activate(a, d);
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
		
		@Override
		public AISubActivation init(Humanoid a, AIManager d) {
			return r.set(a, d);
		}

	};
	
	private final AIPLAN deadWhenStill = new AIPLAN.PLANRES("planDeadWhenStil") {
		
		private final AISUB dead = new AISUB.Simple("planDeadWhenStil") {
			
			@Override
			public AISTATE resume(Humanoid a, AIManager d) {
				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return false;
			};
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return 0;
			};
		};
		
		final Resumer r = new Resumer("dead") {
			
			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				return AISubActivation.make(dead, AI.STATES().layStop.activate(a, d, 0.5));
			}
			
			@Override
			public AISubActivation res(Humanoid a, AIManager d) {
				if (a.speed.isZero()) {
					AIManager.dead = CAUSE_LEAVES.ALL().get(d.subByte);
				}
				return AISubActivation.make(dead, AI.STATES().layStop.activate(a, d, 0.5));
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
		
		@Override
		public AISubActivation init(Humanoid a, AIManager d) {
			return r.set(a, d);
		}

	};
	
	public AiPlanActivation deadWhenStill(Humanoid a, AIManager d, CAUSE_LEAVE l, boolean gore) {
		d.subByte = (byte) l.index();
		return deadWhenStill.activate(a, d);
	};
	
	final AIPLAN GoToThrone = new AIPLAN.PLANRES("planGoThrone") {
		
		final Resumer go = new Resumer(¤¤WalkingToCentre) {

			@Override
			public AISubActivation setAction(Humanoid a, AIManager d) {
				if (a.physics.tileC().tileDistanceTo(THRONE.coo()) < 30)
					return AI.SUBS().STAND.activate(a, d);
				COORDINATE c = PATH().finders.arround.find(THRONE.coo().x(), THRONE.coo().y(), 5, 20);
				if (c == null)
					return AI.SUBS().STAND.activate(a, d);
				AISubActivation s = AI.SUBS().walkTo.coo(a, d, c);
				return trySub(a, d, s, null);
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
		
		@Override
		public AISubActivation init(Humanoid a, AIManager d) {
			return go.set(a, d);
		}
	};
	
	final AIPLAN runToSafety = new AIPLAN.PLANRES("planToSafty"){

		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return pathing.set(a, d);
		}
		
		private final Resumer pathing = new Resumer(¤¤Fleeing) {
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_SOFT) {
					int ri = RND.rInt(DIR.ORTHO.size());
					for (int i = 0; i < DIR.ORTHO.size(); i++) {
						DIR dd = DIR.ORTHO.getC(ri+i);
						if (SETT.PATH().cost.get(a.tc().x(), a.tc().y(), dd) > 0) {
							a.speed.turn2(dd);
							return true;
						}
					}
					a.speed.turn2(-e.norX, -e.norY);
					d.overwrite(a, run.set(a, d));
					return true;
				}else if (e.event == HEvent.MEET_ENEMY) {
					a.speed.turn2(-e.norX, -e.norY);
					d.overwrite(a, run.set(a, d));
				}else if (e.event == HEvent.COLLISION_TILE) {
					d.overwrite(a, run.set(a, d));
					d.overwrite(a, AI.STATES().STOP.activate(a, d));
					return true;
				}else if (e.event == HEvent.EXHAUST) {
					return super.event(a, d, e);
				}else if (e.event == HEvent.COLLISION_HARD) {
					return super.event(a, d, e);
				}
				return false;
			}
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (PATH().finders.entity.findSafety(a, a.physics.tileC().x(), a.physics.tileC().y(), d.path, 100)) {
					return AI.SUBS().walkTo.pathRun(a, d);
				}
				a.speed.turnWithAngel(RND.rFloat0(90));
				
				return run.set(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 1)
					return null;
				if (GAME.ARMIES().enemy().men() > 0) {
					a.speed.turnWithAngel(RND.rInt0(90));
					return AI.SUBS().STAND.activateTime(a, d, 3+RND.rInt(3));
					
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
		
		private final Resumer run = new Resumer(¤¤Fleeing) {
			
			private final AISUB sub = new AISUB.Simple("PlansFlee") {
				@Override
				protected AISTATE resume(Humanoid a, AIManager d) {
					
					d.planByte1--;
					
					if (d.planByte1 > 0)
						return AI.STATES().RUN.activate(a, d, 4f + RND.rFloat()*3);
					return null;
				}
				
				@Override
				protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
					if (d.planByte1 == 0)
						return null;
					d.planByte1 = 0;
					return AI.STATES().RUN.activate(a, d, 4f + RND.rFloat()*3);
				};
			};
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte2 = (byte) (2 + RND.rInt(5));
				d.planByte1 = (byte) (2 + RND.rInt(5));
				return sub.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (STATS.BATTLE().ROUTING.indu().get(a.indu()) == 1)
					return null;
				if (GAME.ARMIES().enemy().men() > 0) {
					return pathing.set(a, d);
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
				// TODO Auto-generated method stub
				return super.poll(a, d, e);
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_SOFT) {
					int ri = RND.rInt(DIR.ORTHO.size());
					for (int i = 0; i < DIR.ORTHO.size(); i++) {
						DIR dd = DIR.ORTHO.getC(ri+i);
						if (SETT.IN_BOUNDS(a.tc(), dd) &&  SETT.PATH().cost.get(a.tc().x(), a.tc().y(), dd) > 0) {
							a.speed.turn2(dd);
							d.overwrite(a, AI.STATES().STOP.activate(a, d));
						}
					}
					a.speed.turn2(-e.norX, -e.norY);
					d.overwrite(a, AI.STATES().STOP.activate(a, d));
					return true;
				}else if (e.event == HEvent.MEET_ENEMY) {
					a.speed.turn2(-e.norX, -e.norY);
					d.overwrite(a, AI.STATES().STOP.activate(a, d));
				}else if (e.event == HEvent.COLLISION_TILE) {
					d.planByte2--;
					if (d.planByte2 < 0) {
						d.overwrite(a, pathing.set(a, d));
						return true;
					}
					
					
					d.overwrite(a, AI.STATES().STOP.activate(a, d));
					return true;
				}else if (e.event == HEvent.EXHAUST) {
					return super.event(a, d, e);
				}else if (e.event == HEvent.COLLISION_HARD) {
					return super.event(a, d, e);
				}
				return false;
			}
		};

	};

	
	
}
