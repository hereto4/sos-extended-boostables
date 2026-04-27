package game.faction;

import init.race.Race;

public abstract class FSlaves extends FactionResource{

	public abstract int available(Race race);
	public abstract void trade(Race race, int am, int credits);
	public abstract int price(Race race, int am);
	
	public static int BASE_PRICE(Race race) {

		int days = race.physics.slaveprice;
		days *= FACTIONS.PRICE().edibleLow();
		return days;
	}
}
