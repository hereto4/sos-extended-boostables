package settlement.path.finders;

import static settlement.main.SETT.TERRAIN;

import settlement.misc.util.FINDABLE;

public final class SFinderIndoors extends SFinderFindable{

	
	SFinderIndoors() {
		super("indoors");
		new TestPath("indoors", this);
	}
	
	@Override
	public FINDABLE getReservable(int tx, int ty) {
		FINDABLE f = TERRAIN().indoors.findable(tx, ty);
		if (f != null && f.findableReservedCanBe())
			return f;
		return null;
	}

	@Override
	public FINDABLE getReserved(int tx, int ty) {
		FINDABLE f = TERRAIN().indoors.findable(tx, ty);
		if (f != null && f.findableReservedIs())
			return f;
		return null;
	}


}

