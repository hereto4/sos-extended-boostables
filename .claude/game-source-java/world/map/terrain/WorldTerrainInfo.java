package world.map.terrain;

import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAIN;
import init.type.TERRAINS;
import snake2d.util.misc.CLAMP;
import util.data.DOUBLE.DoubleImp;
import world.WORLD;
import world.map.regions.centre.WCentre;

public final class WorldTerrainInfo {

	private final DoubleImp[] terrain = new DoubleImp[TERRAINS.ALL().size()];
	private final DoubleImp fertility = new DoubleImp();
	private final DoubleImp[] climates = new DoubleImp[CLIMATES.ALL().size()];
	public int tx;
	public int ty;
	
	public WorldTerrainInfo() {
		for (int i = 0; i < terrain.length; i++) {
			terrain[i] = new DoubleImp();
		}
		for (int i = 0; i < climates.length; i++) {
			climates[i] = new DoubleImp();
		}
	}
	
	public void clear() {
		for (int i = 0; i < terrain.length; i++) {
			terrain[i].setD(0);
		}
		for (int i = 0; i < climates.length; i++) {
			climates[i].setD(0);
		}
		fertility.setD(0);
	}
	
	public void initCity(int x1, int y1) {
		clear();
		for (int y = 0; y < WCentre.TILE_DIM; y++) {
			for (int x = 0; x < WCentre.TILE_DIM; x++) {
				int tx = x + x1;
				int ty = y + y1;
				add(tx, ty);
			}
		}
		tx = x1+1;
		ty = y1+1;
		double d = WCentre.TILE_DIM*WCentre.TILE_DIM;
		divide(d);
	}
	
	public void add(int tx, int ty) {
		fertility.incD(WORLD.MOISTURE().get(tx, ty));
		double f = 0;
		f += WORLD.FOREST().add(this, tx, ty);
		f += WORLD.MOUNTAIN().add(this, tx, ty);
		f += WORLD.WATER().add(this, tx, ty);
		
		add(TERRAINS.NONE(), CLAMP.d(1.0-f, 0, 1));
		
		climates[WORLD.CLIMATE().getter.get(tx, ty).index()].incD(1); 
	}
	
	public void add(TERRAIN t, double v) {
		terrain[t.index()].incD(v);
	}
	
	public DoubleImp get(TERRAIN t) {
		return terrain[t.index()];
	}
	
	public DoubleImp get(CLIMATE c) {
		return climates[c.index()];
	}
	
	public DoubleImp fertility() {
		return fertility;
	}
	

//	@Override
//	public void save(FilePutter file) {
//		file.ds(terrain);
//		file.d(fertility);
//	}
//
//	@Override
//	public void load(FileGetter file) throws IOException {
//		file.ds(terrain);
//		fertility = file.d();
//	}

	public void divide(double d) {
		for (int i = 0; i < terrain.length; i++)
			terrain[i].setD(terrain[i].getD()/d);
		for (int i = 0; i < climates.length; i++)
			climates[i].setD(climates[i].getD()/d);
		fertility.setD(fertility.getD()/d);
	}
	
}
