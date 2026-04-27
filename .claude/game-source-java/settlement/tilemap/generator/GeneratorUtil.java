package settlement.tilemap.generator;

import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import init.paths.PATHS;
import settlement.main.SETT;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.map.MAP_BOOLEANE.BooleanMapE;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.Polymap;

class GeneratorUtil {

	final HeightMap height = new HeightMap(TWIDTH, THEIGHT, 128, 4);
	final FertilityTmp fer = new FertilityTmp();
	final Polymap polly = new Polymap(TWIDTH, THEIGHT);
	final Json json;
	final Checker checker = new Checker();
	
	GeneratorUtil() {
		json = new Json(PATHS.CONFIG().get("GenerationSettlement")).json("GENERATION");
	}
	
	static class Checker extends BooleanMapE {

		private final short[] checks;
		private short sI = 0;
		
		public Checker() {
			super(TWIDTH, THEIGHT);
			checks = new short[THEIGHT*TWIDTH];
		}

		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			checks[tile] = (short) (value ? sI : sI-1);
			return this;
			
		}

		@Override
		public boolean is(int tile) {
			return checks[tile] == sI;
		}
		
		void init() {
			sI++;
		}
		
	}
	
	static class FertilityTmp implements MAP_DOUBLEE{

		private double[][] fer = new double[SETT.TWIDTH][SETT.TWIDTH];

		private FertilityTmp() {

		}

		@Override
		public double get(int x, int y) {
			if (SETT.TILE_BOUNDS.holdsPoint(x, y))
				return fer[y][x];
			return 0;
		}

		@Override
		public double get(int tile) {
			throw new RuntimeException();
		}

		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			throw new RuntimeException();
		}

		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (SETT.TILE_BOUNDS.holdsPoint(tx, ty))
				fer[ty][tx] = value;
			return this;
		}
		
		public void target(int tx, int ty, double value, double delta) {
			fer[ty][tx] = value*delta + (1.0-delta)*fer[ty][tx];
		}
	}
	

	
}
