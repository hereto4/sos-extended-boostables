package settlement.room.law.stocks;

import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.main.util.RoomAreaWrapper;
import snake2d.util.bit.Bits;
import snake2d.util.misc.CLAMP;

final class Tile {

	private final Bits stage = new Bits(0b01111);
	private final Bits available = new Bits(0b01111_0000);
	
	private final ROOM_STOCKS b;
	private int x, y;
	final RoomAreaWrapper wrap = new RoomAreaWrapper();
	
	Tile(ROOM_STOCKS b){
		this.b = b;
	}
	
	public Tile get(int tx, int ty) {
		if (b.constructor.service(tx, ty)) {
			x = tx;
			y = ty;
			return this;
		}
		return null;
	}
	
	public enum STATE {
		
		none,available,reserved,used
		
	}
	
	private final STATE[] states = STATE.values();
	
	public STATE state() {
		return states[stage.get(SETT.ROOMS().data.get(x, y))];
	}
	
	public boolean used() {
		return state() == STATE.reserved || state() == STATE.used;
	}
	
	public void stateSet(STATE state) {
		if (state() == STATE.reserved || state() == STATE.used) {
			b.used --;
		}
		int d = stage.set(SETT.ROOMS().data.get(x, y), state.ordinal());
		wrap.done();
		wrap.init(b.instance, x, y);
		SETT.ROOMS().data.set(wrap.area(), x, y, d);
		if (state == STATE.reserved || state == STATE.used) {
			b.used ++;
		}
	}
	
	public void availableSet(int am) {
		am = CLAMP.i(am, 0, 8);
		if (service.findableReservedCanBe()) {
			b.finder.report(x, y, -1);
		}
		wrap.done();
		wrap.init(b.instance, x, y);
		int d = available.set(SETT.ROOMS().data.get(x, y), am);
		SETT.ROOMS().data.set(wrap.area(), x, y, d);
		if (service.findableReservedCanBe()) {
			b.finder.report(x, y, 1);
		}
	}
	
	public final FSERVICE service = new FSERVICE() {
		
		@Override
		public int y() {
			return y;
		}
		
		@Override
		public int x() {
			return x;
		}
		
		@Override
		public boolean findableReservedIs() {
			return available.get(SETT.ROOMS().data.get(x, y)) < 8;
		}
		
		@Override
		public boolean findableReservedCanBe() {
			return available.get(SETT.ROOMS().data.get(x, y)) > 0;
		}
		
		@Override
		public void findableReserveCancel() {
			availableSet(available.get(SETT.ROOMS().data.get(x, y) + 1));
		}
		
		@Override
		public void findableReserve() {
			availableSet(available.get(SETT.ROOMS().data.get(x, y) - 1));
		}

		@Override
		public void consume() {
			findableReserveCancel();
		}
		
		@Override
		public void startUsing() {
			
		};
	};
	
	
}
