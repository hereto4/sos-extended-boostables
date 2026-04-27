package game.faction.trade;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.FactionResource;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.faction.trade.TradeShipper.Partner;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.type.HCLASSES;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import util.updating.TileUpdater;
import view.interrupter.IDebugPanel;
import world.WORLD;
import world.entity.caravan.Shipment;
import world.region.RD;

public class TradeManager extends FactionResource{

	public static final int TRADE_INTERVAL = 1;
	
	private final TileUpdater updater;
	private final TradeShipper shipper = new TradeShipper();
	private final TradeSorter sorter = new TradeSorter();
	
	public static final int MIN_LOAD = 32;

	public static int totalFee(Faction seller, Faction buyer, double distance, RESOURCE res, int amount) {
		
		if (false) {
			//tradable class. Resources and slaves, and who knows what else
		}
		
		double toll = toll(seller, buyer, distance);
		double tarif = tarif(seller, buyer, res, amount);
		return (int) (Math.floor((toll+tarif))*amount);
		
	}
	
	public static double tarif(Faction seller, Faction buyer, RESOURCE res, int amount) {
		
		if (buyer == FACTIONS.player()) {
			
			FactionNPC npc = (FactionNPC) seller;
			double price = price(npc, res, amount);
			return npc.stockpile.playerTarif(res, amount)*price;
//			buyer = seller;
//			seller = FACTIONS.player();
		}
		
		
		FactionNPC npc = (FactionNPC) buyer;
		
		double price = price(npc, res, amount);
		double d = npc.stockpile.prodRate(res)/npc.stockpile.rate(res);
		double tt = DIP.ALLY().tarif*0.5;
		if (seller == FACTIONS.player()) {
			tt = ROPINIONS.tradeCost(npc);
			tt += npc.stockpile.playerTarif(res, amount);
			if (tt > 0.75)
				tt = 0.75;
		}
		return tt*d*price;
	}
	
	
	public static double toll(FactionNPC f) {
		return toll(FACTIONS.player(), f, RD.DIST().distance(f));
	}

	private static double price(FactionNPC npc, RESOURCE res, int amount) {
		double price = npc.stockpile.price(res.index(), amount);
		return price;
	}
	
	public static double toll(Faction f, Faction f2, double distance) {
		
		distance -= 40;
		distance = Math.max(0, distance);
		distance /= 200.0;

		
		if (f == FACTIONS.player() || f2 == FACTIONS.player()) {
			return 4.0*distance / RD.DIST().boostable.get(HCLASSES.CITIZEN().get(null));
		}else {
			return distance;
		}
	}

	
	public TradeManager(FACTIONS fs){
		
		IDebugPanel.add("Trade all", new ACTION() {
			
			@Override
			public void exe() {
				clear();
				prime();
			}
		});
		
		updater = new TileUpdater(FACTIONS.MAX(), FACTIONS.MAX()+4, TRADE_INTERVAL*TIME.days().bitSeconds()) {
			
			@Override
			protected void update(int iteration, int factionI, int vv, double timeSinceLast) {
				if (factionI == FACTIONS.MAX()/2 || factionI == 0) {
					
					if (iteration == 0) {
						sellPlayer();
					}
					if (shipper.partners() > 0) {
						Partner p = shipper.popNextPartner();						
						Faction b = p.faction();
						ship(b, FACTIONS.player(), p, true);
					}
					return;
				}
				if (factionI == FACTIONS.MAX()/2+1 || factionI == 1) {
					pbuy(FACTIONS.player(), iteration);
					return;
				}
				
				if (factionI < FACTIONS.MAX()/2)
					factionI -= 1;
				else
					factionI -= 3;
				
				if (factionI >= FACTIONS.MAX())
					return;
				
				Faction buyer = FACTIONS.getByIndex(factionI);
				pbuy(buyer, iteration);
				
			}
			
			private void pbuy(Faction buyer, int iteration) {

				if (buyer.isActive() && buyer.capitolRegion() != null) {
					if (iteration == 0) {
						buy(buyer);
					}
					
					if (shipper.partners() > 0) {
						Partner p = shipper.popNextPartner();						
						Faction b = p.faction();
						ship(buyer, b, p, true);
					}					
				}
			}
		};
	}

	@Override
	protected void save(FilePutter file) {
		updater.save(file);
		shipper.save(file);
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		updater.load(file);
		shipper.load(file);
	}

	@Override
	protected void clear() {
		updater.clear();
		shipper.clear();
	}

	@Override
	protected void update(double ds, Faction f) {
		updater.update(ds);
	}
	
	private void sellPlayer() {
	
		
		if (!SETT.exists() || SETT.ENTRY().isClosed())
			return;
		
		shipper.init(FACTIONS.player());
		sorter.sellPlayer(shipper);
		
	}
	
	void buy(Faction buyer) {

		shipper.init(buyer);
		sorter.buy(buyer, shipper);

	}
	
	private void ship(Faction buyer, Faction seller, Partner count, boolean shipping) {
		if (!buyer.isActive())
			return;
		
		int am = 0;
		for (RESOURCE r : RESOURCES.ALL()) {
			am += count.traded(r);
			
		}
		
		if (am <= 0)
			return;
		
		Shipment s = null;
		if (shipping && seller.isActive()) {
			boolean create = buyer == FACTIONS.player() || seller == FACTIONS.player();
			if (!create)
				create = WORLD.ENTITIES().allFast().size() < 200;
			
				if (create) {
					s = WORLD.ENTITIES().caravans.create(seller.capitolRegion(), buyer.capitolRegion(), ITYPE.trade);
					if (s == null)
						LOG.ln("here!");
				}
			
			
		}
		
		if (s != null) {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = count.traded(r);
				if (a > 0) {
					s.loadAlreadyReserved(r, a);
				}
			}
		}else {
			for (RESOURCE r : RESOURCES.ALL()) {
				int a = count.traded(r);
				if (a > 0) {
					buyer.buyer().reserveSpace(r, -am, ITYPE.trade);;
					buyer.buyer().deliver(r, am, ITYPE.trade);
				}
			}
		}
		//LOG.ln();
		
	}
	
	public void prime() {
		for (int i = 0; i < FACTIONS.NPCs().size(); i++) {
			FactionNPC f = FACTIONS.NPCs().get(i);
			if (!f.isActive())
				continue;
			buy(f);
			while (shipper.hasNextPartner()) {
				Partner p = shipper.popNextPartner();
				ship(f, p.faction(), p, false);
			}
			
		}
	}
	
}
