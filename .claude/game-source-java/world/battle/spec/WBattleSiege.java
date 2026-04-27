package world.battle.spec;

import game.faction.FACTIONS;
import world.map.regions.Region;

public abstract class WBattleSiege extends WBattleSpec{


	public Region besiged;
	public double fortifications;
	
	@Override
	public void engage() {
		throw new RuntimeException();
	}
	
	
	public static abstract class Result {
		public Region besiged;
		
		public abstract void occupy(double devastation, double death, int[] enslave, int[] resources);
		public abstract void abandon(double devastation, double death, int[] enslave, int[] resources);
		public abstract void puppet(double devastation, double death, int[] enslave, int[] resources);
		
		public static boolean canPuppet() {
			return FACTIONS.canActivateNext();
		}
		
	}
	
}
