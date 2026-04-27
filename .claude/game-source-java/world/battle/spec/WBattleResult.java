package world.battle.spec;

import init.race.RACES;
import init.resources.RESOURCES;

public abstract class WBattleResult {
	

	public WBattleSide player;
	public WBattleSide enemy;
	public BATTLE_RESULT result;
	public int[] capturedRaces = new int[RACES.all().size()];
	public int[] lostResources = new int[RESOURCES.ALL().size()];
	
	
	public abstract void accept(int[] enslave, int[] resources);
	

}