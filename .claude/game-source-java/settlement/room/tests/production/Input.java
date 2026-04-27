package settlement.room.tests.production;

import init.resources.RESOURCE;

public class Input {

	public final RESOURCE res;
	public final double amount;
	public final Recipe producer;

	Input(double amount, Recipe producer) {
		this.res = producer.res;
		this.amount = amount;
		this.producer = producer;
	}

	double wTot(ProductionSpec ibonuses) {
		return amount * producer.wTotPerItem(ibonuses);
	}

}