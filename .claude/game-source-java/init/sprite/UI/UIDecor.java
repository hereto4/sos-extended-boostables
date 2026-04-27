package init.sprite.UI;

import java.io.IOException;

import init.constant.C;
import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.gui.misc.GText;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ISprite;
import util.spritecomposer.ComposerThings.ISpriteData;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerThings.ITileSprite;
import util.spritecomposer.ComposerUtil;
import util.spritecomposer.SpriteData;

public class UIDecor{



	
	public final SPRITE topDecor = new SPRITE() {
		
		private final TILE_SHEET sheet2 = new ITileSheet(PATHS.SPRITE_UI().get("Decor"), 664, 160) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, 0, 1, 1, 4, 1, d.s32);
				s.full.paste(true);
				return d.s32.saveGui();	 
			}
		}.get();
		
		@Override
		public int width() {
			return 4*32;
		}
		
		@Override
		public int height() {
			return 32;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			GCOLOR.T().H1.bind();
			for (int i = 0; i < 4; i++)
				sheet2.render(r, i, X1+32*i, Y1);
			COLOR.unbind();
		}
	};

	private final TILE_SHEET _borderTop = new ITileSheet() {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(0, s.full.body().y2(), 1, 1, 10, 1, d.s32);
			s.full.setSkip(4, 0);
			s.full.paste(true);
			return d.s32.saveGui();	 
		}
	}.get();
	
	private final TILE_SHEET _borderBottom = new ITileSheet() {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.setSkip(4, 4);
			s.full.paste(true);
			return d.s32.saveGui();	 
		}
	}.get();
	
	private final TILE_SHEET _leftRight = new ITileSheet() {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.setSkip(2, 8);
			s.full.paste(true);
			return d.s32.saveGui();	 
		}
	}.get();
	
	public final TILE_SHEET slider = new ITileSheet() {
		
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(0, s.full.body().y2(), 1, 1, 4, 1, d.s24);
			s.full.paste(true);
			s.full.pasteRotated(1, true);
			return d.s24.saveGui();
		}
	}.get();
	
	public final SPRITE mouse = ISprite.gui(new ISpriteData() {

		@Override
		protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(0, s.full.body().y2(), 1, 1, 1, 1, d.s24);
			s.full.setSkip(1, 0).paste(true);
			return d.s24.saveSprite();
		}
	}.get());
	
	public final SPRITE mouseHov = ISprite.gui(new ISpriteData() {

		@Override
		protected SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(s.full.body().x2(), s.full.body().y1(), 1, 1, 1, 1, d.s24);
			s.full.setSkip(1, 0).paste(true);
			return d.s24.saveSprite();
		}
	}.get());
	
	public final SPRITE up = new ITileSprite(32,16,C.SG) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.init(s.full.body().x2(), s.full.body().y1(), 1, 1, 4, 1, d.s16);
			s.full.setSkip(2, 0).paste(true);
			return d.s16.saveGui();
		}
		
	};
	
	public final SPRITE down = new ITileSprite(32,16,C.SG) {

		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			s.full.setSkip(2, 2).paste(true);
			return d.s16.saveGui();
		}
		
	};

	
	public SPRITE borderTop(int width) {
		return new Adaptive(width, _borderTop);
	}
	
	public SPRITE borderBottom(int width) {
		return new Adaptive(width, _borderBottom);
	}
	
	public SPRITE borderTop(int width, COLOR color) {
		return new Adaptive(width, _borderTop, color);
	}
	
	public SPRITE borderBottom(int width, COLOR color) {
		return new Adaptive(width, _borderBottom, color);
	}
	
	public RENDEROBJ decorate(CharSequence s) {
		return decorate(s, GCOLOR.T().H1);
	}
	
	public RENDEROBJ decorate(CharSequence s, COLOR c) {
		GuiSection sec = new GuiSection();
		sec.add(new RENDEROBJ.Sprite(_leftRight.makeSprite(0)).setColor(c));
		sec.addRightC(C.SG*20, new RENDEROBJ.Sprite(new Text(UI.FONT().H1, s).toUpper()).setColor(c));
		sec.addRightC(C.SG*20, new RENDEROBJ.Sprite(_leftRight.makeSprite(1)).setColor(c));
		return sec;
	}
	
	public RENDEROBJ getDecored(CharSequence s) {
		GText t = new GText(UI.FONT().H1, s) {
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.T().H1.bind();
				int w = (X2-X1)/2;
				w -= topDecor.width()/2;
				topDecor.render(r, X1 + w, Y1-topDecor.height());
				COLOR.unbind();
				super.render(r, X1, X2, Y1, Y2);
			};
		};
		t.lablify();
		t.toUpper();
		return t.r(DIR.C);
	}
	
	public RENDEROBJ frame(RECTANGLE bounds) {
		RENDEROBJ o = new RENDEROBJ.Sprite(new Frame(bounds.width()+32, bounds.height()));
		o.body().centerIn(bounds);
		return o;
	}
	
	public RENDEROBJ frame(RECTANGLE bounds, COLOR color) {
		RENDEROBJ o = new RENDEROBJ.Sprite(new Frame(bounds.width()+32, bounds.height(), color));
		o.body().centerIn(bounds);
		return o;
	}
	
	public GuiSection frame(int width, int height) {
		GuiSection s = new GuiSection();
		s.add(borderTop(width), 0, 0);
		RENDEROBJ r = new RENDEROBJ.Sprite(borderBottom(width));
		r.body().moveY1(height);
		s.add(r);
		return s;
	}
	
	public RENDEROBJ frame(RECTANGLE bounds, CharSequence title) {
		GuiSection f = frame(bounds.width(), bounds.height()+32, title);
		f.body().centerX(bounds);
		f.body().moveY2(bounds.y2()+32);
		return f;
	}
	
	public GuiSection frame(int width, int height, CharSequence title) {
		GuiSection s = frame(width, height);
		RENDEROBJ o = decorate(title);
		s.addRelBody(C.SG*0, DIR.N, o);
		return s;
	}
	
	public GuiSection frameFancy(int width, int height, CharSequence title) {
		GuiSection s = frame(width, height);
		s.addRelBody(C.SG*0, DIR.N, new RENDEROBJ.Sprite(new Text(UI.FONT().H1, title).toUpper()).setColor(GCOLOR.T().H1));
		s.addRelBody(C.SG*0, DIR.N, topDecor);
		return s;
	}
	
	private class Frame implements SPRITE{
		
		private final SPRITE top;
		private final SPRITE bottom;
		private final int width;
		private final int height;
		
		Frame(int width, int height){
			this(width, height, GCOLOR.T().H1);
		}
		
		Frame(int width, int height, COLOR color){
			this.width = width;
			this.height = height+64;
			top = borderTop(width, color);
			bottom = borderBottom(width, color);
		}

		@Override
		public int width() {
			return width;
		}

		@Override
		public int height() {
			return height;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			top.render(r, X1, Y1);
			bottom.render(r, X1, Y2-32);
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private static class Adaptive implements SPRITE{

		private static final int size = 32;
		private final int width;
		private final TILE_SHEET sheet;
		private final COLOR color;
		
		Adaptive(int width, TILE_SHEET sheet){
			this(width, sheet, GCOLOR.T().H1);
		}
		
		Adaptive(int width, TILE_SHEET sheet, COLOR color){
			this.width = width;
			this.sheet = sheet;
			this.color = color;
		}
		
		@Override
		public int width() {
			return width;
		}

		@Override
		public int height() {
			return size;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			color.bind();
			int w = (X2-X1) - 3*size;
			if (w < 0)
				w = 0;
			w /= 2;
			int dw = w%size;
			w /= size;
			
			sheet.render(r, 0, X1, Y1);
			X1 += size;
			for (int i = 0; i < w; i++) {
				sheet.render(r, 1, X1, Y1);
				X1 += size;
			}
			if (dw != 0) {
				sheet.render(r, 1, X1-(size-dw), Y1);
				X1 += dw;
			}
			sheet.render(r, 2, X1, Y1);
			X1 += size;
			for (int i = 0; i < w; i++) {
				sheet.render(r, 1, X1, Y1);
				X1 += size;
			}
			if (dw != 0) {
				sheet.render(r, 1, X1-(size-dw), Y1);
				X1 += dw;
			}
			sheet.render(r, 3, X1, Y1);
			COLOR.unbind();
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		
		
	}
	
	UIDecor() throws IOException{
		
	}
	

}
