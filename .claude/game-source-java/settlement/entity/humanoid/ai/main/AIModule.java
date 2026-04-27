package settlement.entity.humanoid.ai.main;

import game.GameDisposable;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.room.main.ROOMA;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.INDEXED;
import snake2d.util.sprite.SPRITE;

public abstract class AIModule implements INDEXED {
	
	static final ArrayList<AIModule> all = new ArrayList<AIModule>(200);
	{
		all.add((AIModule)null);
	}
	final byte index;
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
				all.add((AIModule)null);
			}
		};
	}
	
	public final CharSequence name;
	public final CharSequence desc;
	private final SPRITE icon;
	
	final Bitmap1D hasType = new Bitmap1D(HTYPES.ALL().size(), false);
	
	public AIModule(SPRITE icon, CharSequence name, CharSequence desc) {
		index = (byte) all.add(this);
		if (index < 0)
			throw new RuntimeException();
		this.icon = icon;
		this.name = name;
		this.desc = desc;
	}
	
	public boolean has(HTYPE t) {
		return hasType.get(t.index());
	}
	
	
	public abstract AiPlanActivation getPlan(Humanoid a, AIManager d);
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		
	}
	protected void cancel(Humanoid a, AIManager d) {
		
	}
	
	protected void finish(Humanoid a, AIManager d) {
		
	}
	
	protected abstract void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay);
	public abstract int getPriority(Humanoid a, AIManager d);

	public AiPlanActivation resume(Humanoid a, AIManager d, int timesResumedBefore) {
		return null;
	}
	
	public final boolean is(Humanoid a, AIManager d) {
		return AIModules.current(d) == this;
	}
	
	public final boolean moduleCanContinue(Humanoid a, AIManager d) {
		AIModule m = AIModules.next(d);
		return m == null || m == this;
	}
	
	public void evictFromRoom(Humanoid a, AIManager d, ROOMA r) {
		
	}

	@Override
	public int index() {
		return index;
	}

	public SPRITE icon() {
		return icon;
	}
	
	
	
}