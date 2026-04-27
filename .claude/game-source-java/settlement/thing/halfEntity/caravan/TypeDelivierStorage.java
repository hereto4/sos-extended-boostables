package settlement.thing.halfEntity.caravan;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;

import settlement.main.SETT;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.infra.importt.ImportThingy;
import settlement.room.infra.logistics.MoveJob.ROOM_MOVE_DEST;
import settlement.room.infra.stockpile.StockpileInstance;
import settlement.room.main.throne.THRONE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.CLAMP;
import util.gui.misc.GBox;
import util.text.D;

class TypeDelivierStorage extends Type{

	static CharSequence ¤¤name = "¤delivering";
	static {
		D.ts(TypeDelivierStorage.class);
	}
	TypeDelivierStorage() {
		super(¤¤name);
	}

	@Override
	public boolean init(Caravan c, int amount) {
		c.amountCarried = (short) amount;
		c.reservedGlobally = (short) amount;
		ROOMS().IMPORT.UNLOADER.initCaravan(c.res, c.reservedGlobally);
		return find(c);
	}
	
	private boolean find(Caravan c) {
		c.reserved = 0;
		ROOM_MOVE_DEST best = null;
		double bi = 1.0;
		for (int si = 0; si < SETT.ROOMS().STOCKPILE.all().size(); si++) {
			StockpileInstance ins = SETT.ROOMS().STOCKPILE.getInstance(si);
			int am = SETT.ROOMS().STOCKPILE.tally().space.get(c.res, ins)-SETT.ROOMS().STOCKPILE.tally().spaceReserved.get(c.res, ins);
			
			if (ins.destSpaceMask().has(c.res) && am > bi && am > 5) {
				best = ins;
				bi = ins.storedD(c.res);
			}
		}
		
		COORDINATE coo = null;
		if (best != null) {
			coo = best.destCrate(c.res.bit, 1, -1, -1);
		}
		
//		if (coo == null)
//			coo = SETT.PATH().finders().storage.reserve(c.ctx(), c.cty(), c.res, Integer.MAX_VALUE);
		if (coo != null) {
			c.reserved = 1;
			TILE_STORAGE s = SETT.PATH().finders().storage.getter.get(coo);
			int extra = CLAMP.i(c.amountCarried-c.reserved, 0, s.storageReservable());
			s.storageReserve(extra);
			c.reserved += extra;
			if (c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false)) {
				c.move();
				return true;
			}
			return false;
		}else {
			
			c.reserved = c.amountCarried;
			coo = SETT.PATH().finders.rndCoo.find(THRONE.coo().x(), THRONE.coo().y(), 8);
			c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false);
			
			if (c.path.isSuccessful()) {
				c.move();
				return true;
			}
			return false;
		}
	}
	
	private boolean refind(Caravan c) {
		c.reserved = 0;
		COORDINATE coo = SETT.PATH().finders().storage.reserve(c.ctx(), c.cty(), c.res, 32);
		if (coo != null) {
			c.reserved = 1;
			TILE_STORAGE s = SETT.PATH().finders().storage.getter.get(coo);
			int extra = CLAMP.i(c.amountCarried-c.reserved, 0, s.storageReservable());
			s.storageReserve(extra);
			c.reserved += extra;
			if (c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false)) {
				c.move();
				return true;
			}
			unreserve(c);
			return false;
		}else {
			return false;
		}
	}
	
	private void unreserve(Caravan c) {
		if (c.reserved > 0) {
			TILE_STORAGE s = SETT.PATH().finders().storage.getter.get(c.path.destX(), c.path.destY());
			if (s != null && s.storageReserved() > 0) {
				s.storageUnreserve(CLAMP.i(c.reserved, 0, s.storageReserved()));
				
			}
		}
		c.reserved = 0;
	}
	
	private void pickup(Caravan c) {
		TILE_STORAGE s = SETT.PATH().finders().storage.getter.get(c.path.destX(), c.path.destY());
		if (s != null && s.storageReserved() > 0) {
			s.storageDeposit(1);
			c.reserved --;
			c.amountCarried--;
		}else {
			SETT.THINGS().resources.create(c.path.destX(), c.path.destY(), c.res, 1);
			c.amountCarried -= 1;
			c.reserved--;
		}
	}

	@Override
	public boolean update(Caravan c, double ds) {
		
		if (!c.returning) {
			if (c.reserved > 0) {
				pickup(c);
			}else if(c.amountCarried > 0 && refind(c)) {
				;
			}else if (PATH().finders.entryPoints.find(c.ctx(), c.cty(), c.path, Integer.MAX_VALUE)) {
				c.move();
				cancel(c, true);
				c.returning = true;
			}else {
				return false;
			}
			return true;
			
		}else {
			return false;
		}
	}

	@Override
	public void cancel(Caravan c, boolean dump) {
		ImportThingy f = ROOMS().IMPORT.UNLOADER;
		f.cancel(c.res, c.reservedGlobally);
		c.reservedGlobally = 0;
		unreserve(c);
		
		if (dump)
			SETT.THINGS().resources.createPrecise(c.ctx(), c.cty(), c.res, c.amountCarried);
		
		c.reserved = 0;
		c.amountCarried = 0;
	}

	@Override
	public void hoverInfo(GBox box, Caravan c) {
		box.text(name);
		if (c.amountCarried > 0)
			box.setResource(c.res, c.amountCarried);
	}
	
}
