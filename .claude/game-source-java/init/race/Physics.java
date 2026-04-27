package init.race;

import init.constant.C;
import snake2d.util.file.Json;

public final class Physics {

	private final transient double heightOverGround;
	private final transient int hitboxSize;
	public final int adultAt;
	public final boolean decays;
	public final boolean sleeps;
	public final int slaveprice;
	public final double raiding;
	
	Physics(Json json){
		json = json.json("PROPERTIES");

//		acceleration = json.d("ACCELERATION", 0, 1000)*C.TILE_SIZE;
//		topSpeed = json.d("TOP_SPEED", 1, 15)*C.TILE_SIZE;
		heightOverGround = json.i("HEIGHT", 0, 200);
		hitboxSize = json.i("WIDTH", 5, 15)*C.SCALE;
		adultAt = json.i("ADULT_AT_DAY");
		decays = json.bool("CORPSE_DECAY");
		sleeps = json.bool("SLEEPS");
		slaveprice = json.i("SLAVE_PRICE", 0, Integer.MAX_VALUE, adultAt*2+5);
		raiding = json.d("RAID_MERCINARY", 0, 100000);
	}

	
	public double height() {
		return heightOverGround;
	}
	
//	public double acceleration() {
//		return acceleration;
//	}
//	
//	public double topSpeed() {
//		return topSpeed;
//	}
	
	public int hitBoxsize() {
		return hitboxSize;
	}
	
}
