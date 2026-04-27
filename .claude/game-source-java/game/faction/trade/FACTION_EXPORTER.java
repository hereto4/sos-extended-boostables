package game.faction.trade;

import game.faction.Faction;
import init.resources.RESOURCE;

public interface FACTION_EXPORTER {

	public int priceSell(RESOURCE res, int amount);
	public void sell(RESOURCE res, int amount, int price, Faction buyer);
	public void remove(RESOURCE res, int amount, ITYPE type);
	public int forSale(RESOURCE res);

}
