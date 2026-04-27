package settlement.entity.humanoid.ai.crime;

import init.type.CAUSE_LEAVES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.util.rnd.RND;
import util.text.D;

final class Murder extends AIPLAN.PLANRES{
	
	private static CharSequence ¤¤verb = "¤Murdering";
	
	static{
		D.ts(Murder.class);
	}
	
	final AIModule_Crime m;
	
	public Murder(String key, AIModule_Crime m) {
		super(key);
		this.m = m;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		d.planByte1 = 0;
		return go.set(a, d);
	}

	private final Resumer go = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (d.planByte1 == 5)
				return null;
			d.planByte1 ++;
			Humanoid h = SETT.PATH().finders.otherHumanoid.find(a, 100);
			if (h != null) {
				//GAME.Notify(a.tc());
				return AI.SUBS().walkTo.follow(a, d, h, false, (byte) 100);
			}
			return null;
			

		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (AI.SUBS().walkTo.followSucess(a, d))
				return murder.set(a, d);
			return set(a, d);
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
				ENTITY ee = SETT.ENTITIES().getByID(d.planObject);
				if (ee != null && ee instanceof Humanoid && ee == e.other){
					m.commitCrime(a, d, true, CRIME.MURDER);
					d.overwrite(a, murder.set(a, d));
					return true;
				}
			}
			return super.event(a, d, e);
		};
	};
	
	private final Resumer murder = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			ENTITY e = SETT.ENTITIES().getByID(d.planObject);
			if ( e == null)
				return null;
			a.speed.turn2(a.body(), e.body());
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.sword_out, AI.STATES().anima.sword_out.time);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			ENTITY e = SETT.ENTITIES().getByID(d.planObject);
			if (e == null)
				return null;
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				double damage = RND.rFloat()*0.99;
				h.inflictDamage(damage, CAUSE_LEAVES.MURDER());
				AIModule_Crime.notify(a);
				if (h.isRemoved())
					return cool_down.set(a, d);
				return chase.set(a, d);
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
	
	private final Resumer cool_down = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.sword, 4);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			AIModule_Crime.notify(a);
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
	
	private final Resumer chase = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AIModule_Crime.notify(a);
			return AI.SUBS().walkTo.follow(a, d, SETT.ENTITIES().getByID(d.planObject), true, (byte) 5);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (AI.SUBS().walkTo.followSucess(a, d))
				return murder.set(a, d);
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
				ENTITY ee = SETT.ENTITIES().getByID(d.planObject);
				if (ee != null && ee instanceof Humanoid && ee == e.other){
					
					d.overwrite(a, murder.set(a, d));
					return true;
				}
			}
			return super.event(a, d, e);
		};
	};
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.NOTIFY_CRIME)
			return false;
		return super.event(a, d, e);
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		super.cancel(a, d);
	}
}
