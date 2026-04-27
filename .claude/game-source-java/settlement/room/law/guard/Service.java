package settlement.room.law.guard;

import settlement.misc.util.FSERVICE;

final class Service implements FSERVICE{

	private GuardInstance ins;
	private int x,y;

	Service(ROOM_GUARD blue) {
		
	}
	
	Service get(GuardInstance ins) {
		this.ins = ins;
		x = ins.body().cX();
		y = ins.body().cY();
		return this;
	}

	@Override
	public boolean findableReservedCanBe() {
		return ins.crimeI < GuardInstance.crimesMax;
	}

	@Override
	public void findableReserve() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean findableReservedIs() {
		return true;
	}

	@Override
	public void findableReserveCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int x() {
		return x;
	}

	@Override
	public int y() {
		return y;
	}

	@Override
	public void consume() {
		// TODO Auto-generated method stub
		
	}
	
}
