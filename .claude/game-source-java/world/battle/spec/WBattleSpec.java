package world.battle.spec;

public abstract class WBattleSpec {

	public WBattleSide player;
	public WBattleSide enemy;
	public boolean victory;

	
	public abstract void retreat();
	public abstract void auto();
	public abstract void engage();
	
}
