package settlement.tilemap.generator;

import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import init.settings.S;
import init.sprite.SPRITES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.path.AvailabilityListener;
import settlement.tilemap.TileMap;
import settlement.tilemap.floor.TGrowth.Grower;
import settlement.tilemap.terrain.TGrowable;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;
import util.text.D;

public final class Generator {

	private static CharSequence ¤¤Generating = "Generating";
	static {
		D.ts(Generator.class);
	}
	
	
	public Generator(CapitolArea area) {

		AvailabilityListener.listenAll(false);
		
		SPRITES.loader().print(¤¤Generating);

		TileMap m = SETT.TILE_MAP(); 
		
		GeneratorUtil util = new GeneratorUtil();
		
		print("fertilizing");
		new GeneratorFertilityInit(area, util);
		
		final LinkedList<COORDINATE> caves = new LinkedList<>();
		print("mountainizing");
		new GeneratorMountain(area, util);
		print("making caves");
		new GeneratorCave(area, util, caves);
		
		print("Generating Rivers");
		new GeneratorRiver(area, util);
		new GeneratorRiverSmall(area, util);
		print("filling lakes");
		new GeneratorLake(area, util);
		new GeneratorWaterFin(area, util);
		
		print("filling oceans");
		new GeneratorOcean(area, util);
		

		
		if (!area.isBattle)
			new GeneratorLakeExtra(area, util);
		
		SPRITES.loader().print("fish...");
		new GeneratorFish(area, util);
		
		print("mineralizing");
		new GeneratorMinerals(area, util, caves);
		
		print("fertilizing");
		new GeneratorGround(area, util);

		SPRITES.loader().print("fertilizing again...");
		new GeneratorFertilityFin(area, util);
		
		
		
		print("planting seeds");
		

		
		new GeneratorGrowth();
		if (!area.isBattle)
			new GeneratorEdibles(area, util, caves);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			
			if (TERRAIN().NADA.is(c) && SETT.MINERALS().amountInt.get(c) == 0) {
				
				Grower g = SETT.TILE_MAP().growth.type(c.x(), c.y());
				if (g != null) {
					double a = SETT.TILE_MAP().growth.growMaxAmount(c.x(), c.y());
					if (a > 0) {
						g.setRoots(c.x(), c.y(), a);
						TerrainTile t = TERRAIN().get(c);
						if (t instanceof TGrowable) {
							TGrowable gg = (TGrowable) t;
							gg.resource.set(c, gg.size.get(c));
						}
					}
				}
			}
		}
		
		
		print("roads");
		new GeneratorRoads(area);
		
		for (int y = 0; y < THEIGHT; y++) {
			for (int x = 0; x < TWIDTH; x++) {
				if (TERRAIN().NADA.is(x, y) && !TERRAIN().WATER.groundWater.is(x, y) && !TERRAIN().WATER.groundWaterSalt.is(x, y)) {
					if (SETT.GROUND().MOISTURE_BASE.get(x, y) + 0.5 + SETT.ENV().map.WATER_SWEET.get(x, y) > 0.2) {
						if (RND.oneIn(50))
							TERRAIN().DECOR_MID.placeRaw(x, y);
					}else if (RND.oneIn(200)) {
						TERRAIN().DECOR_NO.placeRaw(x, y);
					}
					
				}
			}
		}
		
		print("polishing..");
		
		
		for (int y = 0; y < THEIGHT; y++) {
			for (int x = 0; x < TWIDTH; x++) {
				m.topology.get(x, y).placeFixed(x, y);
				SETT.PATH().availability.updateAvailability(x, y);
			}
		}
		
		
	
		
		
		
		print("painting minimap...");
		
		paintMinimap();
		
		AvailabilityListener.listenAll(true);
		
	}
	
	private int printI = 0;

	
	private void print(String debug) {
		String s = ""+¤¤Generating;
		for (int i = 0; i < printI; i++)
			s += ".";
		if (S.get().developer || S.get().debug) {
			s += " " + debug;
		}
		printI++;
		printI%= 6;
		SPRITES.loader().print(s);
	}
	
	public static void paintMinimap() {
		byte[] cs = new byte[SETT.TWIDTH * SETT.TWIDTH * 4];
		for (int y = 0; y < SETT.TWIDTH; y++) {
			for (int x = 0; x < SETT.TWIDTH; x++) {

				int i = (y * SETT.TWIDTH + x) * 4;

				COLOR c = SETT.TILE_MAP().miniC(x, y);
				
				
				cs[i + 0] = c.red();
				cs[i + 1] = c.green();
				cs[i + 2] = c.blue();
				cs[i + 3] = (byte) 255;
			}
		}
		SETT.MINIMAP().putPixels(cs);
	}
	


}
