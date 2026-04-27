package snake2d.util.sprite.text;

import java.io.IOException;

import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;

public class Font {

	public static final short[] map = new short[0x0FFFF];
	private static int renderableChars;
	public static CharSequence set;
	private final static int space = 32;
	public final static char nl = 10;
	public final static char tab = 9;
	private static final Str tmp = new Str(128);

	static {
		setCharset(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~");
	}

	public static int glyps() {
		return renderableChars;
	}
	
	public static CharSequence charset() {
		return set;
	}

	public static void setCharset(CharSequence charset) {
		renderableChars = charset.length();
		set = charset;
		for (int i = 0; i < map.length; i++) {
			map[i] = -1;
		}

		for (int i = 0; i < charset.length(); i++) {
			map[charset.charAt(i)] = (short) i;
		}
		
		map[space] = -1;
		map[nl] = -1;
		map[tab] = -1;

	}
	
	public static boolean hasMapping(char charAt) {
		return map[charAt] != -1;
	}
	
	private static int map(char c) {
		int i = map[c];
		if (i == -1) {
			i = map['?'];
		}
		return i;
	}


	private final String name;
	private final FontGlyph[] glyphs;
	private final int height;
	private double scale = 1.0;
	private final Texture texture = new Texture();
	public final int maxCWidth;

	private final int DY;
	
	public Font(Font m, double scale, int trail) {
		this(m.glyphs, m.height, m.scale*scale, trail);
	}
	
	public Font(FontGlyph[] glyphs, int height, double scale, int trail) {
		if (glyphs.length < renderableChars)
			throw new RuntimeException(glyphs.length + " " + renderableChars);
		name = "generated";
		this.height = height;
		this.scale = scale;
		this.glyphs = glyphs;
		int m = 0;
		int dmi = Integer.MAX_VALUE;
		for (FontGlyph g : glyphs) {
			m = Math.max(m, g.width+g.trail);
			dmi = Math.min(dmi, g.descent);
		}
		DY = -dmi/2;
		maxCWidth = m;
		
		for (FontGlyph g : glyphs)
			g.trail += trail;
		
	}

	public String getName() {
		return name;
	}

	public int height() {
		return (int) Math.ceil(height*scale);
	}
	
	public int height(double scale) {
		return (int) Math.ceil(height*this.scale*scale);
	}

	public void renderFromRow(CharSequence c, int x, int y, int width, int topRow, int maxHeight) {

		int start = 0;
		int end = 0;
		
		while (end < c.length()) {
			end = getEndIndex(c, start, width / 1);
			if (topRow <= 0) {
				render(CORE.renderer(), c, x, y, start, end, 1);
				y += height() * 1;
				maxHeight -= height() * 1;
				
				if (maxHeight <= 0)
					return;
			}

			start = getStartIndex(c, end);
			topRow--;
		}
	}

	public int getHeight(CharSequence c, int width) {
		return getDim(c, width).y();
	}
	
	public int getRowAmount(CharSequence s, int width) {
		int fromI = 0;
		int toI = s.length();
		int am = 0;
		while (fromI < toI) {
			int e = getEndIndex(s, fromI, toI, width, 1.0);
			fromI = getStartIndex(s, e);
			am++;
		}
		
		return am;
	}
	
	public CharSequence[] getRows(CharSequence c, int width) {
		
		int am = getRowAmount(c, width);
		CharSequence[] rows = new CharSequence[am];
		
		int start = 0;
		int end = 0;
		int h = 0;

		while (end < c.length()) {
			end = getEndIndex(c, start, width);
			if (h < rows.length)
				rows[h] = c.subSequence(start, end);
			h += 1;
			start = getStartIndex(c, end);
		}

		return rows;
	}
	
	private final Coo dim = new Coo();
	
	public COORDINATE getDim(CharSequence s) {
		return getDim(s, 0, s.length(), Integer.MAX_VALUE, 1.0);
	}
	
	public COORDINATE getDim(CharSequence s, int maxWidth) {
		return getDim(s, 0, s.length(), maxWidth, 1.0);
	}
	
	public COORDINATE getDim(CharSequence s, int maxWidth, double scale) {
		return getDim(s, 0, s.length(), maxWidth, scale);
	}
	
	public COORDINATE getDim(CharSequence s, 
			int fromI, final int toI, final int maxwidth, double scale) {
		
		final int height = height(scale);
		dim.set(0,0);
		
		while (fromI < toI) {
			
			
			int e = getEndIndex(s, fromI, toI, maxwidth, scale);
			int w = width(s, fromI, e, scale); 
			
			if (w > dim.x())
				dim.xSet(w);
			dim.yIncrement(height);
			fromI = getStartIndex(s, e);;
		}
		
		return dim;
	}
	
	
	public COORDINATE getStartPosition(CharSequence s, int fromIndex, int toI, final int wordLength, final int maxwidth, double scale) {

		final int height = height(scale);
		dim.set(0,0);
		
		int fromI = fromIndex;
		while (fromI < toI + wordLength) {
			int e = getEndIndex(s, fromI,  toI + wordLength, maxwidth, scale);
			int w = width(s, fromI, e, scale); 
			dim.xSet(w);
			
			
			if (e > toI || (e == toI && wordLength == 0)) {
				if (e > 0 && s.charAt(e-1) == nl) {
					dim.xSet(0);
					dim.yIncrement(height);
				}
				
				dim.xIncrement(-width(s, toI, e, scale));
				return dim;
			}
			fromI = getStartIndex(s, e);
			
			
			dim.yIncrement(height);
		}

		if (toI > 0 && s.charAt(toI-1) == nl) {
			dim.xSet(0);
			dim.yIncrement(height);
		}
		
		return dim;
	}

	public int renderC(SPRITE_RENDERER r, int cx, int cy, CharSequence s) {
		COORDINATE c = getDim(s);
		int w = c.x();
		int h = c.y();
		cx -= w/2;
		cy -= h/2;
		return cy + render(r, cx,cy, DIR.C, s, 0, s.length(), w, h, 1.0);
	}
	
	public int renderC(SPRITE_RENDERER r, int cx, int cy, CharSequence s, double scale) {
		COORDINATE c = getDim(s);
		int w = (int) (c.x()*scale);
		int h = (int) (c.y()*scale);
		cx -= w/2;
		cy -= h/2;
		return cy + render(r, cx,cy, DIR.C, s, 0, s.length(), w, h, scale);
	}
	
	public int renderCX(SPRITE_RENDERER r, int cx, int y1, CharSequence s) {
		return renderCX(r, cx, y1, s, 1.0);
	}
	
	public void renderCY(SPRITE_RENDERER r, int x1, int cy, CharSequence s) {
		render(r, s, x1, cy-height()/2, 1.0);
	}
	
	public int renderCX(SPRITE_RENDERER r, int cx, int y1, CharSequence s, double scale) {
		COORDINATE c = getDim(s, Integer.MAX_VALUE, scale);
		int w = c.x();
		int h = c.y();
		cx -= w/2;
		return y1 + render(r, cx,y1, DIR.C, s, 0, s.length(), w, h, scale);
	}

	public int renderIn(SPRITE_RENDERER r, RECTANGLE body, DIR align, CharSequence s) {
		return renderIn(r, body.x1(), body.y1(), align, s, body.width(), body.height(), 1.0);
	}
	
	public int renderIn(SPRITE_RENDERER r, RECTANGLE body, DIR align, CharSequence s, double scale) {
		return renderIn(r, body.x1(), body.y1(), align, s, body.width(), body.height(), scale);
	}
	
	public int renderIn(SPRITE_RENDERER r, int x1, int y1, DIR align, CharSequence s, int maxWidth, int maxHeight, double scale) {
		COORDINATE c = getDim(s, 0, s.length(), maxWidth, scale);
		int w = c.x();
		
		int h = Math.min(c.y(), maxHeight);
		x1 += (align.x()+1)*(maxWidth-w)/2;
		y1 += (align.y()+1)*(maxHeight-h)/2;
		return y1 + render(r, x1, y1, align, s, 0, s.length(), w, h, scale);
	}

	public void renderCropped(SPRITE_RENDERER r, CharSequence c, int x, int y, int width) {
		renderCropped(r, c, x, y, width, 1.0);
	}
	
	public void renderCropped(SPRITE_RENDERER r, CharSequence chars, int x, int y, int width, double scale) {
		

		int w = 0;
		int e = 0;
		while(e < chars.length()) {
			char c = chars.charAt(e);
			if (c == nl)
				break;
			w += width(chars.charAt(e), scale);
			e++;
			if (e < chars.length())
				w -= getBack(chars.charAt(e-1), chars.charAt(e), scale);
			
			if (w >= width)
				break;
			
		}
		
		if (e < chars.length()) {
			tmp.clear().add(chars, 0, CLAMP.i(e-3, 0, e));
			tmp.add('.').add('.').add('.');
		}else
			tmp.clear().add(chars);
		
		render(r, x, y, DIR.W, tmp, 0, e, Integer.MAX_VALUE, height(), scale);
	}
	
	public void render(SPRITE_RENDERER r, CharSequence c, int x, int y) {
		render(r, c, x, y, 0, c.length(), 1);
	}
	
	public void render(SPRITE_RENDERER r, CharSequence c, int x, int y, double scale) {
		render(r, c, x, y, 0, c.length(), scale);
	}

	public void render(SPRITE_RENDERER r, CharSequence c, int x1, int y, int startX, int endX, double scalee) {
		render(r, x1, y, DIR.NW, c, startX, endX, Integer.MAX_VALUE, Integer.MAX_VALUE, scalee);
	}
	
	public int render(SPRITE_RENDERER r, CharSequence c, int x, int y, int width, double scale) {
		return render(r, x, y, DIR.NW, c, 0, c.length(), width, Integer.MAX_VALUE, scale);
	}
	
	/**
	 * 
	 * @param r
	 * @param x1
	 * @param y1
	 * @param align
	 * @param s
	 * @param fromI
	 * @param toI
	 * @param maxWidth
	 * @param scale
	 * @return height of rendered text
	 */
	public int render(SPRITE_RENDERER r, final int x1, int y1, 
			final DIR align, CharSequence s, 
			int fromI, final int toI, final int maxWidth, int maxHeight, double scale) {
		
		
		int h = 0;
		final int height = height(scale);
		
		while (fromI < toI && h < maxHeight) {
			int e = getEndIndex(s, fromI, toI, maxWidth, scale);
			int w = width(s, fromI, e, scale); 
			
			int x = x1 + (align.x()+1)*(maxWidth-w)/2;
			while(fromI < e) {
				char c = s.charAt(fromI);
				renderChar(r, c, x, y1+h, scale);
				
				x += width(c, scale);
				fromI ++;
				if (fromI < e)
					x -= getBack(c, s.charAt(fromI), scale);
			}
			
			
			h+= height;
			fromI = getStartIndex(s, fromI);
		}
		return h;
	}
	
	public int getStartIndex(CharSequence s, int index) {
		if (index >= s.length())
			return index;
		if (s.charAt(index) == space)
			return index+1;
		return index;
	}
	
	public int getEndIndex(CharSequence c, int startX, int maxWidth) {
		return getEndIndex(c, startX, c.length(), maxWidth, 1.0);
	}
	public int getEndIndex(CharSequence chars, int startX, int endIndex, int maxWidth, double scale) {

		int w = 0;
		if (endIndex > chars.length())
			endIndex = chars.length();
		int start = startX;
		while(startX < endIndex) {
			char c = chars.charAt(startX);
			if (c == nl)
				return startX + 1;
			
			int we = wordEnd(chars, startX, endIndex);
			int ww = width(chars, startX, we, scale);
			if (w + ww > maxWidth) {
				if (start == startX) {
					w = width(chars.charAt(start), scale);
					while(w < maxWidth && start< endIndex) {
						start++;
						w += width(chars.charAt(start), scale);
						if (start < endIndex)
							w -= getBack(chars.charAt(start-1), chars.charAt(start), scale);
						
					}
					return start;
				}
				return startX;
			}
			w += ww;
			startX = we;
		}
		return startX;
	}

	
	public int wordEnd(CharSequence s, int fromI, int toI) {
		if (toI > s.length())
			toI = s.length();
		fromI += 1;
		while (fromI < toI) {
			char c = s.charAt(fromI);
			if (c == tab) 
				return fromI;
			if (c == space) 
				return fromI;
			if (c == nl) 
				return fromI;
			fromI++;
		}
		return fromI;
	}
	
	public int width(CharSequence s, int start, int end, double scale) {
		int w = 0;
		while(start < end) {
			w += width(s.charAt(start), scale);
			start++;
			if (start < end)
				w -= getBack(s.charAt(start-1), s.charAt(start), scale);
		}
		return w;
	}
	
	public int width(CharSequence s) {
		return width(s, 0, s.length(), 1);
	}
	
	/**
	 * 
	 * @param c
	 * @param scale
	 * @return width of character with its trail.
	 */
	public int width(char c, double scale) {
		if (c == tab) 
			return size(height()*4, scale);
		else if (c == space)
			return size(height()*0.3, scale);
		if (c == nl) 
			return 0;
		int i = map(c);
		
		return size(glyphs[i].width+glyphs[i].trail, scale);
	}
	
	public int getBack(char prev, char next, double scale) {
		int pi = map(prev);
		if (pi < 0)
			return 0;
		int ni = map(next);
		if (ni < 0)
			return 0;
		
		
		
		int t = glyphs[pi].ne + glyphs[ni].nw;
		int b = glyphs[pi].se + glyphs[ni].sw;
		return (int) (Math.min(t, b)*scale);
	}
	
	private int size(double w, double scale) {
		return (int) Math.ceil(w*this.scale*scale);
	}
	
	public void renderChar(SPRITE_RENDERER r, char ch, int x, int y, double scalee) {
		
		if (ch == tab) 
			return;
		if (ch == space) 
			return;
		if (ch == nl) 
			return;
		
		int i = map(ch);
		
		int x2 = x + size(glyphs[i].width, scalee);
		int y2 = y+height(scalee);
		
		
		int dy = size(glyphs[i].descent + DY, scalee);
		r.renderSprite(x, x2, y+dy, y2+dy, texture.set(i));

	}
	
	public SPRITE getText(CharSequence s) {
		return new Text(this, s);
	}

	public Text getText(Object s) {
		return new Text(this, s.toString());
	}

	public Text getText(int width) {
		return new Text(this, width);
	}
	
	private final class Texture extends TextureCoords {

		private Texture set(int ascii) {
			x1 = glyphs[ascii].tx1;
			x2 = (short) (x1 +glyphs[ascii].width);
			y1 = glyphs[ascii].ty1;
			y2 = (short) (y1 + height);
			return this;
		}

	};
	
	public static class FontGlyph implements SAVABLE {
		
		public short tx1;
		public short ty1;
		public short width;
		public short trail;
		public short descent;
		public byte nw,sw,ne,se;
		
		@Override
		public void save(FilePutter file) {
			file.s(tx1);
			file.s(ty1);
			file.s(width);
			file.s(trail);
			file.s(descent);
			file.b(nw);
			file.b(sw);
			file.b(ne);
			file.b(se);
			
		}
		@Override
		public void load(FileGetter file) throws IOException {
			tx1 = file.s();
			ty1 = file.s();
			width = file.s();
			trail = file.s();
			descent = file.s();
			nw = file.b();
			sw = file.b();
			ne = file.b();
			se = file.b();
		}
		@Override
		public void clear() {
			
			
		}
		
		@Override
		public String toString() {
			return nw + " " + ne + " " + sw + " " + se + " " + descent + " " + trail;
		}
		
	}


}
