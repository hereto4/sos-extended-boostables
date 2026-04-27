package world.map.regions.centre;

import java.io.IOException;

import game.GameDisposable;
import init.constant.C;
import init.paths.PATH;
import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.IInit;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import world.WORLD;
import world.WRenContext;

public class WorldRaceSheet {

	private static KeyMap<TILE_SHEET> map = new KeyMap<TILE_SHEET>();
	private static KeyMap<WallSteriods> mapW = new KeyMap<WallSteriods>();
	
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				map.clear();
				mapW.clear();
			}
		};
	}
	
	public final Town town;
	public final Village village;
	public final WallSteriods walls;
	public final WallSteriods walls_village;
	public final Overlay overlay;
	public final Terrain terrain;
	public final Farm farm;
	
	public WorldRaceSheet(Json json) throws IOException{
		
		PATH getter = PATHS.WORLD().sprite.getFolder("centre");
		
		town = new Town(getter, json);
		village = new Village(getter, json);
		walls = new WallSteriods(6, "WALL", getter, json);
		walls_village = new WallSteriods(4, "WALL_VILLAGE", getter, json);
		overlay = new Overlay(getter, json);
		terrain = new Terrain(getter, json);
		farm = new Farm(getter, json);
	}
	
	
	public static final class Town {
		
		public final TILE_SHEET sheet;
		public final COLOR color; 
		public static final int maxSize = 3;
		
		public Town(PATH getter, Json json) throws IOException {
			String t = "TOWN";
			String f = json.value(t);
			String k = t + "_" + f;
			if (map.containsKey(k))
				sheet = map.get(k);
			else {
				sheet = new ITileSheet(getter.getFolder("town").get(f), 460, 62) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 16, 4, d.s8);
						s.singles.paste(true);
						return d.s8.saveGame();
					}
				}.get();
				map.put(k, sheet);
			}
			color = new ColorImp(json, t + "_COLOR");
			
		}
		
		
		
		public void render(SPRITE_RENDERER r, ShadowBatch s, int size, int ran, int x, int y) {
			size = CLAMP.i(size, 0, maxSize);
			color.bind();
			sheet.render(r, 16*size+(ran&0x0F),x, y);
			sheet.render(s, 16*size+(ran&0x0F),x, y);
			COLOR.unbind();
		}
		
	}
	
	public static final class Village {
		
		public final TILE_SHEET sheet;
		public final COLOR color; 
		public static final int ranI = 16*2-1;
		
		public Village(PATH getter, Json json) throws IOException {
			String t = "VILLAGE";
			String f = json.value(t);
			String k = t + "_" + f;
			if (map.containsKey(k))
				sheet = map.get(k);
			else {
				sheet = new ITileSheet(getter.getFolder("village").get(f), 460, 34) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 16, 2, d.s8);
						s.singles.paste(true);
						return d.s8.saveGame();
					}
				}.get();
				map.put(k, sheet);
			}
			color = new ColorImp(json, t + "_COLOR");
			
		}
		
		
		
		public void render(SPRITE_RENDERER r, ShadowBatch s, int ran, int x, int y) {
			ran &= ranI;
			color.bind();
			sheet.render(r, ran,x, y);
			s.setHeight(1).setDistance2Ground(0);
			sheet.render(s, ran,x, y);
			COLOR.unbind();
		}
		
	}
	
	public static final class Overlay {
		
		public final TILE_SHEET sheet;
		public final COLOR color; 
		public static final int maxSize = 3;
		
		public Overlay(PATH getter, Json json) throws IOException {
			String t = "OVERLAY";
			String f = json.value(t);
			String k = t + "_" + f;
			if (map.containsKey(k))
				sheet = map.get(k);
			else {
				sheet = new ITileSheet(getter.getFolder("overlay").get(f), 236, 34) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 8, 2, d.s8);
						s.singles.paste(true);
						return d.s8.saveGame();
					}
				}.get();
				map.put(k, sheet);
			}
			color = new ColorImp(json, t + "_COLOR");
			
		}
		
		
		
		public void render(WRenContext con, int ran, int x, int y) {
			color.bind();
			sheet.render(con.r, (ran&0x0F),x, y);
			sheet.render(con.s, (ran&0x0F),x, y);
			COLOR.unbind();
		}
		
	}
	
	public static final class WallSteriods {
		private final static int VARS = 5;
		
		
		private final Size[] sizes;

		public final COLOR color; 
		public static final int maxSize = 4;
		private final int sh;
		
		private final SPRITE_RENDERER[] rens = new SPRITE_RENDERER[2];
		
		public WallSteriods(int shadow, String t, PATH getter, Json json) throws IOException {
			String f = json.value(t);
			String k = "WALL" + "_" + f;
			this.sh = shadow;
			color = new ColorImp(json, t + "_COLOR");
			if (mapW.containsKey(k)) {
				sizes = mapW.get(k).sizes;
				return;
			}
			sizes = new Size[VARS];
			
			new IInit(getter.getFolder("wall").get(f), 600, 120) {
				
				@Override
				protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, VARS, 2, 6, 6, d.s8);
					
				}
			};
			
			for (int i = 0; i < VARS; i++)
				sizes[i] = new Size(i);
			mapW.put(k, this);	
		
			
		}

		public void render(WRenContext con, double size, int dim, int cx, int cy) {

			color.bind();
			con.s.setHard();
			con.s.setHeight(sh).setDistance2Ground(0);
			
			rens[0] = con.r;
			rens[1] = con.s;
			Size s = sizes[CLAMP.i((int) Math.round(size*VARS-1), 0, VARS-1)];
			
			int dd = C.TILE_SIZEH;
			int x1 = cx-dim;
			int y1 = cy-dim;
			dim = dim*2-dd;
			int x2 = x1+dim;
			int y2 = y1+dim;
			for (int i = 0; i < rens.length; i++){
				SPRITE_RENDERER r = rens[i];
				{
					int off = i*8;
					double am = (dim-dd*3.0)/(dd*2);
					
					for (int k = 0; k <= am; k++) {
						
						int kk = dd+k*dd;
						
						
						s.walls.render(r, 0+off, x1+kk, y1);
						s.walls.render(r, 1+off, x2-kk, y1);
						
						s.walls.render(r, 2+off, x1, y1+kk);
						s.walls.render(r, 3+off, x2, y1+kk);
//						
						s.walls.render(r, 4+off, x1, y2-kk);
						s.walls.render(r, 5+off, x2, y2-kk);
//						
						s.walls.render(r, 6+off, x1+kk, y2);
						s.walls.render(r, 7+off, x2-kk, y2);
					}
					
					
					
				}
				
				{
					int off = i*4;
					s.corners.render(r, 0+off, x1, y1);
					s.corners.render(r, 1+off, x2, y1);
					s.corners.render(r, 2+off, x1, y2);
					s.corners.render(r, 3+off, x2, y2);
				}
				{
					int off = i*8;
					s.gate.render(r, 0+off, cx-dd, y1);
					s.gate.render(r, 1+off, cx, y1);
					s.gate.render(r, 2+off, x1, cy-dd);
					s.gate.render(r, 3+off, x1, cy);
					s.gate.render(r, 4+off, x2, cy-dd);
					s.gate.render(r, 5+off, x2, cy);
					s.gate.render(r, 6+off, cx-dd, y2);
					s.gate.render(r, 7+off, cx, y2);
				}
				
			}
			{
				
				
			}
			
			
			
			
		}
		
		private static class Size {
			
			private final TILE_SHEET corners;
			private final TILE_SHEET gate;
			private final TILE_SHEET walls;
			
			Size(int var) throws IOException{
				corners = new ITileSheet() {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						final ComposerSources.Full f = s.full;
						for (int i = 0; i <= 1; i++) {
							f.setVar(var+VARS*i);
							f.setSkip(1, 0).paste(true);
							f.setSkip(1, 5).paste(true);
							f.setSkip(1, 30).paste(true);
							f.setSkip(1, 35).paste(true);
						}
						
						return d.s8.saveGame();
						
					}
				}.get();
				
				gate = new ITileSheet() {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						final ComposerSources.Full f = s.full;
						for (int i = 0; i <= 1; i++) {
							f.setVar(var+VARS*i);
							f.setSkip(1, 2).paste(true);
							f.setSkip(1, 3).paste(true);
							f.setSkip(1, 12).paste(true);
							f.setSkip(1, 18).paste(true);
							f.setSkip(1, 17).paste(true);
							f.setSkip(1, 23).paste(true);
							f.setSkip(1, 32).paste(true);
							f.setSkip(1, 33).paste(true);
						}
						
						return d.s8.saveGame();
						
					}
				}.get();
				
				walls = new ITileSheet() {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						final ComposerSources.Full f = s.full;
						for (int i = 0; i <= 1; i++) {
							f.setVar(var+VARS*i);
							f.setSkip(1, 1).paste(true);
							f.setSkip(1, 4).paste(true);
							f.setSkip(1, 6).paste(true);
							f.setSkip(1, 11).paste(true);
							f.setSkip(1, 24).paste(true);
							f.setSkip(1, 29).paste(true);
							f.setSkip(1, 31).paste(true);
							f.setSkip(1, 34).paste(true);
						}
						
						return d.s8.saveGame();
						
					}
				}.get();
			}
			
		}
		
	}
	

	
	public static final class Terrain {
		
		public final TILE_SHEET sheet;
		public final COLOR color; 
		
		public Terrain(PATH getter, Json json) throws IOException {
			String t = "TERRAIN";
			String f = json.value(t);
			String k = t + "_" + f;
			if (map.containsKey(k))
				sheet = map.get(k);
			else {
				sheet = new ITileSheet(getter.getFolder("terrain").get(f), 152, 76) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						ComposerDests.Tile t = d.s16;
						final ComposerSources.Full f = s.full;
						f.init(0, 0, 1, 1, 4, 4, t);
						f.paste(true);
						return t.saveGame();
					}
				}.get();
				map.put(k, sheet);
			}
			color = new ColorImp(json, t + "_COLOR");
		}

		public void render(WRenContext con, int mask, int ran, int x, int y) {
			color.bind();
			int tile = ran &0x0F;
			WORLD.BUILDINGS().sprites.terrainStencil.renderTextured(sheet.getTexture(tile), mask, x, y);
			COLOR.unbind();
		}
		
	}
	
	public static final class Farm {
		
		public final TILE_SHEET sheet;
		public final LIST<ColorImp> color; 
		
		public Farm(PATH getter, Json json) throws IOException {
			String t = "FARM";
			String f = json.value(t);
			String k = t + "_" + f;
			if (map.containsKey(k))
				sheet = map.get(k);
			else {
				sheet = new ITileSheet(getter.getFolder("farm").get(f), 364, 50) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 8, 2, d.s16);
						s.singles.paste(true);
						return d.s16.saveGame();
					}
				}.get();
				map.put(k, sheet);
			}
			color = ColorImp.cols(json, t + "_COLOR");
		}

		public void render(WRenContext con, int ran, int x, int y) {
			color.getC(ran).bind();
			int tile = (ran>>8) &0x0F;
			sheet.render(con.r, tile, x, y);
			COLOR.unbind();
		}
		
	}
	
	
	
}
