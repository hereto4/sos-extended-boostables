package settlement.path.components;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TWIDTH;

import java.util.Arrays;

import settlement.main.SETT;
import snake2d.util.map.MAP_OBJECTE;

final class SComp0Map implements MAP_OBJECTE<SComp0>{

	private final SComp0Factory factory;
	
	private final int[] ids = new int[SETT.TAREA];
	
	
	SComp0Map(SComp0Factory factory){
		this.factory = factory;
		
	}
	
	void clear() {
		Arrays.fill(ids, factory.NONE.index());
	}
	
	@Override
	public SComp0 get(int tile) {
		SComp0 c = factory.get(ids[tile]);
		if (c == factory.NONE)
			return null;
		return factory.get(ids[tile]);
	}
	
	@Override
	public SComp0 get(int tx, int ty) {
		if (SETT.IN_BOUNDS(tx, ty))
			return get(tx+ty*TWIDTH);
		return null;
	}

	@Override
	public void set(int tile, SComp0 object) {
		if (object == null)
			ids[tile] = factory.NONE.index();	
		else
			ids[tile] = object.index();	
	}

	@Override
	public void set(int tx, int ty, SComp0 object) {
		if (IN_BOUNDS(tx, ty)) {
			set(tx+ty*TWIDTH, object);
		}
	}
	


	
}
