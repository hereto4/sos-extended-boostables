package init.type;

import init.race.Race;
import init.sprite.UI.Icon;
import snake2d.util.color.COLOR;
import snake2d.util.sets.LISTE;
import util.info.INFO;
import util.keymap.MAPPED;

public abstract class HCLASS extends INFO implements MAPPED {
	
	HCLASS(LISTE<HCLASS> all, LISTE<HCLASS> allP, String key, CharSequence name, CharSequence names, CharSequence desc, boolean player, COLOR color){
		super(name, names, desc, null);
		this.player = player;
		if (player)
			playerIndex = allP.add(this);
		else
			playerIndex = -1;
		
		this.color = color;
		index = all.add(this);
		this.key = key;
	}
	
	public abstract Icon icon();
	public abstract Icon iconSmall();
	
	private final int index;
	public final boolean player;
	public final COLOR color;
	public final String key;
	public final int playerIndex;

	@Override
	public String toString() {
		return name + "#" + index; 
	}
	
	@Override
	public int index() {
		return index;
	};
	
	public POP_CL get(Race race) {
		return POP_CL.clP(race, this);
	}
	
	@Override
	public String key() {
		return key;
	}
}
