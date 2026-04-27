package settlement.entity.animal;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.PHEIGHT;
import static settlement.main.SETT.PWIDTH;

import java.io.IOException;

import game.debug.Profiler;
import init.constant.C;
import init.paths.PATH;
import init.paths.PATHS;
import init.resources.RESOURCE;
import settlement.entity.ENTITY;
import settlement.entity.animal.spawning.AnimalSpawning;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.room.food.pasture.ROOM_PASTURE;
import snake2d.Errors;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.RMAPS;
import util.rendering.ShadowBatch;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PlacableSimple;
import view.tool.PlacableSimpleTile;

public final class Animals extends SettResource{

	public final AnimalSpawning spawn;
	public RMAPS<AnimalSpecies> map;
	public final LIST<AnimalSpecies> species;
	public final ArrayListGrower<AnimalSpecies> caravans = new ArrayListGrower<AnimalSpecies>();
	private LIST<AnimalSpecies> sett;
	final Sprites sprites;
	
	
	public Animals() throws IOException {
		super("ANIMALS", true);
		
		ArrayListGrower<AnimalSpecies> all = new ArrayListGrower<AnimalSpecies>();
		
		PATH gData = PATHS.INIT().getFolder("animal");
		PATH gText = PATHS.TEXT().getFolder("animal");
		KeyMap<TILE_SHEET> sprites = new KeyMap<>();
		for (String key : gData.getFiles()) {
			Json data = new Json(gData.get(key));
			Json text = new Json(gText.get(key));
			
			AnimalSpecies s = new AnimalSpecies(key, all.size(), data, text, sprites);
			all.add(s);
		}
		
		this.species = all;
		map = new RMAPS<AnimalSpecies>("ANIMAL", all);
		spawn = new AnimalSpawning(this);
		this.sprites = new Sprites();
		
		PLACABLE death = new PlacableSimple("kill animals") {
			
			@Override
			public void place(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Animal) {
						((Animal) e).kill(false, false);
						return;
					}
				}
			}
			
			@Override
			public CharSequence isPlacable(int x, int y) {
				for (ENTITY e : ENTITIES().getAtPointL(x, y)) {
					if (e instanceof Animal) {
						return null;
					}
				}
				return E;
			}
		};
		
		IDebugPanelSett.add(death);
		
		for (AnimalSpecies s : species) {
			PlacableSimple p = new PlacableSimple(s.name) {
				
				@Override
				public void place(int x, int y) {
					new Animal(x, y, s, null);
					
				}
				
				@Override
				public CharSequence isPlacable(int x, int y) {
					return Animals.this.isPlacable(s, x, y) ? null : E;
				}
			};
			IDebugPanelSett.add("animal", p);
			if (s.caravanable)
				caravans.add(s);
		}
		
		if (caravans.isEmpty())
			throw new Errors.DataError("No animals can be caravans");
		
		PlacableSimpleTile p = new PlacableSimpleTile("control animal") {
			
			@Override
			public void place(int tx, int ty) {
				for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
					if (e instanceof Animal) {
						((Animal) e).setState(State.CONTROLLED, 1);
						e.physics.setMass(500);
					}
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
					if (e instanceof Animal)
						return null;
				}
				return E;
			}
		};
		
		IDebugPanelSett.add("animal", p);
	}

	@Override
	protected void save(FilePutter saveFile) {
		spawn.saver.save(saveFile);
		
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		spawn.update(ds);
	}

	@Override
	protected void load(FileGetter saveFile) throws IOException {
		sett = exists(SETT.WORLD_AREA());
		spawn.saver.load(saveFile);
	}
	
	@Override
	protected void generate(CapitolArea area) {
		sett = exists(area);
		
		if (!area.isBattle)
			spawn.generate(this, area);
	}
	
	@Override
	protected void clear() {
		spawn.saver.clear();
	}
	
	public LIST<AnimalSpecies> sett(){
		return sett;
	}
	
	private LIST<AnimalSpecies> exists(CapitolArea area) {

		ArrayList<AnimalSpecies> res = new ArrayList<>(species.size());
		for (AnimalSpecies s : species) {
			if (exists(s, area))
				res.add(s);
		}
		return res;
	}
	
	private boolean exists(AnimalSpecies s, CapitolArea area) {
		for (ROOM_PASTURE p : SETT.ROOMS().PASTURES) {
			if (p.species == s && !p.isAvailable(area.climate()))
				return false;
		}
		return true;
	}
	
	public void renderCaravan(SPRITE_RENDERER r, ShadowBatch s, double movement, int cx, int cy, RESOURCE res, int resAmount, boolean inWater, int dir, int ran) {
		Sprite.renderCaravan(r, s, movement, cx, cy, res, resAmount, inWater, dir, ran);
	}
	
	public void renderMount(AnimalSpecies sp, SPRITE_RENDERER r, ShadowBatch s, double movement, int cx, int cy, boolean inWater, int dir, int ran) {
		Sprite.renderMount(sp, r, s, movement, cx, cy, inWater, dir, ran);
	}
	
	public void renderCorpse(AnimalSpecies s, Renderer r, ShadowBatch shadows, float ds, int x, int y, int state, int rot, int ran, double statef, COLOR decay) {
		Sprite.renderCorpse(s, r, shadows, ds, x, y, state, rot, ran, statef, decay);
	}
	
//	public Animal place(AnimalSpecies s, int x, int y) {
//		if (isPlacable(s, x, y)){
//			return new Animal(x,y,s);
//		}
//		return null;
//	}
	
	public boolean isPlacable(AnimalSpecies s, int x, int y) {
		int x1 = (x-s.hitboxSize/2);
		int x2 = (x+s.hitboxSize/2);
		int y1 = (y-s.hitboxSize/2);
		int y2 = (y+s.hitboxSize/2);
		if (x1 < 0 || x2 >= PWIDTH || y1 < 0 || y2 >= PHEIGHT)
			return false;
		x1 /= C.TILE_SIZE;
		x2 /= C.TILE_SIZE;
		y1 /= C.TILE_SIZE;
		y2 /= C.TILE_SIZE;
		return !PATH().solidity.is(x1, y1) && !PATH().solidity.is(x2, y1) &&
				!PATH().solidity.is(x1, y2) && !PATH().solidity.is(x2, y2) &&
				ENTITIES().getAtPoint(x,y) == null;
	}
	

	
}
