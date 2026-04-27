package world.region;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.stockpile.NPCStockpile;
import game.time.TIME;
import init.resources.Growable;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import snake2d.util.MATH;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;
import world.region.RD.RDInit;
import world.region.pop.RDRace;

public final class RDOutputs {

	private static CharSequence ¤¤taxes = "¤Taxes";
	private static CharSequence ¤¤taxRate = "¤Tax Rate";
	private static CharSequence ¤¤taxD = "¤Taxes are generated from your subjects. Higher tax rate increases taxes, but decreases loyalty.";
	static {
		D.ts(RDOutputs.class);
	}
	
	public INT_OE<Region> taxRate;
	
	public final LIST<RDOutput> ALL;
	public final RDOutput MONEY;
	public final LIST<RDResource> RES;
	
	public RDOutputs(RDInit init) {

		if (false) {
			//swap tax for a "squeeze" action. Instantly delivers materials, 25% devastation and -loyalty 
		}
		
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				if (newOwner == FACTIONS.player())
					taxRate.setD(reg, 0.25);
				for (RDOutput r : ALL) {
					r.yearlyAccumilation.set(reg, 0);
				}
				
			}
		};
		
		ArrayList<RDResource> rr = new ArrayList<>(RESOURCES.ALL().size());
		
		for (RESOURCE res : RESOURCES.ALL()) {
			rr.add(new RDResource(init, res));
		}
		this.RES = rr;
		
		taxRate = init.count.new DataNibble("TAX_RATE", ¤¤taxRate, ¤¤taxD, 10);
		Boostable boost = BOOSTING.push("TAX_INCOME", NPCStockpile.AVERAGE_PRICE, ¤¤taxes, ¤¤taxD, UI.icons().m.coins, BoostableCat.ALL().WORLD);
		MONEY = new RDOutput(boost, init);
		
		ALL = new ArrayList<RDOutputs.RDOutput>(0).join(MONEY).join(rr);
		
		{
			RBooster b = new RBooster(new BSourceInfo(taxRate.info().name, UI.icons().s.money), 1, 2, true) {
	
				@Override
				public double get(Region t) {
					return taxRate.getD(t);
				}
			};
			
			b.add(MONEY.boost);
//			
//			for (RDOutput o : ALL) {
//				b.add(o.boost);
//				b.add(o.boostYearlyPart);
//			}
		}
		

		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				RBooster b = new RBooster(new BSourceInfo(taxRate.info().name, UI.icons().s.money), 1, 0.5, true) {

					@Override
					public double get(Region t) {
						return taxRate.getD(t);
					}
				};
				for (RDRace r : RD.RACES().all) {
					b.add(r.loyalty.target);
				}
			}
		};
		
		BOOSTING.connecter(a);

	}

	void init() {
		
	}
	
	public static class RDOutput {
		
		public final Boostable boost;
		public final Boostable boostYearlyPart;
		public final INT_OE<Region> yearlyAccumilation;
		
		RDOutput(Boostable boost, RDInit init) {
			this.boost = boost;
			this.boostYearlyPart = BOOSTING.push(boost.key+"_YEARLY", 0, boost.name, ¤¤taxD, boost.icon, BoostableCat.ALL().WORLD_DUMP);
			yearlyAccumilation = init.count.new DataInt(boost.key + "_" + "ACC");
		}

		public int getDelivery(Region reg) {
			return (int) (boost.get(reg) + boostYearlyPart.get(reg));
		}
		
		public int loot(Region reg) {
			double d = 1.0-RD.DEVASTATION().current.getD(reg);
			
			return (int) (d* (boost.get(reg)+yearlyAccumilation.get(reg)));
		}
		
		public int daysUntilDailydelivery() {
			int d = 0;
			int now = TIME.days().bitsSinceStart()%(int)TIME.years().bitConversion(TIME.days());
			int remain = (int) MATH.ETA(now, d, (int)TIME.years().bitConversion(TIME.days()));
			return remain;
		}
		
	}
	
	public static class RDResource extends RDOutput{
		
		public final RESOURCE res;
		
		
		RDResource(RDInit init, RESOURCE res) {
			super(BOOSTING.push("RESOURCE_PRODUCTION_" + res.key, 0, Dic.¤¤Production + ": " + res.names, res.desc, res.icon(),  BoostableCat.ALL().WORLD_PRODUCTION), init);
			this.res = res;
			
		}
		
		@Override
		public int daysUntilDailydelivery() {
			
			
			Growable g = RESOURCES.growable().get(res);
			if (g != null) {
				int d = (int) (g.seasonalOffset*TIME.years().bitConversion(TIME.days()));
				int now = TIME.days().bitsSinceStart()%(int)TIME.years().bitConversion(TIME.days());
				int remain = (int) MATH.ETA(now, d, (int)TIME.years().bitConversion(TIME.days()));
				return remain;
			}else {
				return super.daysUntilDailydelivery();
			}
		}
		
	}
	
	public RDResource get(RESOURCE res){
		return RES.get(res.index());
	}
	
	public RESOURCE fromBoost(Boostable bo) {
		if (bo.index() >= RES.get(0).boost.index() && bo.index() < RES.get(RES.size()-1).boostYearlyPart.index()) {
			
			return RESOURCES.ALL().get((bo.index()-RES.get(0).boost.index())/2);
		}
		return null;
	}
	
	
	
}
