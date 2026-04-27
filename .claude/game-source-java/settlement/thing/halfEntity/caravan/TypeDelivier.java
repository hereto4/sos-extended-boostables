package settlement.thing.halfEntity.caravan;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;

import settlement.main.SETT;
import settlement.room.infra.importt.ImportThingy;
import snake2d.util.datatypes.COORDINATE;
import util.gui.misc.GBox;
import util.text.D;

class TypeDelivier extends Type{

	static CharSequence ¤¤name = "¤delivering";
	static {
		D.ts(TypeDelivier.class);
	}
	TypeDelivier() {
		super(¤¤name);
	}

	@Override
	public boolean init(Caravan c, int amount) {
		c.amountCarried = (short) amount;
		c.reservedGlobally = c.amountCarried;
		ROOMS().IMPORT.UNLOADER.initCaravan(c.res, c.amountCarried);

		return deliver(c) && c.path.isSuccessful();
	}
	
	private boolean deliver(Caravan c) {
		c.path.clear();
		ImportThingy f = ROOMS().IMPORT.UNLOADER;
		COORDINATE coo = f.getReservableSpot(c.ctx(), c.cty(), c.res);
		if (coo == null)
			return false;
		
		int am = f.reservable(c.res, coo);
		if (am > c.amountCarried)
			am = c.amountCarried;
		c.reserved = (short) am;
		f.reserve(c.res, coo, am);
		c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false);
		if (!c.path.isSuccessful()) {
			f.reserve(c.res, coo, -am);
			c.reserved = 0;
			return false;
		}
		c.move();
		return true;
	}
	
	private boolean pickup(Caravan c) {
		
		ImportThingy f = ROOMS().IMPORT.UNLOADER;
		coo.set(c.path.destX(), c.path.destY());
		int am = f.reserved(c.res, coo);
		if (am <= 0) {
			c.reserved = 0;
			return false;
		}
		int max = 1;
		if (max > am)
			max = am;
		if (max > c.amountCarried)
			max = c.amountCarried;
		f.finish(c.res, coo, max);
		c.amountCarried -= max;
		c.reserved -= max;
		
		return true;
	}

	@Override
	public boolean update(Caravan c, double ds) {
		if (!c.returning) {
			if (c.reserved > 0) {
				pickup(c);
			}else if (c.amountCarried > 0 && deliver(c)) {
				
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
		if (!c.returning && c.amountCarried > 0) {
			coo.set(c.path.destX(), c.path.destY());
			int am = f.reserved(c.res, coo);
			if (am > 0) {
				if (am > c.amountCarried)
					am = c.amountCarried;
				f.finish(c.res, coo, am);
				c.amountCarried -= am;
				
			}
			f.cancel(c.res, c.amountCarried);
			if (dump)
				SETT.THINGS().resources.createPrecise(c.ctx(), c.cty(), c.res, c.amountCarried);
			c.amountCarried = 0;
		}
		if (c.amountCarried > 0) {
			c.amountCarried = 0;
		}
		
		c.reserved = 0;
		
	}

	@Override
	public void hoverInfo(GBox box, Caravan c) {
		box.text(name);
		if (c.amountCarried > 0)
			box.setResource(c.res, c.amountCarried);
	}
	
}
