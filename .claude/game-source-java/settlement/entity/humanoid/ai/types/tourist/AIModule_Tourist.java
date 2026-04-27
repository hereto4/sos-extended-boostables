package settlement.entity.humanoid.ai.types.tourist;

import game.GAME;
import game.time.TIME;
import game.tourism.TOURISM;
import init.sprite.UI.UI;
import init.type.CAUSE_LEAVES;
import init.type.HTYPE;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AIPLAN.PLANRES;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanWalkPath;
import settlement.entity.humanoid.ai.util.AIUtilMoveH;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.infra.inn.InnInstance;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.RoomInstance;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;

public class AIModule_Tourist extends AIModule{

	private static final int time = (int) (1*TIME.hoursPerDay());
	static final int shasCheckedIn = 0b0000_0001;
	static final int shasCheckedOut = 0b0000_0010;
	static final int shasReview = 0b0000_0100;
	static final int shasSight = 0b0000_1000;
	static final int shasService = 0b0001_0000;
	
	private static CharSequence ¤¤name = "¤Sight Seeing";
	private static CharSequence ¤¤checkin = "¤checking in";
	private static CharSequence ¤¤checkout = "¤checking out";
	private static CharSequence ¤¤sight = "¤sightseeing";
	private static CharSequence ¤¤leaving = "¤leaving";
	
	static {
		D.ts(AIModule_Tourist.class);
	}
	
	public AIModule_Tourist() {
		super(UI.icons().s.crossheir, ¤¤name, null);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		if (SETT.ENTRY().beseiged()) {
			return leaving.activate(a, d);
		}
		
		if ((AIModules.data().byte1.get(d) & shasCheckedIn) == 0) {
			if (SETT.ROOMS().INN.service().finder.reserve(a.tc(), d.path, Integer.MAX_VALUE)) {
				AIModules.data().coo(d).set(d.path.destX(), d.path.destY());
				return checkIn.activate(a, d);
			}
		}
		
		if ((AIModules.data().byte1.get(d) & shasSight) == 0) {
			AIModules.data().byte1.set(d, AIModules.data().byte1.get(d) | shasSight); 
			return see.activate(a, d);
		}
		
		if ((AIModules.data().byte1.get(d) & shasService) == 0) {
			AIModules.data().byte1.set(d, AIModules.data().byte1.get(d) | shasService); 
			return TOURISM.servicePlan(a, d);
		}
		
		
		if (Bits.getDistance(AIModules.data().byte2.get(d), TIME.hours().bitsSinceStart(), 0x0FF) > time) {
			
			
			if ((AIModules.data().byte1.get(d) & shasCheckedOut) == 0) {
				if (service(d) == null) {
					if (SETT.ROOMS().INN.service().finder.reserve(a.tc(), d.path, Integer.MAX_VALUE)) {
						AIModules.data().coo(d).set(d.path.destX(), d.path.destY());
						return checkout.activate(a, d);
					}
					return leaving.activate(a, d);
				}else {
					if (d.path.request(a.tc(), AIModules.data().x.get(d), AIModules.data().y.get(d)))
						return checkout.activate(a, d);
					
				}
			}
			return leaving.activate(a, d);
		}
		
		

		return null;
	}
	

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		if (SETT.ENTRY().beseiged()) {
			return 10;
		}
		
		int s = AIModules.data().byte1.get(d);
		if ((s & shasCheckedIn) == 0)
			return 9;
		if ((s & shasReview) == 1)
			return 9;
		if ((s & shasSight) == 0)
			return 1;
		if ((s & shasService) == 0)
			return 1;
		if (Bits.getDistance(AIModules.data().byte2.get(d), TIME.hours().bitsSinceStart(), 0x0FF) > time)
			return 5;
		return 0;
	}
	
	@Override
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		AIModules.data().byte1.set(d, 0);
		AIModules.data().byte2.set(d, TIME.hours().bitsSinceStart()&0x0FF);
		AIModules.data().coo(d).set(-1, -1);
	}
	
	public static boolean isTouristIn(Humanoid a, RoomInstance ins) {
		return ins.is(AIModules.data().coo((AIManager)a.ai()));
	}
	
	public static InnInstance inn(Humanoid a) {
		if (service((AIManager)a.ai()) != null)
			return SETT.ROOMS().INN.getter.get(AIModules.data().coo((AIManager)a.ai()));
		return null;
	}
	
	AIPlanWalkPath checkIn = new AIPlanWalkPath("touristCheckin", ¤¤checkin) {
		final Resumer res = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().STAND.activateRndDir(a, d, 2);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				service(d).startUsing();
				AIModules.data().byte1.orSet(d, shasCheckedIn);
				
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
		public AISubActivation next(Humanoid a, AIManager d) {
			return res.set(a, d);
		}
		
		@Override
		protected boolean shouldContinue(Humanoid a, AIManager d) {
			return service(d) != null;
		};
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			consume(d);
			super.cancel(a, d);
		}
	};
	
	AIPlanWalkPath checkout = new AIPlanWalkPath("studentCheckout", ¤¤checkout) {
		final Resumer res = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 0;
				DIR dir = SETT.ROOMS().INN.sleepDir(AIModules.data().x.get(d), AIModules.data().y.get(d));
				AIUtilMoveH.moveToTile(a, AIModules.data().x.get(d), AIModules.data().y.get(d), dir);
				return AI.SUBS().subSleep.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1++;
				if (d.planByte1 > 24) {
					AIUtilMoveH.unfuck(a);
					AIModules.data().byte1.orSet(d, shasCheckedOut);
					return null;
				}
				return AI.SUBS().subSleep.activate(a, d);
				
				
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
		public AISubActivation next(Humanoid a, AIManager d) {
			return res.set(a, d);
		}
		
		@Override
		protected boolean shouldContinue(Humanoid a, AIManager d) {
			return service(d) != null;
		};
	};
	
	AIPlanWalkPath leaving = new AIPlanWalkPath("TouristLeave", ¤¤leaving, true) {

		@Override
		public AiPlanActivation activate(Humanoid a, AIManager d) {
			if ((AIModules.data().byte1.get(d) & shasReview) == 0) {
				TOURISM.touristFinish(a.indu(), AIModules.data().coo(d));
			}
			AIModules.data().byte1.orSet(d, shasReview);
			consume(d);
			
			if (SETT.PATH().finders.entryPoints.find(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)) {
				return super.activate(a, d);
			}
			HumanoidResource.dead = CAUSE_LEAVES.OTHER();
			return AI.plans().NOP.activate(a, d);
		};
		
		@Override
		public AISubActivation next(Humanoid a, AIManager d) {
			cancel(a, d);
			return null;
		}
		
		@Override
		protected boolean shouldContinue(Humanoid a, AIManager d) {
			return true;
		};
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			HumanoidResource.dead = CAUSE_LEAVES.OTHER();
			super.cancel(a, d);
		}
	};
	
	AIPLAN see = new PLANRES("touristSee") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			
			d.planByte2 = 4;
			RoomBlueprintIns<?> b = TOURISM.attraction(a.indu());
			if (b.instancesSize() > 0) {
				
				int ii = RND.rInt(b.instancesSize());
				for (int k = 0; k < b.instancesSize(); k++) {
					RoomInstance ins = b.getInstance(((ii+k)%b.instancesSize()));
					if (!ins.reachable())
						continue;
					if (ins.employees().employed() > 0 && ins.reachable()) {
						AISubActivation s = AI.SUBS().walkTo.room(a, d, ins);
						if (s != null) {
							inspect.set(a, d);
							return s;
						}
						
					}
				}
				
			}
			inspect.set(a, d);
			if (SETT.PATH().finders.randomDistanceAway.find(a.tc().x(), a.tc().y(), d.path, 64)) {
				return AI.SUBS().walkTo.path(a, d);
			}
			return AI.SUBS().STAND.activate(a, d);
		};
		
		private final Resumer inspect = new Resumer("Inspecting") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte1 = 24;
				return null;
			};
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				d.planByte1--;
				if (d.planByte1 <= 0) {
					AIModules.data().byte1.orSet(d, shasSight);
					return null;
				}
				RoomInstance r = SETT.ROOMS().map.instance.get(a.tc());
				if (r != null && RND.oneIn(5)) {
					return AI.SUBS().walkTo.room(a, d, r);
				}
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				return true;
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				if (e.event == HEvent.INTERRACT) {
					AIModules.data().byte1.orSet(d, shasSight);
				}
				return super.event(a, d, e);
			};
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.CAN_INTERRACT)
					return 1.0;
				return super.poll(a, d, e);
			}

			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			};
		};
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			string.add(¤¤sight);
		}
		
	};
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		if (service(d) != null) {
			service(d).consume();
		}
	}
	
	static void consume( AIManager d) {
		if (service(d) != null)
			service(d).consume();
		AIModules.data().coo(d).set(-1, -1);
	}

	static FSERVICE service(AIManager d) {
		FSERVICE s = SETT.ROOMS().INN.service().service(AIModules.data().x.get(d), AIModules.data().y.get(d));
		if (s != null && !s.findableReservedIs())
			GAME.Notify(AIModules.data().coo(d));
		if (s != null && s.findableReservedIs())
			return s;
		return null;
	}

}
