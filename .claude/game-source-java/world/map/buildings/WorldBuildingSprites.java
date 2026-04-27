
package world.map.buildings;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public class WorldBuildingSprites {

	private final PATH getter = PATHS.SPRITE().getFolder("world").getFolder("map").getFolder("buildings");
//	
//	public final TILE_SHEET houses = new ITileSheet(getter.getFolder("houses").get("Normal"), 460, 62) {
//		
//		@Override
//		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
//			s.singles.init(0, 0, 1, 1, 16, 4, d.s8);
//			s.singles.paste(true);
//			return d.s8.saveGame();
//		}
//	}.get();
	
//	public final TILE_SHEET village = new ITileSheet(getter.getFolder("houses").get("Village"), 460, 34) {
//		
//		@Override
//		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
//			s.singles.init(0, 0, 1, 1, 16, 2, d.s8);
//			s.singles.paste(true);
//			return d.s8.saveGame();
//		}
//	}.get();
//	public final TILE_SHEET farms = new ITileSheet(getter.get("Farms"), 364, 50) {
//		
//		@Override
//		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
//			s.singles.init(0, 0, 1, 1, 8, 2, d.s16);
//			s.singles.paste(true);
//			return d.s16.saveGame();
//		}
//	}.get();
	public final TILE_SHEET garrison = new ITileSheet(getter.get("Garrison"), 236, 20) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, 0, 1, 1, 8, 1, d.s8);
			s.singles.paste(3, true);
			return d.s8.saveGame();
		}
	}.get();
	
	public final TILE_SHEET terrainStencil = new ITileSheet(getter.get("TerrainStencil"), 120, 60) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(0, 0, 1, 1, 3, 3, d.s16);
			s.full.paste(true);
			return d.s16.saveGame();
		}
	}.get();
	
	public final TILE_SHEET mines = new ITileSheet(getter.get("Mines"), 364, 100) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, 0, 1, 1, 8, 1, d.s16);
			s.singles.paste(true);
			return d.s16.saveGame();
		}
	}.get();
	
	public final TILE_SHEET roads = new ITileSheet(getter.get("Roads"), 576, 172) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.house.init(0, 0, 4, 2, d.s16);
			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return d.s16.saveGame();
		}
	}.get();
	
	public final TILE_SHEET roadsMini = new ITileSheet() {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.house.init(0, 0, 4, 2, d.s16);
			for (int i = 0; i < 4; i++)
				s.house.setVar(4+i).paste(1, true);
			return d.s16.saveGame();
		}
	}.get();
	
	public final TILE_SHEET bridge = new ITileSheet() {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, s.house.body().y2(), 1, 1, 3, 1, d.s16);
			for (int i = 0; i < 3; i++)
				s.singles.setSkip(i, 1).paste(3, true);
			return d.s16.saveGame();
		}
	}.get();
	
//	public final TILE_SHEET wallCity = new ITileSheet(getter.get("Wall"), 600, 120) {
//		
//		@Override
//		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
//			ComposerDests.Tile t = d.s16;
//			final ComposerSources.Full f = s.full;
//			f.init(0, 0, 5, 2, 3, 3, t);
//			for (int i = 0; i < 10; i++)
//				f.setVar(i).paste(true);
//			return t.saveGame();
//			
//		}
//	}.get();
//	
//	public final TILE_SHEET wallTown = new ITileSheet(getter.get("WallTown"), 616, 44) {
//		
//		@Override
//		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
//			ComposerDests.Tile t = d.s16;
//			final ComposerSources.Full f = s.full;
//			f.init(0, 0, 7, 1, 2, 2, t);
//			for (int i = 0; i < 7; i++)
//				f.setVar(i).paste(true);
//			return t.saveGame();
//		}
//	}.get();
	
	
	public final TILE_SHEET siege = new ITileSheet(getter.get("Siege"), 120, 60) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			final ComposerSources.Full f = s.full;
			f.init(0, 0, 1, 1, 3, 3, t);
			f.paste(true);
			return t.saveGame();
			
		}
	}.get();

	
	public final TILE_SHEET harbour = new ITileSheet(getter.get("Harbour"), 252, 220) {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, 0, 1, 1, 4, 4, d.s24);
			s.singles.paste(3, true);
			return d.s24.saveGame();
		}
	}.get();

	public final TILE_SHEET harbourRiver = new ITileSheet() {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, s.singles.body().y2(), 1, 1, 4, 4, d.s16);
			s.singles.paste(3, true);
			return d.s16.saveGame();
		}
	}.get();
	
	WorldBuildingSprites() throws IOException {
		
		
		
	}
	
	
	
}

