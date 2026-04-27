package settlement.entity.humanoid.ai.battle;

import game.GAME;
import game.boosting.BOOSTABLES;
import init.constant.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISTATES;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.rnd.RND;

public class SubFight extends AISUB.Resumable {

	

	private final double stopMom = 0.5;

	public SubFight(String key) {
		super(key, "fighting");
	}

	AISubActivation initReady(AIManager d, Humanoid a, ENTITY other, double norX, double norY, double faceDot,
			double momentum) {
		if (a.division() != null)
			a.division().reporter.reportReachable(a, false);
		d.otherEntitySet((Humanoid) other);
		if (momentum > stopMom) {
			return activate(a, d, stop);
		} else if (faceDot > 0.5) {
			return activate(a, d, strike);
		} else {
			return activate(a, d, beBraced);
		}
	}

	@Override
	protected AISTATE init(Humanoid a, AIManager d) {
		if (d.otherEntity() != null)
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
		d.subPathByte = 0;
		return stop.set(a, d);
	}
	
	private final Resumer stop = new ResumerB() {

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			switch (e.type) {
			case IMPACT_DAMAGE:
				return 0;
			default:
				return super.poll(a, d, e);
			}
		}

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			switch (e.event) {
			case COLLISION_SOFT:

				if (d.otherEntity() != null)
					a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
				else if (e.other != null)
					a.speed.setDirCurrent(DIR.get(a.body(), e.other.body()));
				return false;
			case COLLISION_TILE:
				if (d.otherEntity() != null)
					a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
				else
					a.speed.setDirCurrent(DIR.get(-e.norX, -e.norY));
				return false;
			case MEET_ENEMY:
				d.otherEntitySet((Humanoid) e.other);
				a.speed.setDirCurrent(DIR.get(-e.norX, -e.norY));
				return false;
			default:
				return super.event(a, d, e);
			}

		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			if (a.speed.isZero())
				return findFooting.set(a, d);
			return AI.STATES().anima.sword_out.activate(a, d, 1.0+RND.rFloat0(0.5));
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			if (!a.speed.isZero())
				return AI.STATES().STOP.activate(a, d);
			return findFooting.set(a, d);
		}
	};

	private final Resumer findFooting = new ResumerB() {

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.IMPACT_DAMAGE)
				return 0;
			return super.poll(a, d, e);
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			if (d.otherEntity() != null)
				a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			return AI.STATES().STAND.activate(a, d, 0.1 + BattleUtil.getAttackPause(a, d) * 5.0f);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}

	};

	private final Resumer beBraced = new ResumerB() {

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			if (d.otherEntity() == null)
				return exit.set(a, d);
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			return AI.STATES().SWORD.STOP_SWORD.activate(a, d, 0.1 + BattleUtil.getAttackPause(a, d) * 5.0f);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			Humanoid ene = d.otherEntity();

			AIPLAN p = AI.modules().battle.escape.plan(a, d);
			if (p != null)
				return d.resumeOtherPlanState(a, p);
			
			AISTATE s = escape.set(a, d);
			if (s != null)
				return s;
			
			if (ene == null || ene.isRemoved()) {
				return exit.set(a, d);
			}

			int dist = a.body().getDistance(ene.body());
			if (dist > C.TILE_SIZE * 10)
				return exit.set(a, d);
			if (dist <= (a.body().width() + ene.body().width()) / 2 - 6)
				return backup.set(a, d);
			if (dist > C.TILE_SIZE * 4)
				return charge.set(a, d);
			if (dist > (a.body().width() + a.body().width()) / 2 + 8)
				return move_closer.set(a, d);
			
			return strike.set(a, d);
		}
	
	};
	
	private final Resumer escape = new ResumerB() {
		
		private final VectorImp vec = new VectorImp();
		
		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			
			if (!BattleUtil.shouldMoveIntoDivPosition(a, d))
				return null;

			if (!a.division().reporter.posHas(a))
				return null;
			
			double m = vec.set(a.body().cX(), a.body().cY(), a.division().reporter.getPixel(a));
			if (m > C.TILE_SIZE*2) {
				return AI.STATES().STAND_SWORD.activate(a, d, 0.1);
			}
			boolean can = true;
			if (m > C.TILE_SIZE) {
				for (int i = 1; i < 3 && m > 0; i++) {
					int tx = ((int) (a.body().cX() + vec.nX() * C.TILE_SIZE * i)) >> C.T_SCROLL;
					int ty = ((int) (a.body().cY() + vec.nY() * C.TILE_SIZE * i)) >> C.T_SCROLL;
					if (!SETT.IN_BOUNDS(tx, ty) || SETT.PATH().finders().entity.getEnemies(a, tx, ty) > 0) {
						can = false;
						break;
					}
					m -= C.TILE_SIZE;
				}
			}
			if (can) {
				
				AISTATE s = AI.STATES().WALK2_SWORD.free(a, d, (int)(a.body().cX()+vec.nX()*C.TILE_SIZE*2), (int)(a.body().cY()+vec.nY()*C.TILE_SIZE*2));
				a.speed.setPrevDir();
				return s;
			}
			
			Humanoid ene = d.otherEntity();
			if (ene == null) {
				return null;
			}
			vec.set(ene.body().cX(), ene.body().cY(), a.body().cX(), a.body().cY());
			int tx = ((int) (a.body().cX() + vec.nX() * C.TILE_SIZE)) >> C.T_SCROLL;
			int ty = ((int) (a.body().cY() + vec.nY() * C.TILE_SIZE)) >> C.T_SCROLL;
			if (SETT.IN_BOUNDS(tx, ty) && SETT.PATH().finders().entity.getEnemies(a, tx, ty) <= 0) {
				AISTATE s = AI.STATES().WALK2_SWORD.free(a, d, (int)(a.body().cX()+vec.nX()*C.TILE_SIZE*2), (int)(a.body().cY()+vec.nY()*C.TILE_SIZE*2));
				a.speed.setPrevDir();
				return s;
			}
			
			return null;

		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			boolean ret = super.event(a, d, e);
			if (e.speedHasChanged && d.subByte == index && d.plansub() == SubFight.this) {
				d.otherEntitySet(null);
				a.speed.setPrevDir();
				
				float dx = d.X - a.body().cX();
				float dy = d.Y - a.body().cY();

				if (dx * a.speed.nX() < 0 || dy * a.speed.nY() < 0) {
					a.speed.turn2(a, d.X, d.Y).magnitudeTargetSet(0.4 + RND.rFloat(0.05));
					return false;
				}else {
					d.overwrite(a, AI.STATES().STAND_SWORD.activate(a, d, 0.1));
				}
				return true;
			}
			
			
			return ret;
			
			
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return null;
		}
	};


	private final Resumer move_closer = new ResumerB() {
		private final AISTATES.WALK state = AI.STATES().SWORD.WALK;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			return state.activate(a, d, 1 + RND.rFloat(1), d.otherEntity());
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				d.otherEntitySet(null);
				d.overwrite(a, stop.set(a, d));
				return true;
			}
			return super.event(a, d, e);
		}

	};

	private final Resumer charge = new ResumerB() {
		private final AISTATES.WALK state = AI.STATES().SWORD.RUN;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			return state.activate(a, d, 1 + RND.rFloat(1), d.otherEntity());
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				d.otherEntitySet(null);
				d.overwrite(a, stop.set(a, d));
				return true;
			}
			return super.event(a, d, e);
		}

	};

	private final Resumer strike = new ResumerB() {
		private final double offMom = 0.25;
		private final AISTATE state = AI.STATES().SWORD.strike;
		private final float time = (float) HSprites.SWORD_OUT.time;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {

			if (d.otherEntity() != null) {
				a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
				if (RND.rInt(3) == 0)
					return charge.set(a, d);
			}
			a.spriteTimer = 0;
			d.stateTimer = time;
			a.speed.magnitudeTargetSet(0);
			return state;
		}

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.MEET_ENEMY) {
				if (e.speedHasChanged)
					a.speed.setPrevDir();
				if (e.momentum > offMom)
					return super.event(a, d, e);
				return false;
			} else {
				return super.event(a, d, e);
			}
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return strike2.set(a, d);
		}

	};

	private final Resumer strike2 = new ResumerB() {

		private final AISTATE state = AI.STATES().SWORD.strikeIn;
		private final double offMom = 0.25;
		private final float time = (float) HSprites.SWORD_IN.time;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			
			if (d.otherEntity() != null) {
				int dist = a.body().getDistance(d.otherEntity().body());
				if (dist <= (a.body().width() + d.otherEntity().body().width()) / 2 + 24) {
					Humanoid enemy = d.otherEntity();
					attack(a, d, enemy);
					//Util.attack(a, d, enemy);
					
				}
			}

			a.spriteTimer = 0;
			d.stateTimer = time;
			a.speed.magnitudeTargetSet(0);
		
			return state;
		}

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.MEET_ENEMY) {
				if (e.speedHasChanged)
					a.speed.setPrevDir();
				if (e.momentum > offMom)
					return super.event(a, d, e);
				return false;
			}
			return super.event(a, d, e);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}

	};

	public void attack(Humanoid a, AIManager d, Humanoid enemy) {
		GAME.battle().fight.attack(a, enemy);
	}
	
	private final Resumer backup = new ResumerB() {

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE && RND.rBoolean()) {

				a.speed.turn90();
				if (RND.rBoolean()) {
					a.speed.turn90();
					a.speed.turn90();
				}
				return true;
			} else if (e.event == HEvent.COLLISION_SOFT) {
				a.speed.setPrevDir();
				beBraced.set(a, d);
				return false;
			} else {
				return super.event(a, d, e);
			}
		}

		private final AISTATE state = AI.STATES().SWORD.backup;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.setDirCurrent(DIR.get(a.body(), d.otherEntity().body()));
			d.stateTimer = 0.25f + RND.rFloat(0.5);
			a.speed.turn2(a.body(), d.otherEntity().body()).turn90().turn90();
			a.speed.setDirCurrent(a.speed.dir().perpendicular());
			a.speed.magnitudeTargetSet(0.3);
			return state;
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return beBraced.set(a, d);
		}
	};

	private final Resumer exit = new ResumerB() {

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			return AI.STATES().SWORD.STOP_SWORD.activate(a, d, 1.5f);
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			d.otherEntitySet(null);
			return null;
		}
	};

	private abstract class ResumerB extends Resumer {

		private final double stopMom = 0.5;
	
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			switch (e.event) {
			case COLLISION_SOFT:
				a.speed.setPrevDir();
				if (e.momentum > stopMom) {
					d.overwrite(a, stop.set(a, d));
				}
				return true;
			case COLLISION_HARD:
				d.otherEntitySet(null);
				return super.event(a, d, e);
			case COLLISION_TILE:
				a.speed.setPrevDir();
				d.overwrite(a, stop.set(a, d));
				return false;
			case MEET_HARMLESS:
				return false;
			case MEET_ENEMY:
				if (e.other.isRemoved()) {
					a.speed.setPrevDir();
					return false;
				}

				d.otherEntitySet((Humanoid) e.other);

				if (e.momentum > stopMom) {
					d.overwrite(a, stop.set(a, d));
				} else if (e.facingDot > 0.5) {
					d.overwrite(a, strike.set(a, d));
				} else if (e.speedHasChanged) {
					a.speed.setPrevDir();
				}
				return false;
			case EXHAUST:
				if (RND.oneIn(BOOSTABLES.PHYSICS().STAMINA.get(a.indu()) * 4)) {
					
					STATS.NEEDS().EXHASTION.indu().inc(a.indu(), 1);
				}
				return false;
			default:
				return InterBattle.listener.event(a, d, e);
			}
		}

		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			switch (e.type) {
			case WILL_COLLIDE_WITH:
				return 1;
			default:
//				if (ready) {
//					return InterBattle.pollReady(a, d, e);
//				}
				return InterBattle.listener.poll(a, d, e);
			}
		}

	}
	
	@Override
	protected AISTATE resume(Humanoid a, AIManager d) {
		if (d.subByte >= 0) {
			AIPLAN p = AI.modules().battle.escape.plan(a, d);
			if (p != null)
				return d.resumeOtherPlanState(a, p);
		}
		
		return super.resume(a, d);
	}

}
