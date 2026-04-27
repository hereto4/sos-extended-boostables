package menu;

import java.io.IOException;
import java.nio.file.Path;

import game.audio.AudioFactory;
import init.INIT;
import init.constant.C;
import init.paths.PATH;
import init.paths.PATHS;
import snake2d.CORE;
import snake2d.CORE.GlJob;
import snake2d.SoundEffect;
import snake2d.SoundStream;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.file.SnakeImage;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerThings.ITileSprite;
import util.spritecomposer.ComposerUtil;
import util.spritecomposer.Initer;

final class RESOURCES {

	private RSprites s;
	private RSound sound;
	
	public RESOURCES() {
		new GlJob() {
			@Override
			public void doJob() {
				new Initer() {
					
					@Override
					public void createAssets() throws IOException {
						new INIT();
						s = new RSprites();
						
					}
				}.get("menu", PATHS.textureSize(), 0);
				sound = new RSound();
			}
		}.perform();
	}
	
	public RSprites s() {
		return s;
	}
	
	public RSound sound() {
		return sound;
	}
	

	static class RSound {

		public boolean playing;
		public final  SoundStream music;
		public final  SoundStream s;
		
		public final SoundStream logo;
		
		RSound(){
			
			Json json = new Json(PATHS.AUDIO().config.get("Menu"));
			
			AudioFactory<SoundStream> fm = new AudioFactory<SoundStream>("MUSIC", PATHS.AUDIO().music, new SoundStream.Dummy()) {
				
				@Override
				protected SoundStream create(LinkedList<SoundStream> all, Path p, String key) {
					return CORE.getSoundCore().getStream(p, true);
				}
			};
			
			music = fm.read("MENU", json).get(0);
			logo = fm.read("LOGO", json).get(0);
			s = fm.read("TORCH", json).get(0);
			
			AudioFactory<SoundEffect> sm = new AudioFactory<SoundEffect>("SOUND", PATHS.AUDIO().mono, new SoundEffect.Dummy()) {
				
				@Override
				protected SoundEffect create(LinkedList<SoundEffect> all, Path p, String key) {
					return CORE.getSoundCore().getEffect(p);
				}
			};
			
			
			ClickableAbs.defaultClickSound = sm.read("CLICK", json).get(0);
			ClickableAbs.defaultHoverSound = sm.read("HOVER", json).get(0);
			
			CORE.getSoundCore().set(C.WIDTH()/2, C.HEIGHT()/2);
			
		}
		
		void play() {
			if (playing)
				return;
			logo.stop();
			//music.setLooping(true);
			music.play();
			s.setLooping(true);
			s.play();
		}
		
	}
	
	static final class RSprites {
		
		private final PATH g = PATHS.SPRITE().getFolder("menu");
		
		public final TILE_SHEET background;
		public final TILE_SHEET backgroundCr;
		public final int backgroundTilesX;
		{
			COORDINATE dd = SnakeImage.dim(g.get("Background"));
			backgroundTilesX = (dd.x()-6*4)/(32*2);
			int ty = (dd.y()-6*2)/32;
			background = new ITileSheet(g.get("Background"), dd.x(), dd.y()) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, backgroundTilesX, ty, d.s32);
					s.full.paste(true);
					return d.s32.save(2);
				}
			}.get();
			backgroundCr = new ITileSheet(g.get("BackgroundCr"), 3096, 396) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, 48, 12, d.s32);
					s.full.paste(true);
					return d.s32.save(2);
				}
			}.get();
			
		}
		
		
		private final static int lHeight = 67;
		public final SPRITE[] logoGlyps = new SPRITE[] {
			new ITileSprite(45,lHeight,1,g.get("GamatronLogo"), 1552, 92) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, 3, 5, d.s16);
					s.full.paste(true);
					return d.s16.save(1);
				}
			},
			glyph(56),
			glyph(64),
			glyph(55),
			glyph(46),
			glyph(57),
			glyph(48),
			glyph(57),		
		};
		public final SPRITE logoFlash = glyph(55);
		public final SPRITE logoPresents = new ITileSprite(8*16,19,1) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(s.full.body().x2(), 0, 1, 1, 8, 2, d.s16);
				s.full.paste(true);
				return d.s16.save(1);
			}
		};
		public final SPRITE logo = new ITileSprite(352,32*7,1,g.get("Logo"), 728, 224) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, 0, 1, 1, 11, 7, d.s32);
				s.full.paste(true);
				return d.s32.save(1);
			}
		};
		public final COLOR[] logoColors = new COLOR[] {
			new ColorImp(61, 5, 15).saturateSelf(0.75),
			new ColorImp(75, 26, 5).saturateSelf(0.75),
			new ColorImp(84, 60, 10).saturateSelf(0.75),
			new ColorImp(15, 75, 4).saturateSelf(0.75),
			new ColorImp(15, 75, 10).saturateSelf(0.75),
			new ColorImp(2, 10, 75).saturateSelf(0.75),
			new ColorImp(61, 5, 15).saturateSelf(0.75),
			new ColorImp(75, 30, 5).saturateSelf(0.75),
		};
		
		public final SPRITE creditsSmallFrame;
		public final SPRITE[] creditsSmall;
		public final SPRITE creditsBigFrame;
		public final SPRITE[] creditsBig;
		
		private SPRITE glyph(int width) throws IOException {
			int tx = (int)Math.ceil((double)width/(16));
			return new ITileSprite(width,lHeight,1) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(s.full.body().x2(), 0, 1, 1, tx, 5, d.s16);
					s.full.paste(true);
					return d.s16.save(1);
				}
			};
		}

		RSprites() throws IOException{
			creditsSmall = new SPRITE[13];
			creditsSmallFrame =new ITileSprite(64,64,3,g.get("CreditSmall"), 2128, 76) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.full.init(0, 0, creditsSmall.length+1, 1, 2, 2, d.s32);
					s.full.setVar(0).paste(true);
					return d.s32.save(3);
				}
			};
		
			for (int i = 0; i < creditsSmall.length; i++) {
				final int k = i;
				
				creditsSmall[i] = new ITileSprite(64,64,3) {
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.full.setVar(k+1).paste(true);
						return d.s32.save(3);
					}
				};
			}
			creditsBig = new SPRITE[11];
			creditsBigFrame = new ITileSprite(96,128,3,g.get("CreditLarge"), 2596, 140) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.full.init(0, 0, creditsBig.length+1, 1, 3, 4, d.s32);
					s.full.setVar(0).paste(true);
					return d.s32.save(3);
				}
			};
			
			for (int i = 0; i < creditsBig.length; i++) {
				final int k = i;
				creditsBig[i] = new ITileSprite(96,128,3) {
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.full.setVar(k+1).paste(true);
						return d.s32.save(3);
					}
				};
			}
		}
	}
	
}
