package settlement.entity.humanoid.ai.util;

import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.battle.SubFight;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.service.arena.RoomArenaWork;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;

public abstract class AIPlanGladiator extends AIPLAN.PLANRES{

	private final CharSequence ¤¤name;
	private final double inj = 0.3;
	private final boolean toDeath;
	
	public AIPlanGladiator(String key, boolean toDeath, CharSequence verb) {
		super(key);
		¤¤name = verb;
		this.toDeath = toDeath;
	}
	
	@Override
	protected void name(Humanoid a, AIManager d, Str string) {
		string.add(¤¤name);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		if (!w(a, d).gladiatorInArena(d.planTile.x(), d.planTile.y()))
			throw new RuntimeException();
		return walk.set(a, d);
	}
	
	protected abstract RoomArenaWork w(Humanoid a, AIManager d);
	
	private final SubFight fightSub = new SubFight(key + "Fighting") {
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			if (!shouldFight(a, d))
				return null;
			if (!isFighter(d.otherEntity()))
				return null;
			return super.resume(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if ( isFighter(e.other)) {
				if (e.event == HEvent.MEET_HARMLESS || e.event == HEvent.COLLISION_SOFT) {
					e.event = HEvent.MEET_ENEMY;
				}
				
			}
			
			
			return super.event(a, d, e);
		}
		
		@Override
		public void attack(Humanoid a, AIManager d, Humanoid enemy) {
			AI.modules().battle.soundSword.rnd(a);
		};
	};
	
	private boolean shouldFight(Humanoid a, AIManager d) {
		if (!w(a,d).gladiatorInArena(a.tc().x(), a.tc().y()))
			return false;
		if (!toDeath &&  STATS.NEEDS().INJURIES.COUNT.indu().getD(a.indu()) >= inj)
			return false;
		if (!toDeath && STATS.WORK().WORK_TIME.indu().isMax(a.indu()))
			return false;
		
		return true;
	}
	
	private final Resumer walk = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s != null)
				return s;
			cancel(a, d);
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!toDeath)
				return taunt.set(a, d);
			else
				return ready.set(a, d);
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
	
	private final Resumer taunt = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			a.speed.impulseBreak(1.0);
			d.planByte1 = 4;
			a.speed.setDirCurrent(DIR.ALL.rnd());
			w(a, d).gladiatorDrawMakeSheer(d.planTile);
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 3+RND.rInt(3));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 -= 1;
			if (d.planByte1 <= 0) {
				return ready.set(a, d);
			}
			a.speed.setDirCurrent(DIR.ALL.rnd());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 3+RND.rInt(3));
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
	
	private final Resumer ready = new Ready();
	
	private class Ready extends ResFigher {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 8;
			d.otherEntitySet(null);
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.sword, 4+RND.rInt(4));
		}

		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!shouldFight(a, d)) {
				cancel(a, d);
				return null;
			}
			if (isFighter(d.otherEntity()))
				return fight.set(a, d);
			d.planByte1 --;
			if (d.planByte1 < 0)
				return null;
			Humanoid other = other(a, d);
			if (other != null) {
				
				d.otherEntitySet(other);
				w(a, d).gladiatorDrawMakeSheer(d.planTile);
				return fight.set(a, d);
			}
			a.speed.setDirCurrent(DIR.ALL.rnd());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.sword, 4+RND.rInt(4));
		}
		
		private Humanoid other(Humanoid a, AIManager d) {
			RECTANGLE rec = w(a, d).gladiatorArea(d.planTile.x(), d.planTile.y());
			LIST<ENTITY> ents = SETT.ENTITIES().fillTiles(rec);
			if (ents.size() < 1)
				return null;
			int k = RND.rInt(ents.size());
			for (int i = 0; i < ents.size(); i++) {
				ENTITY e = ents.getC(i+k);
				if (e == a)
					continue;
				if (isFighter(e)) {
					Humanoid a2 = (Humanoid) e;
					AIManager d2 = (AIManager) a2.ai();
					if (!isFighter(d2.otherEntity())) {
						d2.otherEntitySet(a);
					}
					return a2;
				}
				
			}
			return null;
		}
		
	}
	
	private final Resumer fight = new ResFigher() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = fightSub.activate(a, d);
			d.planByte1 = (byte) (3 + RND.rInt(8));
			if (s == null) {
				cancel(a, d);
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (d.planByte1-- < 0 && !toDeath)
				return yield.set(a, d);
			if (!toDeath && STATS.NEEDS().INJURIES.COUNT.indu().getD(a.indu()) >= inj)
				return yield.set(a, d);
			if (!isFighter(d.otherEntity()))
				return taunt2.set(a, d);
			return ready.set(a, d);
		}
		
	};
	
	private final Resumer yield = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().LAY.activateTime(a, d, 8+RND.rInt(14));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			cancel(a, d);
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
	
	
	
	private final Resumer taunt2 = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			a.speed.impulseBreak(1.0);
			w(a,d).gladiatorDrawMakeSheer(d.planTile);
			d.planByte1 = 4;
			a.speed.setDirCurrent(DIR.ALL.rnd());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 3+RND.rInt(3));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 -= 1;
			if (d.planByte1 <= 0) {
				return null;
			}
			a.speed.setDirCurrent(DIR.ALL.rnd());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 3+RND.rInt(3));
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
	
	private boolean isFighter(ENTITY e) {
		if (e instanceof Humanoid && !e.isRemoved()) {
			Humanoid a2 = (Humanoid) e;
			AIManager d2 = (AIManager) a2.ai();
			if (d2.plan() instanceof AIPlanGladiator) {
				return AIPlanGladiator.this.getResumer(d2) instanceof ResFigher;
			}
		}
		
		return false;
		
	}
	

	

	

	
	private final Resumer removed = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activate(a, d);
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
		if (e.event == HEvent.ROOM_REMOVED && SETT.ROOMS().map.get(d.planTile) == e.room) {
			d.planTile.set(-1,-1);
			d.overwrite(a, removed.set(a, d));
			return true;
		}
		return super.event(a, d, e);
	}
	
	private abstract class ResFigher extends Resumer {

		protected ResFigher() {
			super(¤¤name);
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
			
			if (e.type == HPoll.WILL_COLLIDE_WITH)
				return isFighter(e.other) ? 1 : 0;
			
			
			
			return super.poll(a, d, e);
		}

		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			
			if (e.event == HEvent.COLLISION_HARD || e.event == HEvent.MEET_HARMLESS || e.event == HEvent.COLLISION_SOFT) {
				if (isFighter(e.other)) {
					Humanoid oo = (Humanoid) e.other;
					if (shouldFight(oo, d) && RND.oneIn(10)) {
						AIManager d2 = (AIManager)oo.ai();
						double am = 0.1 + RND.rFloat(inj-0.1);
						CAUSE_LEAVE ll = CAUSE_LEAVES.SLAYED();
						if (((AIPlanGladiator)d2.plan()).toDeath) {
							ll = CAUSE_LEAVES.EXECUTED();
						}else {
							double inj = STATS.NEEDS().INJURIES.COUNT.indu().getD(oo.indu());
							am = Math.min(am, AIPlanGladiator.this.inj-inj);
							if (am < 0)
								return false;
						}
						
						if (!oo.inflictDamage(0.1, ll))
							return true;
						
					}
					
					if (d.plansub() == fightSub) {
						fightSub.event(a, d, e);
					}else if (shouldFight(a, d)) {
						d.otherEntitySet((Humanoid) e.other);
						d.overwrite(a, fight.set(a, d));
					}
					if (toDeath && RND.oneIn(5)) {
						if (!a.inflictDamage(RND.rFloat(), CAUSE_LEAVES.EXECUTED()))
							return false;
						
//							d.overwrite(a, yield.set(a, d));
//							return false;
						
					}
					return true;
				}
			}
			return super.event(a, d, e);
		}
			

	}

}
