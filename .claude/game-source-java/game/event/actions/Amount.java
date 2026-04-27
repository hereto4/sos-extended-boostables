package game.event.actions;

import game.event.actions.EventAction.CInt;
import game.event.engine.EContext;
import game.event.engine.Event;
import settlement.stats.STATS;
import snake2d.util.file.Json;

class Amount {
	
	public final CInt amount;
	public double rel = 0;
	public double perPerson = 0;
	public double abs = 0;

	Amount(CInt amount){
		this.amount = amount;
	}
	
	void read(Json json, int min) {
		rel = json.dTry("RELATIVE", min, 1000, 0);
		perPerson = json.dTry("PER_PERSON", min, 1000, 0);
		abs = json.dTry("AMOUNT", min, Integer.MAX_VALUE, 0);
		json.checkUnused();
	}
	
	void set(Event event, EContext c, int available) {
		int am = (int) (rel*available);
		am += perPerson*STATS.POP().POP.data().get(null);
		am += abs;
		amount.set(event, c, am);
	}
	
	
	void inc(Event event, EContext c, int available) {
		int am = (int) (rel*available);
		am += perPerson*STATS.POP().POP.data().get(null);
		am += abs;
		amount.set(event, c, amount.get(event, c)+am);
	}
	
}