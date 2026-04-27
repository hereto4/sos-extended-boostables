package settlement.entity.humanoid.ai.main;

import static settlement.main.SETT.PATH;

import game.GAME;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.EPHYSICS;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import util.text.D;

public final class AIEventListeners {

	public interface HEventListener {
		
		public boolean event(Humanoid a, AIManager d, HEventData e);
		public double poll(Humanoid a, AIManager d, HPollData e);

	}
	{
		D.t(this);
	}
	public AIEventListeners() {

	}
	
	public final static Default def = new Default() {
	};
	
	public interface Default extends HEventListener {

		@Override
		public default boolean event(Humanoid a, AIManager d, HEventData e) {
			switch(e.event) {
			case COLLISION_HARD:
				d.interrupt(a, e);
				d.overwrite(a, AI.listeners().PUSHED.push(d, a, e.momentum));
				return false;
			case COLLISION_SOFT:
				d.interrupt(a, e);
				d.overwrite(a, AI.listeners().STOP.activate(a, d));
				return false;
			case COLLISION_TILE:
				d.interrupt(a, e);
				d.overwrite(a, AI.listeners().STOP_TILE.activate(a, d));
				return true;
			case MEET_ENEMY:
				if (a.division() == null) {
					int tx = a.physics.tileC().x();
					int ty = a.physics.tileC().y();
					if (RND.oneIn(3) && PATH().finders.entity.getFriendlies(a, tx, ty) > PATH().finders.entity.getEnemies(a, tx, ty)/4){
						tmp = e.other;
						d.overwrite(a, AI.listeners().fight);	
					}else {
						d.overwrite(a, AI.plans().runToSafety);
					}
				}else {
					tmp = e.other;
					d.overwrite(a, AI.listeners().fight);	
				}
				return false;
			case MEET_HARMLESS:
				if (a.speed.dot(e.other.speed.nX(), e.other.speed.nY())< 0.5) {
					double t = a.speed.magnitudeTarget()*0.5;
					if (a.speed.magnitude() > t) {
						a.speed.magnitudeInit(t);
					}
				}
				if (a.speed.magnitude() < e.other.speed.magnitude()) {
					double t = a.speed.magnitudeTarget()*0.5;
					if (a.speed.magnitude() > t) {
						a.speed.magnitudeInit(t);
					}
				}
				return false;
			case CHECK_MORALE:
				int tx = a.physics.tileC().x();
				int ty = a.physics.tileC().y();
				if (a.division() == null && GAME.ARMIES().enemy().men() > 0 && PATH().finders.entity.getEnemies(a, tx, ty) > 0) {
					d.overwrite(a, AI.plans().runToSafety);
				}else if (a.division() != null && !AI.modules().battle.is(a, d)) {
					int p = AI.modules().battle.getPriority(a, d);
					if (p > 0) {
						AIModule m = AIModules.current(d);
						if (m == null || m.getPriority(a, d) < p) {
							d.overwrite(a, AI.modules().battle.interrrupt(a, d));
							AIModules.data().currentModule.set(d, AI.modules().battle.index());
						}
					}
					AIModules.data().nextModule.set(d, AI.modules().battle.index());
				}
				return false;
				
			case ALERT_DANGER:
				tmp = null;
				d.overwrite(a, AI.listeners().flee);
				return true;
			case EXHAUST:
				double s = a.speed.magnitudeRelative();
				if (STATS.NEEDS().EXHASTION.indu().getD(a.indu()) > 0.5 && AIModules.current(d).getPriority(a, d) <= 5) {
					d.overwrite(a, AI.listeners().exhausted);
				}
				if (s > 0.75) {
					STATS.NEEDS().EXHASTION.indu().inc(a.indu(), 2);
				}else if(s <= 0.6 && RND.oneIn(16)) {
					STATS.NEEDS().EXHASTION.indu().inc(a.indu(), -1);
				}
				return false;
			case NOTIFY_CRIME:
				if (AI.modules().work.isLawEnforcement(a, d)) {
					tmp = e.other;
					d.overwrite(a, AI.listeners().followCriminal);

				}else if(a.indu().hType() != HTYPES.PRISONER()){
					tmp = e.other;
					d.overwrite(a, AI.listeners().flee);
				}
				return false;
			case MAKE_PRISONER:
				AI.modules().makePrisoner(a, d);
				return true;
			case ROOM_REMOVED:
				return false;
			case PRISON_EXECUTE:
				return false;
			case COLLISION_UNREACHABLE:
				return false;
			case INTERRACT:
				if (HPoll.Handler.canInterract(a, e.other)) {
					d.otherEntitySet((Humanoid) e.other);
					d.overwrite(a, AI.modules().idle.interract());
					return true;
				}
				return false;
			case FISHINGTRIP_OVER:
				return false;
			}
			
				
			return false;
		}

		@Override
		public default double poll(Humanoid a, AIManager d, HPollData e) {
			switch(e.type) {
			case WILL_COLLIDE_WITH:
				if (e.other instanceof Animal) {
					if (!((Animal) e.other).domesticated())
						return 1;
					return 0;
				}
				if (e.other instanceof Humanoid) {
					return ((Humanoid) e.other).indu().hostile() != a.indu().hostile() ? 1 : 0;
				}
				return 0;
			case DEFENCE_SKILL:
				//double def = BOOSTABLES.BATTLE().DEFENCE.get(a.indu())*0.25;
				return 0.1*(0.1 + 0.9*e.facingDot);
			case BATTLE_READY:
				return 0;
			case SCARE_ANIMAL_NOT:
				return 0;
			case IMPACT_DAMAGE:
				return 0;
			case WORKING:
				return 0;
			case IS_SLAVE_READY_FOR_UPRISING:
				return -1;
			case IS_ENEMY:
				if (e.other instanceof Humanoid) {
					Humanoid o = (Humanoid) e.other;
					if (o.indu().hostile() != a.indu().hostile()) {
						return 1;
					}
				}
				return 0;
			case CAN_INTERRACT:
				return 0;
			case COLLIDES:
				return 1;
			case PARRY_SKILL:
				return 0;
			
			
			
			}
			
			return 0;
		}
		
	}
	

	static ENTITY tmp;
	
	public AIPLAN catchCriminal(Humanoid other) {
		tmp = other;
		return followCriminal;
	}
	
	private final AIPLAN fight = new AIPLAN.PLANRES("eventFight") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer(D.g("Fighting")) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.modules().battle.fight(a, d, tmp);
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
				// TODO Auto-generated method stub
				
			}
		};
	};
	
	private final AIPLAN followCriminal = new AIPLAN.PLANRES("eventFloolowCrim") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer(D.g("Catching-Criminal", "Catching Criminal")) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.follow(a, d, tmp, true, (byte) 20);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (AI.SUBS().walkTo.followSucess(a, d)) {
					return knockCriminal.set(a, d);
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
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.MEET_HARMLESS) {
					if (e.other == SETT.ENTITIES().getByID(d.planObject)){
						d.overwrite(a, knockCriminal.set(a, d));
						return true;
					}
				}
				return super.event(a, d, e);
			};
		};
		
		private final Resumer knockCriminal = new Resumer(first.name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				ENTITY e = SETT.ENTITIES().getByID(d.planObject);
				if (e == null || !(e instanceof Humanoid)) {
					return null;
				}
				a.speed.turn2(a.body(), e.body());
				a.speed.magnitudeInit(0);
				HEvent.Handler.makePrisoner(((Humanoid) e));
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.box, AI.STATES().anima.box.time);
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
				// TODO Auto-generated method stub
				
			}
		};
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.NOTIFY_CRIME) {
				return false;
			}else {
				return super.event(a, d, e);
			}
		};
		
	};
	
	
	final AIPLAN flee = new AIPLAN.PLANRES("eventFlee") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer(D.g("Fleeing")) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (tmp == null) {
					return AI.SUBS().walkTo.run_arround_crazy(a, d, 1 + RND.rInt(2));
				}
				return AI.SUBS().walkTo.flee(a, d, tmp);
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
				// TODO Auto-generated method stub
				
			}
		};
		
	};
	
	private final AISUB STOP = new AISUB.Simple("InterStop", D.g("Stopping")) {
		

		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			d.subByte++;
			if (d.subByte == 1)
				return AI.STATES().STOP.activate(a, d, 1f + RND.rFloat());
			return null;
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_SOFT) {
				d.subByte = 0;
			}else if (e.event == HEvent.COLLISION_TILE)
				return true;
			return super.event(a, d, e);
		};
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
//			if (e.type == HPoll.COLLIDING)
//				return !a.speed.isZero() ? 1 : 0;
			return super.poll(a, d, e);
		};
		

		
	};
	
	final AISUB STOP_TILE = new AISUB.Simple("InterConfused", D.g("Confused")) {
		
		@Override
		public AISTATE resume(Humanoid a, AIManager d) {
			if (!a.speed.isZero()) {
				return AI.STATES().STOP.instant(a, d);
			}
			d.subByte += 1;
			if (d.subByte == 5) {
				return AI.STATES().STAND.activate(a, d, 5f);
			}
			if (d.subByte < 5) {
				a.speed.turn2Angle(RND.rInt(45));
				return AI.STATES().STAND.activate(a, d, 0.3f);
			}
			return null;
		}

	};
	
	public final AISUB EXHAUSTED = new AISUB.Simple("InterExhaust", D.g("Out-of-breath", "Out-of-breath")){
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			if (false) {
				//this was fun for kids. Readd for them
			}
//			if (d.subByte++ > 3 && a.inWater && SETT.TERRAIN().WATER.open.is(a.tc())) {
//				HumanoidResource.dead = CAUSE_LEAVES.DROWNED();
//				return null;
//			}
			if (STATS.NEEDS().EXHASTION.indu().getD(a.indu()) > 0.25) {
				return AI.STATES().layStop.activate(a, d, 2+RND.rFloat(15));
			}
			return null;
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.EXHAUST) {
				STATS.NEEDS().EXHASTION.indu().inc(a.indu(), -(1 + RND.rInt(4)));
				return true;
			}else if (e.event == HEvent.COLLISION_TILE)
				return true;
			else if (e.event == HEvent.COLLISION_HARD) {
				d.overwrite(a, AI.STATES().layStop.activate(a, d, 2+RND.rFloat(15)));
				return true;
			}else
				return false;
		};
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.DEFENCE_SKILL)
				return super.poll(a, d, e)*0.2;
			return super.poll(a, d, e);
		};
	};
//	
	private final AIPLAN.PLANRES exhausted = new AIPLAN.PLANRES("interexhaustplan") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			if (a.division() != null)
				a.division().reporter.reportReachable(a, false);
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer(D.g("Out-of-breath", "Out-of-breath")) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return EXHAUSTED.activate(a, d);
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.DEFENCE_SKILL)
					return super.poll(a, d, e)*0.2;
				return super.poll(a, d, e);
			};
		};
		
	};
	
	public final AISUB KNOCK_CRIMINAL = new AISUB.Simple("InterKnockCrim", D.g("criminal", "Handling Criminal")){
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
//			d.subByte ++;
//			if (d.subByte == 1) {
//				return AI.STATES().anima.box.activate(a, d);
//			}else {
//				for (ENTITY e : SETT.ENTITIES().getInProximity(a, 1)) {
//					if (e instanceof Humanoid && ((Humanoid) e).indu().hType() == HTYPE.CRIMINAL) {
//						HEvent.Handler.makePrisoner(((Humanoid) e));
//						return null;
//					}
//				}
//				return null;
//			}
			return null;
		}
		
//		@Override
//		public boolean event(Humanoid a, AIManager d, HEventData e) {
//			if (e.event == HEvent.EXHAUST) {
//				STATS.NEEDS().EXHASTION.inc(a.indu(), -1);
//				return true;
//			}else if (e.event == HEvent.COLLISION_TILE)
//				return true;
//			else if (e.event == HEvent.COLLISION_HARD) {
//				d.overwrite(a, AI.STATES().layStop.activate(a, d, 2+RND.rFloat(15)));
//				return true;
//			}else
//				return false;
//		};
//		
//		@Override
//		public double poll(Humanoid a, AIManager d, HPollData e) {
//			if (e.type == HPoll.COLLIDING)
//				return 0;
//			else if (e.type == HPoll.DEFENCE)
//				return super.poll(a, d, e)*0.2;
//			return super.poll(a, d, e);
//		};
	};
	
	final SubPushed PUSHED = new SubPushed();
	
	final class SubPushed {

		private final double flyForce = 1.5;
		private final CharSequence name = D.g("tackled");
		
		AISubActivation push(AIManager d, Humanoid a, double momentum) {
			
			if (momentum > 5) {
				momentum = 5;
			}
			
			if (a.division() != null)
				a.division().reporter.reportReachable(a, false);
			
			if (momentum > flyForce) {
				AISTATE state = AI.STATES().FLY.activate(a,d, (float)(0.25 + (momentum - flyForce)/3.0));
				AISubActivation ac = subFly.activate(a, d, state);
				return ac;
			}else {
				AISTATE state = AI.STATES().layStop.activate(a, d, 2+RND.rFloat(5));
				AISubActivation ac = subPushed.activate(a, d, state);
				return ac;
			}
		}
		
		private final AISUB.Simple subFly = new AISUB.Simple("InterFly", name) {
			
			
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte++;
				if (d.subByte == 1) {
					return AI.STATES().layStop.activate(a, d, 10+RND.rFloat(30));
				}
				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_TILE) {
					d.subByte = 0;
					return true;
				}else if(e.event == HEvent.COLLISION_HARD) {
					
					double mom = e.momentum*a.physics.getMassI()*EPHYSICS.MOM_TRESHOLDI;
					
					if (mom > flyForce) {
						AISTATE state = AI.STATES().FLY.add(a,d, (float)((mom-flyForce)/3.0));
						d.overwrite(a, state);
						d.subByte = 0;
					}
					return true;
				}
				return false;
			};
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.COLLIDES) {
					return a.physics.getZ() < 100 ? 1 : 0;
				}
				
				if (e.type == HPoll.WILL_COLLIDE_WITH) {
					if (e.other instanceof Humanoid) {
						return ((Humanoid) e.other).indu().hostile() != a.indu().hostile() ? 1 : 0;
					}
					return 0;
				}else if(e.type == HPoll.DEFENCE_SKILL)
					return super.poll(a, d, e)*0.1;
				return 0;
			};
			
		};
		
		private final AISUB.Simple subPushed = new AISUB.Simple("InterPushed", name) {
			
			
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				return null;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_TILE) {
					d.subByte = 0;
					return true;
				}else if(e.event == HEvent.COLLISION_HARD) {
					AI.STATES().layStop.activate(a, d, 2+RND.rFloat(15));
					d.subByte = 0;
					return true;
				}
				return false;
			};
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.COLLIDES)
					if (e.other instanceof Humanoid) {
						return ((Humanoid) e.other).indu().hostile() != a.indu().hostile() ? 1 : 0;
					}
				if (e.type == HPoll.WILL_COLLIDE_WITH) {
					if (e.other instanceof Humanoid) {
						return ((Humanoid) e.other).indu().hostile() != a.indu().hostile() ? 1 : 0;
					}
					return 0;
				}else if(e.type == HPoll.DEFENCE_SKILL)
					return super.poll(a, d, e)*0.2;
				return 0;
			};
			
		};

	}
	
}
