package init.type;

import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import util.info.INFO;
import util.keymap.MAPPED;

public abstract class TERRAIN extends INFO implements MAPPED{

	public final String key;
	private final int index;
	public final boolean world;
	
	TERRAIN(ArrayList<TERRAIN> all, String key, Json json, CharSequence name, CharSequence desc, boolean world){
		super(name, desc);
		this.key = key;
		json.json(key);
		this.index = all.add(this);
		this.world = world;
	}

	@Override
	public int index() {
		return index;
	}
	
	public abstract SPRITE icon();
	
	public abstract double value(int wx, int wy);
	
	@Override
	public String key() {
		return key;
	}
	
}
