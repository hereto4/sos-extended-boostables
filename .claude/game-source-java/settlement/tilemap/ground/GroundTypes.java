package settlement.tilemap.ground;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.util.color.COLOR;
import snake2d.util.file.Json;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerUtil;
import util.text.D;

public class GroundTypes {

	private static CharSequence ¤¤nnmae = "Rich";
	private static CharSequence ¤¤ndesc = "Richest of soils, very suitable for farming.";
	private static CharSequence ¤¤fnmae = "Alluvium";
	private static CharSequence ¤¤fdesc = "Trees will grow here, but soil is poor for agriculture. Suitable for woodcutters and orchards.";
	private static CharSequence ¤¤pnmae = "Till";
	private static CharSequence ¤¤pdesc = "Not the best soil, but all right for agriculture.";
	private static CharSequence ¤¤rnmae = "Rock";
	private static CharSequence ¤¤rdesc = "Can not be cultivated";
	private static CharSequence ¤¤snmae = "Sand";
	private static CharSequence ¤¤sdesc = "Sand is devoid of nutrients and can hardly be cultivated.";
	
	private static CharSequence ¤¤poor = "Infertile";
	private static CharSequence ¤¤poorD = "Infertile soil is devoid of nutrients and hard to work.";
	
	static {
		D.ts(GroundTypes.class);
	}
	
	public final static int VARS = 16*4;
	final TILE_SHEET s_masks;
	final TILE_SHEET c_masks;
	public final GroundType NORMAL;
	public final GroundType FOREST;
	public final GroundType PASTURE;
	public final GroundType SAND;
	public final GroundType ROCK;
	public final GroundType INFERTILE;
	public final GroundType[] ALL;
	
	GroundTypes() throws IOException{
		
		s_masks = new ComposerThings.ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Ground"), 536, 408) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, 0, 1, 1, 8, 1, d.s16);
				s.full.setSkip(8, 0);
				s.full.paste(true);
				s.full.pasteRotated(1, true);
				s.full.pasteRotated(2, true);
				s.full.pasteRotated(3, true);
				return d.s16.saveGame();
			}
		}.get();;
		c_masks = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, 0, 1, 1, 16, 1, d.s16);
				s.full.setSkip(8, 8);
				s.full.paste(true);
				s.full.pasteRotated(1, true);
				s.full.pasteRotated(2, true);
				s.full.pasteRotated(3, true);
				return d.s16.saveGame();
			}
		}.get();;
		
		TILE_SHEET s_normal = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		TILE_SHEET s_rock = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		TILE_SHEET s_sand = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		TILE_SHEET s_tree = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();

		TILE_SHEET s_pasture = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		TILE_SHEET s_infertile = new ComposerThings.ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, s.full.body().y2(), 1, 1, 16, 4, d.s16);
				s.full.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
		Json j = new Json(PATHS.CONFIG().get("SettColors"));
		
		NORMAL = new GroundType(0, s_normal, ¤¤nnmae, ¤¤ndesc, 1, 1.1);
		FOREST = new GroundType(1, s_tree, ¤¤fnmae, ¤¤fdesc, 1, 0.75);
		PASTURE = new GroundType(2, s_pasture, ¤¤pnmae, ¤¤pdesc, 1, 0.9);
		INFERTILE = new GroundType(3, s_infertile, ¤¤poor, ¤¤poorD, 0.25, 0.5);
		ROCK = new GroundType(4, s_rock, ¤¤rnmae, ¤¤rdesc, 1, 0.4).setColors(j.json("GROUND_ROCK"));
		SAND = new GroundType(5, s_sand, ¤¤snmae, ¤¤sdesc, 0.1, 0.4).setColors(j.json("GROUND_SAND"));
		
		
		ROCK.miniC.set(COLOR.WHITE25);
		ALL = new GroundType[] {
			NORMAL,
			FOREST,
			PASTURE,
			INFERTILE,
			ROCK,
			SAND
		};
		
	}
	


	
}
