package settlement.entity.humanoid.ai.crime;

import game.GAME;
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
import settlement.path.finders.SFinderRND;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.rnd.RND;
import util.text.D;
import view.main.VIEW;

final class SerialKiller extends AIPLAN.PLANRES{
	
	public SerialKiller(String key) {
		super(key);
		// TODO Auto-generated constructor stub
	}

	private static CharSequence ¤¤verb = "¤Walking around";
	
	static{
		D.ts(SerialKiller.class);
	}
	

	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		d.planByte1 = 0;
		d.planByte2 = 0;
		d.otherEntitySet(null);
		
		return go.set(a, d);
	}

	private final Resumer go = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			if (isVictim(d.otherEntity())) {
				return leave.set(a, d);
			}
			
			if (d.planByte1 ++ > 8)
				return null;
			
			if (SETT.PATH().finders().randomDistanceAway.find(a.tc().x(), a.tc().y(), d.path, 64, SFinderRND.otherPeople)) {
				return AI.SUBS().walkTo.pathFull(a, d);
			}
			return stand.set(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return stand.set(a, d);
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
	
	private final Resumer stand = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte2 = (byte) (6 + RND.rInt(8));
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte2 --;
			if (d.planByte2 < 0)
				return go.set(a, d);
			
			return AI.SUBS().STAND.activateRndDir(a, d, 2+RND.rInt(4));
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
	
	private final Resumer leave = new Resumer(¤¤verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (SETT.PATH().finders().randomDistanceAway.find(a.tc().x(), a.tc().y(), d.path, 64, SFinderRND.noPeople)) {
				return AI.SUBS().walkTo.pathFull(a, d);
			}
			return AI.SUBS().STAND.activateRndDir(a, d, 2+RND.rInt(4));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!isVictim(d.otherEntity()))
				return null;
			
			if (VIEW.s().getWindow().zoomout() <= 1 && VIEW.s().getWindow().pixels().touches(d.otherEntity()))
				return null;
			
			Humanoid v = d.otherEntity();
			int tx = v.tc().x();
			int ty = v.tc().y();
			v.kill(false, CAUSE_LEAVES.MURDER());
			Corpse c = SETT.THINGS().corpses.tGet.get(tx, ty);
			if (c != null)
				GAME.events().killer.reportKill(c);
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
		if (d.planByte1 < 8 && e.event == HEvent.MEET_HARMLESS) {
			if (!isVictim(d.otherEntity()) && isVictim(e.other)) {
				d.otherEntitySet((Humanoid) e.other);
			}
		}
		return super.event(a, d, e);
	};
	
	private boolean isVictim(ENTITY e) {
		if (e instanceof Humanoid) {
			Humanoid o = (Humanoid) e;
			if (o.indu().clas().player && o.race() == GAME.events().killer.victimRace()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		super.cancel(a, d);
	}
}
