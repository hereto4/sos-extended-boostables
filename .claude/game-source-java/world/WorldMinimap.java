package world;

import static world.WORLD.MOUNTAIN;
import static world.WORLD.REGIONS;
import static world.WORLD.WATER;

import java.io.IOException;

import game.faction.FACTIONS;
import game.faction.Faction;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LinkedList;
import util.colors.GCOLOR;
import util.rendering.Minimap;
import util.text.D;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;

public final class WorldMinimap {

	public static final int WIDTH = 256;
	public static final int HEIGHT = 256;
	
	private final Map map = new Map();
	private final int dc = 8;
	private final Chunk[][] chunks;
	private Chunk[][] regionMap = new Chunk[WREGIONS.MAX][];
	private boolean dirty;
	
	private final byte Ivisible = 0b0001;
	private final byte ItmpVisible = 0b0010;
	private final byte ItmpHilit = 0b0100;
	private boolean repaint = true;
	
	private static CharSequence ¤¤painting = "¤Painting world minimap";
	static {
		D.ts(WorldMinimap.class);
	}
	
	public WorldMinimap(int width, int height) throws IOException{ 
		
		chunks = new Chunk[(int) Math.ceil((double)width/dc)][(int) Math.ceil((double)height/dc)];
		
		for (int y = 0; y < chunks.length; y++) {
			for (int x = 0; x < chunks[y].length; x++)
				chunks[y][x] = new Chunk(x, y);
		}
		
		new RD.RDOwnerChanger() {
			
			@Override
			public void change(Region reg, Faction oldOwner, Faction newOwner) {
				map.updateRegion(reg);
				if (oldOwner == FACTIONS.player() || newOwner == FACTIONS.player()) {
					dirty = true;
				}
			}
		};
		
	}
	
	public void repaint() {
		this.repaint = true;
		dirty = true;
	}
	
	private void prepaint() {
		map.repaint();
		int[][] map = new int[chunks.length][chunks[0].length];
		for (Region reg : WORLD.REGIONS().active()) {
			LinkedList<Chunk> chunks = new LinkedList<>();
			for (COORDINATE c : reg.info.bounds()) {
				if (!reg.is(c))
					continue;
				Chunk ch = chunk(c.x(), c.y());
				if (map[ch.y][ch.x] != reg.index()) {
					chunks.add(ch);
					map[ch.y][ch.x] = reg.index();
				}
			}
			regionMap[reg.index()] = new Chunk[chunks.size()];
			int i = 0;
			for (Chunk c : chunks) {
				regionMap[reg.index()][i++] = c;
				c.dirty = false;
			}
		}
		for (Chunk[] cs : chunks)
			for (Chunk c : cs)
				c.dirty = false;
		dirty = true;
	}
	
	public void setDirty() {
		dirty = true;
	}
	
	private Coo cUp = new Coo();
	
	public void update() {
		if (repaint) {
			repaint = false;
			prepaint();
		}
		
		for (int i = 0; i < 5; i++) {
			int x = cUp.x();
			int y = cUp.y();
			
			Chunk c = chunks[y][x];
			
			if (c.dirty) {
				c.dirty = false;
				map.redraw(c.x*dc, c.y*dc, dc, dc);
			}
			
			cUp.xIncrement(1);
			if (cUp.x() >= chunks[0].length) {
				cUp.xSet(0);
				cUp.yIncrement(1);
				if (cUp.y() >= chunks.length) {
					cUp.ySet(0);
				}
			}
		}
	}
	
	public void render(SPRITE_RENDERER r, int x1, int y1) {

		if (dirty) {
			dirty = false;
			
			for (Chunk[] cs : chunks) {
				for (Chunk c : cs)
					c.type &= ~Ivisible;
			}
			
			for (Region reg : WORLD.REGIONS().active()) {
				if (reg.faction() == FACTIONS.player() || RD.DIST().reachable(reg) || reg.faction() != null && RD.DIST().reachable(reg.faction())) {
					set(reg, Ivisible);
				}
			}
			
		}
		
		map.map.render(r, x1, y1);
		
		for (int ai = 0; ai < WORLD.ENTITIES().armies.max(); ai++) {
			WArmy a = WORLD.ENTITIES().armies.get(ai);
			if (a == null)
				continue;

			if (a.faction() == FACTIONS.player()) {
				
				chunk(a.ctx(), a.cty()).type |= ItmpVisible;
				for (DIR d : DIR.ALL) {
					int dx = (int) (a.ctx() + d.xN()*4);
					int dy = (int) (a.cty() + d.yN()*4);
					if (WORLD.TBOUNDS().holdsPoint(dx, dy))
						chunk(dx, dy).type |= ItmpVisible;
				}
				continue;
			}
			
			if (WORLD.FOW().is(a.ctx(), a.cty()))
				continue;
			
			int cx = map.map.width()*a.ctx()/WORLD.TWIDTH();
			int cy = map.map.height()*a.cty()/WORLD.THEIGHT();
			
			cx += x1;
			cy += y1;
			OPACITY.O25TO100.bind();
			COLOR.BLACK.render(r, cx-4, cx+4, cy-4, cy+4);
			COLOR.WHITE100.render(r, cx-3, cx+3, cy-3, cy+3);
			COLOR c = GCOLOR.MAP().get(a.faction());
			
			c.render(r, cx-2, cx+2, cy-2, cy+2);
			
		}
		OPACITY.unbind();
		COLOR.unbind();
		
		int ci = 0;
		
		OPACITY.O50.bind();
		for (Chunk[] cs : chunks) {
			for (Chunk c : cs) {
				
				if (ci < 5 && c.dirty) {
					c.dirty = false;
					ci++;
					map.redraw(c.x*dc, c.y*dc, dc, dc);
				}
				
				if ((c.type & ItmpHilit) != 0) {
					int x = x1 + c.x*dc;
					int y = y1 + c.y*dc;
					COLOR.WHITE120_2_WHITE150.render(r, x, x+dc, y, y+dc);
				}else if (!WORLD.FOW().toggled.is() || ((c.type & Ivisible) != 0 || (c.type & ItmpVisible) != 0)) {
					
					
				}else {
					int x = x1 + c.x*dc;
					int y = y1 + c.y*dc;
					COLOR.BLACK.render(r, x, x+dc, y, y+dc);
				}
				
				c.type &= Ivisible;
				
			}
			
		}
		OPACITY.unbind();
		
		
	}
	
	public void hilight(Faction fac) {
		for (int i = 0; i < fac.realm().regions(); i++)
			hilight(fac.realm().region(i));
	}
	
	public void hilight(Region reg) {
		set(reg, ItmpHilit);
	}
	
	private void set(Region reg, byte mask) {
		
		if (regionMap[reg.index()] == null)
			return;
		for (Chunk c : regionMap[reg.index()])
			c.type |= mask;
		
	}
	
	private Chunk chunk(int tx, int ty) {
		return chunks[ty/dc][tx/dc];
	}
	
	public void updateRegion(Region c) {
		
		int px1 = c.info.bounds().x1()/dc-4;
		int py1 = c.info.bounds().y1()/dc-4;
		int px2 = c.info.bounds().x2()/dc+4;
		int py2 = c.info.bounds().y2()/dc+4;
		
		for (int y = py1; y < py2; y++) {
			if (y >= 0 && y < chunks.length) {
				for (int x = px1; x < px2; x++) {
					if (x >= 0 && x < chunks.length) {
						chunks[y][x].dirty = true;
					}
				}
			}
		}
	}
	
	public void update(int tx, int ty) {
		
		int px1 = tx/dc-4;
		int py1 = ty/dc-4;
		int px2 = tx/dc+4;
		int py2 = ty/dc+4;
		
		for (int y = py1; y < py2; y++) {
			if (y >= 0 && y < chunks.length) {
				for (int x = px1; x < px2; x++) {
					if (x >= 0 && x < chunks.length) {
						chunks[y][x].dirty = true;
					}
				}
			}
		}
	}
	
	private static class Chunk {
		
		private final int x;
		private final int y;
		private byte type;
		private boolean dirty;
		
		Chunk(int x, int y){
			this.x = x;
			this.y = y;
		}
	}

	private static final class Map {
		
		Map() throws IOException{
			// TODO Auto-generated constructor stub
		}
		
		public final Minimap map = new Minimap(WIDTH);
		private Bitmap1D changes = new Bitmap1D(WREGIONS.MAX, false);
		private int changedI = WREGIONS.MAX;
		private final ColorImp cWork = new ColorImp();
		
		private void updateRegion(Region region) {
			changedI = Math.max(region.index(), changedI);
			changes.set(region.index(), true);
		}
		
		void clear() {
			changedI = WREGIONS.MAX;
			changes.clear();
		}
		
		void redraw(int x1, int y1, int w, int h) {
			int px1 = CLAMP.i(map.width()*x1/WORLD.TWIDTH(), 0, map.width());
			int py1 = CLAMP.i(map.height()*y1/WORLD.THEIGHT(), 0, map.height());
			int px2 = CLAMP.i(map.width()*(x1+w)/WORLD.TWIDTH(), 0, map.width());
			int py2 = CLAMP.i(map.height()*(y1+h)/WORLD.THEIGHT(), 0, map.height());
			
			for (int py = py1; py < py2; py++) {
				for (int px = px1; px < px2; px++) {
					cWork.set(getColorP(px, py));
					cWork.shadeSelf(0.5);
					map.putPixel(px, py, cWork);
				}
				
			}
		}
		

		private final static COLOR cNone = new ColorImp(100, 100, 100);
		private final COLOR cBorderDark = new ColorImp(35,35,35);
		private final COLOR cBorderLight = new ColorImp(127, 127, 127);
		private final COLOR cOcean = new ColorImp(30,35,60);
		private final COLOR cOceanDeep = cOcean.shade(0.75);;
		private final COLOR cOceanBorder = cOcean.shade(0.5);
		private final COLOR cMountainTop = new ColorImp(81,75,70);
		private final COLOR cMountainBorder = cMountainTop.shade(0.3);
		
		
		private COLOR getColorP(int pixelX, int pixelY) {
			
			double dx = WORLD.TWIDTH();
			dx /= map.width();
			double dy = WORLD.THEIGHT();
			dy /= map.height();
			
			double wx = (double)WORLD.TWIDTH()*pixelX/map.width();		
			double wy = (double)WORLD.THEIGHT()*pixelY/map.height();
			
			
			
			{
				double rx = dx*6;
				double ry = dy*6;
				
				for (int y = (int) (wy-ry); y <= wy+ry; y++) {
					for (int x = (int) (wx-rx); x <= wx+rx; x++) {
						if (!WORLD.IN_BOUNDS(x, y))
							continue;
						Region r2 = WORLD.REGIONS().map.get(x, y);
						if (r2 != null && r2.capitol() && r2.info.cx() == x && r2.info.cy() == y) {
							int px = (int) (x/dx);
							int py = (int) (y/dy);
							int ddx = pixelX-px;
							int ddy = pixelY-py;
							double rad = Math.abs(ddx) + Math.abs(ddy);
							//return COLOR.BLACK;
							
							if (rad < 3)
								return COLOR.BLACK;
							if (rad == 3)
								return COLOR.WHITE100;
						}	
					}
				}
			}
		
			
			Region r = REGIONS().map.get((int)wx,(int)wy);
			
			if (r != null) {
				
				COLOR c = r.faction() == null ? cNone : r.faction().banner().colorBG();
				
				if (WORLD.IN_BOUNDS((int)(wx+dx), (int)(wy)) && !REGIONS().map.is((int)(wx+dx), (int)(wy), r))
					return ColorImp.TMP.interpolate(c, cBorderDark, 0.75);
				else if (WORLD.IN_BOUNDS((int)(wx), (int)(wy+dy)) && !REGIONS().map.is((int)(wx), (int)(wy+dy), r))
					return ColorImp.TMP.interpolate(c, cBorderDark, 0.75);
				else if (WORLD.IN_BOUNDS((int)(wx-dx), (int)(wy)) && isDiffRealm((int)(wx-dx), (int)(wy), r))
					return ColorImp.TMP.interpolate(c, cBorderLight, 0.75);
				else if (WORLD.IN_BOUNDS((int)(wx), (int)(wy-dy)) && isDiffRealm((int)(wx), (int)(wy-dy), r))
					return ColorImp.TMP.interpolate(c, cBorderLight, 0.75);

				return c;
			}
			
			int tx = (int) wx;
			int ty = (int) wy;
			if (WATER().has.is(tx, ty) && WATER().coversTile.is(tx, ty)) {
				for (DIR d : DIR.ORTHO) {
					int ddx = (int)(tx+d.x()*dx);
					int ddy = (int)(ty+d.y()*dy);
					if (!WATER().coversTile.is(ddx, ddy) || WORLD.REGIONS().map.is(ddx, ddy))
						return cOceanBorder;
				}
				if (WATER().OCEAN.deep.is(tx, ty) || WATER().LAKE.deep.is(tx, ty))
					return cOceanDeep;
				return cOcean;
			} else if (MOUNTAIN().is(tx, ty)) {
				for (DIR d : DIR.ORTHO) {
					int ddx = (int)(tx+d.x()*dx);
					int ddy = (int)(ty+d.y()*dy);
					
					if (!MOUNTAIN().is(tx, ty) || WORLD.REGIONS().map.is(ddx, ddy))
						return cMountainBorder;
					
					
					
					
				}
				return ColorImp.TMP.interpolate(cMountainBorder, cMountainTop, MOUNTAIN().getHeight(tx, ty)/15.0);
			}
			return cNone;
		}
		
		private boolean isDiffRealm(int x, int y, Region r) {
			Region r2 = REGIONS().map.get(x, y);
			if (r == r2)
				return false;
			if (r2 == null)
				return true;
			if (r.realm() == null || r2.realm() == null || r.realm() != r2.realm())
				return true;
			return false;
		}
		
		public void repaint() {
			SPRITES.loader().print(¤¤painting);
			clear();
			int pWidth = map.width();
			int pHeight = map.height();

			byte[] pixels = new byte[pWidth * pHeight * 4];

			int i = 0;
			
			for (int py = 0; py < pHeight; py ++) {
				for (int px = 0; px < pWidth; px++) {
					setPixel(pixels, i, getColorP(px, py));
					i += 4;
				}
			}

			map.putPixels(pixels);
		}
		
		private static void setPixel(byte[] pixels, int i, COLOR c) {
			pixels[i + 0] = (byte) ((c.red()&0x0FF));
			pixels[i + 1] = (byte) ((c.green()&0x0FF));
			pixels[i + 2] = (byte) ((c.blue()&0x0FF));
			pixels[i + 3] = (byte) 255;
		}
		
	}


	
}
