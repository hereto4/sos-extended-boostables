package settlement.room.service.food.canteen;

import game.audio.SoundRace;
import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResG;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import snake2d.util.bit.Bits;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.data.GETTER;
import util.data.INT.INTE;

class SOven implements SETT_JOB{

	public final static int I = 1;
	
	private int data;
	private CanteenInstance ins;
	private final Coo coo = new Coo();
	private final ROOM_CANTEEN b;

	
	SOven(ROOM_CANTEEN b){
		this.b = b;
	}
	
	SOven get(int tx, int ty) {
		if (b.is(tx, ty) && SETT.ROOMS().fData.tileData.get(tx, ty) == I) {
			ins = b.getter.get(tx, ty);
			coo.set(tx, ty);
			data = SETT.ROOMS().data.get(tx, ty);
			return this;
		}
		return null;
	}
	
	public void dispose(int x, int y) {
		if (get(x, y) != null)
			edible.dispose();
		
	}
	
	public final Edibi edible = new Edibi();
	
	public final INTE coal = new INTE() {
		
		private final Bits bits = new Bits(0x000F0000);
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return 0x0F;
		}
		
		@Override
		public int get() {
			return bits.get(data);
		}
		
		@Override
		public void set(int t) {
			data = bits.set(data, t);
			SETT.ROOMS().data.set(ins, coo, data);
		}
	};
	
	public final INTE coalReserved = new INTE() {
		
		private final Bits bits = new Bits(0x00F00000);
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return 4;
		}
		
		@Override
		public int get() {
			return bits.get(data);
		}
		
		@Override
		public void set(int t) {
			data = bits.set(data, t);
			SETT.ROOMS().data.set(ins, coo, data);
		}
	};
	
	public final BOOLEAN_MUTABLE workReserved = new BOOLEAN_MUTABLE() {
		
		private final Bits bits = new Bits(0x00F000000);
		
		@Override
		public boolean is() {
			return bits.get(data) == 1;
		}
		
		@Override
		public BOOLEAN_MUTABLE set(boolean b) {
			data = bits.set(data, b ? 1 : 0);
			SETT.ROOMS().data.set(ins, coo, data);
			return this;
		}
	};
	
	public final INTE coalWithDraw = new INTE() {
		
		private final Bits bits = new Bits(0x0F0000000);
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return 8;
		}
		
		@Override
		public int get() {
			return bits.get(data);
		}
		
		@Override
		public void set(int t) {
			if (coal.get() > 1) {
				int a = CLAMP.i(t, 0, coal.get()-1);
				coal.inc(-a);
				t -= a;
			}else if(!workReserved.is()) {
				int a = CLAMP.i(t, 0, coal.get());
				coal.inc(-a);
				t -= a;
			}
			data = bits.set(data, t);
			SETT.ROOMS().data.set(ins, coo, data);
		}
	};

	@Override
	public void jobReserve(RESOURCE r) {
		if (r == b.industryFuel.ins().get(0).resource) {
			if (coalReserved.get() == 0)
				coalReserved.inc(1);
		}else if (r == null) {
			if (!workReserved.is())
				workReserved.set(true);
		}else if (r != null && edible.canReserve() && edible.resMask().has(r)) {
			edible.reserve(r);
		}else
			throw new RuntimeException("" + r + " " + edible.get().resource);
	}

	@Override
	public boolean jobReservedIs(RESOURCE r) {
		if (r == b.industryFuel.ins().get(0).resource) {
			return coalReserved.get() > 0;
		}else if (r == null) {
			return workReserved.is();
		}else{
			return edible.isReserved(r);
		}
	}

	@Override
	public void jobReserveCancel(RESOURCE r) {
		if (r == b.industryFuel.ins().get(0).resource && coalReserved.get() > 0) {
			coalReserved.inc(-1);
		}else if (r == null) {
			workReserved.set(false);
		}else if (r!= null)
			edible.reserveCancel(r);
	}
	
	@Override
	public boolean jobReserveCanBe() {
		if (isReadyToWork())
			return true;
		if (coalReserved.get() == 0 && coal.get() == 0)
			return true;
		return edible.canReserve();
	}
	
	public COLOR debug() {
		if (isReadyToWork())
			return COLOR.GREEN100;
		if (coalReserved.get() == 0 && coal.get() == 0)
			return COLOR.ORANGE100;
		if (edible.amountReserved.get(data) > 0)
			return COLOR.RED100;
		return COLOR.BLACK;
	}

	private final RBITImp btmp = new RBITImp();
	
	@Override
	public RBIT jobResourceBitToFetch() {
		
		if (isReadyToWork())
			return null;
		
		btmp.clear();
		
		if (coalReserved.get() == 0 && coal.get() == 0)
			btmp.or(b.industryFuel.ins().get(0).resource);
		if (edible.canReserve())
			btmp.or(edible.resMask());
		return btmp;
		
	}
	
	@Override
	public int jobResourcesNeeded(Humanoid skill) {
		return Math.min(0x0F, SETT.ROOMS().STOCKPILE.carryCap(skill));
	}
	
	private boolean isReadyToWork() {
		if (coal.get() <= 0)
			return false;
		if (edible.get() == null)
			return false;
		if (workReserved.is())
			return false;
		return true;
	}

	@Override
	public double jobPerformTime(Humanoid skill) {
		return 30;
	}

	@Override
	public void jobStartPerforming() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
		if (r == b.industryFuel.ins().get(0).resource && coalReserved.get() > 0) {
			coalReserved.inc(-1);
			coal.inc(ram);
		}else if (r == null && workReserved.is()) {
			workReserved.set(false);
			coalWithDraw.set(coalWithDraw.get());
			ins.tally(edible.get(), 1, 0);
			edible.cook();
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (b.food.get(coo.x()+d.x(), coo.y()+d.y()) != null) {
					b.food.check();
				}
			}
		}else if (r != null && edible.isReserved(r)) {
			edible.work(r, ram);
		}
		return null;
	}

	@Override
	public COORDINATE jobCoo() {
		return coo;
	}

	@Override
	public CharSequence jobName() {
		return b.employment().verb;
	}

	@Override
	public boolean jobUseTool() {
		return false;
	}

	@Override
	public SoundRace jobSound() {
		return b.employment().sound();
	}
	
	class Edibi implements GETTER<ResG>{
		
		private final Bits bits = 			new Bits(0x000000FF);
		private final Bits amount = 		new Bits(0x00000F00);
		private final Bits amountReserved = new Bits(0x0000F000);
		
		@Override
		public ResG get() {
			if (amount.get(data) > 0)
				return RESOURCES.EDI().all().getC(bits.get(data));
			return null;
		}
		
		public void dispose() {
			if (amount.get(data) > 0)
				SETT.THINGS().resources.create(coo, get().resource, amount.get(data));
			amountReserved.set(data, 0);
			
		}

		boolean canReserve() {
			return amountReserved.get(data) == 0 && amount.get(data) == 0 && !ins.fetchMask().isClear();
		}
		
		RBIT resMask() {
			if (amount.get(data) > 0 || amountReserved.get(data) > 0) {
				return RESOURCES.EDI().all().getC(bits.get(data)).resource.bit;
			}
			return ins.fetchMask();
		}
		
		boolean isReserved(RESOURCE res) {
			return amountReserved.get(data) > 0 && res == RESOURCES.EDI().all().getC(bits.get(data)).resource;
		}
		
		private int total() {
			return amountReserved.get(data) + amount.get(data);
		}
		
		private void save() {
			int ndata = data;
			
			data = SETT.ROOMS().data.get(coo);
			if (total() > 0) {
				ins.tally(RESOURCES.EDI().all().getC(bits.get(data)), 0, -total());
			}
			
			data = ndata;
			if (total() > 0)
				ins.tally(RESOURCES.EDI().all().getC(bits.get(data)), 0, total());
			
			SETT.ROOMS().data.set(ins, coo, data);
		}
		
		void reserveCancel(RESOURCE res) {
			if (amountReserved.get(data) > 0) {
				data = amountReserved.inc(data, -1);
				save();
			}
		}
		
		void work(RESOURCE res, int am) {
			if (isReserved(res)) {
				data = amountReserved.inc(data, -1);
				data = amount.inc(data, am);
				save();
			}
		}
		
		void cook() {
			data = amount.inc(data, -1);
			save();
		}
		
		void reserve(RESOURCE res) {
			if (total() == 0) {
				data = bits.set(data, RESOURCES.EDI().get(res).index());
			}
			data = amountReserved.inc(data, 1);
			save();
		}
		
	}


	
}
