package settlement.battle.invasion;

import game.GameDisposable;
import snake2d.util.sets.ArrayListGrower;
import world.entity.army.WArmy;

public abstract class InvasionListener{

	static final ArrayListGrower<InvasionListener> all = new ArrayListGrower<>();
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				all.clear();
			}
		};
	}
	public InvasionListener() {
		all.add(this);
	}
	
	
	protected abstract void register(WArmy a, int ref);
	protected abstract void defeat(int lossed, int killsf, int ref);
	protected abstract void victory(int lossed, int kills, int ref);
	protected abstract void weirdness(int ref);
}
