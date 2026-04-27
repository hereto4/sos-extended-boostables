package settlement.room.law.execution;

import game.time.TIME;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.main.util.RoomBits;
import snake2d.util.bit.Bit;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;

public final class ExecutionStation{

	public final static int services = 8;
	
	private final static Bit workReserved = new Bit(0b010000);
	private final static Bits state = new Bits(0b01111);
	private final static int STATE_RESERVABLE = 0;
	private final static int STATE_RESERVED = 1;
	private final static int STATE_USED = 2;
	private final static int STATE_EXECUTING = 3;
	
	private final Coo coo = new Coo();
	private int data;
	private ExecutionInstance ins;
	private int code;
	private static final ExecutionStation self = new ExecutionStation();
	
	static ExecutionStation init(int tx, int ty) {
		ExecutionInstance ins = SETT.ROOMS().EXECUTION.get(tx, ty);
		if (ins == null)
			return null;
		int c = SETT.ROOMS().fData.tileData.get(tx, ty);
		if (c != Constructor.codeChopp && c != Constructor.codeHang)
			return null;
		self.code = c;
		self.data = SETT.ROOMS().data.get(tx, ty);
		self.ins = ins;
		self.coo.set(tx, ty);
		return self;
		
	}
	
	static FSERVICE service(int tx, int ty) {
		if (init(tx, ty) != null)
			return self.service;
		return null;
	}
	
	
	public boolean isChop() {
		return code == Constructor.codeChopp;
	}
	
	public boolean isHang() {
		return code == Constructor.codeHang;
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
			if (isChop() && SETT.ROOMS().fData.tileData.get(coo, d) == Constructor.codeChoppTurn)
				return d.perpendicular();
			else if(isHang())
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
		data = workReserved.clear(data);
		save();
	}
	
	public boolean workReserved() {
		if (state.get(data) <= STATE_RESERVED)
			workCancel();
		return workReserved.is(data);
	}
	
	public boolean workExecute() {
		if (workReserved() && state.get(data) == STATE_USED) {
			for (ENTITY e : SETT.ENTITIES().getAtTile(coo.x(), coo.y())) {
				if (e instanceof Humanoid) {
					HEvent.Handler.executePrisoner((Humanoid) e);
					return true;
				}
			}
		}
		return false;
	}
	
	static boolean hasBox(int data) {
		return data != STATE_EXECUTING;
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

	private final FSERVICE service = new FSERVICE() {
		
		private final RoomBits free = new RoomBits(coo, new Bits(0b0001_1110_0000)) {
			@Override
			protected void remove() {
				if (findableReservedCanBe())
					ins.blueprintI().data.report(service, -1);
			};
			
			@Override
			protected void add() {
				if (findableReservedCanBe())
					ins.blueprintI().data.report(service, 1);
			};
		};
		
		@Override
		public int y() {
			return coo.y();
		}
		
		@Override
		public int x() {
			return coo.x();
		}
		
		@Override
		public boolean findableReservedIs() {
			return free.get() < services;
		}
		
		@Override
		public boolean findableReservedCanBe() {
			return free.get() > 0;
		}
		
		@Override
		public void findableReserveCancel() {
			free.inc(ins, 1);
		}
		
		@Override
		public void findableReserve() {
			free.inc(ins, -1);
		}
		
		@Override
		public void consume() {
			free.inc(ins, 1);
		}
	};
	
}
