package init.sprite.UI;

import java.io.IOException;

import init.constant.C;
import init.paths.PATHS;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.IInit;
import util.spritecomposer.ComposerThings.ISprite;
import util.spritecomposer.ComposerThings.ISpriteData;
import util.spritecomposer.ComposerThings.ISpriteList;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import util.spritecomposer.SpriteData;

public class UIConses{
	
	{
		new IInit(PATHS.SPRITE_UI().get("Cons"), 1008, 328) {
			
			@Override
			protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException {
				s.singles.init(0, 144, 1, 1, 32, 1, d.s16);
			}
		};
	}
	
	public final Small TINY = new Small();
	public final Big BIG = new Big();
	
	public final Icons ICO = new Icons();
	public final Rotaters ROT = new Rotaters();
	public final TILE_SHEET fullArrows = new ITileSheet() {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.singles.init(0, s.singles.body().y2(), 1, 1, 1, 1, d.s16);
			s.singles.paste(3, true);
			s.combo.init(s.singles.body().x2(), s.singles.body().y1(), 1, 1, 2, d.s16);
			s.combo.paste(3, true);
			s.combo.init(s.combo.body().x2(), s.singles.body().y1(), 1, 1, 3, d.s16);
			s.combo.paste(3, true);
			return d.s16.saveGame();

		}
	}.get();
	
	
	

	
	public UIConses() throws IOException{
		
	}

	
	public static final class Small {
		
		public final UICons high = new UICons(new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.house.init(0, 0, 7, 1, d.s8);
				s.house.setVar(0).paste(true);
				return d.s8.save(C.SCALE*2);
			}
		}.get());
		public final UICons low = getTiny(1);
		public final UICons flat = getTiny(2);
		public final UICons outline = getTiny(3);
		public final UICons dashed = getTiny(4);
		public final UICons full = getTiny(5);
		public final UICons dots = getTiny(6);
		private Small() throws IOException{
			
		}
		
		
		private UICons getTiny(int nr) throws IOException {
			
			return new UICons(new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.setVar(nr).paste(true);
					return d.s8.save(C.SCALE*2);
				}
			}.get());
			
		}
		
	}
	
	public final class Big {
		
		public final UICons outline = new UICons(new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				
				s.house.init(0, s.house.body().y2(), 7, 2, d.s16);
				s.singles.init(0, s.house.body().y2(), 1, 1, 16, 1, d.s16);
				s.house.setVar(0).paste(true);
				s.singles.setSkip(0, 1);
				s.singles.pasteEdges(true);
				return d.s16.saveGame();
			}
		}.get(), TINY.outline);
		public final UICons dashed = getSmall(1, TINY.dashed);
		public final UICons dashedThick = getSmall(2, TINY.dashed);
		public final UICons solid = getSmall(3, TINY.full);
		public final UICons dots = getSmall(4, TINY.dots);
		public final UICons line = getSmall(5, TINY.dashed);
		public final UICons dashed_hollow = getSmall(7, TINY.dashed);

		public final UICons filled = getSmall(9, TINY.full);
		//
		public final UICons filled_striped = getSmall(11, TINY.full);
		
		private Big() throws IOException{
			
		}
		
		private UICons getSmall(int nr, UICons tiny) throws IOException {
			
			return new UICons(new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.house.setVar(nr).paste(true);
					s.singles.setSkip(nr, 1);
					s.singles.pasteEdges(true);
					return d.s16.saveGame();
				}
				
				
			}.get(), tiny);
			
		}
	}
	

	
	public final class Icons {
		
		public final SPRITE unclear = ISprite.game(new ISpriteData() {

			@Override
			protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, s.singles.body().y2(), 1, 1, 19, 1, d.s16);
				s.singles.setSkip(0, 1).paste(true);
				return d.s16.saveSprite();
			}
			
		}.get()); 
		public final SPRITE clear = getS(1);
		public final SPRITE cancel = getS(2);

		
		public final LIST<SPRITE> arrows = ISprite.game(new ISpriteList() {

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(3, 1).pasteRotated(i, true);
				return d.s16.saveSprite();
			}

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				return 4;
			}
			
		}.get()); 
		public final LIST<SPRITE> arrows2 = ISprite.game(new ISpriteList() {

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(4+ (i & 1), 1).pasteRotated(i/2, true);
				return d.s16.saveSprite();
			}

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				return 8;
			}
			
		}.get());
		public final SPRITE crosshair = getS(6);
		public final SPRITE smallup = getS(7);
		public final SPRITE repair = getS(8);
		public final SPRITE arrows_inward = getS(9);
		public final SPRITE warning = getS(10);
		public final SPRITE tile = getS(11);
		public final SPRITE scratch = getS(12);
		public final LIST<SPRITE> arrows_inwards = ISprite.game(new ISpriteList() {

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(13, 1).pasteRotated(i, true);
				return d.s16.saveSprite();
			}

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				return 4;
			}
			
		}.get()); 
		public final LIST<SPRITE> arrows_entity; 
		
		private Icons() throws IOException{
			LIST<SPRITE> li = ISprite.game(new ISpriteList() {

				@Override
				protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.setSkip(14, 1).pasteRotated(i, true);
					return d.s16.saveSprite();
				}

				@Override
				protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return 4;
				}
				
			}.get());
			LIST<SPRITE> l2 = ISprite.game(new ISpriteList() {

				@Override
				protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.setSkip(14, 1).pasteRotated(i, true);
					return d.s16.saveSprite();
				}

				@Override
				protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return 4;
				}
				
			}.get()); 
			ArrayList<SPRITE> ea = new ArrayList<SPRITE>(8);
			for (int i = 0; i < 4; i++) {
				ea.add(li.get(i));
				ea.add(l2.get(i));
			}
			arrows_entity = ea;
			
		}
		
		private SPRITE getS(int nr) throws IOException {
			return ISprite.game(new ISpriteData() {

				@Override
				protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.setSkip(nr, 1).paste(true);
					return d.s16.saveSprite();
				}
				
			}.get()); 
			
		}
		
		
	}
	
	public final class Rotaters {
		
		public final LIST<SPRITE> single = ISprite.game(new ISpriteList() {

			@Override
			protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(0, 1).pasteRotated(i, true);
				return d.s16.saveSprite();
			}

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, s.singles.body().y2(), 1, 1, 8, 1, d.s16);
				return 4;
			}
			
		}.get()); 
		public final LIST<SPRITE> join = getS(1);
		public final LIST<SPRITE> join_thin = getS(2);
		public final LIST<SPRITE> north_south = getS(3);
		public final LIST<SPRITE> full = getS(4);
		public final LIST<SPRITE> join_big = getS(5);

		

		
		private Rotaters() throws IOException{
			
		}
		
		private LIST<SPRITE> getS(int nr) throws IOException {
			return ISprite.game(new ISpriteList() {

				@Override
				protected SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.setSkip(nr, 1).pasteRotated(i, true);
					return d.s16.saveSprite();
				}

				@Override
				protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					return 4;
				}
				
			}.get()); 
			
		}
		
		
	}
	
}