package settlement.path;

import static settlement.main.SETT.PATH;

import snake2d.PathGame.COST;

public class CostMethods{
	
	CostMethods(){

	}
	
	public final COST player = new COST() {
		@Override
		public double getCost(int fromX, int fromY, int toX, int toY) {

			AVAILABILITY a = PATH().getAvailability(toX, toY);
			if (a.player < 0) {
				return BLOCKED;
			}
			if (fromX != toX && fromY != toY) {
				if (PATH().getAvailability(fromX, toY).player <= -1 || PATH().getAvailability(toX, fromY).player <= -1) {
					return SKIP;
				}
			}
			
			return a.player + PATH().getAvailability(fromX, fromY).from;
			
		}
	};
	
	public final COST enemy = new COST() {
		@Override
		public double getCost(int fromX, int fromY, int toX, int toY) {

			AVAILABILITY a = PATH().getAvailability(toX, toY);
			if (a.enemy < 0) {
				return BLOCKED;
			}
			if (fromX != toX && fromY != toY) {
				if (PATH().getAvailability(fromX, toY).enemy <= -1 || PATH().getAvailability(toX, fromY).enemy <= -1) {
					return SKIP;
				}
			}
			
			return a.enemy;
			
		}
	};

	
}
