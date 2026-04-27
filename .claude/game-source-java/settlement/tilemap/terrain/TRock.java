package settlement.tilemap.terrain;

import static settlement.main.SETT.TWIDTH;

import java.io.IOException;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.constant.C;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public final class TRock extends TerrainTile{
	

	private final TILE_SHEET sheet;
	public final int MAX_AMOUNT = 	0b001111;
	private final static int SET = 16;

	private final TerrainClearing clearing = new TerrainClearing() {
		
		private final SoundRace sound = AUDIO.race("CLEAR_STONE");
		
		@Override
		public RESOURCE clear1(int tx, int ty) {
			amountDecrease(tx, ty);
			return RESOURCES.STONE();
		}
		
		@Override
		public boolean can() {
			return true;
		}

		@Override
		public int clearAll(int tx, int ty) {
			int a = amountGet(shared.data.get(tx, ty));
			shared.NADA.placeFixed(tx, ty);
			return a;
		}
		
		@Override
		public SoundRace sound(int tx, int ty) {
			return sound;
		}
	};
	
	private final SPRITE icon;
	
	TRock(Terrain t) throws IOException {
		super("ROCK", t,"rock", SPRITES.icons().m.cancel, t.colors.minimap.rock);
		sheet = new ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Rock"), 716, 182) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s16;
				s.singles.init(0, 0, 1, 1, 16, 8, t);
				
				for (int i = 0; i < 8; i++)
					s.singles.setSkip(i*16, 16).paste(true);
				return t.saveGame();
			}
		}.get();
		
		icon = new SPRITE.Imp(Icon.L) {
			
			private COLOR bg = new ColorImp(102, 87, 65);
			private COLOR bg2 = bg.shade(0.6);
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {

				bg2.render(r, X1, X2, Y1, Y2);
				bg.render(r, X1+2, X2-2, Y1+2, Y2-2);
				
				int x = X1 + (Icon.L-C.T_PIXELS)/4;
				int y = Y1 + (Icon.L-C.T_PIXELS)/4;
				int dd = C.T_PIXELS/2;
				int w = C.T_PIXELS;
				
				int tile;
				
				sheet.render(r, SET + 3, x, x+w, y, y+w);
				
				tile = 2*SET;
				sheet.render(r, tile, x+dd, x+dd+w, y, y+w);
				
				tile = 2*SET + 1;
				sheet.render(r, tile, x, x+w, y+dd, y+dd+w);
				
				tile = 2*SET + 2;
				sheet.render(r, tile, x+dd, x+dd+w, y+dd, y+dd+w);
				
			}
		};
	}

	@Override
	public SPRITE getIcon() {
		return icon;
	}
	
	
	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	@Override
	protected boolean place(int tx, int ty) {
		if (!is(tx, ty)) {
			shared.data.set(tx, ty, 0);
			setCode(tx, ty);
			return true;
		}
		int ro = shared.data.get(tx, ty);
		setCode(tx, ty);
		return ro != shared.data.get(tx, ty);
	}
	
	private void setCode(int x, int y) {

		int am = 0;
		if (is(x, y))
			am = amountGet(shared.data.get(x, y));
		super.placeRaw(x, y);
		
		if (am == 0)
			am = 1;

		shared.data.set(x, y, 0);
		amoutSet(x, y, am);
		
	}

	
	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		return false;
	}
	
	@Override
	protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {

		int x = i.x();
		int y = i.y();
		int ran = i.ran() & 0x00F;
		int a = amountGet(data);
		
		
		s.setHeight(1).setDistance2Ground(0);
		int tile;
		
		if (a >= 5) {

			
			if (a >= 5) {
				ran = (i.ran()>>2) & 0x00F;
				tile = 2*SET + ran;
				sheet.render(r, tile, x+C.TILE_SIZEH, y);
				sheet.render(s, tile, x+C.TILE_SIZEH, y);
				a-= 5;
			}else if (a > 0){
				ran = (i.ran()>>2) & 0x00F;
				tile = (7-(a%7))*SET + ran;
				sheet.render(r, tile, x+C.TILE_SIZEH, y);
				sheet.render(s, tile, x+C.TILE_SIZEH, y);
			}
			
			if (a >= 5) {
				ran = (i.ran()>>1) & 0x00F;
				tile = 2*SET + ran;
				sheet.render(r, tile, x, y+C.TILE_SIZEH);
				sheet.render(s, tile, x, y+C.TILE_SIZEH);
				a-= 5;
			}else if (a > 0){
				ran = (i.ran()>>1) & 0x00F;
				tile = (7-(a%7))*SET + ran;
				sheet.render(r, tile, x, y+C.TILE_SIZEH);
				sheet.render(s, tile, x, y+C.TILE_SIZEH);
			}
			
			if (a >= 5) {
				ran = (i.ran()) & 0x00F;
				tile = 2*SET + ran;
				sheet.render(r, tile, x+C.TILE_SIZEH, y+C.TILE_SIZEH);
				sheet.render(s, tile, x+C.TILE_SIZEH, y+C.TILE_SIZEH);
				a-= 5;
			}else if (a > 0){
				ran = (i.ran()) & 0x00F;
				tile = (7-(a%7))*SET + ran;
				sheet.render(r, tile, x+C.TILE_SIZEH, y+C.TILE_SIZEH);
				sheet.render(s, tile, x+C.TILE_SIZEH, y+C.TILE_SIZEH);
			}
			
			
			
			
		}
		a = amountGet(data);

		
		tile = (a>>3);
		if (tile > 7)
			tile = 7;
		tile = (7-tile)*SET + ran;
		sheet.render(r, tile, x, y);
		sheet.render(s, tile, x, y);
		
		
		return false;
		

	}
	


	@Override
	public AVAILABILITY getAvailability(int x, int y) {
		return AVAILABILITY.PENALTY2;
	}
	
	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}
	
	public void placeAmount(int tx, int ty, double amount) {
		
		int a = (int) Math.ceil(MAX_AMOUNT*amount);
		if (a < 1)
			a = 1;
		if (a > MAX_AMOUNT)
			a = MAX_AMOUNT;
		placeRaw(tx, ty);
		amoutSet(tx, ty, a);
	}
	
	public void amountIncrease(int tx, int ty) {
		if (is(tx, ty)) {
			int a = amountGet(shared.data.get(tx, ty));
			amoutSet(tx, ty, a+1);
		}
		placeFixed(tx, ty);
	}
	
	public void amountDecrease(int tx, int ty) {
		if (!is(tx, ty))
			return;
		int a = amountGet(shared.data.get(tx, ty));
		if (a > 1) {
			amoutSet(tx, ty, a-1);
			placeFixed(tx, ty);
		}else
			shared.NADA.placeFixed(tx, ty);
	}
	
	private int amountGet(int data) {
		return ((data & 0x03F00) >> 8);
	}
	
	public int amountGet(int tx, int ty) {
		if (!is(tx, ty))
			return 0;
		return amountGet(shared.data.get(tx, ty))/2;
	}
	
	
	private void amoutSet(int tx, int ty, int a) {
		if (a < 0)
			a = 0;
		if (a > MAX_AMOUNT)
			a = MAX_AMOUNT;
		a = (a << 8);
		int d = shared.data.get(tx, ty);
		d &= ~0x03F00;
		d |= a;
		shared.data.set(tx, ty, d);
	}
	
	@Override
	public void hoverInfo(GBox box, int tx, int ty) {
		box.add(RESOURCES.STONE().icon());
		box.textLL(RESOURCES.STONE().name);
		super.hoverInfo(box, tx, ty);
		int d = amountGet(tx, ty);
		box.tab(6);
		box.add(GFORMAT.i(box.text(), d));
	}
	
//	@Override
//	COLOR getColor(int x, int y) {
//		for (DIR d : DIR.ORTHO) {
//			if (!is(x, y, d))
//				return ColorImp.TMP.set(super.getColor(x, y)).shadeSelf(0.7);
//		}
//		return super.getColor(x, y);
//	}
	
	@Override
	public COLOR miniC(int x, int y) {
		return super.miniC(x, y);
	}
	
	@Override
	public int miniDepth() {
		return 1;
	}
	
	@Override
	public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
		COLOR col = SETT.GROUND().minimap.miniC(x, y);
		c.interpolate(col, miniC, 0.5+0.5*amountGet(shared.data.get(x+y*TWIDTH))/(double)MAX_AMOUNT);
		return c;
	}
	

}
