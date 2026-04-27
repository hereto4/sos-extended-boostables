package settlement.path.finders;

import static settlement.main.SETT.TWIDTH;

import settlement.misc.util.FINDABLE;
import settlement.misc.util.FSERVICE;
import snake2d.util.map.MAP_OBJECT;

public abstract class SFinderRoomService extends SFinderFindable implements MAP_OBJECT<FSERVICE>{

	public SFinderRoomService(CharSequence s) {
		super(s);
	}
	
	@Override
	public FINDABLE getReservable(int tx, int ty) {
		FINDABLE f = get(tx, ty);
		if(f != null && f.findableReservedCanBe())
			return f;
		return null;
	}
	
	@Override
	public FINDABLE getReserved(int tx, int ty) {
		FINDABLE f = get(tx, ty);
		if(f != null && f.findableReservedIs())
			return f;
		return null;
	}
	

	@Override
	public FSERVICE get(int tile) {
		return get(tile%TWIDTH, tile/TWIDTH);
	}
	
}
