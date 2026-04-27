package settlement.job;

import static settlement.main.SETT.FLOOR;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TERRAIN;

import game.GAME;
import game.audio.AUDIO;
import game.audio.SoundRace;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.value.Lock;
import settlement.entity.humanoid.Humanoid;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.stats.standing.STANDINGS;
import settlement.tilemap.floor.Floors.Floor;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.gui.misc.GButt.Panel;
import util.info.GFORMAT;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import util.text.D;
import util.text.Dic;
import view.tool.PLACABLE;
import view.tool.PlacableMessages;
import view.tool.PlacableMulti;
import view.tool.ToolConfig;

public final class JobBuildRoad extends JobBuild{

	private static CharSequence ¤¤Convert = "Convert";
	private static CharSequence ¤¤ConvertD = "Convert existing roads into this type.";
	private static CharSequence ¤¤durability = "Durability";
	static {
		D.ts(JobBuildRoad.class);
	}
	
	public final static class JobBuildRoads {
		
		public final LIST<JobBuildRoad> all;
		private final JobComboPlacer pla;
		private boolean convert = false;
		
		JobBuildRoads() {
			ArrayList<JobBuildRoad> all = new ArrayList<>(SETT.FLOOR().roads.size());
			for (Floor f : SETT.FLOOR().roads) {
				all.add(new JobBuildRoad(f));
			}
			this.all = all;
			pla = new JobComboPlacer(this.all, "ROAD_TYPE");
		}
		
		public Job getPlacable() {
			return pla.current();
		}
		

	}
	
	private final Floor floor;
	private final Placer placer;
	private boolean showRoads = true;

	
	
	

	
	private JobBuildRoad(Floor floor) {
		super(	"ROAD_" + floor.key,
				floor.resource, 
				floor.resAmount, 
				false, 
				floor.name, 
				floor.desc, 
				floor.getIcon());
		this.floor = floor;

		
		LinkedList<CLICKABLE> bs = new LinkedList<CLICKABLE>();
		bs.add(new Panel(SPRITES.icons().m.cog) {
			@Override
			protected void clickA() {
				showRoads = !showRoads;
			}
			
			@Override
			protected void renAction() {
				selectedSet(showRoads);
				if (showRoads)
					SETT.OVERLAY().ROADING.add();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(SETT.OVERLAY().ROADING.name);
				text.text(SETT.OVERLAY().ROADING.desc);
			}
		});
		
		bs.add(new Panel(SPRITES.icons().m.arrow_right) {
			@Override
			protected void clickA() {
				SETT.JOBS().roads.convert = !SETT.JOBS().roads.convert;
			}
			
			@Override
			protected void renAction() {
				selectedSet(SETT.JOBS().roads.convert);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				
				text.title(¤¤Convert);
				text.text(¤¤ConvertD);
			}
		});
		

		
		placer = new Placer(this, floor.resource, floor.resAmount, floor.desc) {
			
			{
				bs.add(bOverwrite);
			}
			
			@Override
			public void hoverDesc(GBox box) {
				super.hoverDesc(box);
				box.NL(4);
				
				box.textL(STANDINGS.CITIZEN().fullfillment.info().name);
				box.NL();
				int ta = 0;
				for (Race race : RACES.all()) {
					
					box.add(race.appearance().icon.medium);
					box.add(GFORMAT.perc(box.text(), floor.pref(race)));
					box.space();
					if (ta++ > 4) {
						box.NL();
						ta = 0;
					}
						
				}
				box.NL(8);
				
				box.textL(Dic.¤¤Speed);
				box.tab(5);
				box.add(GFORMAT.percInc(box.text(), (floor.speed.movementSpeed-AVAILABILITY.NORMAL.movementSpeed)));
				box.NL();
				
				for (SettEnv e : SETT.ENV().map.all()) {
					if (floor.envValue(e) != 0) {
						box.textL(e.info.name);
						box.tab(5);
						box.add(GFORMAT.perc(box.text(), floor.envValue(e)));
						box.NL();
					}
				}
				box.NL();
				box.textL(¤¤durability);
				box.tab(5);
				box.add(GFORMAT.perc(box.text(), floor.durability));
				
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return bs;
			}
		};
	}
	
	@Override
	public PlacableMulti placer() {
		return placer;
	}

	@Override
	void renderAbove(SPRITE_RENDERER r, int x, int y, int mask, int tx, int ty) {
		for (DIR d: DIR.ORTHO) {
			if (FLOOR().getter.is(tx, ty, d) || JOBS().getter.get(tx, ty, d) == this)
				mask |= d.mask();
		}
		SPRITES.cons().BIG.dashed.render(r, mask, x, y);
		
	}
	
	@Override
	protected CharSequence problem(int tx, int ty, boolean overwrite) {
		
		if (SETT.JOBS().roads.convert) {
			if (SETT.FLOOR().getter.get(tx, ty) == floor)
				return PLACABLE.E;
			if (SETT.FLOOR().getter.get(tx, ty) == null)
				return PLACABLE.E;
		}
		
		if (ROOMS().map.is(tx, ty))
			return PlacableMessages.¤¤ROOM_BLOCK;
		
		if (SETT.FLOOR().getter.get(tx, ty) == floor){
			return PlacableMessages.¤¤ROAD_ALREADY;
		}
		
		if (!overwrite) {
			if (JOBS().getter.is(tx, ty)) {
				return PlacableMessages.¤¤JOB_BLOCK;
			}
		}
		
		if (SETT.TERRAIN().WATER.BRIDGE.is(tx, ty) || SETT.TERRAIN().WATER.DEEP.is(tx, ty))
			return lockText();
		
		if (SETT.PATH().solidity.is(tx, ty))
			return PlacableMessages.¤¤BLOCKED;
		
		if (!SETT.TERRAIN().get(tx, ty).roofIs() && !SETT.TERRAIN().get(tx, ty).clearing().can())
			return PlacableMessages.¤¤BLOCKED;
		
		if (SETT.FLOOR().getter.get(tx, ty) == floor){
			return PlacableMessages.¤¤ROAD_ALREADY;
		}
		
		return lockText();
		
	}
	
	public static CharSequence problem(int tx, int ty) {
		
		if (ROOMS().map.is(tx, ty))
			return PlacableMessages.¤¤ROOM_BLOCK;
		
		if (SETT.FLOOR().getter.get(tx, ty) != null){
			return PlacableMessages.¤¤ROAD_ALREADY;
		}
		
		if (SETT.PATH().solidity.is(tx, ty))
			return PlacableMessages.¤¤BLOCKED;
		
		if (!SETT.TERRAIN().get(tx, ty).roofIs() && !SETT.TERRAIN().get(tx, ty).clearing().can())
			return PlacableMessages.¤¤BLOCKED;

		return null;
	}
	
	@Override
	public CharSequence lockText() {
		Str.TMP.clear().add(Dic.¤¤Requires);
		Str.TMP.NL();
		boolean has = false;
		for (Lock<Faction> i : floor.reqs.all()) {
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
	boolean terrainNeedsClear(int tx, int ty) {
		if (TERRAIN().get(tx, ty).roofIs())
			return false;
		return super.terrainNeedsClear(tx, ty);
	}
	
	@Override
	protected double constructionTime(Humanoid skill) {
		return 25;
	}
	
	private final SoundRace sound = AUDIO.race("BUILD_ROAD");
	
	@Override
	protected SoundRace constructSound() {
		return sound;
	}
	
	@Override
	protected void renderBelow(Renderer r, ShadowBatch shadowBatch, RenderIterator i, int state) {
		if (!FLOOR().getter.is(i.tile()))
			super.renderBelow(r, shadowBatch, i, state);
	}
	
	@Override
	public void doSomethingExtraRender() {
		if (showRoads)
			SETT.OVERLAY().ROADING.add();
	}
	
	@Override
	protected boolean construct(int tx, int ty) {
		if (FLOOR().getter.get(tx, ty) != floor) {
			if (floor.resource != null)
				GAME.player().res().inc(floor.resource, RTYPE.CONSTRUCTION,  -floor.resAmount);
			floor.placeFixed(tx, ty);
			FLOOR().degrade.set(tx, ty, 0);
		}else
			FLOOR().degrade.set(tx, ty, FLOOR().degrade.get(tx, ty)-0.25);
		return FLOOR().degrade.get(tx, ty) != 0;
	}
	
	@Override
	public boolean isConstruction() {
		return true;
	}
	
	@Override
	public TerrainTile becomes(int tx, int ty) {
		return TERRAIN().NADA;
	}

	@Override
	public ToolConfig config() {
		return SETT.JOBS().roads.pla.get(this);
	}
	
	public static Job getPlacable() {
		return SETT.JOBS().roads.pla.current();
	}
	

	
	
}
