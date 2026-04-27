package settlement.tilemap.terrain;

import java.io.IOException;

import game.audio.AUDIO;
import game.audio.SoundRace;
import game.time.TIME;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.text.D;
import util.spritecomposer.ComposerUtil;

public final class TMushroom extends TerrainTile{
	

	private final TILE_SHEET sheet;
	private final static int SET = 16;
	public final static double TARGET_FERTILITY = 0.55; 
	public final static double DELTA_FERTILITY = 0.25; 
	private static CharSequence ¤¤name = "¤Shrooms";
	
	static {
		D.ts(TMushroom.class);
	}
	
	private final TerrainClearing clearing = new TerrainClearing() {
		
		private final SoundRace sound = AUDIO.race("CLEAR_BUSH");
		
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
	
	TMushroom(Terrain t) throws IOException {
		super("MUSHROOM", t,¤¤name, SPRITES.icons().m.cancel, null);
		sheet = new ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Mushroom"), 716, 94) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, 0, 1, 1, 16, 4, d.s16);
				s.singles.paste(true);
				return d.s16.saveGame();
			}
		}.get();
	}

	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	@Override
	protected boolean place(int tx, int ty) {
		super.placeRaw(tx, ty);
		shared.data.set(tx, ty, RND.rInt(3));
		return false;
	}
	
	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		return false;
	}
	
	@Override
	protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {


		
		if (TIME.seasons().current() == TIME.seasons().AUTUMN) {
			int x = i.x();
			int y = i.y();
			int t = i.ran() & 0x0F;
			
			data = (int) Math.round(((i.ran()>>4)&3)*TIME.seasons().bitPartOfC());
			t+= data*SET;
			sheet.render(r, t, x, y);
			s.setDistance2Ground(0).setHeight(2);
			sheet.render(s,  t, x, y);
			i.countVegetation();
		}
		
		return false;
		

	}

	@Override
	public AVAILABILITY getAvailability(int x, int y) {
		return null;
	}
	
	@Override
	public boolean isPlacable(int tx, int ty) {
		return true;
	}

}
