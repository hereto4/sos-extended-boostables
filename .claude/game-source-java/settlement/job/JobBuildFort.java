package settlement.job;

import static settlement.main.SETT.FLOOR;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TERRAIN;

import game.GAME;
import game.audio.SoundRace;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.Faction;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.value.Lock;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.tilemap.terrain.TFortification;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.text.D;
import util.text.Dic;
import view.tool.PlacableMessages;
import view.tool.ToolConfig;

public final class JobBuildFort extends JobBuild{

	private final TFortification fort;
	private static CharSequence ¤¤dStairs = "¤Stairs are used for getting access to the top of fortifications. Should be placed adjacent to one.";
	
	static {
		D.ts(JobBuildFort.class);
	}
	
	static LIST<Job> make(){
		ArrayList<Job> all = new ArrayList<>(TERRAIN().FORTIFICATIONS.all().size());
		for (TFortification s : TERRAIN().FORTIFICATIONS.all()) {
			all.add(new JobBuildFort(s));
		}
		return all;
	}

	public static class JobBuildForts {
		
		public final LIST<Job> all;
		final  JobComboPlacer pla;
		public final Job build_stairs = new JobBuildFort.Stairs();
		
		JobBuildForts() {
			ArrayList<Job> all = new ArrayList<>(TERRAIN().FORTIFICATIONS.all().size());
			for (TFortification s : TERRAIN().FORTIFICATIONS.all()) {
				all.add(new JobBuildFort(s));
			}
			this.all = all;
			pla = new JobComboPlacer(all.join(build_stairs), "FORTIFICATIONS");
		}
		
		public Job getPlacable() {
			return pla.current();
		}

		
	}
	
	
	JobBuildFort(TFortification fort) {
		super(
				"FORT_" + fort.key(),
				fort.resource, 
				fort.resAmount, 
				true, 
				fort.tile.name(), 
				fort.desc, 
				fort.tile.getIcon());
		this.fort = fort;
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		for (DIR d: DIR.ORTHO) {
			if (FLOOR().getter.is(tx, ty, d) || JOBS().getter.get(tx, ty, d) instanceof JobBuildFort)
				mask |= d.mask();
		}
		SPRITES.cons().BIG.dashed.render(r, mask, x, y);
	}
	
	@Override
	public CharSequence lockText() {
		Str.TMP.clear().add(Dic.¤¤Requires);
		Str.TMP.NL();
		boolean has = false;
		for (Lock<Faction> i : fort.reqs.all()) {
			if (!i.unlocker.inUnlocked(FACTIONS.player())) {
				Str.TMP.NL();
				has = true;
				Str.TMP.add(i.unlocker.name);
			}
		}
		if (has)
			return Str.TMP;
		return null;
	}
	
	@Override
	protected double constructionTime(Humanoid skill) {
		return 50;
	}
	
	@Override
	protected SoundRace constructSound() {
		return fort.sound;
	}
	
	@Override
	protected boolean construct(int tx, int ty) {
		if (fort.resource != null)
			GAME.player().res().inc(fort.resource,  RTYPE.CONSTRUCTION, -fort.resAmount);
		fort.tile.placeFixed(tx, ty);
		SETT.FLOOR().clearer.clear(tx, ty);
		return false;
	}
	
	@Override
	public boolean becomesSolid() {
		return true;
	}
	
	@Override
	public boolean isConstruction() {
		return true;
	}
	
	static final class Stairs extends JobBuild{

		Stairs() {
			super("STAIRS", RESOURCES.STONE(), 2, false, SETT.TERRAIN().FSTAIRS.name(), ¤¤dStairs, SETT.TERRAIN().FSTAIRS.getIcon());
		}

		@Override
		protected double constructionTime(Humanoid skill) {
			return 50;
		}

		@Override
		protected boolean construct(int tx, int ty) {
			GAME.player().res().inc(res, RTYPE.CONSTRUCTION, -resAmount);
			SETT.TERRAIN().FSTAIRS.placeFixed(tx, ty);
			return false;
		}

		@Override
		protected SoundRace constructSound() {
			
			return null;
		}
		
		@Override
		protected CharSequence problem(int tx, int ty, boolean overwrite) {
			
			TerrainTile t = TERRAIN().get(tx, ty);
			if (t instanceof TFortification.Normal && SETT.PATH().availability.get(tx, ty).player < 0)
				return null;
			
			if (super.problem(tx, ty, overwrite) != null)
				return super.problem(tx, ty, overwrite);
			
			
			if (PATH().solidity.is(tx, ty))
				return PlacableMessages.¤¤SOLID_BLOCK;
			if (t.clearing().needs() && !t.clearing().can())
				return PlacableMessages.¤¤MISC;
			return null;
		}

		@Override
		void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
			SPRITES.cons().BIG.dashed.render(r, 0, x, y);
		}
		
		@Override
		public TerrainTile becomes(int tx, int ty) {
			return SETT.TERRAIN().FSTAIRS;
		}
		

		
		@Override
		public ToolConfig config() {
			return SETT.JOBS().build_fort.pla.get(this);
		}
		
		
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return fort.tile;
	}
	
	
	
	@Override
	public ToolConfig config() {
		return SETT.JOBS().build_fort.pla.get(this);
	}
	


}
