package settlement.misc.placers;

import static settlement.main.SETT.HUMANOIDS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TERRAIN;
import static settlement.misc.placers.Tiles.__;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.FCredits;
import init.paths.PATHS;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.type.CAUSE_ARRIVES;
import init.type.HTYPES;
import init.type.POP_CL;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.placers.Tiles.Conpound;
import settlement.stats.STATS;
import settlement.tilemap.terrain.TBuilding;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import view.main.VIEW;

final class PlacerLanding{

	
	static Placer get() {
		
		TBuilding b = SETT.TERRAIN().BUILDINGS.MUD;
		if (b == null)
			b = TERRAIN().BUILDINGS.all().get(0);
		
		Tile ww = new Tiles.Terrain(b.wall) {
			@Override
			public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
				if (TERRAIN().CAVE.is(tx, ty)) {
					TERRAIN().MOUNTAIN.placeFixed(tx, ty);
				}else
					super.place(tx, ty, grid, rx, ry);
			}
		};
		Tile roof = new Tiles.Terrain(b.roof) {
			@Override
			public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
				if (TERRAIN().CAVE.is(tx, ty))
					return;
				super.place(tx, ty, grid, rx, ry);
			}
		};
		
		
		//Tile ff = new Tiles.Floor(FLOOR().defaultRoad);
		Tile rr = new Tiles.Conpound(roof);
		
		Tile throne = new Tile() {
			@Override
			public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
				return SPRITES.cons().ICO.cancel;
			}
			
			@Override
			public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
				ROOMS().THRONE.init.place(tx, ty, 2);
				
				FACTIONS.player().credits().inc((int) (5000*BOOSTABLES.CIVICS().LANDING.get(POP_CL.clP(null, null))), FCredits.CTYPE.MISC);
				
			}
			
			@Override
			public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
				return ROOMS().THRONE.init.placableTile(tx, ty);
			}
		};
		Tile thone2 = new Tile() {
			@Override
			public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
				return SPRITES.cons().ICO.cancel;
			}
			
			@Override
			public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
				
			}
			
			@Override
			public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
				return ROOMS().THRONE.init.placableTile(tx, ty);
			}
		};
		
		
		Tile th = new Conpound(roof,throne);
		Tile to = new Conpound(roof,thone2);
		Json j = new Json(PATHS.INIT().getFolder("config").get("LandingParty")).json("RESOURCES");
		LIST<String> keys = j.keys();
		
		Tile R1 = new Conpound(rr, new Resource(j, keys, 0));
		Tile R2 = new Conpound(rr, new Resource(j, keys, 1));
		Tile R3 = new Conpound(rr, new Resource(j, keys, 2));
		Tile R4 = new Conpound(rr, new Resource(j, keys, 3));
		Tile R5 = new Conpound(rr, new Resource(j, keys, 4));
		Tile R6 = new Conpound(rr, new Resource(j, keys, 5));
		Tile R7 = new Conpound(rr, new Resource(j, keys, 6));
		Tile R8 = new Conpound(rr, new Resource(j, keys, 7));
		Tile R9 = new Conpound(rr, new Resource(j, keys, 8));
		Tile RA = new Conpound(rr, new Resource(j, keys, 9));
		

		Tile dd = new Tile() {
			
			private int am = -1;
			private int ePerTile = -1;
			@Override
			public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
				return SPRITES.cons().ICO.cancel;
			}
			
			@Override
			public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
				if (STATS.POP().POP.data(null).get(null) == 0) {
					am = 10 + (int) (10*BOOSTABLES.CIVICS().LANDING.get(POP_CL.clP(null, null)));
					
					ePerTile = (int) Math.ceil((am)/10.0);
				}
				for (int i = 0; i < ePerTile; i++) {
					if (am <= 0)
						continue;
					Humanoid h = HUMANOIDS().create(FACTIONS.player().race(), tx, ty, HTYPES.SUBJECT(), CAUSE_ARRIVES.IMMIGRATED());
					STATS.POP().TYPE.IMMIGRANT.set(h.indu());
					am--;
				}
				VIEW.messages().hide();
				
			}
			
			@Override
			public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
				return !PATH().solidity.is(tx, ty);
			}
		};
		
		
		Tile[][] grid = new Tile[][] {
			{ww,ww,ww,ww,ww,ww,ww,ww,ww},
			{ww,rr,th,to,to,to,to,rr,ww},
			{ww,rr,to,to,to,to,to,rr,ww},
			{ww,rr,to,to,to,to,to,rr,ww},
			{ww,rr,R7,R3,R1,R2,R8,rr,ww},
			{ww,rr,R9,R5,R4,R6,RA,rr,ww},
			{ww,rr,rr,rr,rr,rr,rr,rr,ww},
			{ww,ww,ww,rr,rr,rr,ww,ww,ww},
			{__,dd,__,__,__,__,__,dd,__},
			{__,dd,dd,__,__,__,dd,dd,__},
			{__,dd,dd,__,__,__,dd,dd,__},
			{__,__,__,__,__,__,__,__,__},
		};
		
		return new Placer("Landing Party", new TileGrid(grid));
		

	}
	
	private static class Resource implements Tile {

		final RESOURCE r;
		final int amount;
		
		Resource(Json j, LIST<String> keys, int index){
			if (index >= keys.size()) {
				this.r = null;
				this.amount = 0;
			}else {
				r = RESOURCES.map().get(keys.get(index), j);
				amount = j.i(keys.get(index), 1, 500);
			}
			
		}

		
		@Override
		public boolean placable(int tx, int ty, TileGrid grid, int rx, int ry) {
			return true;
		}

		@Override
		public void place(int tx, int ty, TileGrid grid, int rx, int ry) {
			if (r != null)
				SETT.THINGS().resources.createPrecise(tx, ty, r, amount + (int)(amount*BOOSTABLES.CIVICS().LANDING.get(POP_CL.clP(null, null))));
		}
		
		@Override
		public SPRITE sprite(TileGrid grid, int rx, int ry, int mask) {
			return SPRITES.cons().ICO.clear;
		}
		
	}
	
}
