package settlement.misc.util;

import snake2d.util.datatypes.COORDINATE;

public interface FINDABLE extends COORDINATE{

	public abstract boolean findableReservedCanBe();
	public abstract void findableReserve();
	public abstract boolean findableReservedIs();
	public abstract void findableReserveCancel();
	
}
