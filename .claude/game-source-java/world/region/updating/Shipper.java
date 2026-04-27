package world.region.updating;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.trade.ITYPE;
import game.time.TIME;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.map.regions.Region;
import world.region.RD;
import world.region.RDOutputs.RDResource;

final class Shipper {


	public Shipper() {
		
	}


	
	public void ship(Region r, double seconds) {
		
		Faction f = r.faction();
		
		if (f == null)
			return;
		
		if (r.besieged())
			return;
		
		if (f.capitolRegion() == null)
			return;
		
		double days = seconds*TIME.secondsPerDayI();
		int am = 0;
		
		if (f == FACTIONS.player()) {
			if (r.capitol())
				return;
		}
		
		f.credits().inc(RD.OUTPUT().MONEY.boost.get(r)*days, CTYPE.TAX);
		
		for (RDResource res : RD.OUTPUT().RES) {
			count(res, r, seconds);
			am += amount(res, r, seconds);
			
		}
		
		if (am <= 0)
			return;
		
		Shipment c = WORLD.ENTITIES().caravans.create(r, f.capitolRegion(), ITYPE.tax);
		if (c != null) {
			for (RDResource res : RD.OUTPUT().RES) {
				int a = amount(res, r, seconds);
				if (a > 0) {
					c.loadAndReserve(res.res, a);
					clear(res, r);
				}
			}
		}

	}

	private void count(RDResource res, Region r, double seconds) {
		
		double am = res.boostYearlyPart.get(r)*seconds*TIME.secondsPerDayI();
		int a = (int) am;
		if (am-a > RND.rFloat())
			a++;
		
		res.yearlyAccumilation.inc(r, a);
	}
	
	private void clear(RDResource res, Region r) {
		if (res.daysUntilDailydelivery() == 0) {
			res.yearlyAccumilation.set(r, 0);
		}
		
	}
	
	private int amount(RDResource res, Region r, double seconds) {
		
		int am = (int) Math.ceil(res.boost.get(r)*seconds*TIME.secondsPerDayI());
		
		if (res.daysUntilDailydelivery() == 0) {
			am += res.yearlyAccumilation.get(r);
		}
		return am;
	}
	
	public void shipAll(Faction f, double days) {
		
		for (int ri = 0; ri < f.realm().regions(); ri++) {
			Region reg = f.realm().region(ri);
			for (RDResource res : RD.OUTPUT().RES) {
				int a = (int) Math.ceil(res.boost.get(reg)*days);
				if (a > 0) {
					f.buyer().deliver(res.res, a, ITYPE.tax);
				}
			}
			
		}
		
	}
}
