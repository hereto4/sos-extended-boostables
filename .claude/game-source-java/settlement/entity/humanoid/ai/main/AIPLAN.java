package settlement.entity.humanoid.ai.main;

import game.GAME;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI.AIElement;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIEventListeners.HEventListener;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sprite.text.Str;

public abstract class AIPLAN extends AIElement implements HEventListener{
	
	private final static AiPlanActivation activation = new AiPlanActivation();
	
	private AIPLAN(String key) {
		super("PLAN_" + key);
	}
	
	public static class AiPlanActivation {
		
		private AIPLAN plan;
		private AISUB.AISubActivation sub;
		
		AIPLAN plan() {
			AIPLAN p = plan;
			plan = null;
			return p;
		}
		
		AISUB.AISubActivation sub(){
			AISUB.AISubActivation s = sub;
			sub = null;
			return s;
		}
		
	}
	
	public abstract AiPlanActivation activate(Humanoid a, AIManager d);
	abstract void cancel(Humanoid a, AIManager d);
	protected void remove(Humanoid a, AIManager d) {
		
	}
	
	abstract AISubActivation resume(Humanoid a, AIManager d);
	abstract boolean shouldContinue(Humanoid a, AIManager d);
	abstract void name(Humanoid a, AIManager d, Str string);
	abstract AISubActivation resumeFailed(Humanoid a, AIManager d, HEvent event);
	
	public boolean notifyIfSubFails() {
		return true;
	}
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		return d.plansub().event(a, d, e);
	}
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		return d.plansub().poll(a, d, e);
	}
	
	private static String empty = "";
	protected String debug(Humanoid a, AIManager d) {
		return empty;
	}
	
	protected  static AISubActivation trySub(Humanoid a, AIManager d, AISubActivation trial, AIDataSuspender suspender) {
		if (trial == null) {
			if (suspender != null)
				suspender.suspend(d);
			return AI.SUBS().failure.activate(a, d);
		}
		return trial;
	}
	
	public static abstract class PLANRES extends AIPLAN {
		
		final ArrayListResize<ResumerRaw> resumers = new ArrayListResize<>(10, 100);

		protected final Resumer WAIT_AND_EXIT = new Resumer("waiting") {

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
			}
			
		};
		
		public PLANRES(String key) {
			super(key);
		}
		
		@Override
		public AiPlanActivation activate(Humanoid a, AIManager d) {
			d.planResumerByte = -1;
			AISubActivation ac = init(a, d);
			
			if (ac != null) {
				activation.plan = this;
				activation.sub = ac;
				if (d.planResumerByte < 0)
					GAME.Error(""+this.getClass());
				return activation;
			}
			return null;
		}
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			if (resumers.size() <= d.planResumerByte)
				System.err.println(key + this);
			resumers.get(d.planResumerByte).can(a, d);
			d.planResumerByte = -5;
		}

		@Override
		protected AISubActivation resume(Humanoid a, AIManager d) {
			return resumers.get(d.planResumerByte).res(a, d);
		}

		@Override
		protected boolean shouldContinue(Humanoid a, AIManager d) {
			return resumers.get(d.planResumerByte).con(a, d);
		}

		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			resumers.get(d.planResumerByte).name(a, d, string);
		}
		
		@Override
		protected AISubActivation resumeFailed(Humanoid a, AIManager d, HEvent event) {
			return resumers.get(d.planResumerByte).resFailed(a, d, event);
		}
		
		@Override
		protected String debug(Humanoid a, AIManager d) {
			if (d.planResumerByte < 0) {
				return empty;
			}
			return this.getClass().getSimpleName() + " " + d.planResumerByte + " " + resumers.get(d.planResumerByte).name + " " + resumers.get(d.planResumerByte);
		}
		
		protected final ResumerRaw getResumer(AIManager d) {
			if (d.planResumerByte < 0 || d.planResumerByte >= resumers.size())
				return null;
			return resumers.get(d.planResumerByte);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return resumers.get(d.planResumerByte).event(a, d, e);
		}
		
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (resumers.size() <= d.planResumerByte)
				System.err.println(this);
			return resumers.get(d.planResumerByte).poll(a, d, e);
		}
		
		protected abstract AISubActivation init(Humanoid a, AIManager d);
		
		public static abstract class ResumerRaw implements HEventListener{
			public final CharSequence name;
			final byte index;
			
			public ResumerRaw(AIPLAN.PLANRES daddy, CharSequence verb){
				this.name = verb;
				this.index = (byte) daddy.resumers.add(this);
			}
			
			public ResumerRaw(AIPLAN.PLANRES daddy){
				this.name = "";
				this.index = (byte) daddy.resumers.add(this);
			}
			
			public AISubActivation resFailed(Humanoid a, AIManager d, HEvent event) {
				return null;
			}

			public final AISubActivation set(Humanoid a, AIManager d) {
				d.planResumerByte = index;
				return setAction(a, d);
			}
			
			public final AISubActivation trySet(Humanoid a, AIManager d) {
				byte old = d.planResumerByte;
				d.planResumerByte = index;
				AISubActivation s = setAction(a, d);
				if (s != null)
					return s;
				d.planResumerByte = old;
				return null;
			}
			
			
			protected abstract AISubActivation setAction(Humanoid a, AIManager d);
			protected abstract AISubActivation res(Humanoid a, AIManager d);
			public abstract boolean con(Humanoid a, AIManager d);
			public abstract void can(Humanoid a, AIManager d);
			
//			AISUB resFailed(Humanoid a, AIManager d) {
//				return null;
//			}
			
			protected void name(Humanoid a, AIManager d, Str string) {
				string.add(name);
			}
			
			@Override
			public boolean event(Humanoid a, AIManager d, HEventData e) {
				return d.plansub().event(a, d, e);
			}
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (d.plansub() == null)
					System.err.println(d.plan().key + " " + d.plan().className + " " + d.planResumerByte);
				return d.plansub().poll(a, d, e);
			}
		}
		
		public abstract class Resumer extends ResumerRaw {
			
			public Resumer(CharSequence verb){
				super(AIPLAN.PLANRES.this, verb);
			}
			
			public Resumer(){
				super(AIPLAN.PLANRES.this);
			}

		}
		

		
	}
	

	
}