package settlement.entity.humanoid.ai.util;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import snake2d.util.sprite.text.Str;

public abstract class AIPlanWalkPath extends AIPLAN.PLANRES {

	private final CharSequence name;
	private final boolean full;
	
	public AIPlanWalkPath(String key, CharSequence name){
		this(key, name, false);
	}
	
	public AIPlanWalkPath(String key, CharSequence name, boolean full){
		super(key);
		this.name = name;
		this.full = full;
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return walk.set(a, d);
	}
	
	@Override
	protected void name(Humanoid a, AIManager d, Str string) {
		string.add(name);
	}
	
	private final Resumer walk = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (full)
				return AI.SUBS().walkTo.pathFull(a, d);
			return AI.SUBS().walkTo.path(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return next(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	public abstract AISubActivation next(Humanoid a, AIManager d);
	


	
	

}
