package game.faction;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.time.TIME;
import game.time.TIMECYCLE;
import init.resources.RESOURCE;
import init.type.HCLASSES;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import util.data.DOUBLE;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryInt;
import util.text.D;
import util.text.Dic;

public class FCredits extends FactionResource implements DOUBLE{

	protected double credits;

	static {
		D.ts(FCredits.class);
		D.gInit(FCredits.class);
	}
	
	private static CharSequence ¤¤Treasury = "¤Treasury";
	private static CharSequence ¤¤TreasuryD = "¤The amount of Denari at disposal.";
	private final HistoryInt creditsH;

	
	
	public FCredits(int saved, TIMECYCLE time) {

		creditsH = new HistoryInt(¤¤Treasury, ¤¤TreasuryD, saved, time, true);
		
		
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(credits);
		creditsH.save(file);
	}


	@Override
	protected void load(FileGetter file) throws IOException {
		credits = file.d();
		creditsH.load(file);
	}


	@Override
	protected void clear() {
		credits = 0;
		creditsH.clear();
	}

	@Override
	protected void update(double ds, Faction f) {
		
		double inf = credits*0.2*ds/(TIME.years().bitSeconds()*BOOSTABLES.CIVICS().DEFALTION.get(f));
		int i = (int) inf;
		
		if (Math.abs(inf-i) > RND.rFloat()) {
			i+= Math.signum(i);
		}
		
		inc(-i, CTYPE.INFLATION);
		
	}
	
	public HISTORY_INT creditsH() {
		return creditsH;
	}
	
	public double credits() {
		return credits;
	}
	
	@Override
	public double getD() {
		return credits;
	}
	
	protected void inccc(double amount) {
		credits += amount;
		creditsH.set((int) credits);
	}
	
	public void set(double amount) {
		credits = amount;
	}
	
	public void inc(double amount, CTYPE t) {
		inccc(amount);
	}
	
	public void inc(double amount, CTYPE t, RESOURCE res, int resAm) {
		inccc(amount);
	}
	

	
	public enum CTYPE {
		
		
		TRADE(D.g("Trade"), D.g("TradeD", "Money that flow through imports and exports.")),
		INFLATION(D.g("Inflation"), D.g("InflationD", "Over time, Inflation adds credits to a negative treasury and removes credits from a positive one.")),
		MISC(D.g("Misc"), D.g("MiscD", "Special sources")),
		TRIBUTE(D.g("Tribute"), D.g("TributeD", "Denari spent/gained from paying off other armies and factions.")),
		
		DIPLOMACY(Dic.¤¤Diplomacy, D.g("DiplomacyD", "Denari spent/gained from diplomacy with other factions.")),
		MERCINARIES(D.g("Mercenaries"), D.g("MercinariesD", "Mercenaries can be conscripted into your armies and cost credits to upkeep each day.")),
		TOURISM(D.g("Tourism"), D.g("PTourismD", "Tourists that visit your city will give you some money at the end of their stay.")),
		CONSTRUCTION(D.g("Construction"), D.g("ConstructionD", "Construction of buildings in your kingdom.")),
		TAX(D.g("Tax"), D.g("TaxD", "Taxation from the realm.")),
		SLAVES(HCLASSES.SLAVE().name, D.g("SlaveD", "Transactions from slave trade.")),
		;
		
		public final CharSequence name;
		public final CharSequence desc;
		
		CTYPE(CharSequence name, CharSequence desc){
			this.name = name;
			this.desc = desc;
		}
		
	}
	
}
