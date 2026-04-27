package settlement.misc.util;

import init.resources.RESOURCE;
import snake2d.util.datatypes.COORDINATE;

public interface TILE_STORAGE extends COORDINATE{

	public RESOURCE resource();
	public void storageDeposit(int amount);
	public int storageReservable();
	public int storageReserved();
	public void storageReserve(int amount);
	public void storageUnreserve(int amount);
	
	public default boolean storageIsFindable() {
		return true;
	}
}
