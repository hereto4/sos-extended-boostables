package settlement.room.law.court;

import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.main.util.RoomBits;
import snake2d.util.datatypes.Coo;

public final class Service implements FSERVICE{

	private final Coo coo = new Coo();
	private CourtInstance ins;
	private static final Service self = new Service();

	final RoomBits breservable 	= new RoomBits(coo, 0b0000_0000_0001) {
		
		@Override
		protected void remove() {
			if (get() == 1)
				ins.blueprintI().access.report(Service.this, -1);
		};
		
		@Override
		protected void add() {
			if (get() == 1)
				ins.blueprintI().access.report(Service.this, 1);
		};
	};
	
	static Service init(int tx, int ty) {
		
		CourtInstance ins = SETT.ROOMS().COURT.get(tx, ty);
		if (ins == null)
			return null;
		
		if (SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.codeSpectator) {
			self.ins = ins;
			self.coo.set(tx, ty);
			return self;
		}
		return null;
	}
	
	static void initInit(int tx, int ty, CourtInstance ins) {
		
		
		if (SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.codeSpectator) {
			self.ins = ins;
			self.coo.set(tx, ty);
			ins.blueprintI().access.report(self, 1);
		}
	}

	@Override
	public boolean findableReservedCanBe() {
		return !findableReservedIs();
	}

	@Override
	public void findableReserve() {
		breservable.set(ins, 0);
	}

	@Override
	public boolean findableReservedIs() {
		return breservable.get() == 0;
	}

	@Override
	public void findableReserveCancel() {
		breservable.set(ins, 1);
	}

	@Override
	public int x() {
		return coo.x();
	}

	@Override
	public int y() {
		return coo.y();
	}

	@Override
	public void consume() {
		findableReserveCancel();
	}
	
	void activate() {
		findableReserveCancel();
	}

	void deactivate() {
		findableReserve();
	}

	
}
