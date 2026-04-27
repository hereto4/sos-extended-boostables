package game.faction.trade;

import game.faction.Faction;
import init.resources.RESOURCE;

public interface FACTION_IMPORTER {

	
	/**
	 * resource will be bought. credits payed
	 * @param res
	 * @param price
	 * @param value
	 */
	public void buy(RESOURCE res, int amount, int price, Faction f);
	public void reserveSpace(RESOURCE res, int am, ITYPE type);
	public void deliver(RESOURCE res, int am, ITYPE type);

	/**
	 * 
	 * @param res
	 * @param price
	 * @return returns < 1 if doesn't want to buy, else a double representing the value of this deal
	 */
//	public double buyValue(RESOURCE res, int amount, double price);
	
	public double buyPriority(RESOURCE res, int amount, double bestPrice);
	
	public int buyPrice(RESOURCE res, int amount);
	
	public int spaceForTribute(RESOURCE res);
//	public void setBestBuyValue(double value);
}
