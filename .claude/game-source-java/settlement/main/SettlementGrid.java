package settlement.main;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.LIST;
import view.main.VIEW;
import view.sett.IDebugPanelSett;
import world.map.regions.centre.WCentre;

public class SettlementGrid {
	
	/**
	 * Nr of tiles that makes up the settlement map
	 */
	public static int TILES = WCentre.TILE_DIM;
	/**
	 * size of one world tile on the worldmap
	 */
	public static int QUAD_SIZE = SETT.TWIDTH / TILES;
	public static int QUAD_AREA = QUAD_SIZE * QUAD_SIZE;
	public static int QUAD_HALF = QUAD_SIZE / 2;
	public static int QUAD_QUATER = QUAD_HALF / 2;
	public static int QUAD_EIGHTH = QUAD_QUATER / 2;
	
	private final RECTANGLE[][] bounds = new RECTANGLE[TILES][TILES];
	private final ArrayList<Tile> tiles = new ArrayList<Tile>(TILES * TILES);
	
	SettlementGrid() {
		
		STRING_RECIEVER reciever = new STRING_RECIEVER() {
			
			@Override
			public void acceptString(CharSequence string) {
				//this is what someone types
			}
		};
		
		ACTION a = new ACTION() {
			
			@Override
			public void exe() {
				VIEW.inters().input.requestInput(reciever, "type something");
			}
		};
		
		IDebugPanelSett.add("prort", a);
		
		for (int y = 0; y < TILES; y++)
			for (int x = 0; x < TILES; x++) {
				bounds[y][x] = new Rec(x * QUAD_SIZE, x * QUAD_SIZE + QUAD_SIZE, y * QUAD_SIZE,
						y * QUAD_SIZE + QUAD_SIZE);
				tiles.add(new Tile(x, y));
			}
	}
	
	public static class Tile {
		
		public final RECTANGLE bounds;
		private final COORDINATE[][] coos = new COORDINATE[TILES][TILES];
		
		private final COORDINATE[] coosInner = new COORDINATE[DIR.ALL.size()];
		private final COORDINATE coosInnerC;
		private final ArrayListResize<DIR> dirs = new ArrayListResize<>(4, 9);
		
		private Tile(int quadX, int quadY) {
			bounds = new Rec(quadX * QUAD_SIZE, quadX * QUAD_SIZE + QUAD_SIZE, quadY * QUAD_SIZE,
					quadY * QUAD_SIZE + QUAD_SIZE);
			
			dirs.add(DIR.E);
			dirs.add(DIR.SE);
			dirs.add(DIR.S);
			dirs.add(DIR.C);
			if (quadX == 0) {
				dirs.add(DIR.W);
				dirs.add(DIR.SW);
			}
			if (quadY == 0) {
				dirs.add(DIR.NE);
				dirs.add(DIR.N);
				if (quadX == 0)
					dirs.add(DIR.NW);
			}
			
			dirs.trim();
			
			for (int y = 0; y < TILES; y++) {
				for (int x = 0; x < TILES; x++) {
					int qx = bounds.x1() + (x*QUAD_HALF);
					int qy = bounds.y1() + (y*QUAD_HALF);

					if (qx >= SETT.TWIDTH)
						qx = SETT.TWIDTH-1;
					if (qy >= SETT.THEIGHT)
						qy = SETT.THEIGHT-1;
					
					coos[y][x] = new Coo(qx, qy);
				}
			}
			
			for (DIR d : DIR.ALL) {
				int qx = bounds.cX() + d.x()*(QUAD_QUATER+QUAD_EIGHTH);
				int qy = bounds.cY() + d.y()*(QUAD_QUATER+QUAD_EIGHTH);
				coosInner[d.id()] = new Coo(qx, qy);
			}
			
			coosInnerC = new Coo(bounds.cX(), bounds.cY());
				
		}

		public LIST<DIR> getDirs(){
			return dirs;
		}
		
		public COORDINATE cooInner(DIR d) {
			if (d == DIR.C)
				return coosInnerC;
			return coosInner[d.id()];
		}
		
		public COORDINATE coo(DIR d) {
			return coos[d.y() + 1][d.x() + 1];
		}
		
	}
	
	public Tile tile(int tile) {
		return tiles.get(tile);
	}
	
	public Tile tile(int x, int y) {
		return tiles.get(x + y*TILES);
	}
	
	public LIST<Tile> tiles(){
		return tiles;
	}
	
	
}
