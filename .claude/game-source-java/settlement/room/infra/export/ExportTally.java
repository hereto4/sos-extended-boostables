package settlement.room.infra.export;

import static util.text.Dic.¤¤noTrade;
import static util.text.Dic.¤¤noTradePartners;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.trade.FACTION_EXPORTER;
import game.faction.trade.ITYPE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.data.BOOLEANO;
import util.data.INT_O;
import util.keymap.RMapInt;
import util.text.D;
import world.region.RD;

public final class ExportTally implements FACTION_EXPORTER{
	
	double timer;
	final RMapInt<RESOURCE> attempting = new RMapInt<RESOURCE>(RESOURCES.map()) {
		
		@Override
		public int min(RESOURCE t) {
			return 0;
		};
		
	};
	public final RMapInt<RESOURCE> promised = new RMapInt<RESOURCE>(RESOURCES.map()) {
		@Override
		public int min(RESOURCE t) {
			return 0;
		};
	};
	private final RMapInt<RESOURCE> pAmount = new RMapInt<RESOURCE>(RESOURCES.map());
	private final RMapInt<RESOURCE> pCapacity =new RMapInt<RESOURCE>(RESOURCES.map());
	public final INT_O<RESOURCE> amount = pAmount;
	public final INT_O<RESOURCE> capacity = pCapacity;
	public final RMapInt<RESOURCE> priceCapsI = new RMapInt<RESOURCE>(RESOURCES.map()) {
		
		@Override
		public int max(RESOURCE t) {
			return 1000000;
		};
		
		@Override
		public int min(RESOURCE t) {
			return 1;
		};
		
		@Override
		public void clear() {
			for (RESOURCE r : RESOURCES.ALL())
				set(r, 1);
		};
		
	};
	
	public final BOOLEANO<RESOURCE> exporting = new BOOLEANO<RESOURCE>() {

		
		@Override
		public boolean is(RESOURCE res) {
			int am = (amount.get(res)-promised.get(res));
			return am > 0;
		}
	};
	
	ExportTally() {

	}
	
	final SAVABLE saver = new SAVABLE() {
		@Override
		public void save(FilePutter file) {
			promised.save(file);
			pAmount.save(file);
			pCapacity.save(file);
			attempting.save(file);
			priceCapsI.save(file);
			file.d(timer);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			promised.load(file);
			pAmount.load(file);
			pCapacity.load(file);
			attempting.load(file);
			priceCapsI.load(file);
			timer = file.d();
			pAmount.clear();
			for (ExportInstance i : SETT.ROOMS().EXPORT.all()) {
				if (i.resource() != null)
					pAmount.inc(i.resource(), i.amount);
			}
			
		}

		@Override
		public void clear() {
			promised.clear();
			pAmount.clear();
			pCapacity.clear();
			attempting.clear();
			priceCapsI.clear();
			timer = 0;
		}
	};
	
	void inc(RESOURCE r, int amount, int capacity) {
		this.pAmount.inc(r, amount);
		this.pCapacity.inc(r, capacity);
	}


	@Override
	public int priceSell(RESOURCE res, int amount) {
		return 1;
	}
	
	@Override
	public void sell(RESOURCE res, int amount, int price, Faction buyer) {
		sellFake(res, amount, price);
	}
	
	public void sellFake(RESOURCE res, int amount, int price) {
		FACTIONS.player().credits().inc(price, CTYPE.TRADE, res, amount);
		remove(res, amount, ITYPE.trade);
		GAME.count().TRADE_SALES.inc(price);
	}
	
	@Override
	public void remove(RESOURCE res, int amount, ITYPE type) {
		FACTIONS.player().res().inc(res, type.rtype, -amount);
		promised.inc(res, amount);
	}
	

	
	@Override
	public int forSale(RESOURCE res) {
		int aa = (amount.get(res) - (promised.get(res)));
		if (aa < 0)
			return aa;
		if (SETT.ENTRY().isClosed())
			return 0;
		
		return aa;
			
	}
	
	public void debug() {
		String s = "";
		for (RESOURCE r : RESOURCES.ALL()) {
			s += r.name + " ";
			s += "att: " + attempting.get(r) + " ";
			s += "pro: " + promised.get(r) + " ";
			s += "am: " + amount.get(r) + " ";
			s += "ca: " + capacity.get(r);
			s += "\r";
			
		}
		GAME.Notify(s);
	}





	public double prio(RESOURCE r) {
		if (pCapacity.get(r) == 0)
			return 0;
		
		
		double am = amount.get(r)-promised.get(r);
		return am/pCapacity.get(r);
	}
	
	public boolean okPrice(RESOURCE r, int price) {
		if (price >= priceCapsI.get(r))
			return true;
		return false;
	}

	
	private static CharSequence ¤¤ExportProblem = "¤You don't have any export depots set to this resource. No exporting can be done.";
	private static CharSequence ¤¤ExportFull = "¤Our export depots are full. We must increase their space if we are to export at full capacity.";
	private static CharSequence ¤¤priceCapProblem = "The current price is below your price cap. To trade, you must disable or decrease the price cap.";
	private static CharSequence ¤¤NoPrice = "¤There is no one willing to buy this resource. You must decrease either the tariff or the toll.";
	static {
		D.ts(ExportTally.class);
	}
	
	public CharSequence problem(RESOURCE res, boolean storage){
		
		if (storage && capacity.get(res) == 0) {
			return ¤¤ExportProblem;
		}
		
		if (RD.DIST().neighs().size() == 0) {
			return ¤¤noTrade;
		}

		if (DIP.traders().size() == 0)
			return ¤¤noTradePartners;
		
		if (FACTIONS.player().trade.pricesSell.get(res) <= 0) {
			return ¤¤NoPrice;
		}
		
		return null;
	}

	public CharSequence warning(RESOURCE res) {
		
		if (capacity.get(res) > 0 && amount.get(res) >= capacity.get(res)) {
			return ¤¤ExportFull;
		}
		
		if (FACTIONS.player().trade.pricesSell.get(res) > 0 && FACTIONS.player().trade.pricesSell.get(res) < priceCapsI.get(res)) {
			return ¤¤priceCapProblem;
		}
			
		
		return null;
	}

	

}
