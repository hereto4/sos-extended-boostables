package settlement.entity.humanoid.ai.main;

import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI.AIElement;
import settlement.entity.humanoid.ai.main.AIEventListeners.HEventListener;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LISTE;

public abstract class AISUB extends AIElement implements AIEventListeners.Default {

	public static class AISubActivation {
		
		private static AISubActivation i = new AISubActivation();
		private AISUB sub;
		private AISTATE state;
		private final AISubActivationI inter = new AISubActivationI();
		
		private AISubActivation() {
			
		}
		
		public AISUB get() {
			AISUB s = sub;
			sub = null;
			return s;
		}
		
		public AISTATE state() {
			return this.state;
		}
		
		
		public AISubActivation setState(AISTATE state) {
			this.state = state;
			return this;
		}
		
		static AISubActivation make(AISUB s, AISTATE state) {
			i.sub = s;
			i.state = state;
			if (i.state == null)
				throw new RuntimeException(i.sub.getClass().getName());
			return i;
		}
		
		public AISubActivationI i(){
			return inter;
		}
		
		class AISubActivationI {
			
			private AISubActivationI() {
				
			}
			
			public AISUB get() {
				AISUB s = sub;
				sub = null;
				return s;
			}
			
			public AISTATE state() {
				return state;
			}
		}
		
	}
	
	protected AISUB(String key) {
		super("SUB_" + key);
	}
	
	/**
	 * Called after an interruption has finished. Returning null is considered a failure
	 * @param a
	 * @param d
	 * @return
	 */
	abstract AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event);

	public abstract AISubActivation activate(Humanoid a, AIManager d);
	
	/**
	 * Called when this sub has finished. If false is returned the plan will be cancelled
	 * Will not cancel the sub. This should have been done by the sub
	 * @param a
	 * @param d
	 * @return
	 */
	abstract boolean isSuccessful(Humanoid a, AIManager d);

	abstract AISTATE resume(Humanoid a, AIManager d);

	abstract void cancel(Humanoid a, AIManager d);
	
	abstract CharSequence name(Humanoid a, AIManager d);
	

	public static abstract class Simple extends AISUB {

		private final CharSequence name;
		
		protected Simple(String key) {
			super(key);
			this.name = getClass().getSimpleName();
		}
//		
		protected Simple(String key, CharSequence name) {
			super(key);
			this.name = name;
		}

		@Override
		protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
			return null;
		}

		@Override
		public AISubActivation activate(Humanoid a, AIManager d) {
			d.subByte = 0;
			AISTATE s = resume(a, d);
			return AISubActivation.make(this, s);
		}
		
		public AISubActivation activate(Humanoid a, AIManager d, AISTATE s) {
			d.subByte = 0;
			return AISubActivation.make(this, s);
		}
		
		@Override
		protected boolean isSuccessful(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		protected abstract AISTATE resume(Humanoid a, AIManager d);

		@Override
		protected void cancel(Humanoid a, AIManager d) {
			
		}
		
		@Override
		protected CharSequence name(Humanoid a, AIManager d) {
			return name;
		}
		
		protected boolean isBattleReady() {
			return false;
		}
		

		

	}
	
	public static abstract class Resumable extends AISUB implements HEventListener {

		private final CharSequence name;
		private final LISTE<Resumer> all = new ArrayList<Resumer>(20);
		
		protected Resumable(String key, CharSequence name) {
			super(key);
			this.name = name;
		}
		
		protected Resumable(String key) {
			super(key);
			this.name = this.getClass().getSimpleName();
		}

		@Override
		protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
			return all.get(d.subByte).resI(a, d, event);
		}

		
		
		@Override
		public final AISubActivation activate(Humanoid a, AIManager d) {
			d.subByte = -1;
			return AISubActivation.make(this, resume(a, d));
		}
		
		protected final AISubActivation activate(Humanoid a, AIManager d, Resumer res) {
			d.subByte = res.index;
			return AISubActivation.make(this, res.set(a, d));
		}
		
		protected abstract AISTATE init(Humanoid a, AIManager d);
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			if (d.subByte == -1)
				return init(a, d);
			return all.get(d.subByte).res(a, d);
		}
		
		@Override
		protected boolean isSuccessful(Humanoid a, AIManager d) {
			if (d.subByte == -1)
				return true;
			return all.get(d.subByte).success(a, d);
		}

		@Override
		protected void cancel(Humanoid a, AIManager d) {
			if (d.subByte == -1)
				return;
			all.get(d.subByte).can(a, d);
		}
		
		protected Resumer getResumer(Humanoid a, AIManager d) {
			if (d.subByte < 0)
				return null;
			return all.get(d.subByte);
		}
		
		
		@Override
		protected CharSequence name(Humanoid a, AIManager d) {
			return name;
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return all.get(d.subByte).event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return all.get(d.subByte).poll(a, d, e);
		}
		
		public abstract class Resumer implements AIEventListeners.Default{
			protected final byte index;
			
			public Resumer() {
				this.index = (byte) all.add(this);
			}
			
			public final AISTATE set(Humanoid a, AIManager d) {
				d.subByte = index;
				return setAction(a, d);
			}
			
			protected abstract AISTATE setAction(Humanoid a, AIManager d);
			protected abstract AISTATE res(Humanoid a, AIManager d);
			protected AISTATE resI(Humanoid a, AIManager d, HEvent event) {
				return null;
			}
			protected boolean success(Humanoid a, AIManager d) {
				return true;
			}
			protected void can(Humanoid a, AIManager d) {
				
			}
		}
		
		final Resumer get(Humanoid a, AIManager d) {
			return all.get(d.subByte);
		}
		
		class Success extends Resumer{

			@Override
			public AISTATE res(Humanoid a, AIManager d) {
				return null;
			}

			@Override
			public AISTATE resI(Humanoid a, AIManager d, HEvent event) {
				return AI.STATES().STAND.activate(a, d, 0.2f);
			}

			@Override
			public boolean success(Humanoid a, AIManager d) {
				return true;
			}

			@Override
			public AISTATE setAction(Humanoid a, AIManager d) {
				return AI.STATES().STAND.activate(a, d, 0.2f);
			}
			
		}
		
		class Fail extends Resumer{

			@Override
			public AISTATE res(Humanoid a, AIManager d) {
				return null;
			}

			@Override
			public AISTATE resI(Humanoid a, AIManager d, HEvent event) {
				return AI.STATES().STAND.activate(a, d, 0.2f);
			}

			@Override
			public boolean success(Humanoid a, AIManager d) {
				return false;
			}

			@Override
			public AISTATE setAction(Humanoid a, AIManager d) {
				return AI.STATES().STAND.activate(a, d, 0.2f);
			}
			
		}
		
	}
	
	
	

}