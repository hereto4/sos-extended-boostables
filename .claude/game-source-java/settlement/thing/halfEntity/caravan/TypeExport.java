package settlement.thing.halfEntity.caravan;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;

import game.GAME;
import settlement.main.SETT;
import settlement.room.infra.export.ExportFetcher;
import snake2d.util.datatypes.COORDINATE;
import util.gui.misc.GBox;
import util.text.D;

class TypeExport extends Type{

	private static CharSequence ¤¤verb = "¤fetching";
	
	static {
		D.ts(TypeExport.class);
	}
	
	TypeExport() {
		super(¤¤verb);
	}

	@Override
	public boolean init(Caravan c, int amount) {
		c.reservedGlobally = (short) amount;
		ExportFetcher f = ROOMS().EXPORT.FETCHER;
		f.initCaravan(c.res, amount);
		fetch(c);
		return c.path.isSuccessful();
	}
	
	private boolean fetch(Caravan c) {
		c.path.clear();
		
		c.tmp = 1;
		
		c.reserved = 0;
		int target = c.reservedGlobally;
		
		ExportFetcher f = ROOMS().EXPORT.FETCHER;
		COORDINATE coo = f.getReservableSpot(c.ctx(), c.cty(), c.res);
		if (coo != null) {
			int am = f.reservable(c.res, coo);
			if (am > target)
				am = target;
			c.reserved = (short) am;
			f.reserve(c.res, coo, am);
			c.path.request(c.ctx(), c.cty(), coo.x(), coo.y(), false);
			c.move();
			if (!c.path.isSuccessful()) {
				GAME.Notify(c.ctx() + " " + c.cty() + "->" + coo.x() + " " + coo.y());
				return false;
			}
			c.tmp = 1;
			return true;
		}
		
		if (SETT.PATH().finders.resource.find(c.res().bit, c.res().bit, c.res().bit, c.ctx(), c.cty(), c.path, Integer.MAX_VALUE) != null) {
			c.reserved = 1;
			if (target - 1 > 0) {
				c.reserved += SETT.PATH().finders.resource.reserveExtra(true, true, c.res, c.path.destX(), c.path.destY(), target-1);
			}
			c.move();
			c.tmp = 2;
			
			return true;
		}
		
		return false;
	}
	
	private boolean pickup(Caravan c) {
		
		if (c.reserved <= 0)
			return false;
		
		ExportFetcher f = ROOMS().EXPORT.FETCHER;
		if (c.tmp == 1) {
			
			coo.set(c.path.destX(), c.path.destY());
			int am = f.reserved(c.res, coo);
			if (am > 0) {
				int max = 1;
				if (max > am)
					max = am;
				if (max > c.reserved)
					max = c.reserved;
				f.finish(c.res, coo, max);
				f.cancel(c.res, max, max);
				c.amountCarried += max;
				c.reserved -= max;
				c.reservedGlobally -= max;
				return true;
			}
			
		}else if (c.tmp == 2) {
			if (SETT.PATH().finders.resource.pickup(c.res, c.path.destX(), c.path.destY(), 1) == 1) {
				c.reserved --;
				c.reservedGlobally --;
				c.amountCarried ++;
				f.cancel(c.res, 1, 1);
				return true;
			}
		}
		c.reserved = 0;
		return false;
		
	}

	@Override
	public boolean update(Caravan c, double ds) {
		if (!c.returning) {
			if (c.reserved > 0) {
				pickup(c);
			}else if (c.reservedGlobally > 0 && fetch(c)) {
				;
			}else if (c.amountCarried != 0 && PATH().finders.entryPoints.find(c.ctx(), c.cty(), c.path, Integer.MAX_VALUE)) {
				c.move();
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
		ExportFetcher f = ROOMS().EXPORT.FETCHER;
		if (c.reserved > 0) {
			if (c.tmp == 1) {
				coo.set(c.path.destX(), c.path.destY());
				int am = f.reserved(c.res, coo);
				if (am > 0) {
					if (am > c.reserved)
						am = c.reserved;
					f.finish(c.res, coo, am);
					f.cancel(c.res, am, am);
					c.amountCarried += am;
					c.reserved -= am;
					c.reservedGlobally -= am;
				}
				
			}else if (c.tmp == 2) {
				int am = SETT.PATH().finders.resource.pickup(c.res, c.path.destX(), c.path.destY(), c.reserved);
				c.amountCarried += am;
				c.reserved -= am;
				c.reservedGlobally -= am;
				f.cancel(c.res, am, am);
			}
			c.reserved = 0;
		}
		f.cancel(c.res, 0, c.reservedGlobally);
		c.reservedGlobally = 0;
		
	}
	
	@Override
	public void hoverInfo(GBox box, Caravan c) {
		box.text(name);
		if (c.reservedGlobally-c.amountCarried > 0)
			box.setResource(c.res, c.reservedGlobally-c.amountCarried);
	}
	
	@Override
	int carryCap(Caravan c) {
		return c.reservedGlobally + c.amountCarried;
	}
	
}
