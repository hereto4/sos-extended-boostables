package game.event.engine;

import snake2d.util.file.Json;

class EAmount {
	
	public double rel = 0;
	public double perPerson = 0;
	public double abs = 0;

	EAmount(int am){
		this.abs = am;
	}
	
	EAmount(Json json, int min) {
		rel = json.dTry("RELATIVE", min, 1000, 0);
		perPerson = json.dTry("PER_PERSON", min, 1000, 0);
		abs = json.dTry("AMOUNT", min, Integer.MAX_VALUE, 0);
		json.checkUnused();
	}
	
	public int am(double perPerson, double rel) {
		double am = abs;
		am += perPerson*this.perPerson;
		am += rel*this.rel;
		return (int) Math.ceil(am);
	}
	
}