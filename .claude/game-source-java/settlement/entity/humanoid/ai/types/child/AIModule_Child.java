package settlement.entity.humanoid.ai.types.child;

import java.util.Arrays;
import java.util.Comparator;

import game.time.TIME;
import init.constant.C;
import init.race.RACES;
import init.type.CAUSE_LEAVES;
import init.type.HCLASSES;
import init.type.HTYPE;
import init.type.HTYPES;
import init.type.NEEDS;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
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
import settlement.misc.util.FSERVICE;
import settlement.room.knowledge.school.ROOM_SCHOOL;
import settlement.room.main.Room;
import settlement.room.service.nursery.ROOM_NURSERY;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.text.D;

public final class AIModule_Child extends AIModule{
		
	static CharSequence ¤¤play = "¤Playing";
	static CharSequence ¤¤sleep = "¤Sleeping";
	static CharSequence ¤¤Homeless = "¤Homeless";
	static CharSequence ¤¤Studying = "¤Studying";
	static CharSequence ¤¤WarmingUp = "¤Staying indoors";
	private final ROOM_NURSERY[] rooms = new ROOM_NURSERY[RACES.all().size()];
	
	static{
		D.ts(AIModule_Child.class);
	}
	
	public AIModule_Child() {
		super(HCLASSES.CHILD().icon(), ¤¤play, null);
		for (ROOM_NURSERY r : SETT.ROOMS().NURSERIES) {
			rooms[r.race.index] = r;
		}
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		
		
		ROOM_NURSERY n = rooms[a.race().index];
		if (n == null)
			return starve.activate(a, d);
		
		if (!checkSpot(a, d) && !getSpot(a, d))
			return starve.activate(a, d);
		
		if (TIME.light().nightIs() || NEEDS.TYPES().HUNGER.stat().getPrio(a.indu()) > 0 || STATS.NEEDS().EXPOSURE.COUNT.indu().get(a.indu())> 0) {
			return sleep.activate(a, d);
		}else {
			if (TIME.light().partOf() < 0.4 && STATS.EDUCATION().canEducateChild(a.indu())) {
				AiPlanActivation p = school.activate(a, d);
				if (p != null)
					return p;
			}
			return play.activate(a, d);
		}
	}

	private boolean checkSpot(Humanoid a, AIManager d) {
		ROOM_NURSERY n = rooms[a.race().index];
		if (n == null)
			return false;
		COORDINATE c = AI.modules().coo(d);
		if (n.childIsReservedSpot(c.x(), c.y(), a.race())) {
			return true;
		}
		n.childCancelSpot(c.x(), c.y());
		AI.modules().coo(d).set(-1, -1);
		return false;
	}
	
	private boolean checkReady(Humanoid a, AIManager d) {
		if (checkSpot(a, d)) {
			ROOM_NURSERY n = rooms[a.race().index];
			COORDINATE c = AI.modules().coo(d);
			if (n.childIsReservedAndUsableSpot(c.x(), c.y(), a.race())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean getSpot(Humanoid a, AIManager d) {
		ROOM_NURSERY n = rooms[a.race().index];
		COORDINATE s = n.childGetAndReserveSpot(a.tc().x(), a.tc().y());
		if (s != null) {
			AI.modules().coo(d).set(s);
			return true;
		}
		return false;
	}
	
	public static boolean isResident(Humanoid h, int tx, int ty) {
		if (h.indu().hType() != HTYPES.CHILD())
			return false;
		Room r = SETT.ROOMS().map.get(AI.modules().coo((AIManager) h.ai()));
		if (r == null)
			return false;
		if (!r.isSame(tx, ty, tx, ty))
			return false;
		return true;
	}
	
	@Override
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		d.planTile.set(-1, -1);
	}

	@Override
	protected void cancel(Humanoid a, AIManager d) {
		ROOM_NURSERY n = rooms[a.race().index];
		if (n != null) {
			COORDINATE c = AI.modules().coo(d);
			n.childCancelSpot(c.x(), c.y());
			AI.modules().coo(d).set(-1, -1);
		}
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		return 5;
	}
	
	private abstract class Plan extends AIPLAN.PLANRES {
		
		public Plan(String key) {
			super(key);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.ROOM_REMOVED) {
				if (e.room.is(AI.modules().coo(d)))
					AI.modules().coo(d).set(-1, -1);
				return true;
			}
			return super.event(a, d, e);
		}
	}
	
	private final AIPLAN play = new Plan("childPlay") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			d.planByte1 = 0;
			return stand.set(a, d);
		}
		
		private final Resumer stand = new Resumer(¤¤play) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				
				Humanoid f = getFriend(d);
				if (f != null && RND.oneIn(2)) {
					if (d.path.request(a.tc(), f.tc()))
						return AI.SUBS().walkTo.pathRun(a, d);
				}
				
				if (SETT.PATH().finders.randomDistanceAway.find(a.tc().x(), a.tc().y(), d.path, 100)) {
					return AI.SUBS().walkTo.pathRun(a, d);
				}
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (RND.oneIn(8))
					return null;
				if (RND.oneIn(4))
					return AI.SUBS().LAY.activateRndDir(a, d);
				return AI.SUBS().STAND.activateRndDir(a, d);
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
		
		private Humanoid getFriend(AIManager d) {
			int i = d.planObject;
			ENTITY e = SETT.ENTITIES().getByID(i);
			if (e != null && e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				if (h.indu().hType() == HTYPES.CHILD())
					return h;
			}
			d.planObject = -1;
			return null;
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.MEET_HARMLESS) {
				if (e.other instanceof Humanoid) {
					Humanoid h = (Humanoid) e.other;
					if (h.indu().hType() == HTYPES.CHILD()) {
						if (getFriend(d) == null || RND.oneIn(8))
							d.planObject = h.id();
					}
				}
				
			}
			
			if (e.event == HEvent.COLLISION_SOFT || e.event == HEvent.COLLISION_HARD)
				d.planByte1 = (byte) CLAMP.i(d.planByte1+1, 0, 3);
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
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (d.planByte1 < 3 && e.type == HPoll.WILL_COLLIDE_WITH && e.other instanceof Humanoid && ((Humanoid)e.other).indu().hType() != HTYPES.CHILD() && RND.oneIn(8))
				return 1;
			return super.poll(a, d, e);
		};
	};
	
	private final AIPLAN sleep = new Plan("childSleep") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return walk.set(a, d);
		}
		
		private final Resumer walk = new Resumer(¤¤sleep) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.cooFull(a, d, AI.modules().coo(d));
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (checkReady(a, d)) {
					if (NEEDS.TYPES().HUNGER.stat().getPrio(a.indu()) > 0)
						return eat.set(a, d);
				}
				if (TIME.light().nightIs())
					return sleep.set(a, d);
				return recoperate.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return checkSpot(a, d);
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		private final Resumer eat = new Resumer(¤¤sleep) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (checkReady(a, d)) {
					return AI.SUBS().WORK_HANDS.activate(a, d, 7);
				}
				return sleep.set(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				ROOM_NURSERY n = rooms[a.race().index];
				COORDINATE c = AI.modules().coo(d);
				n.childUseSpot(c.x(), c.y(), true);
				NEEDS.TYPES().HUNGER.stat().fix(a.indu());
				if (RND.oneIn(4))
					STATS.NEEDS().DIRTINESS.set(a.indu(), 0);
				return sleep.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return checkSpot(a, d);
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
		};
		

		
		private final Resumer sleep = new Resumer(¤¤sleep) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (checkReady(a, d)) {
					ROOM_NURSERY n = rooms[a.race().index];
					COORDINATE c = AI.modules().coo(d);
					DIR dir = n.childSleepDir(c.x(), c.y());
					
					int cx = ((c.x()) << C.T_SCROLL)+C.TILE_SIZEH+dir.x()*C.TILE_SIZEH;
					int cy = ((c.y()) << C.T_SCROLL)+C.TILE_SIZEH+dir.y()*C.TILE_SIZEH;
					a.physics.body().moveC(cx, cy);
					a.speed.setRaw(dir, 0);
					d.planByte1 = 5;
				}
				return AI.SUBS().subSleep.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				STATS.NEEDS().EXPOSURE.fix(a.indu());
				if (NEEDS.TYPES().HUNGER.stat().getPrio(a.indu()) > 0) {
					if (checkReady(a, d)) {
						return eat.set(a, d);
					}
					if (d.planByte1 <= 0)
						return null;
					d.planByte1--;
					return AI.SUBS().single.activate(a, d, AI.STATES().anima.wave, 5);
				}
				if (TIME.light().dayIs()) {
					ROOM_NURSERY n = rooms[a.race().index];
					COORDINATE c = AI.modules().coo(d);
					n.childUseSpot(c.x(), c.y(), false);
					return null;
				}
				
				return AI.SUBS().subSleep.activate(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return checkSpot(a, d);
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
		};
		
		private final Resumer recoperate = new Resumer(¤¤WarmingUp) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (checkReady(a, d)) {
					ROOM_NURSERY n = rooms[a.race().index];
					COORDINATE c = AI.modules().coo(d);
					DIR dir = n.childSleepDir(c.x(), c.y());
					
					int cx = ((c.x()) << C.T_SCROLL)+C.TILE_SIZEH+dir.x()*C.TILE_SIZEH;
					int cy = ((c.y()) << C.T_SCROLL)+C.TILE_SIZEH+dir.y()*C.TILE_SIZEH;
					a.physics.body().moveC(cx, cy);
					a.speed.setRaw(dir, 0);
				}
				return AI.SUBS().subSleep.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				STATS.NEEDS().EXPOSURE.fix(a.indu());
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return checkSpot(a, d);
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
		};
		


	};
	
	private final AIPLAN starve = new Plan("childStarve") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return starving.set(a, d);
		}
		
		private final Resumer starving = new Resumer(¤¤Homeless) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (STATS.FOOD().STARVATION.indu().getD(a.indu()) > 0) {
					AIManager.dead = CAUSE_LEAVES.STARVED();
				}
				return AI.SUBS().STAND.activate(a, d, AI.STATES().anima.wave.activate(a, d, 15));
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (RND.rBoolean())
					return AI.SUBS().LAY.activateTime(a, d, 5);
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
	
	private final AIPLAN school = new Plan("childSchool") {
		
		private final ROOM_SCHOOL[] best = new ROOM_SCHOOL[SETT.ROOMS().SCHOOLS.size()];
		
		{
			for (ROOM_SCHOOL s : SETT.ROOMS().SCHOOLS)
				best[s.typeIndex()] = s;
			Arrays.sort(best, new Comparator<ROOM_SCHOOL>() {

				@Override
				public int compare(ROOM_SCHOOL o1, ROOM_SCHOOL o2) {
					if (o1.learningSpeed > o2.learningSpeed)
						return 1;
					return -1;
				}
			
			});
		}
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			for (ROOM_SCHOOL s : best) {
				s.stat().indu().set(a.indu(), 0);
			}
			for (ROOM_SCHOOL s : best) {
				if (s.access(a.race())) {
					AISubActivation ss = AI.SUBS().walkTo.serviceInclude(a, d, s.service().finder, 250);
					if (ss != null) {
						d.planByte1 = (byte) s.typeIndex();
						s.stat().indu().set(a.indu(), 1);
						walk.set(a, d);
						return ss;
					}
				}
			}
			return null;
		}
		
		private ROOM_SCHOOL b(AIManager d) {
			return SETT.ROOMS().SCHOOLS.get(d.planByte1);
		}
		
		private final Resumer walk = new Resumer(¤¤Studying) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return study.set(a, d);
			
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				b(d).stat().indu().set(a.indu(), 0);
				
			}
		};
		
		private final Resumer study = new Resumer(¤¤Studying) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte2 = 0;
				d.planTile.set(d.path.destX(), d.path.destY());
				DIR dir = b(d).childDir(d.path.destX(), d.path.destY());
				AISubActivation s = AI.SUBS().walkTo.coo(a, d, d.path.destX()+dir.x(), d.path.destY()+dir.y());
				ss(d).startUsing();
				if (s != null)
					return s;
				cancel(a, d);
				return null;
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				d.planByte2++;
				
				if (d.planByte2 < 5 && (!TIME.light().dayIs() || TIME.light().partOf() > 0.5)) {
					FSERVICE f = ss(d);
					f.consume();
					if (b(d) != null)
						STATS.EDUCATION().educate(a.indu(), b(d).learningSpeed(a.tc().x(), a.tc().y()));
					return null;
				}
				
				DIR dir = b(d).childDir(d.planTile.x(), d.planTile.y());
				if (RND.oneIn(5)) {					
					dir = dir.next(-2+RND.rInt(5));
				}
				a.speed.setDirCurrent(dir);
				return AI.SUBS().STAND.activateTime(a, d, 5);
				
			}
			
			FSERVICE ss(AIManager d) {
				return b(d).service().service(d.planTile.x(), d.planTile.y());
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				
				return b(d) != null && ss(d) != null && ss(d).findableReservedIs();
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				FSERVICE f = ss(d);
				if (f != null && f.findableReservedIs())
					f.findableReserveCancel();
				b(d).stat().indu().set(a.indu(), 0);
			}
		};
		

	};

}
