package settlement.tilemap;

import game.GAME;
import settlement.main.SETT;
import settlement.tilemap.TileMap.SMinimapGetter;
import snake2d.TextureHolder.TextureHolderChunk;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayListShort;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.TextureCoords;
import util.rendering.Minimap;

final class MinimapColorGetter {
	
	private final ColorImp col = new ColorImp();
	private DIR[] dirNorth = new DIR[] {DIR.W, DIR.NW,DIR.N, DIR.NE};
	private DIR[] dirShade = new DIR[] {DIR.E, DIR.SE,DIR.S, DIR.SW};
	
	private final int qs = 8;
	private final int sc = Integer.numberOfTrailingZeros(qs);
	private final int ww = SETT.TWIDTH >> sc;
	
	private Bitmap1D bits = new Bitmap1D(SETT.TWIDTH*SETT.THEIGHT/(qs*qs), false);
	private ArrayListShort queue = new ArrayListShort(SETT.TWIDTH*SETT.THEIGHT/(qs*qs));
	private final TextureHolderChunk chunk = new TextureHolderChunk(qs, qs);
	
	
	public COLOR get(int x, int y){
		
		
		SMinimapGetter r = miniR(x, y);
		COLOR c = r.miniC(x, y);
		boolean n = false;
		boolean s = false;
		
		for (DIR d : dirNorth) {
			int dx = x + d.x();
			int dy = y + d.y();
			if (SETT.IN_BOUNDS(dx, dy)) {
				SMinimapGetter r2 = miniR(dx, dy);
				if (r2 == null || r != r2) {
					n = true;
					break;
				}
			}
			
		}
		for (DIR d : dirShade) {
			int dx = x + d.x();
			int dy = y + d.y();
			if (SETT.IN_BOUNDS(dx, dy)) {
				SMinimapGetter r2 = miniR(dx, dy);
				if (r2 == null || r != r2) {
					s = true;
					break;
				}
			}
			
		}
		
		col.set(c);
		
		return r.miniColorPimped(col, x, y, n, s);
		
	}
	
	private SMinimapGetter miniR(int x, int y) {
		if (!SETT.IN_BOUNDS(x, y))
			return null;
		COLOR c = SETT.ROOMS().miniC.miniC(x, y);
		if (c != null)
			return SETT.ROOMS().miniC;
		c = SETT.TERRAIN().get(x, y).miniC(x, y);
		if (c != null)
			return SETT.TERRAIN().get(x, y);
		c = SETT.FLOOR().minimap.miniC(x, y);
		if (c != null)
			return SETT.FLOOR().minimap;
		return SETT.GROUND().minimap;
	}
	
	public void update(int tx, int ty) {
		for (int di = 0; di < DIR.ALLC.size(); di++) {
			DIR dir = DIR.ALLC.get(di);
			int dx = tx+dir.x();
			int dy = ty+dir.y();
			if (SETT.IN_BOUNDS(dx, dy)) {
				dx = dx >> sc;
				dy = dy >> sc;
				int i = dx + dy*ww;
				if (!bits.get(i)) {
					queue.add(i);
					bits.setTrue(i);
				}
			}
		}
		
		//SETT.MINIMAP().putPixel(tx, ty, get(tx, ty));
	}
	
	void clear() {
		queue.clear();
		bits.clear();
	}
	
	void update() {
		
		if (queue.size() > 0) {
			int q = queue.remove(queue.size()-1);
			bits.setFalse(q);
			int tx = q%ww;
			int ty = q/ww;
			tx = tx << sc;
			ty = ty << sc;
			
			int i = 0;
			for (int dy = 0; dy < qs; dy++) {
				for (int dx = 0; dx < qs; dx++) {
					COLOR c = get(tx+dx, ty+dy);

					chunk.put(i++, Minimap.getC(c.red()), Minimap.getC(c.green()), Minimap.getC(c.blue()), (byte)0x0FF);
				}
			}
			
			TextureCoords c = SETT.MINIMAP().texture(tx, ty, chunk.width, chunk.height);
			
			GAME.texture().addChunk(c.x1, c.y1, chunk.width, chunk.width*chunk.height, chunk);
			
		}
	}
	
}