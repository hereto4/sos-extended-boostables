package settlement.room.law.slaver;

import game.time.TIME;
import settlement.main.SETT;
import snake2d.util.bit.Bit;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;

public final class SlaverStation{

	private final static Bit workReserved = new Bit(0b010000);
	private final static Bits state = new Bits(0b01111);
	private final static int STATE_RESERVABLE = 0;
	private final static int STATE_RESERVED = 1;
	private final static int STATE_USED = 2;
	private final static int STATE_EXECUTING = 3;
	
	private final Coo coo = new Coo();
	private int data;
	private ExecutionInstance ins;
	private static final SlaverStation self = new SlaverStation();
	
	static SlaverStation init(int tx, int ty) {
		ExecutionInstance ins = SETT.ROOMS().SLAVER.get(tx, ty);
		if (ins == null)
			return null;
		int c = SETT.ROOMS().fData.tileData.get(tx, ty);
		if (c != Constructor.codeService)
			return null;
		self.data = SETT.ROOMS().data.get(tx, ty);
		self.ins = ins;
		self.coo.set(tx, ty);
		return self;
		
	}
	
	public boolean clientReseveredCanBe() {
		return state.get(data) == STATE_RESERVABLE;
	}
	
	void clientReserve() {
		if (!clientReseveredCanBe())
			throw new RuntimeException();
		data = state.set(data, STATE_RESERVED);
		save();
	}
	
	public boolean clientReserved() {
		return state.get(data) >= STATE_RESERVED;
	}
	
	public boolean clientIsUsing() {
		return state.get(data) == STATE_USED;
	}
	
	public void clientUse() {
		data = state.set(data, STATE_USED);
		save();
	}
	

	public void clientClear() {
		data = state.set(data, STATE_RESERVABLE);
		ins.lastExecution = (byte) TIME.currentSecond();
		save();
	}
	
	public boolean clientExecuted() {
		return state.get(data) == STATE_EXECUTING;
	}
	
	public DIR clientGetTurn() {
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			if (SETT.ROOMS().fData.tileData.get(coo, d) == Constructor.codeHead)
				return d;
		}
		throw new RuntimeException();
	}
	
	boolean workReservedCanBe() {
		return !workReserved.is(data) && state.get(data) > STATE_RESERVED;
	}
	
	void workReserve() {
		data = workReserved.set(data);
		save();
	}

	public void workCancel() {
		data = 0;
		save();
	}
	
	public boolean workReserved() {
		if (state.get(data) <= STATE_RESERVED)
			workCancel();
		return workReserved.is(data);
	}
	
	public boolean workExecute() {
		if (workReserved() && state.get(data) == STATE_USED) {
			data = state.set(data, STATE_EXECUTING);
			save();
		}
		return false;
	}
	
	private void save() {
		add(SETT.ROOMS().data.get(coo.x(), coo.y()), -1);
		add(data, 1);
		SETT.ROOMS().data.set(ins, coo, data);
	}
	
	private void add(int data, int delta) {
		if (state.get(data) != STATE_RESERVABLE)
			ins.inc(delta, 0);
		if (!workReserved.is(data) && state.get(data) > STATE_RESERVED)
			ins.inc(0, delta);
	}
	
	public COORDINATE coo() {
		return coo;
	}


	
}
