package settlement.tilemap.terrain;

import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.resources.GrowableSprite;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.map.MAP_INTE;
import snake2d.util.rnd.RND;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.text.D;

public final class TFlower extends TerrainTile{
	
	private static CharSequence ¤¤name = "Flower";	
	private final GrowableSprite sprite;

	static {
		D.ts(TFlower.class);
	}
	
	private final TerrainClearing clearing = new TerrainClearing() {
		
		private final SoundRace sound = AUDIO.race("CLEAR");
		
		@Override
		public RESOURCE clear1(int tx, int ty) {
			shared.NADA.placeFixed(tx, ty);
			return null;
		}
		
		@Override
		public boolean can() {
			return true;
		}

		@Override
		public int clearAll(int tx, int ty) {
			shared.NADA.placeFixed(tx, ty);
			return 0;
		}
		
		@Override
		public SoundRace sound(int tx, int ty) {
			return sound;
		}
		
		@Override
		public boolean isEasilyCleared() {
			return true;
		};
	};
	
	TFlower(Terrain t) throws IOException {
		super("FLOWER", t, ¤¤name, SPRITES.icons().m.cancel,  null);
		
		sprite = RESOURCES.growable().sprite("_Flower", 1.0, 1.0);
		
		sprite.trunk.sheight = 4;
		sprite.trunk.sheightoverGround = 0;
		sprite.trunk.setColors(null, new ColorImp(27, 90, 22), new ColorImp(19,52,15));
		
		sprite.growth.sheight = 0;
		sprite.growth.sheightoverGround = 5;
		sprite.growth.setColors(null, new ColorImp(27, 90, 22).shade(1.2), null);
		
//		sprite.growth.cripe[0] = new ColorImp(127,0,0);
//		sprite.growth.cripe[1] = new ColorImp(110,0,0);
//		sprite.growth.cripe[2] = new ColorImp(0,0,127);
//		sprite.growth.cripe[3] = new ColorImp(127,0,127);
//		sprite.growth.cripe[4] = new ColorImp(127,127,60);
//		sprite.growth.cripe[5] = new ColorImp(100,5,100);
		for (int i = 0; i < sprite.growth.cripe.length; i++) {
			
			if (i % 4 == 0) {
				sprite.growth.cripe[i] = new ColorImp(50+RND.rInt(50),RND.rInt(25),50+RND.rInt(50));
			}
//			else if(i % 8 == 1) {
//				sprite.growth.cripe[i] = new ColorImp(100+RND.rInt(27),RND.rInt(10),RND.rInt(10));
//			}
//			else if(i % 8 == 2) {
//				sprite.growth.cripe[i] = new ColorImp(RND.rInt(10),RND.rInt(10),100+RND.rInt(27));
//			}
//			else if(i % 4 == 2) {
//				sprite.growth.cripe[i] = new ColorImp(RND.rInt(25),RND.rInt(50),75+RND.rInt(35));
//			}else if(i % 4 == 3) {
//				sprite.growth.cripe[i] = new ColorImp(100+RND.rInt(20),100,100);
//			}
		}
		
		
		sprite.setPollenColor(new ColorImp(110,110,100));
		
		sprite.makeSheet("_FLOWER");
		
	}

	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	@Override
	protected boolean place(int tx, int ty) {
		if (!is(tx, ty)) {
			shared.data.set(tx, ty, 0);
			super.placeRaw(tx,ty);
		}
		return false;
	}
	
	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		return false;
	}
	
	
	@Override
	protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		double a = (data+1)*amount.maxI;
		sprite.render(r, s, i, a, a);
		return false;

	}
	
	public void render(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i) {
		sprite.render(r, s, i, 1.0, 1.0);
	}
	


	@Override
	public AVAILABILITY getAvailability(int x, int y) {
		return null;
	}
	
	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}
	
	public final TAmount amount = new TAmount(16, "Flower") {
		
		@Override
		public int get(int tile) {
			if (TERRAIN().get(tile) == TFlower.this) {
				return 1 + (shared.data.get(tile) & 0x0F);
			}
			return 0;
		}
		
		@Override
		public MAP_INTE set(int tile, int value) {
			
			if (value == 0) {
				if (TERRAIN().get(tile) == TFlower.this)
					TERRAIN().NADA.placeFixed(tile%TWIDTH, tile/TWIDTH);
			}else {
				if (TERRAIN().get(tile) != TFlower.this)
					TFlower.this.placeFixed(tile%TWIDTH, tile/TWIDTH);
				shared.data.set(tile, value-1);
			}
			return this;
		}
	};
	

	

	

}
