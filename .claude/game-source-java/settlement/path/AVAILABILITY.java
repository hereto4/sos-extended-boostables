package settlement.path;

import game.GAME;
import game.battle.Army;

public enum AVAILABILITY{
	
	SOLID				(-1, 	0,	true, 	1, 		false),
	NOT_ACCESSIBLE		(-1, 	0,	false, 	0.5, 	false),
	NORMAL				(1,		0,	false, 	1, 		true),
	AVOID_PASS			(4,		5,	false, 	0.5, 	false),
	AVOID_LIKE_FUCK		(32,	1,	false, 	0.25,	false),

	ROAD0				(0.5,	0,	false, 	1.0, 	true),
	ROAD1				(0.5,	0,	false, 	1.05, 	true),
	ROAD2				(0.5,	0,	false, 	1.10, 	true),
	ROAD3				(0.5,	0,	false, 	1.15, 	true),
	ROAD4				(0.5,	0,	false, 	1.20, 	true),
	PENALTY2			(2,		0,	false, 	0.8, 	true),
	PENALTY3			(3.0,	0,	false, 	0.5, 	true),
	PENALTY4			(4,		1,	false, 	0.5,	true),
	ROOM				(1.2,	0,	false, 	1.0, 	false),
	
	ROOM_SOLID		(-1, 	0,	true, 	1, 		false),
	
	ENEMY				(0.5,	0,	true, 	1.0, 	false, -1);
	
	public static final int Penalty = 2;
	public static AVAILABILITY[] ROADS = new AVAILABILITY[] {
		ROAD0,
		ROAD1,
		ROAD2,
		ROAD3,
		ROAD4
	};
	public final boolean availableToStandOn;
	public final double from;
	public final double player;
	public final double enemy;
	public final boolean tileCollide;
	public final double movementSpeed;
	public final double movementSpeedI;
	
	public final static AVAILABILITY[] values = values();
	
	private AVAILABILITY(double player, double from, boolean tileCollide, double movementBonus, boolean available) {
		this(player, from, tileCollide, movementBonus, available, player);
	}
	
	private AVAILABILITY(double player, double from, boolean tileCollide, double movementBonus, boolean available, double enemy) {
		this.player = player;
		this.enemy = enemy;
		this.from = from;
		this.tileCollide = tileCollide;
		this.movementSpeed = movementBonus;
		movementSpeedI = 1.0/movementSpeed;
		this.availableToStandOn = available;
	}


	public boolean isSolid(Army a) {
		if (a == GAME.ARMIES().player())
			return player < 0;
		return enemy < 0;
	}
}