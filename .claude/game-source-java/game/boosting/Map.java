package game.boosting;

import snake2d.Errors;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import util.keymap.RMAP;

final class Map extends RMAP<Boostable> {

	private final ArrayListGrower<Boostable> li;
	
	public Map() {
		super("BOOST", new ArrayListGrower<>());
		li = (ArrayListGrower<Boostable>) this.all();
	}
	
	void clear() {
		li.clear();
		map.clear();
	}
	
	public void add(Boostable b) {
		if (map.containsKey(key))
			throw new Errors.GameError("Another boostable with the same key exists " + key);
		li.add(b);
		map.put(b.key, b);

		
	}
	
	public KeyMap<Boostable> map(){
		return map;
	}
	
}
