package world.map.terrain;

import static world.WORLD.MOUNTAIN;
import static world.WORLD.TBOUNDS;
import static world.WORLD.THEIGHT;
import static world.WORLD.TWIDTH;
import static world.WORLD.WATER;

import game.GAME;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.WorldGen;
import world.WorldGen.WorldGenMapType;

final class Generator {
	
	
	public Generator(WorldGen spec, ACTION loadprint){
		generateAll(spec, loadprint);
		loadprint.exe();
		new GeneratorValidator(null);
		loadprint.exe();
		for (int y = 0; y < THEIGHT(); y++){
			for(int x = 0; x < TWIDTH(); x++){
				WATER().get(x, y).pplace(x, y);
				WATER().get(x, y-1).pplace(x, y-1);
				WATER().get(x-1, y).pplace(x-1, y);
				MOUNTAIN().fix(x, y);
			}
		}
		WORLD.MINIMAP().repaint();
	}

	
	public void clear() {
		WORLD.GROUND().clear();
		WORLD.MOUNTAIN().clear();
		WORLD.FOREST().clear();
		WORLD.WATER().clear();
	}
	
	public void generateAll(WorldGen spec, ACTION loadprint) {
		
		loadprint.exe();
		clear();
		RND.setSeed(spec.seed);
		
		loadprint.exe();
		
		HeightMap height = new HeightMap(TWIDTH(), THEIGHT(), TWIDTH()/8, 4);
		if (spec.map != null) {
			WorldGenMapType m = new WorldGenMapType(spec.map, TWIDTH()); 
			
			for (COORDINATE c : TBOUNDS()) {
				height.increment(c, m.h(c.x(), c.y(), TWIDTH(), THEIGHT()));
			}
		}else {
			new GeneratorHeight(height, spec);
		}
		
		
		
		
		loadprint.exe();
		new GeneratorMountains(height, spec);
		loadprint.exe();
		
		loadprint.exe();
		new GeneratorOcean(height);
		
//		new GeneratorElevator(spec, height);
//		RES.loader().print("elevating...");
//		new GeneratorOcean(spec, height);
//		
		loadprint.exe();
		new GeneratorRiver();
		loadprint.exe();
		new GeneratorValidator(null);
		
		for (int y = 0; y < THEIGHT(); y++){
			for(int x = 0; x < TWIDTH(); x++){
				WATER().get(x, y).pplace(x, y);
				WATER().get(x, y-1).pplace(x, y-1);
				WATER().get(x-1, y).pplace(x-1, y);
				MOUNTAIN().fix(x, y);
			}
		}
		
		float[][] fertility = new float[THEIGHT()][TWIDTH()]; 
		loadprint.exe();
		new GeneratorSeasoner(GAME.world(), spec.lat, fertility);
		loadprint.exe();
		new GeneratorForest(fertility);
		loadprint.exe();

	}
	
}
