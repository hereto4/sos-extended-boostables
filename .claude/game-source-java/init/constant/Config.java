package init.constant;

import init.INIT;
import init.INIT.InitResource;
import init.paths.PATHS;
import snake2d.Errors;
import snake2d.util.file.Json;

public final class Config extends InitResource{
	
	static {
		if (!PATHS.inited()) {
			throw new RuntimeException("paths must be inited first!");
		}
	}
	
	private static Json j = null;
	private static ConfigBattle BATTLE;
	private static ConfigSett SETT ;
	private static ConfigWorld WORLD;
	
	public Config(INIT init){
		super(init);
		if (!PATHS.inited()) {
			throw new RuntimeException("paths must be inited first!");
		}
		j = new Json(PATHS.CONFIG().get("Battle"));
		BATTLE = new ConfigBattle();
		j = new Json(PATHS.CONFIG().get("Sett"));
		SETT = new ConfigSett();
		j = new Json(PATHS.WORLD().folder("config").init.get("General"));
		WORLD = new ConfigWorld();
	}
	
	public static ConfigBattle battle() {
		return BATTLE;
	}

	public static ConfigSett sett() {
		return SETT;
	}

	public static ConfigWorld world() {
		return WORLD;
	}

	public static final class ConfigBattle {

		public final double MORALE_HOLDOUT = j.d("MORALE_HOLDOUT", 0, 10000);
		public final int TRAINING_DEGRADE = j.i("TRAINING_DEGRADE", 0, 50);
		public final int MEN_PER_DIVISION = j.i("MEN_PER_DIVISION", 1, 255);
		public final int DIVISIONS_PER_ARMY = j.i("DIVISIONS_PER_ARMY", 1, 126);
		public final int DIVISIONS_PER_BATTLE = DIVISIONS_PER_ARMY*2;
		public final int MEN_PER_ARMY = MEN_PER_DIVISION*DIVISIONS_PER_ARMY;
		public final int REGION_MAX_DIVS = j.i("REGION_MAX_DIVS", 0, 127);
		public final int REGION_MAX_MEN = REGION_MAX_DIVS*MEN_PER_DIVISION;
		public final double DAMAGE_REDUCTION = j.d("DAMAGE_REDUCTION", 1, 10000);
		
		ConfigBattle(){
			
			
		}
		
	}
	
	public static final class ConfigSett {

		public final double HAPPINESS_EXPONENT = j.d("HAPPINESS_EXPONENT");
		public final int TOURIST_PER_YEAR_MAX = j.i("TOURIST_PER_YEAR_MAX");
		public final double TOURIST_CRETIDS = j.d("TOURIST_CRETIDS");
		public final int DIMENSION = j.i("DIMENSION", 256, 16000);
		
		public final double POP_RAIDER_WORTH = j.i("POP_RAIDER_WORTH", 1, 10000);;
		public final int secondsPerHour = j.i("SECONDS_PER_HOUR");
		public final int hoursPerDay = j.i("HOURS_PER_DAY");
		
		
		ConfigSett(){
			if (DIMENSION % 64 != 0)
				throw new Errors.DataError("SETT DIMENSION MUST BE A MULTIPLE OF 64");
		}
		
	}
	
	public static final class ConfigWorld {
		
		public final double TRIBUTE = j.d("REGION_TRIBUTE_AMOUNT");
		public final double TRADE_COST_PER_TILE = j.d("TRADE_COST_PER_TILE", 0, 1000);
		public final int POPULATION_MAX_CAPITOL = j.i("POPULATION_MAX_CAPITOL", 101, 256000);
		public final int WORLD_SIZE = j.i("TILE_DIMENSION", 128, 512);
		public final double FOREST_AMOUNT = j.d("FOREST_AMOUNT", 0, 1);
		public final double LOYALTY_FROM_POP = j.d("POPULATION_LOYALTY_PENALTY", 0, 10000);
		public final double REGION_SIZE = j.i("REGION_SIZE", 0, 1000);

		public final int CREDITS_PER_WORKDAY = j.i("CREDITS_PER_WORKDAY", 0, 1000);
		
		
		
		public static Json json(String resource) {
			return new Json(PATHS.WORLD().folder("config").init.get(resource));
		}
		
	}
	
}
