package settlement.tilemap.terrain;

import java.io.IOException;

import game.audio.AUDIO;
import game.audio.SoundRace;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import init.sprite.game.SheetPair;
import init.sprite.game.SheetType;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public final class TDecor extends TerrainTile{
	
	private final LIST<SheetPair> sheets;
	
	
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
	
	TDecor(Terrain t, CharSequence name, String sKey) throws IOException {
		super("DECORD_" + sKey, t,name, SPRITES.icons().m.cancel, null);
		sheets = SPRITES.GAME().sheets(SheetType.s1x1, new Json(PATHS.CONFIG().get("SETT_MAP_DECORATION")).json(sKey));
	}

	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	@Override
	protected boolean place(int tx, int ty) {
		super.placeRaw(tx, ty);
		return false;
	}
	
	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator i, int data) {
		
		return false;
	}
	
	@Override
	protected final boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderData.RenderIterator it, int data) {
		
		if (sheets.size() == 0)
			return false;
		
		int ran = it.ran();
		SheetPair sheet = sheets.getC(ran);
		if (sheet == null)
			return false;
		ran = ran>>5;
		sheet.d.color(ran).bind();
		ran = ran>>4;
		
		int frame = sheet.d.frame(ran, 1.0);
		int tile = SheetType.s1x1.tile(sheet.s, sheet.d, 0, frame, ran&0b11);
		
		sheet.s.render(sheet.d, it.x(), it.y(), it, r, tile, ran, 0);
		COLOR.unbind();
		if (s != null)
			sheet.s.renderShadow(sheet.d, it.x(), it.y(), it, s, tile, ran);
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
