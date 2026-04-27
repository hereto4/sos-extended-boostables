package world.map.terrain;

import java.io.IOException;

import init.constant.C;
import init.paths.PATHS;
import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TileTexture.TileTextureScroller;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

final class WorldWaterSprites{

	public final TILE_SHEET bg = (new ITileSheet(PATHS.SPRITE_WORLD_MAP().get("Water"), 576, 272) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house.init(0, 0, 4, 1, t);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return t.saveGame();

		}
	}).get();
	
	public final TILE_SHEET bgSingles = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
			s.full.paste(true);
			return d.s16.saveGame();
		}
	}).get();
	
	public final TILE_SHEET sheet = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			
			s.house.init(0, s.full.body().y2(), 4, 1, d.s16);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return d.s16.saveGame();

		}
	}).get();
	
	public final TILE_SHEET sheetCorners = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.house.setVar(0).setSkip(0, 1).pasteEdges(true);
			return d.s16.saveGame();

		}
	}).get();
	
	public final TILE_SHEET sheetSingles = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
			s.full.paste(true);
			return d.s16.saveGame();
		}
	}).get();
	
	public final TILE_SHEET deep = (new ITileSheet() {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.house.init(0, s.full.body().y2(), 4, 1, d.s16);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return d.s16.saveGame();
		}
	}).get();
	
	public final TILE_SHEET riverBG = (new ITileSheet(PATHS.SPRITE_WORLD().getFolder("map").get("RiverBig"), 576, 172) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house.init(0, 0, 4, 1, t);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return t.saveGame();

		}
	}).get();
	
	public final TILE_SHEET riverFG = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house.init(0, s.house.body().y2(), 4, 1, t);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return t.saveGame();

		}
	}).get();
	
	public final TILE_SHEET deltaShore = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.singles.init(0, s.house.body().y2(), 1, 1, 4, 1, t);
			for (int i = 0; i < 4; i++) {
				s.singles.setSkip(i, 1).paste(3, true);
			}
			return t.saveGame();
		}
	}).get();
	
	public final TILE_SHEET delta = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.singles.init(s.singles.body().x2(),s.singles.body().y1(), 1, 1, 4, 1, t);
			for (int i = 0; i < 4; i++) {
				s.singles.setSkip(i, 1).paste(3, true);
			}
			return t.saveGame();
		}
	}).get();
	
	public final TILE_SHEET riverSmallBG = (new ITileSheet(PATHS.SPRITE_WORLD().getFolder("map").get("RiverSmall"), 576, 144) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house.init(0, 0, 4, 1, t);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return t.saveGame();

		}
	}).get();
	
	public final TILE_SHEET riverSmallFG = (new ITileSheet() {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			s.house.init(0, s.house.body().y2(), 4, 1, t);

			for (int i = 0; i < 4; i++)
				s.house.setVar(i).paste(1, true);
			return t.saveGame();

		}
	}).get();
	
	public WorldWaterSprites() throws IOException {
		// TODO Auto-generated constructor stub
	}



	public void renderBackground(SPRITE_RENDERER r, RenderIterator it, int rot, int corner) {
		if (rot == 0) {
			bgSingles.render(r, it.ran()&0x0F, it.x(), it.y());
		}else if (rot != 0x0F && corner != 0x0F){
			bg.render(r, 16*(it.ran()&0b0111)+rot, it.x(), it.y());
		}
	}
	
	public void render(SPRITE_RENDERER r, RenderIterator it, int rot, int corner) {
		if (rot == 0) {
			sheetSingles.render(r, it.ran()&0x0F, it.x(), it.y());
		}else {
			sheet.render(r, 16*(it.ran()&0b0111)+rot, it.x(), it.y());
			sheetCorners.render(r, corner, it.x(), it.y());
		}
		renderTexture(it);
	}
	
	public void renderDeep(SPRITE_RENDERER r, RenderIterator it, int rot) {
		deep.render(r, 16*(it.ran()&0b011)+rot, it.x(), it.y());
		renderTexture(it);
	}
	
	
	private final TileTextureScroller dis1 = SPRITES.textures().dis_tiny.scroller(1.5, 1.5);
	private final TileTextureScroller dis2 = SPRITES.textures().dis_small.scroller(-2, -2);
	private final TileTextureScroller tex1 = SPRITES.textures().water.scroller(-1.0, 1);
	private final TileTextureScroller tex2 = SPRITES.textures().bumps.scroller(2, 2);
	private final OpacityImp o1 = new OpacityImp((int) (255 * 0.1));
	private final OpacityImp o2 = new OpacityImp((int) (255 * 0.2));
	
	void update(double ds) {
		dis1.update(ds);
		dis2.update(ds);
		tex1.update(ds);
		tex2.update(ds);

	}

	public void renderTexture(RenderData.RenderIterator i) {

		o2.bind();

		
		COLOR c = CORE.renderer().colorGet();
		COLOR.unbind();
		
		CORE.renderer().renderDisplace(
				dis1.x1(i.tx()), dis1.y1(i.ty()), tex1.x1(i.tx()), tex1.y1(i.ty()), 16, 16, 8, i.x(), i.x() + C.TILE_SIZE, i.y(), i.y() + C.TILE_SIZE);
		
		c.bind();
		o1.bind();
		CORE.renderer().renderDisplace(
				dis2.x1(i.tx()), dis2.y1(i.ty()), tex2.x1(i.tx()), tex2.y1(i.ty()), 16, 16, 4, i.x(), i.x() + C.TILE_SIZE, i.y(), i.y() + C.TILE_SIZE);
//		d = dis1.get(i.tx() + 4, i.ty() + 4);
//		c = tex1.get(i.tx() + 4, i.ty() + 4);
//		
//		
//		CORE.renderer().renderDisplaced(i.x(), i.x() + C.TILE_SIZE, i.y(), i.y() + C.TILE_SIZE, d, c);
//
//		o2.bind();
//		d = dis2.get(i.tx(), i.ty());
//		c = tex2.get(i.tx(), i.ty());
//		CORE.renderer().renderDisplaced(i.x(), i.x() + C.TILE_SIZE, i.y(), i.y() + C.TILE_SIZE, d, c);

		OPACITY.unbind();
	}
	
}
