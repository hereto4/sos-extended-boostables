package world.map.regions.centre;

import static world.WORLD.THEIGHT;
import static world.WORLD.TWIDTH;

import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.RECTANGLEE;
import snake2d.util.datatypes.Rec;
import view.tool.PlacableMessages;
import world.WORLD;
import world.map.regions.Region;
import world.map.road.WTRAV;


public final class WorldCentrePlacablity{


	private WorldCentrePlacablity() {
		
	}
	
	private static final int TILE_DIM = WCentre.TILE_DIM; 
	private static final RECTANGLEE TILES = new Rec(WCentre.TILE_DIM, WCentre.TILE_DIM); 
	
	public static CharSequence terrainC(int tx, int ty) {
		return terrain(tx-TILE_DIM/2, ty-TILE_DIM/2);
	}
	
	public static CharSequence terrain(int tileX1, int tileY1) {
		
		if (tileX1 < 1 || tileY1 < 1 || tileX1+TILE_DIM >= TWIDTH() || tileY1+TILE_DIM >= THEIGHT())
			return PlacableMessages.¤¤IN_MAP;
		
		int cx = tileX1+TILE_DIM/2;
		int cy = tileY1+TILE_DIM/2;
		
		if (can(cx, cy) != null) {
			return can(cx, cy);
		}
		
		
		
		boolean oneClear = false;
		for (int di = 0; di < DIR.ORTHO.size() && !oneClear; di++) {
			if (WTRAV.isGoodLandTile(cx+DIR.ORTHO.get(di).x(), cy+DIR.ORTHO.get(di).y()) && WTRAV.canLand(cx, cy, DIR.ORTHO.get(di), false))
				oneClear = true;
		}
		
		if (!oneClear)
			return PlacableMessages.¤¤ONE_CLEAR_TILE;
		

		return null;
	}
	
	
	private static CharSequence can(int tx, int ty) {
		
		if (!WTRAV.isGoodLandTile(tx, ty)) {
			if (!WORLD.WATER().RIVER.is(tx, ty) || WORLD.MOUNTAIN().is(tx, ty))
				return PlacableMessages.¤¤TERRAIN_BLOCK;
		}
		return null;
		
	}
	
	public static CharSequence regionC(int tx, int ty) {
		return region(tx-TILE_DIM/2, ty-TILE_DIM/2);
	}
	
	public static CharSequence regionMiniC(int tx, int ty) {
		return regionMini(tx-TILE_DIM/2, ty-TILE_DIM/2);
	}
	
	public static CharSequence regionMini(int tileX1, int tileY1) {
		CharSequence t = terrain(tileX1, tileY1);
		if (t != null)
			return t;
		Region r = WORLD.REGIONS().map.get(tileX1, tileY1);
		if (r == null)
			return PlacableMessages.¤¤REGION;

		for (int y = tileY1 ; y < tileY1 + TILE_DIM; y++){
			for (int x = tileX1 ; x < tileX1 + TILE_DIM; x++){
				if (r != WORLD.REGIONS().map.get(x, y)) {
					return PlacableMessages.¤¤SAME_REGION;
				}	
			}
		}
		
		return null;
	}
	
	public static CharSequence region(int tileX1, int tileY1) {
		CharSequence t = regionMini(tileX1, tileY1);
		if (t != null)
			return t;
		Region r = WORLD.REGIONS().map.get(tileX1, tileY1);
		if (r == null)
			return PlacableMessages.¤¤REGION;

		for (int y = tileY1-1 ; y < tileY1 + TILE_DIM+1; y++){
			for (int x = tileX1-1 ; x < tileX1 + TILE_DIM+1; x++){
				if (r != WORLD.REGIONS().map.get(x, y) && WORLD.REGIONS().map.get(x, y) != null) {
					return PlacableMessages.¤¤SAME_REGION;
				}	
			}
		}
		return null;
	}
	
	public static RECTANGLE tilesC(int cx, int cy) {
		TILES.moveX1Y1(cx-TILE_DIM/2, cy-TILE_DIM/2);
		return TILES;
	}
	
	
}
