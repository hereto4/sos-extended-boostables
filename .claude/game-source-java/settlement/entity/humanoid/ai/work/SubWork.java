package settlement.entity.humanoid.ai.work;

import game.GAME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.misc.job.SETT_JOB;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class SubWork {

	private static final String sworking = "working";
	
	public static abstract class SubWorkTool extends AISUB.Simple {
		
		
		public SubWorkTool(String key) {
			super(key);
		}
		
		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			throw new RuntimeException();
		}
		
		public AISubActivation activate(Humanoid a, AIManager d, SETT_JOB j) {
			AISubActivation k = super.activate(a, d, AI.STATES().anima.toolSlam.activate(a, d));
			int iters = (int) (j.jobPerformTime(a)/(AI.STATES().anima.toolSlam.time+AI.STATES().anima.toolBack.time));
			if (j.jobPerformTime(a) -iters > 0)
				iters++;
			iters*= 2;
			iters &= ~1;
			if (iters > 0x0FF) {
				//GAME.Notify("bah " + iters + " " + j.jobPerformTime(a) + " " + j);
				iters = 0x0FF;
			}
			if (iters <= 0) {
				GAME.Notify("bah " + iters + " " + j.jobPerformTime(a) + " " + j.jobCoo());
				d.subByte = 2;
			}
			
			d.subByte = (byte) iters;
			
			return k;
		}
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			
			d.subByte --;
			
			int s = d.subByte & 0x0FF;
			if (s == 0)
				return null;
			if ((s & 1) == 1) {
				SETT_JOB j = getJob(a, d);
				if (j == null)
					return null;
				if (j.jobSound() != null) {
					if (a == null) {
						System.err.println("a");
					}else if (a.physics == null)
						System.err.println("phy");
					else if (a.physics.body() == null)
						System.err.println("2");
					j.jobSound().rnd(a);
				}
				return AI.STATES().anima.toolBack.activate(a, d);
			}else {
				return AI.STATES().anima.toolSlam.activate(a, d);
			}
		}
		
		@Override
		protected CharSequence name(Humanoid a, AIManager d) {
			if (getJob(a, d) == null) {
				return sworking;
			}
			return getJob(a, d).jobName(); 
		};
		
		protected abstract SETT_JOB getJob(Humanoid a, AIManager d);
		
	};
	
	public static abstract class SubWorkHands extends AISUB.Simple {
		
		private final Animation ani = AI.STATES().anima.box;
		
		public SubWorkHands(String key) {
			super(key);
		}
		
		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			throw new RuntimeException();
		}
		
		public AISubActivation activate(Humanoid a, AIManager d, SETT_JOB j) {
			AISubActivation  k= super.activate(a, d, ani.resume(a, d, 5));
			int iters = (int) Math.ceil(j.jobPerformTime(a)/5.0);
			iters--;
			d.subByte = (byte) iters;
			if (d.subByte < 0)
				throw new RuntimeException("" + d.subByte + " " + j);
			return k;
		}
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			d.subByte --;
			
			if (d.subByte < 0) {
				return null;
			}
			SETT_JOB j = getJob(a, d);
			if (j == null)
				return null;
			if (j.jobSound() != null)
				j.jobSound().rnd(a);
			if (d.subByte == 0) {
				double t = j.jobPerformTime(a)%5;
				if (t == 0)
					return ani.resume(a, d, 5);
				return ani.resume(a, d, t); 
			}
			
			return ani.resume(a, d, 5.0); 
		}
		
		@Override
		protected CharSequence name(Humanoid a, AIManager d) {
			if (getJob(a, d) == null) {
				return sworking;
			}
			return getJob(a, d).jobName(); 
		};
		
		protected abstract SETT_JOB getJob(Humanoid a, AIManager d);
		
	};
	
	public static abstract class SubWorkThink extends AISUB.Simple {
		
		public SubWorkThink(String key) {
			super(key);
		}
		
		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			throw new RuntimeException();
		}
		
		public AISubActivation activate(Humanoid a, AIManager d, SETT_JOB j) {
			AISubActivation  k = super.activate(a, d, resume(a, d, 5));
			int iters = (int) Math.ceil(j.jobPerformTime(a)/5.0);
			iters--;
			d.subByte = (byte) iters;
			if (d.subByte < 0)
				throw new RuntimeException("" + d.subByte);
			return k;
		}
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			d.subByte --;
			
			if (d.subByte < 0) {
				return null;
			}
			SETT_JOB j = getJob(a, d);
			if (j == null)
				return null;
			if (j.jobSound() != null)
				j.jobSound().rnd(a);
			if (d.subByte == 0) {
				double t = j.jobPerformTime(a)%5;
				if (t == 0)
					return resume(a, d, 5);
				return resume(a, d, t); 
			}
			
			return resume(a, d, 5.0); 
		}
		
		private AISTATE resume(Humanoid a, AIManager d, double time) {
			if (RND.oneIn(8)) {
				a.speed.setDirCurrent(a.speed.dir().next(-1+RND.rInt(3)));
				return AI.STATES().anima.stand.activate(a, d, time);
			}
			a.speed.setDirCurrent(DIR.get(a.tc(), getJob(a, d).jobCoo()));
			if (RND.rBoolean())
				return AI.STATES().anima.fistRight.resume(a, d, time);
			else
				return AI.STATES().anima.stand.activate(a, d, time);
		}
		
		@Override
		protected CharSequence name(Humanoid a, AIManager d) {
			if (getJob(a, d) == null) {
				return sworking;
			}
			return getJob(a, d).jobName(); 
		};
		
		protected abstract SETT_JOB getJob(Humanoid a, AIManager d);
		
	};
	
}
