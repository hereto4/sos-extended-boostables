package settlement.job;

import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TERRAIN;

import game.GAME;
import game.audio.SoundRace;
import game.faction.FResources.RTYPE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.text.D;
import view.main.VIEW;
import view.tool.PlacableMessages;

final class JobBuildFillCave extends JobBuild{

	private static CharSequence ¤¤name = "¤Refill Mountain Cave";
	private static CharSequence ¤¤desc = "¤Refills dug tunnels or natural mountain caves";
	
	static {
		D.ts(JobBuildFillCave.class);
	}
	
	JobBuildFillCave() {
		super(
				"FILL_MOUNTAIN",
				RESOURCES.STONE(),
				2, true, 
				¤¤name, 
				¤¤desc, 
				SETT.TERRAIN().MOUNTAIN.getIcon());
		
		this.placer = new Placer(this, RESOURCES.STONE(), 2, ¤¤desc) {
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
		};
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		SPRITES.cons().ICO.unclear.render(r, x, y);
	}
	
	@Override
	protected CharSequence problem(int tx, int ty, boolean overwrite) {
		if (ROOMS().map.is(tx, ty))
			return PlacableMessages.¤¤ROOM_BLOCK;
		if (SETT.PLACA().willBlock.is(tx, ty)) {
			return PlacableMessages.¤¤BLOCK_WILL;
		}
		if (!overwrite) {
			if (JOBS().getter.is(tx, ty)) {
				return PlacableMessages.¤¤JOB_BLOCK;
			}
		}
		
		if (!TERRAIN().CAVE.is(tx, ty))
			return PlacableMessages.¤¤CAVE_MUST;
		return null;
	}
	
	@Override
	boolean terrainNeedsClear(int tx, int ty) {
		return false;
	}

	@Override
	protected double constructionTime(Humanoid h) {
		return 20;
	}

	@Override
	protected boolean construct(int tx, int ty) {
		GAME.player().res().inc(RESOURCES.STONE(), RTYPE.CONSTRUCTION, -2);
		SETT.FLOOR().clearer.clear(tx, ty);
		TERRAIN().MOUNTAIN.placeFixed(tx, ty);
		TERRAIN().MOUNTAIN.strengthSet(tx, ty, 0);
		
		for (DIR d : DIR.ALLC) {
			if (TERRAIN().CAVE.canFix(tx+d.x(), ty+d.y())) {
				TERRAIN().CAVE.fix(tx+d.x(), ty+d.y());
			}
		}
		return false;
	}

	@Override
	protected SoundRace constructSound() {
		return TERRAIN().MOUNTAIN.clearing().sound(coo.x(), coo.y());
	}
	
	@Override
	public boolean becomesSolid() {
		return true;
	}
	
	@Override
	public boolean isConstruction() {
		return true;
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return TERRAIN().MOUNTAIN;
	}

	
}
