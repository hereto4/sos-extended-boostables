package settlement.entity.humanoid.ai.main;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI.AIElement;
import settlement.entity.humanoid.spirte.HSprite;

public abstract class AISTATE extends AIElement {
	
	private final String name;
	
	AISTATE(String key, String name){
		super("STATE_" + key);
		this.name = name;
	}
	
	public abstract HSprite sprite(Humanoid a);
	protected abstract boolean update(Humanoid a, AIManager d, double ds);
	protected String name() {
		return name;
	}
	
	public static abstract class Custom extends AISTATE {
		
		private final HSprite sprite;
		
		public Custom(String key, String name, HSprite sprite){
			super("STATE_" + key, name);
			this.sprite = sprite;
		}
		
		@Override
		public HSprite sprite(Humanoid a) {
			return sprite;
		}

	}
}