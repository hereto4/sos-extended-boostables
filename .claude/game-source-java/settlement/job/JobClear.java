package settlement.job;

import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.TERRAIN;

import game.GAME;
import game.audio.SoundRace;
import game.faction.FResources.RTYPE;
import init.resources.RESOURCE;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.job.StateManager.State;
import settlement.main.SETT;
import settlement.overlay.Addable;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PlacableMulti;

abstract class JobClear extends Job{

	private final Placer placer;
	private final CharSequence names;
	
	JobClear(String key, CharSequence name, CharSequence desc, CharSequence verb, SPRITE icon) {
		super("CLEAR_"+key, name, icon);
		this.placer = new Placer(this, desc) {
			private final String jobs = "Jobs: ";
			@Override
			public void placeInfo(GBox b, int okTiles, AREA a) {
				super.placeInfo(b, okTiles, a);
				if (okTiles > 0) {
					VIEW.hoverBox().add(VIEW.hoverBox().text().add(jobs).add(okTiles));
				}
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return SETT.JOBS().clearss.butts;
			}
			
			@Override
			public void updateRegardless(GameWindow window, AREA selected) {
				SETT.JOBS().clearss.currentOverlay = overlay();
				if (overlay() != null && SETT.JOBS().clearss.overlay) {
					overlay().add();
					
				}
					
			}
			
		};
		names = verb;
	}

//	@Override
//	public long jobResourceBitToFetch() {
//		return 0;
//	}
	
	Addable overlay() {
		return null;
	}

	@Override
	public int resAmount() {
		return 0;
	}
	
	@Override
	public double jobPerformTime(Humanoid skill) {
		return 30;
	}

	@Override
	public void jobStartPerforming() {
	
	}
	

	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int rAm) {
		TerrainTile t = TERRAIN().get(coo);
		if (!t.clearing().can()) {
			PlacerDelete.place(coo.x(), coo.y());
			return null;
		}
		RESOURCE res = t.clearing().clear1(coo.x(), coo.y());
		if (res != null) {
			GAME.player().res().inc(res, RTYPE.PRODUCED, 1);
		}
		if (t != TERRAIN().get(coo) || TERRAIN().NADA.is(coo)) {
			PlacerDelete.place(coo.x(), coo.y());
		}else {
			JOBS().state.set(State.RESERVABLE, this);
		}

		return res;
		
	}

	@Override
	public CharSequence jobName() {
		return names;
	}

	@Override
	public boolean jobUseTool() {
		return true;
	}

	@Override
	public SoundRace jobSound() {
		return TERRAIN().get(coo).clearing().sound(coo.x(), coo.y());
	}

	@Override
	void renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i, int state) {
		
		
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		SPRITES.cons().ICO.clear.render(r, x, y);
	}

	@Override
	void init(int tx, int ty) {
		// TODO Auto-generated method stub
		
	}

	@Override
	boolean becomesSolidNext() {
		return false;
	}


	@Override
	public PlacableMulti placer() {
		return placer;
	}
	
	@Override
	public RESOURCE resourceCurrentlyNeeded() {
		return null;
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return TERRAIN().NADA;
	}

}
