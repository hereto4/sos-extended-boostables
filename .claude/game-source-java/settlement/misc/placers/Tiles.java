package settlement.misc.placers;

import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.sprite.SPRITE;

final class Tiles {

	static final Tile __ = new Tile() {
		
		@Override
		public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
			
		}
		
		@Override
		public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
			return !SETT.PATH().solidity.is(tx, ty);
		}

		@Override
		public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
			return SPRITES.cons().BIG.dashed.get(0);
		}
	};
	
	static final Tile xx = new Tile() {
		
		@Override
		public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
			SETT.TERRAIN().NADA.placeFixed(tx, ty);
		}
		
		@Override
		public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
			return true;
		}

		@Override
		public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
			return SPRITES.cons().BIG.dashed.get(0);
		}
	};
	
	static final Tile na = new Tile() {
		
		@Override
		public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
			
		}
		
		@Override
		public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
			return true;
		}
		
		@Override
		public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
			return SPRITES.cons().BIG.dashed.get(0);
		}
	};
	
	private Tiles() {
		
	}
	
	static class Terrain implements Tile {

		final TerrainTile t;
		
		Terrain(TerrainTile t){
			this.t = t;
		}
		
		@Override
		public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
			return true;
		}

		@Override
		public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
			t.placeFixed(tx, ty);
			if (t.clearing().isStructure())
				SETT.GRASS().current.set(tx, ty, 0);
		}
		
		@Override
		public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
			return SPRITES.cons().BIG.dashedThick.get(0);
		}
		
	}
	
	static class Floor implements Tile {

		final settlement.tilemap.floor.Floors.Floor f;
		final double degrade;
		
		Floor(settlement.tilemap.floor.Floors.Floor f){
			this(f, 1);
		}
		
		Floor(settlement.tilemap.floor.Floors.Floor f, double degrade){
			this.f = f;
			this.degrade = degrade;
		}
		
		@Override
		public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
			return true;
		}

		@Override
		public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
			f.placeFixed(tx, ty);
			if (SETT.TERRAIN().get(tx, ty).clearing().isEasilyCleared())
				SETT.TERRAIN().NADA.placeFixed(tx, ty);
		}
		
		@Override
		public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
			return SPRITES.cons().BIG.dashed.get(0);
		}
		
	}
	
	static class Resource implements Tile {

		final RESOURCE r;
		final int amount;
		
		Resource(RESOURCE r, int amount){
			this.r = r;
			this.amount = amount;
		}

		
		@Override
		public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
			return true;
		}

		@Override
		public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
			SETT.THINGS().resources.createPrecise(tx, ty, r, amount);
		}
		
		@Override
		public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
			return SPRITES.cons().ICO.clear;
		}
		
	}
	
	static class Conpound implements Tile {
		
		final Tile[] tiles;
		
		Conpound(Tile... tiles){
			this.tiles = tiles;
		}

		@Override
		public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
			for (Tile t : tiles)
				if (!t.placable(tx, ty, grid, rx, ry))
					return false;
			return true;
		}

		@Override
		public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
			for (Tile t: tiles)
				t.place(tx, ty, grid, rx, ry);
		}
		
		@Override
		public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
			return SPRITES.cons().ICO.clear;
		}
		
	}
	
}
