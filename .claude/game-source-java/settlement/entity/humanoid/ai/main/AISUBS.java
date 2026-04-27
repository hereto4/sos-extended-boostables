package settlement.entity.humanoid.ai.main;

import static settlement.main.SETT.THINGS;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.constant.C;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.subwalk.AISUB_walkTo;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

public final class AISUBS {

	//remove this and put it in plan
	public final Stand STAND = new Stand();
	public final Lay LAY = new Lay("subsLAy");
	public final AISUB_walkTo walkTo = new AISUB_walkTo();
	public final Work WORK = new Work();
	public final WorkHands WORK_HANDS = new WorkHands();
	public final AISUB failure = new AISUB.Simple("rethinking") {
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			if (!a.speed.isZero())
				return AI.STATES().STOP.activate(a, d);
			d.subByte++;
			if (d.subByte == 1)
				return AI.STATES().STAND.activate(a, d, 0.5f + RND.rFloat());
			return null;
		}
		
		@Override
		protected boolean isSuccessful(Humanoid a, AIManager d) {
			return false;
		}
	};
	public final Single single = new Single("subsSingle");
	
	public final AISUB.Simple DUMMY = new AISUB.Simple("DUMMY") {
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	public static class Work {

		private final AISUB.Simple sub = new AISUB.Simple("working") {

			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				if (!a.speed.isZero())
					return AI.STATES().STOP.activate(a, d);
				if(d.subByte == 1) {
					d.subByte ++;
					return AI.STATES().WORK.state;
				}
				if (d.subByte == 2) {
					d.subByte ++;
					return AI.STATES().STAND.activate(a, d, 0.2f);
				}
				return null;
			}
			
			@Override
			protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
				if (d.subByte >= 2)
					return AI.STATES().STAND.activate(a, d, 0.2f);
				return null;
			};
			
		};
		
		public AISubActivation activate(Humanoid a, AIManager d, double time) {
			AISubActivation k = sub.activate(a, d, AI.STATES().WORK.activate(a, d, time));
			if (time > 0) {
				d.subByte = (1);
			}else
				d.subByte = (2);
			return k;
		}
		
		AISubActivation activate(Humanoid a, AIManager d, Animation animation, double time) {
			AISubActivation k = sub.activate(a, d);
			d.subByte = 1;
			animation.activate(a, d, time);
			return k;
		}
		
		
	}
	
	public abstract static class Throw extends AISUB.Simple{

		public Throw(String key) {
			super(key);
		}
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			if (!a.speed.isZero())
				return AI.STATES().STOP.activate(a, d);
			d.subByte ++;
			if(d.subByte == 1) {
				return AI.STATES().anima.throww.activate(a, d);
			}
			if (d.subByte == 2) {
				DIR dd = a.speed.dir();
				int sx = a.body().cX();
				int sy = a.body().cY();
				dd = dd.next(1);
				sx += dd.x()*C.TILE_SIZEH/2;
				sy += dd.y()*C.TILE_SIZEH/2;
				
				THINGS().rubbish.throww(sx, sy, destX(a,d), destY(a,d));
				return AI.STATES().anima.fistRight.activate(a, d, 2);
			}
			if (d.subByte == 3) {
				return  AI.STATES().anima.stand.activate(a, d, 4);
			}
			
			return null;
		}
		
		public abstract int destX(Humanoid a, AIManager d);
		public abstract int destY(Humanoid a, AIManager d);
		
	}
	
	public static class WorkHands {

		private final AISUB.Simple sub = new AISUB.Simple("workhands") {

			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				if (!a.speed.isZero())
					return AI.STATES().STOP.activate(a, d);
				if(d.subByte == 1) {
					d.subByte ++;
					return AI.STATES().anima.box.activate(a, d);
				}
				if (d.subByte == 2) {
					d.subByte ++;
					return AI.STATES().STAND.activate(a, d, 0.2f);
				}
				return null;
			}
			
			@Override
			protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
				if (d.subByte >= 2)
					return AI.STATES().STAND.activate(a, d, 0.2f);
				return null;
			};
			
		};
		
		public AISubActivation activate(Humanoid a, AIManager d, double time) {
			AISubActivation k = sub.activate(a, d, AI.STATES().anima.box.activate(a, d, time));
			if (time > 0) {
				d.subByte = (1);
			}else
				d.subByte = (2);
			return k;
		}
		
		AISubActivation activate(Humanoid a, AIManager d, Animation animation, double time) {
			AISubActivation k = sub.activate(a, d);
			d.subByte = 1;
			animation.activate(a, d, time);
			return k;
		}
		
		
	}
	
	public static class Stand extends AISUB.Simple{

		public Stand() {
			super("subsstanding");
		}

		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			return activateTime(a, d, 1);
		}
		
		public AISubActivation activateRndDir(Humanoid a, AIManager d){
			
			if (a.speed.isZero()) {
				a.speed.setRaw(a.speed.dir().next(1*(RND.rBoolean()?1:-1)), 0);
			}
			activateTime(a, d, 4+RND.rInt(4));
			return super.activate(a, d);
		}
		
		public AISubActivation activateRndDir(Humanoid a, AIManager d, int seconds){
			
			if (a.speed.isZero()) {
				a.speed.setRaw(a.speed.dir().next(1*(RND.rBoolean()?1:-1)), 0);
			}
			activateTime(a, d, seconds);
			return super.activate(a, d);
		}
		
		public AISubActivation activateTime(Humanoid a, AIManager d, int seconds){
			d.subPathByte2 = (byte) seconds;
			return super.activate(a, d);
		}
		
		@Override
		public AISTATE resume(Humanoid a, AIManager d) {
			d.subByte ++; 
			if (!a.speed.isZero()) {
				return AI.STATES().STOP.activate(a, d);
			}else if (d.subByte == 1) {
				return AI.STATES().STAND.activate(a, d, d.subPathByte2);
			}
			return null;
		}
		
	}
	
	public static class Single extends AISUB.Simple{

		public Single(String key) {
			super(key);
		}

		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			throw new RuntimeException();
		}
		
		public AISubActivation activate(Humanoid a, AIManager d, AISTATES.Animation animation, double seconds){
			return super.activate(a, d, animation.activate(a, d, seconds));
		}
		
		@Override
		public AISTATE resume(Humanoid a, AIManager d) {
			return null;
		}
		
	}
	
	public static class Lay extends AISUB.Simple{

		public Lay(String key) {
			super(key);
		}

		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			return activateTime(a, d, 2);
		}
		
		public AISubActivation activateRndDir(Humanoid a, AIManager d){
			
			if (a.speed.isZero()) {
				a.speed.setRaw(a.speed.dir().next(1*(RND.rBoolean()?1:-1)), 0);
			}
			return activateTime(a, d, 4+RND.rInt(4));
		}
		
		public AISubActivation activateTime(Humanoid a, AIManager d, int seconds){
			d.subPathByte2 = (byte) seconds;
			return super.activate(a, d);
		}
		
		@Override
		public AISTATE resume(Humanoid a, AIManager d) {
			d.subByte ++; 
			if (!a.speed.isZero()) {
				return AI.STATES().STOP.activate(a, d);
			}else if (d.subByte == 1) {
				return AI.STATES().LAY.activate(a, d, d.subPathByte2);
			}
			return null;
		}
		
	}
	
	public final AISUB confused = new AISUB.Simple("subConfused") {
		
		@Override
		public AISTATE resume(Humanoid a, AIManager d) {
			if (!a.speed.isZero())
				return AI.STATES().STOP.activate(a, d);
			d.subByte += 1;
			if (d.subByte < 5) {
				a.speed.turn2Angle(RND.rInt(45));
				return AI.STATES().STAND.activate(a, d, 0.3f);
			}

			return null;
		}

	};
	
	public final AISUB desperate = new AISUB.Simple("SubDesperate"){
		
		@Override
		public boolean event(Humanoid a, AIManager d, settlement.entity.humanoid.HEvent.HEventData e) {
			
			if (e.event == HEvent.COLLISION_TILE) {
				return true;
			}
			
			if (e.event == HEvent.COLLISION_SOFT) {
				d.overwrite(a, AI.STATES().STOP.activate(a, d));
				return true;
			}
			
			if (e.event == HEvent.MEET_HARMLESS) {
				if (RND.oneIn(4) && e.other != null && d.planByte1 < 5) {
					d.planByte1 ++;
					d.overwrite(a, AI.STATES().RUN.activateFRom(a, d, 3+RND.rFloat(3), e.other));
				}
			}
			return super.event(a, d, e);
			
			
		}; 
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {

			d.subByte++;
			if (d.subByte < 5)
				return state(a, d);
			
			
			return null;
		}
		
		AISTATE state(Humanoid a, AIManager d) {
			if (!a.speed.isZero())
				return AI.STATES().STOP.activate(a, d);
			switch(RND.rInt(8)) {
			case 0:
				return AI.STATES().LAY.activate(a, d, 5 + RND.rFloat(5));
			case 1:
				return AI.STATES().anima.box.activate(a, d, 1 + RND.rFloat(5));
			case 2: 
				return AI.STATES().anima.wave.activate(a, d, 1 + RND.rFloat(5));
			case 3:
				return AI.STATES().RUN.activateRND(a, d, 0.2f + RND.rFloat(1));
			}
			return AI.STATES().STAND.aDirRND(a, d, 0.1f + RND.rFloat(1));
		}
		
		@Override
		protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
			return state(a, d);
		}
		

	};
	
	final AISUB NONE = new AISUB("NONE") {
		
		@Override
		AISTATE resume(Humanoid a, AIManager d) {
			return null;
		}

		@Override
		AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
			return null;
		}

		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			return null;
		}

		@Override
		boolean isSuccessful(Humanoid a, AIManager d) {
			return false;
		}

		@Override
		void cancel(Humanoid a, AIManager d) {
		}

		private String name = "none";
		
		@Override
		CharSequence name(Humanoid a,AIManager d) {
			return name;
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return 0;
		};
		
		@Override
		public boolean event(Humanoid a, AIManager d, settlement.entity.humanoid.HEvent.HEventData e) {
			return false;
		}
		
	};
	
	public final AISUB subSleep = new AISUB.Simple("subSleeping"){
		
		private final SoundRace sound = AUDIO.race("SLEEP");
		
		@Override
		public AISTATE resume(Humanoid a, AIManager d) {

			switch(d.subByte) {
			case 0: 
				d.subByte = (1);
				if (!a.speed.dir().isOrtho())
					a.speed.setRaw(a.speed.dir().next(RND.rInt0(1)), 0);
				sound.rnd(a);
				return AI.STATES().SLEEP.activate(a, d, 8f);
			default:
				return null;
			}


			
		}
	};
	
	
	
}
