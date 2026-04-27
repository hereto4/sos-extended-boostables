package settlement.entity.humanoid.ai.types.prisoner;

import game.time.TIME;
import init.sprite.UI.UI;
import init.type.CAUSE_ARRIVES;
import init.type.CAUSE_LEAVES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.main.HAI;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.Processing.Punishment;
import snake2d.util.rnd.RND;
import util.text.D;
import util.text.Dic;

public final class AIModule_Prisoner extends AIModule{
	

	public static PrisonerData DATA() {
		return PrisonerData.self;
	}
	private static AIModule_Prisoner self;
	private final Jail jailed = new Jail();
	private final Executed executed = new Executed();
	private final Prison prison = new Prison();
	private final Judged judged = new Judged();
	private final Enslaved slave = new Enslaved();
	private final Temple temple = new Temple();
	private final Arena arenaDeath = new Arena();
	private final Stocked stocked = new Stocked();
	static final byte PRISON_DAYS = (byte) (2*TIME.years().bitConversion(TIME.days()));
	
	private static CharSequence ¤¤name = "Atonement";
	static {
		D.ts(AIModule_Prisoner.class);
	}
	public AIModule_Prisoner() {
		super(UI.icons().s.bars, ¤¤name, null);
		new PrisonerData();
		self = this;
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {

		if (PrisonerData.self.jailed.get(d) == 0) {
			AiPlanActivation p = jailed.activate(a, d);
			if (p != null) {
				PrisonerData.self.jailed.set(d, 1);
				return p;
			}
		}
		
		if (DATA().judged.get(d) == 0 && STATS.LAW().prisonerType.get(a.indu()).isJudged) {
			AiPlanActivation s = judged.activate(a, d);
			if (s != null)
				return s;
			if ( SETT.ROOMS().COURT.employment().employed() <= 0)
				DATA().hasWaitedJudge.set(d, DATA().hasWaitedJudge.max(d));
			if (!DATA().hasWaitedJudge.isMax(d)) {
				AiPlanActivation p = jailed.activate(a, d);
				if (p != null)
					return p;
			}
		}
		
		if (LAW.process().stocks.allowed.is(a.race())) {
			if (!TIME.light().nightIs() && (DATA().stockedTried.get(d) == 0 || ((TIME.days().bitsSinceStart()+STATS.RAN().get(a.indu(), 3)) & 7) == 0)) {
				DATA().stockedTried.set(d, 1);
				AiPlanActivation s = stocked.activate(a, d);
				if (s != null) {
					DATA().stockedTried.set(d, 0);
					return s;
				}
			}
			
			
		}else {
			DATA().stockedTried.set(d, 0);
		}
		
//		int ran = TIME.days().bitsSinceStart()+STATS.RAN().get(a.indu(), 1);
//		ran &= 7;
//		if (!TIME.light().nightIs() && ran == 0) {
//			DATA().stockedTried.set(d, 1);
//			if (LAW.process().stocks.allowed.is(a.race())) {
//				AiPlanActivation s = stocked.activate(a, d);
//				if (s != null) {
//					DATA().stockedTried.set(d, 0);
//					return s;
//				}
//			}
//		}
		
		AiPlanActivation s = temple.activate(a, d);
		if (s != null)
			return s;
		Punishment p = punishment(a, d);
		
		AIPLAN plan = plan(a, p);
		
		s = plan.activate(a, d);
		
		
		
		if (s != null)
			return s;
		s = jailed.activate(a, d);
		if (s != null)
			return s;
		return exile.activate(a, d);
		
	}
	
	public static Punishment punishment(Humanoid a, HAI d) {
		Punishment p = DATA().punishmentSet.get(d);
		if (p != null) {
			return p;
		}
		return LAW.process().getPunishment(a);
		
		
	}
	
	private AIPLAN plan(Humanoid a, Punishment p) {

		if (p == LAW.process().none) {
			return jailed;
		}
		
		if(p == LAW.process().execution)
			return executed;
		if (p == LAW.process().prison) {
			if (DATA().prisonTimeLeft.get(a.ai()) <= 0)
				return free;
			return prison;
		}
		if (p == LAW.process().enslaved)
			return slave;
		if (p == LAW.process().pardoned) {
			LAW.process().pardoned.inc(a.race());
			return free;
		}
		if (p == LAW.process().exile)
			return exile;
		if (p == LAW.process().arena)
			return arenaDeath;
		throw new RuntimeException(p + " " + p.name);
	}
	
//	private final AiPlanActivation judge(Humanoid a, AIManager d) {
//		if (DATA().judged.get(d) == 0 && DATA().noJudge.get(d) == 0) {
//			AiPlanActivation s = judged.activate(a, d);
//			if (s != null)
//				return s;
//			if (DATA().hasWaitedJudge.isMax(d)) {
//				return null;
//			}
//			return prison.activate(a, d);
//		}
//		return null;
//	}
	
	@Override
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		STATS.MULTIPLIERS().PROSECUTION.mark(a, false);
		DATA().init(a, d);
	}
	
	public static boolean isPrisoner(Humanoid a, RoomInstance room) {
		AIManager d = (AIManager) a.ai();
		if (room.blueprintI() == SETT.ROOMS().STOCKADE && d.plan() == self.jailed) {
			return room.is(d.planTile);
		}
		else if (room.blueprintI() == SETT.ROOMS().PRISON && d.plan() == self.prison)
			return room.is(AI.modules().coo(d));
		return false;
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		
		if (DATA().judged.get(d) == 0 && DATA().hasWaitedJudge.isMax(d)) {
			LAW.process().judgement.inc(a.race(), false);
		}
		
		super.cancel(a, d);
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		if (newDay) {
			DATA().prisonTimeLeft.inc(d, -1);
			DATA().hasWaitedJudge.inc(d, 1);
			if (DATA().stockedTried.get(d) == 1 && DATA().stocked.get(d) == 0) {
				LAW.process().stocks.inc(a.race(), false);
			}
			
		}
		
	}

	@Override
	public int getPriority(Humanoid a, AIManager d) {
		return 10;
	}
	
	public void makePrisoner(Humanoid h, AIManager m) {
		
		
		if (h.indu().hType() == HTYPES.PRISONER())
			return;
		if (h.indu().hType().hostile) {
			h.kill(false, CAUSE_LEAVES.EXECUTED());
			return;
		}
		
		if (h.indu().hType() == HTYPES.SLAVE()) {
			h.kill(false, CAUSE_LEAVES.EXECUTED());
			return;
		}
		h.HTypeSet(HTYPES.PRISONER(), CAUSE_LEAVES.PUNISHED(), null);
		
		m.overwrite(h, plan);
		
		
		
	}
	
	private final AIPLAN plan = new AIPLAN.PLANRES("prisStart") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}
		
		Resumer start = new Resumer("unconsious") {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().LAY.activateTime(a, d, 10+RND.rInt(10));
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return null;
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
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
	};
	
	private final AIPLAN exile = new AIPLAN.PLANRES("prisLeave") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			
			return start.set(a, d);
		}
		
		Resumer start = new Resumer(LAW.process().exile.verb) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				if (SETT.PATH().finders.entryPoints.find(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)){
					return AI.SUBS().walkTo.pathFull(a, d);
				}
				return finish(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return finish(a, d);
			}
			
			private AISubActivation finish(Humanoid a, AIManager d) {
				DATA().reportPunishment(a, d, LAW.process().exile);
				AIManager.dead = CAUSE_LEAVES.EXILED();
				return AI.SUBS().LAY.activateTime(a, d, 10);
			}
			
			@Override
			public boolean con(Humanoid a, AIManager d) {
				if (punishment(a, d) != LAW.process().exile) {
					RoomInstance ins = SETT.ROOMS().STOCKADE.registerPrisoner(a.tc());
					if (ins != null) {
						SETT.ROOMS().STOCKADE.unregisterPrisoner(ins.mX(), ins.mY());
						return false;
					}
				}
				
				return true;
			}
			
			@Override
			public void can(Humanoid a, AIManager d) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return super.event(a, d, e);
			};
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				return super.poll(a, d, e);
			};
		};
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			super.cancel(a, d);
		}
	};
	
	private final AIPLAN free = new AIPLAN.PLANRES("prisFree") {
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			return start.set(a, d);
		}
		
		Resumer start = new Resumer(Dic.¤¤Free) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				return AI.SUBS().STAND.activate(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				return free.set(a, d);
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
		
		Resumer free = new Resumer(Dic.¤¤Free) {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {	
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				a.HTypeSet(HTYPES.SUBJECT(), null, CAUSE_ARRIVES.PAROLE());
				STATS.LAW().EX_CON.indu().setD(a.indu(), 1.0);
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

}
