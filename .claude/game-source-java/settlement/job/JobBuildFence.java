package settlement.job;

import static settlement.main.SETT.FLOOR;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.TERRAIN;

import game.GAME;
import game.audio.SoundRace;
import game.faction.FResources.RTYPE;
import init.sprite.SPRITES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.tilemap.terrain.TFence;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.text.D;
import view.tool.ToolConfig;

public final class JobBuildFence extends JobBuild{

	private final TFence fence;
	private static CharSequence ¤¤desc = "¤Stops subjects and animals from wandering where you don't desire them.";
	
	static LIST<Job> make(){
		D.ts(JobBuildFence.class);
		ArrayList<Job> all = new ArrayList<>(TERRAIN().FENCES.all().size());
		for (TFence s : TERRAIN().FENCES.all()) {
			all.add(new JobBuildFence(s));
		}
		return all;
	}
	
	JobBuildFence(TFence fence) {
		super(
				"FENCE_"+fence.key(),
				fence.tile.resource, 
				1, 
				true, 
				fence.tile.name(), 
				¤¤desc, 
				fence.tile.getIcon());
		needsFerClear = false;
		this.fence = fence;
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		for (DIR d: DIR.ORTHO) {
			if (FLOOR().getter.is(tx, ty, d) || JOBS().getter.get(tx, ty, d) instanceof JobBuildFence)
				mask |= d.mask();
		}
		SPRITES.cons().BIG.dashedThick.render(r, mask, x, y);
	}
	
	@Override
	protected double constructionTime(Humanoid skill) {
		return 10;
	}
	
	@Override
	protected SoundRace constructSound() {
		return fence.tile.clearing().sound(coo.x(), coo.y());
	}
	
	@Override
	protected boolean construct(int tx, int ty) {
		if (fence.tile.resource != null)
			GAME.player().res().inc(fence.tile.resource, RTYPE.CONSTRUCTION, -fence.tile.resAmount);
		fence.tile.placeFixed(tx, ty);
		return false;
	}
	
	@Override
	public boolean isConstruction() {
		return true;
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return fence.tile;
	}

	private static JobComboPlacer pla = null;
	
	@Override
	public ToolConfig config() {
		return pp().get(this);
	}
	
	public static Job getPlacable() {
		return pp().current();
	}
	
	static JobComboPlacer pp() {
		if (pla == null)
			pla = new JobComboPlacer(SETT.JOBS().fences, "FENCE");
		return pla;
	}
	
}
