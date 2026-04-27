package settlement.thing.halfEntity.caravan;

import snake2d.util.datatypes.COORDINATE;

public interface CaravanPickup extends COORDINATE {
	
	public int reserved();
	
	public int reservable();

	public void reserve(int i);
	
	public void pickup(int i);
	
}