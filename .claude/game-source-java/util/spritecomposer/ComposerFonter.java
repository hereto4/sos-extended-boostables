package util.spritecomposer;

import java.io.IOException;

import snake2d.Errors;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SnakeImage;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Font;
import snake2d.util.sprite.text.Font.FontGlyph;
import util.spritecomposer.ComposerDests.Tile;

public class ComposerFonter {

	private final ComposerUtil c;
	private final int TRANS = 0x00000000;
	private final int GREEN = 0x00FF00FF;
	
	ComposerFonter(ComposerUtil c){
		this.c = c;
	}
	
	public Font save(final int x1, final int y1, int trail) {

		FilePutter p = Resources.p;
		
		Source s = new Source(c.getSource());
		Tile dest = getDest(s.height());
		final Dest d = new Dest(s, dest);
		
		
		FontGlyph[] ggs = new FontGlyph[Font.glyps()];
		for (int i = 0; i < ggs.length; i++)
			ggs[i] = new FontGlyph();
		
		int maxX = dest.startX + dest.tilesX*dest.size;
		
		int hh = 0;
		
		for (int i = 0; i < Font.glyps(); i++) {
			
			s.set(i);
			
			int width = s.width();
			
			if ((d.x1 + width) >= maxX) {
				d.x1 = dest.startX;
				d.y1 += dest.size;
			}
			
			ggs[i].width = (short) s.width();
			ggs[i].ty1 = (short) d.y1;
			ggs[i].tx1 = (short) d.x1;

			setDescent(ggs[i], s);
			if (Character.isLetter(Font.charset().charAt(i)))
				setCorners(ggs[i], s);
			
			setTrail(ggs[i], s);

			hh = Math.max(hh, (s.hh));

			c.copy(s);
			c.paste(d);
			d.x1 += width;
			
		}
		
		for (FontGlyph g : ggs) {
			g.descent -= s.height-hh;
		}
		
		int tStart = dest.x1()/dest.size;
		int tEnd = d.x1()/dest.size + dest.tilesX*(d.y1 - dest.y1())/dest.size;
		tEnd += d.x1() % dest.size != 0 ? 1 : 0;
		dest.skip(tEnd - tStart);
		
		p.mark("font");
		p.i(s.height);
		p.i(d.d.size);
		for (int i = 0; i < Font.glyps(); i++) {
			ggs[i].save(p);
		}
		
//		LOG.ln(hh + " " + s.im.path);
		
		
		return new Font(ggs, s.height(), 1.0, trail);
	}
	
	private Tile getDest(int h){
		
		if (h <= Resources.dests.s16.size()) {
			return Resources.dests.s16;
		}
		if (h <= Resources.dests.s24.size()) {
			return Resources.dests.s24;
		}
		if (h <= Resources.dests.s32.size()) {
			return Resources.dests.s32;
		}
		throw new Errors.DataError("Unable to create font. Font height too big: " + h, c.getSourcePath());
		
	}
	
	private void setDescent(FontGlyph g, Source d) {
		
		int dd = 0;
		for (int y = 0; y < d.height(); y++) {
			if (d.im.rgb.get(d.x1()-1, y+d.y1()) != GREEN) {
				dd++;
			}
		}
		
		g.descent = (short) dd;
		
		
	}
	
	private void setTrail(FontGlyph g, Source s) {
		
		for (int y = 0; y < s.height(); y++) {
			int rgb = s.im.rgb.get(s.x1()+s.width-1, y+s.y1());
			rgb &= 0x0FF;
			if (rgb > 32) {
				g.trail = (short) Math.max(g.trail, 1);
				return;
			}
	
		}
	}
	
	private void setCorners(FontGlyph g, Source d) {
		
		{
			int off = d.width/2;
			outer:
			for (int x = 0; x < d.width; x++) {
				for (int y = 0; y < d.height()/2-g.descent; y++) {
					if (d.im.rgb.get(x+d.x1(), y+d.y1()) != TRANS) {
						off = x;
						break outer;
					}
				}
			}
			g.nw = (byte) CLAMP.i(off-1, 0, off);
		}
		
		{
			int off = d.width/2;
			outer:
				for (int x = 0; x < d.width; x++) {
					for (int y = 0; y < d.height()/2-g.descent; y++) {
						if (d.im.rgb.get(d.x1()+d.width-1-x, y+d.y1()) != TRANS) {
							off = x;
							break outer;
						}
				}
			}
			g.ne = (byte) CLAMP.i(off-1, 0, off);
		}
		
		{
			int off = d.width/2;
			outer:
			for (int x = 0; x < d.width; x++) {
				for (int y = 0; y < d.height()/2; y++) {
					int y1 = y+d.height()/2+d.y1();
					y1 -= g.descent;
					if (y1 < d.y1())
						break;
					if (d.im.rgb.get(x+d.x1(), y1) != TRANS) {
						off = x;
						break outer;
					}
				}
			}
			g.sw = (byte) CLAMP.i(off-1, 0, off);
		}
		
		{
			int off = d.width/2;
			outer:
			for (int x = 0; x < d.width; x++) {
				for (int y = 0; y < d.height()/2; y++) {
					int y1 = y+d.height()/2+d.y1();
					y1 -= g.descent;
					if (y1 < d.y1())
						break;
					if (d.im.rgb.get(d.x1()+d.width-1-x, y1) != TRANS) {
						off = x;
						break outer;
					}
				}
			}
			g.se = (byte) CLAMP.i(off-1, 0, off);
		}
		
		
	}
		
	static Font get(int trail) throws IOException {
		FileGetter g = Resources.g;
		g.check("font");
		int h = g.i();
		int dy = Optimizer.get(g.i()).startY;
		
		FontGlyph[] ggs = new FontGlyph[Font.glyps()];
		for (int i = 0; i < ggs.length; i++)
			ggs[i] = new FontGlyph();
		for (int i = 0; i < Font.glyps(); i++) {
			
			ggs[i].load(g);
			ggs[i].ty1 += dy;
		}

		return new Font(ggs, h, 1.0, trail);
	}
	
	private final class Source extends ComposerSources.Source {
		
		private final Rec body = new Rec();
		private int dim;
		private final int height;
		private int width;
		private int pixelX1,pixelY1;
		private final SnakeImage im;
		private int hh;
		
		Source(SnakeImage im){
			this.im = im;
			dim = im.width/(2*64)-2;
			height = dim;
		}
		
		@Override
		public RECTANGLE body() {
			return body;
		}

		@Override
		int height() {
			return height;
		}

		@Override
		int width() {
			return width;
		}

		@Override
		int x1() {
			return pixelX1;
		}

		@Override
		int y1() {
			return pixelY1;
		}
		
		Source set(int cI) {
			
			pixelX1 = (cI%64)*(dim+2)+1;
			pixelY1 = (cI/64)*(dim+2)+1;
			width = dim;
			
			{
				outer:
				for (int x = dim-1; x >= 0; x--) {
					for (int y = 0; y < dim; y++) {
						if (im.rgb.get(pixelX1+x, pixelY1+y) != TRANS) {
							width = x+1;
							break outer;
						}
					}
				}
			}
			
			{
				outer:
				for (int y = dim-1; y > 0; y--) {
					for (int x = 0; x < dim; x++) {
						if (im.rgb.get(pixelX1+x, pixelY1+y) != TRANS) {
							hh = y;
							break outer;
						}
					}
					
				}
				
			}
			
			return this;
			
		}
	}
	
	private class Dest extends ComposerDests.Dest{

		private int x1; int y1;
		private final Tile d;
		private final Source s;
		
		Dest(Source s, Tile d){
			this.s = s;
			this.d = d;
			x1 = d.x1();
			y1 = d.y1();
		}
		
		@Override
		int x1() {
			return x1;
		}

		@Override
		int y1() {
			return y1;
		}

		@Override
		int width() {
			return s.width();
		}

		@Override
		int height() {
			return s.height();
		}

		@Override
		public void jump(int i) {
			d.jump(i);
		}
		
		@Override
		public void dispose() {
			// TODO Auto-generated method stub
			
		}

		@Override
		int size() {
			return d.size();
		}

		@Override
		public void diffuseSet(int x, int y, int c) {
			d.diffuseSet(x, y, c);
		}

		@Override
		public int diffuseGet(int x, int y) {
			return d.diffuseGet(x, y);
		}

		@Override
		public void normalSet(int x, int y, int c) {
			d.normalSet(x, y, c);
		}

		@Override
		public int normalGet(int x, int y) {
			return d.normalGet(x, y);
		}

		@Override
		public int destWidth() {
			return d.destWidth();
		}
		
		
	}
	
}
