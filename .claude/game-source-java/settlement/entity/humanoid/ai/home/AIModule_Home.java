package settlement.entity.humanoid.ai.home;

import static settlement.main.SETT.PATH;

import game.time.TIME;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIData.AIDataBit;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIUtilMoveH;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.room.home.HOME;
import settlement.room.home.chamber.ChamberInstance;
import settlement.room.home.house.HomeInstance;
import settlement.room.home.house.HomeInstance.DirCoo;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import snake2d.LOG;
import snake2d.util.MATH;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;

public final class AIModule_Home extends AIModule{

	private final AIDataBit hasHoused;
	private final AIDataBit hasSlept;
	
	private static CharSequence ¤¤name = "relaxing";
	private static CharSequence ¤¤name2 = "Go Home";
	private static CharSequence ¤¤desc = "Spend some time off the bustling street and have a nap. Preferably in a home.";
	private static CharSequence ¤¤curfew = "Staying off the streets";
	private static CharSequence ¤¤ground = "Going to sleep";
	private static CharSequence ¤¤groundS = "Sleeping";
	
	static{
		D.ts(AIModule_Home.class);
	}
	
	public static final int CURFEW_PRIO = 5;
	
	private final AIPLAN dump = new PlanReturn();
	
	public AIModule_Home(){
		super(UI.icons().s.house, ¤¤name2, ¤¤desc);
		hasHoused = AI.data().new AIDataBit("home");
		hasSlept = AI.data().new AIDataBit("sleep");
		
		
	}

	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		
		
		hasHoused.set(d, true);
		STATS.HOME().GETTER.hasSearched.indu().set(a.indu(), 0);
		if (a.indu().clas() == HCLASSES.NOBLE()) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h != null && !(h instanceof ChamberInstance)) {
				STATS.HOME().GETTER.set(a, null);
			}
			
			if (h == null) {
				for (int i = 0; i < SETT.ROOMS().CHAMBER.instancesSize(); i++) {
					ChamberInstance c = SETT.ROOMS().CHAMBER.getInstance(i);
					if (c.canOccupy(a)) {
						h = c;
						STATS.HOME().GETTER.set(a, h);
					}
				}
			}
			
			if (h != null) {
				if (h.is(a.tc().x(), a.tc().y())) {
					AiPlanActivation p = dump.activate(a, d);
					if (p != null)
						return p;
					return sleep_noble.activate(a, d);
				}
				d.path.request(a.tc().x(), a.tc().y(), h.serviceX(), h.serviceY(), true);
				if (d.path.isSuccessful()) {
					return sleep_noble.activate(a, d);
				}
			}
			STATS.HOME().GETTER.set(a, null);
			STATS.HOME().GETTER.hasSearched.indu().set(a.indu(), 1);
			AiPlanActivation p = dump.activate(a, d);
			if (p != null)
				return p;
			if (!hasSlept.is(d)) {
				hasSlept.set(d, true);
				return sleep_groud.activate(a, d);
			}
			
			return null;
			
			
		}
		
		
		
		if (STATS.HOME().GETTER.has(a)) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h.is(a.tc().x(), a.tc().y())) {
				AiPlanActivation p = dump.activate(a, d);
				if (p != null)
					return p;
				
				
				if (h instanceof HomeInstance)
					return sleep_home.activate(a, d);
				else
					throw new RuntimeException(""+h);
			}
		}
		
		if (SETT.PATH().finders().home.findHome(a, d.path)) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h instanceof HomeInstance)
				return sleep_home.activate(a, d);
			else
				throw new RuntimeException(""+h);
		}
		
		if (STATS.HOME().GETTER.has(a)) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			int sx = h.serviceX();
			int sy = h.serviceY();
			if (!SETT.PATH().reachability.is(sx, sy))
				LOG.ln(sx + " " + sy);
			STATS.HOME().GETTER.set(a, null);
		}
		
		STATS.HOME().GETTER.hasSearched.indu().set(a.indu(), 1);
		
		AiPlanActivation p = dump.activate(a, d);
		if (p != null)
			return p;
		
		if (!hasSlept.is(d)) {
			hasSlept.set(d, true);
			return sleep_groud.activate(a, d);
		}
		return null;
	}
	
	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int upI) {
		
		if (AIModules.current(d) == this)
			return;
		
		if (newDay)
			hasSlept.set(d, false);
		
		if (newDay || ((upI&3 )== 0 &&  !STATS.HOME().GETTER.has(a) && 	STATS.HOME().GETTER.hasSearched.indu().get(a.indu()) == 1)) {
			hasHoused.set(d, false);
		}
		
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (SETT.PATH().finders.otherHumanoid.enemiesAreNear(a)) {
			return 7;
		}
		
		if (STATS.HOME().GETTER.has(a)) {
			if (LAW.curfew().is())
				return 5;
		}
		
		if (STATS.HOME().GETTER.has(a)) {
			if ((AIModules.current(d) == this)){
				return 1;
			}
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h.is(a.tc().x(), a.tc().y())) {
				return 1;
			}
			if (!hasHoused.is(d))
				return 1;
			if (((STATS.RAN().get(a.indu(), 27) + TIME.days().bitsSinceStart()) & 1) == 1) {
				if (MATH.distance(TIME.hours().bitCurrent(), a.getNewDayHour(), TIME.hoursPerDay()) > 3)
					return 1;
			}
			return 0;
			
		}
		
		if (!hasHoused.is(d))
			return STATS.HOME().GETTER.hasSearched.indu().get(a.indu()) == 1 ? 7 : 1;
		
		if (STATS.WORK().EMPLOYED.get(a) == null && a.indu().clas() != HCLASSES.NOBLE()) {
			if (SETT.ROOMS().HOME.odd.has(a))
				return 1;
		}
		
		
		return 0;
		
	}
	
	public int getPriority(Humanoid a) {
		return getPriority(a, (AIManager) a.ai());
	}
	
	private final AIPLAN sleep_noble = new AIPLAN.PLANRES("homeNoble") {
		
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h.is(a.tc().x(), a.tc().y())) {
				if (RND.oneIn(6))
					return walk.set(a, d);
				return bed.set(a, d);
			}
			return first.set(a, d);
		}
		
		private final Resumer first = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.pathFull(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d) && isHome(a, d)) {
					if (RND.oneIn(6))
						return walk.set(a, d);
					return bed.set(a, d);
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
		
		private final Resumer walk = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				
				if (moduleCanContinue(a, d) && isHome(a, d)) {
					return AI.SUBS().walkTo.room(a, d, get(a));
				}
				return null;
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d) && isHome(a, d)) {
					if (RND.oneIn(8)) {
						if (RND.oneIn(3))
							return bed.set(a, d);
						return AI.SUBS().walkTo.room(a, d, get(a));
					}else {
						return AI.SUBS().STAND.activateRndDir(a, d, 4);
					}
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
		
		private final Resumer bed = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 8;
				HOME h = get(a);
				int sx = h.serviceX();
				int sy = h.serviceY();
				return AI.SUBS().walkTo.cooFull(a, d, sx, sy);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d) && isHome(a, d)) {
					d.planByte1--;
					if (d.planByte1 <= 0)
						return walk.set(a, d);
					return sleepBed(a, d);
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
		
		private AISubActivation sleepBed(Humanoid a, AIManager d) {
			d.planTile.set(d.path.destX(), d.path.destY());
			int tx = d.planTile.x();
			int ty = d.planTile.y();
			int cx = SETT.ROOMS().CHAMBER.getSleepPixelX(tx, ty);
			int cy = SETT.ROOMS().CHAMBER.getSleepPixelY(tx, ty);
			a.physics.body().moveC(cx, cy);
			a.speed.setRaw(SETT.ROOMS().CHAMBER.getSleepDir(tx, ty), 0);
			return AI.SUBS().subSleep.activate(a, d);
		}

		
		private boolean isHome(Humanoid a, AIManager d) {
			ChamberInstance h = get(a);
			return h != null && h.is(a.tc());
		}
		
		private ChamberInstance get(Humanoid a) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h != null) {
				if (h instanceof ChamberInstance)
					return (ChamberInstance) h;
			}
			return null;
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (LAW.curfew().is())
				string.add(¤¤curfew);
			else
				string.add(¤¤name);
		};
		
	};
	
	private final AIPLAN sleep_home = new AIPLAN.PLANRES("home") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			d.planByte2 = 4;
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h.is(a.tc().x(), a.tc().y())) {
				return use.set(a, d);
			}
			return walk.set(a, d);
		}
		
		private final Resumer walk = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().walkTo.pathFull(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return use.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				
			}
		};
		
		private final Resumer use = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte2 = 5;
				HomeInstance h = get(a);
				if (h == null)
					return null;
				if (!h.is(a.tc())) {
					int sx = h.serviceX();
					int sy = h.serviceY();
					return AI.SUBS().walkTo.cooFull(a, d, sx, sy);
				}
				h.use();
				return AI.SUBS().STAND.activateRndDir(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				
				if (shouldRes(a, d)) {
					AISubActivation s = null;
					switch(RND.rInt(4)) {
					case 0: 
						s = bed.set(a, d);
						break;
					case 1:
						s = move.set(a, d);
						break;
					default: 
					}
					if (s != null)
						return s;
					
					if (!SETT.ENTITIES().hasAtTileHigher(a, a.tc().x(), a.tc().y()))
						return AI.SUBS().STAND.activateRndDir(a, d);
					
					HomeInstance h = get(a);
					for (DIR dir : DIR.ORTHO) {
						if (h.is(a.tc(), dir) && !SETT.PATH().solidity.is(a.tc(), dir)) {
							return AI.SUBS().walkTo.cooFull(a, d, a.tc().x()+dir.x(), a.tc().y()+dir.y());
						}
					}
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				can(a, d);
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
				if (e.type == HPoll.CAN_INTERRACT)
					return 1.0;
				return super.poll(a, d, e);
			};
		};
		
		private final Resumer bed = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				HomeInstance h = get(a);
				DirCoo c = h.findService(a);
				
				if (c == null)
					return AI.SUBS().STAND.activateRndDir(a, d);
				d.planTile.set(c);
				d.planByte1 = (byte) (8+RND.rInt(2));
				AISubActivation s = AI.SUBS().walkTo.coo(a, d, c);
				if (s != null)
					return s;
				
				
				return AI.SUBS().STAND.activateRndDir(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (shouldRes(a, d)) {
					
					
					if (d.planByte1-- <= 0) {
						moveOK(a, d);
						return use.set(a, d);
					}
					HomeInstance h = get(a);
					DirCoo c = h.getService(d.planTile.x(), d.planTile.y());
					if (c == null) {
						can(a, d);
						return null;
					}
					
					AIUtilMoveH.moveToTile(a, c.x(), c.y(), c.isLay ? c.dir : DIR.C);
					
					a.speed.setDirCurrent(c.dir);
					
					if (c.isLay && a.race().physics.sleeps) {
						return AI.SUBS().subSleep.activate(a, d);
					}else
						return AI.SUBS().STAND.activateTime(a, d, 8);
				}
				can(a, d);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				moveOK(a, d);
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.COLLISION_UNREACHABLE)
					return false;
				return super.event(a, d, e);
			};
		};
		
		private final Resumer move = new Resumer(¤¤name) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				HomeInstance h = get(a);
				for (DIR dir : DIR.ORTHO) {
					if (h.is(a.tc(), dir) && !SETT.PATH().solidity.is(a.tc(), dir) && !SETT.ENTITIES().hasAtTile(a.tc().x()+dir.x(), a.tc().y()+dir.y())) {
						return AI.SUBS().walkTo.cooFull(a, d, a.tc().x()+dir.x(), a.tc().y()+dir.y());
					}
				}
				return null;
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (shouldRes(a, d)) {
					if (RND.oneIn(8)) {
						return use.set(a, d);
					}
					return AI.SUBS().STAND.activateRndDir(a, d);
				}
				can(a, d);
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
		
		private boolean shouldRes(Humanoid a, AIManager d) {
			if (!isHome(a, d))
				return false;
			if (!moduleCanContinue(a, d)) {
				return false;
			}
			return true;
				
		}

		
		private void moveOK(Humanoid a, AIManager d) {
			if (SETT.PATH().solidity.is(a.tc())) {
				AIUtilMoveH.unfuck(a);
			}
		}
		
		private boolean isHome(Humanoid a, AIManager d) {
			HomeInstance h = get(a);
			if (h != null) {
				boolean ret = h.is(a.tc());
				
				return ret;
			}
			return false;
		}
		
		private HomeInstance get(Humanoid a) {
			HOME h = STATS.HOME().GETTER.get(a, this);
			if (h != null && h instanceof HomeInstance)
				return (HomeInstance) h;
			return null;
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (LAW.curfew().is())
				string.add(¤¤curfew);
			else
				string.add(¤¤name);
		};
		
	};
	
	
	private final AIPLAN sleep_groud = new AIPLAN.PLANRES("homeGround") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			STATS.HOME().dump(a);
			return findBed.set(a, d);
		}
		
		private final Resumer findBed = new Resumer(¤¤ground) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				FINDABLE f = PATH().finders.indoor.getReservable(a.tc().x(), a.tc().y());
				d.planByte2 = 0;
				if (f != null) {
					d.planTile.set(a.tc());
					f.findableReserve();
					d.planByte2 = 1;
					return exit.set(a, d);
				}
				int dist = LAW.curfew().is() ? 256 : 64;
				AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, PATH().finders.indoor, dist);
				
				if (s == null && a.inWater) {
					PATH().finders.water.findLand(a.physics.tileC(), d.path, 16);
					if (d.path.isSuccessful()) {
						d.planTile.set(-1, -1);
						s = AI.SUBS().walkTo.path(a, d);
					}
				}
				
				if (s != null) {
					d.planTile.set(d.path.destX(), d.path.destY());
					d.planByte2 = 1;
					return s;
				}
				return exit.set(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				return exit.set(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				if (d.planByte2 == 1) {
					FINDABLE f = PATH().finders.indoor.getReserved(d.planTile.x(), d.planTile.y());
					if (f != null)
						f.findableReserveCancel();
					d.planByte2 = 0;
				}
			}
		};
		
		private final Resumer exit = new Resumer(¤¤groundS) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = (byte) (5 + RND.rInt(5));
				if (LAW.curfew().is())
					return AI.SUBS().STAND.activateRndDir(a, d);
				return AI.SUBS().subSleep.activate(a, d);
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				if (moduleCanContinue(a, d) && d.planByte1-- > 0) {
					if (d.plansub() == AI.SUBS().subSleep) {
						if (RND.oneIn(8))
							return AI.SUBS().STAND.activateRndDir(a, d);
						return AI.SUBS().subSleep.activate(a, d);
					}
					
					if (!RND.oneIn(7) && !LAW.curfew().is()) {
						return AI.SUBS().subSleep.activate(a, d);
					}else {
						return AI.SUBS().STAND.activateRndDir(a, d);
					}
					
				}
				can(a, d);
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				if (d.planByte2 == 1) {
					FINDABLE f = PATH().finders.indoor.getReserved(d.planTile.x(), d.planTile.y());
					if (f != null)
						f.findableReserveCancel();
					d.planByte2 = 0;
				}
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.CAN_INTERRACT)
					return d.plansub() == AI.SUBS().STAND ? 1.0 : 0;
				return super.poll(a, d, e);
			};
		};

		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (LAW.curfew().is() || SETT.PATH().finders.otherHumanoid.enemiesAreNear(a))
				string.add(¤¤curfew);
			else
				string.add(¤¤name);
		};
		
	};
	

}
