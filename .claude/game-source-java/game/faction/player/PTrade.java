package game.faction.player;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.info.INFO;
import util.statistics.HistoryResource;
import util.text.D;
import util.text.Dic;
import world.region.RD;

public class PTrade {


	public final HistoryResource pricesBuy;
	public final HistoryResource pricesSell;
	public final HistoryResource pricesAve;
	
	public final HistoryResource unitsImported;
	public final HistoryResource unitsExported;
	public final HistoryResource priceImported;
	public final HistoryResource priceExported;
	
	public final HistoryResource outImported;
	public final HistoryResource inExported;
	
	private static CharSequence ¤¤InExported = "¤Exported";
	private static CharSequence ¤¤InExportedD = "¤Moneys earned from exports.";
	
	private static CharSequence ¤¤OutImported = "¤Imported";
	private static CharSequence ¤¤OutImportedD = "¤Moneys spent on imports.";
	
	
	private static CharSequence ¤¤units = "¤units";
	private static CharSequence ¤¤unitsD = "¤The amount of units traded.";
	private static CharSequence ¤¤price = "¤Price";
	private static CharSequence ¤¤priceD = "¤Price per unit.";
	
	static {
		D.ts(PTrade.class);
	}
	
	PTrade() {
		pricesBuy = new HistoryResource(
				new INFO(Dic.¤¤buyPrice, ""),
				PCredits.history, TIME.days(), true);
		pricesSell = new HistoryResource(
				new INFO(Dic.¤¤sellPrice, ""),
				PCredits.history, TIME.days(), true);
		pricesAve = new HistoryResource(
				new INFO(Dic.¤¤sellPrice, ""),
				PCredits.history, TIME.days(), true);
		
		unitsImported = new HistoryResource(new INFO(¤¤units, ¤¤unitsD), PCredits.history, TIME.days(), false);
		unitsExported = new HistoryResource(new INFO(¤¤units, ¤¤unitsD), PCredits.history, TIME.days(), false);
		priceImported = new HistoryResource(new INFO(¤¤price, ¤¤priceD), PCredits.history, TIME.days(), false);
		priceExported = new HistoryResource(new INFO(¤¤price, ¤¤priceD), PCredits.history, TIME.days(), false);
		
		inExported = new HistoryResource(new INFO(¤¤InExported, ¤¤InExportedD), PCredits.history, TIME.days(), false);
		outImported = new HistoryResource(new INFO(¤¤OutImported, ¤¤OutImportedD), PCredits.history, TIME.days(), false);
	}

	int ri = 0;
	
	void update(double ds) {
		
		ri %= RESOURCES.ALL().size();
		RESOURCE res = RESOURCES.ALL().get(ri);
		
		int s = 0;
		int m = Integer.MAX_VALUE;
		if (DIP.traders().size() == 0) {
			for (FactionNPC f : RD.DIST().neighs()) {
				if (f.capitolRegion() != null) {
					s = Math.max(s, f.seller().priceBuyP(res));
					m = Math.min(m, f.buyer().priceSellP(res));
				}
				
			}
		}else {
			for (Faction ff : DIP.traders()) {
				FactionNPC f = (FactionNPC) ff;
				if (f.capitolRegion() != null) {
					s = Math.max(s, f.seller().priceBuyP(res));
					m = Math.min(m, f.buyer().priceSellP(res));
				}
				
			}
		}
		
		
		
		if (m == Integer.MAX_VALUE)
			m = 0;
		pricesSell.set(res, s);
		pricesBuy.set(res, m);
		pricesAve.set(res, FACTIONS.PRICE().get(res));
		
		ri ++;
		

	}
	
	public void trade(double amount, RESOURCE res, int resAm) {
		if (amount < 0) {
			outImported.inc(res, (int) -amount);
			unitsImported.inc(res, resAm);
			priceImported.set(res, (outImported.get(res)+1)/(unitsImported.get(res)+1));
		}
		else {
			inExported.inc(res, (int) amount);
			unitsExported.inc(res, resAm);
			priceExported.set(res, (inExported.get(res)+1)/(unitsExported.get(res)+1));
		}
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {

			pricesBuy.save(file);
			pricesSell.save(file);
			pricesAve.save(file);
			outImported.save(file);
			inExported.save(file);
			unitsImported.save(file);
			unitsExported.save(file);
			priceImported.save(file);
			priceExported.save(file);
			file.i(ri);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			pricesBuy.load(file);
			pricesSell.load(file);
			pricesAve.load(file);
			outImported.load(file);
			inExported.load(file);
			unitsImported.load(file);
			unitsExported.load(file);
			priceImported.load(file);
			priceExported.load(file);
			ri = file.i();
		}

		
		@Override
		public void clear() {
			pricesBuy.clear();
			pricesSell.clear();
			pricesAve.clear();
			outImported.clear();
			inExported.clear();
			unitsImported.clear();
			unitsExported.clear();
			priceImported.clear();
			priceExported.clear();
		}
	};
	

	
}
