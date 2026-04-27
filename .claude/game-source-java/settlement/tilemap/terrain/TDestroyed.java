package settlement.tilemap.terrain;

import static settlement.main.SETT.TERRAIN;

import game.audio.SoundRace;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.job.Job;
import settlement.path.AVAILABILITY;
import settlement.tilemap.TILE_FIXABLE;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import settlement.tilemap.terrain.TerrainDiagonal.Diagonalizer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.sprite.text.Str;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.text.D;

public final class TDestroyed extends TerrainTile implements TILE_FIXABLE, Diagonalizer{
	
	private static CharSequence ¤¤broken = "¤broken {0}";
	static {
		D.ts(TDestroyed.class);
	}
	

	private final TerrainClearing clearing = new TerrainClearing() {
		
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
			return get(tx, ty).clearing().sound(tx, ty);
		}
		
		@Override
		public boolean isEasilyCleared() {
			return true;
		};
		
		@Override
		public void destroy(int tx, int ty) {
//			GAME.Notify("here");
//			super.destroy(tx, ty);
		};
		
		@Override
		public boolean canDestroy(int tx, int ty) {
			return false;
		};
		
		@Override
		public double strength() {
			return 0;
		};
	};
	
	TDestroyed(Terrain t) {
		super("DESTROYED", t,"", SPRITES.icons().m.cancel,  new ColorImp(80, 60, 60));
	}
	
	public CharSequence name(int tx, int ty) {
		Str.TMP.clear().add(¤¤broken);
		Str.TMP.insert(0, get(tx, ty).name());
		return Str.TMP;
	}

	public TerrainTile get(int tx, int ty) {
		int i = shared.data.get(tx, ty) & 0x0FF;
		TerrainTile t = shared.all().get(i);
		if (t != null && t instanceof TDestoryable) {
			return t;
		}
		return TERRAIN().NADA;
	}
	
	@Override
	public TerrainClearing clearing() {
		return clearing;
	}
	
	public <T extends TerrainTile & TDestoryable> void place(int tx, int ty, T t, int data) {
		
		int d = data << 9;
		if (t instanceof Diagonalizer) {
			d |= ((Diagonalizer) t).getDia(tx, ty) ? 0b0100000000 : 0;
		}
		
		d |= t.code;
		shared.data.set(tx, ty, d);
		
		placeFixed(tx, ty);
	}
	
	public int getData(int tx, int ty) {
		return (shared.data.get(tx, ty) >> 8) & 0x0FF;
	}
	
	@Override
	protected boolean place(int tx, int ty) {
		int d = shared.data.get(tx, ty);
		super.placeRaw(tx,ty);
		shared.data.set(tx, ty, d);
		return false;
	}
	
	@Override
	protected boolean renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator i, int data) {
		int x = i.x();
		int y = i.y();
		int ran = i.ran();
		TDestoryable t = (TDestoryable) get(i.tx(), i.ty());
		t.breakableRes().renderDebris(r, s, x, y, ran, t.resAmount());
		return false;
	}

	@Override
	public AVAILABILITY getAvailability(int tx, int ty) {
		return AVAILABILITY.PENALTY2;
	}
	
	@Override
	public boolean isPlacable(int tx, int ty) {
		return false;
	}
	
	@Override
	public COLOR miniC(int x, int y) {
		return get(x, y).miniC(x, y);
	}
	
	@Override
	public COLOR miniColorPimped(ColorImp c, int x, int y, boolean northern, boolean southern) {
		return get(x, y).miniColorPimped(c, x, y, northern, southern);
	}

	@Override
	protected boolean renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator i, int data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Job fixJob(int tx, int ty) {
		return ((TDestoryable)get(tx, ty)).fixJob();
	}
	
	interface TDestoryable {
		
		public Job fixJob();
		public int resAmount();
		public RESOURCE breakableRes();
	}

	@Override
	public void setDia(int tx, int ty, boolean dia) {
		
	}

	@Override
	public boolean getDia(int tx, int ty) {
		return ((shared.data.get(tx, ty) >> 8) & 1) == 1;
	}

	@Override
	public TerrainTile getTerrain(int tx, int ty) {
		return get(tx, ty);
	}

}
