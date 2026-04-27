package util.gui.misc;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;
import util.data.DOUBLE;

public class GMeter{

	public final static GMeterCol C_ORANGE = new GMeterCol(
			new ColorImp(15, 6, 1),
			new ColorImp(52, 26, 5),
			new ColorImp(158/2, 83/2, 28/2),
			new ColorImp(127, 75, 20));
	
	public final static GMeterCol C_YELLOW = new GMeterCol(
			new ColorImp(15, 6, 1),
			new ColorImp(45, 33, 10),
			new ColorImp(64, 46, 13),
			new ColorImp(127, 93, 28));
	
	public final static GMeterCol C_BLUE = new GMeterCol(
			new ColorImp(0, 0, 12),
			new ColorImp(9, 9, 45),
			new ColorImp(0, 0, 85),
			new ColorImp(0, 40, 127));
	
	public final static GMeterCol C_REDGREEN = new GMeterCol(
			new ColorImp(45, 16, 16),
			new ColorImp(31, 82, 35).shade(0.5),
			new ColorImp(23, 80, 28));
	
	public final static GMeterCol C_INACTIVE = new GMeterCol(
			new ColorImp(16, 16, 16),
			new ColorImp(48, 48, 48),
			new ColorImp(78, 78, 78));
	
	public final static GMeterCol C_REDORANGE = new GMeterCol(
			new ColorImp(45, 16, 16),
			new ColorImp(96, 38, 0),
			new ColorImp(127, 53, 0));
	
	public final static GMeterCol C_REDPURPLE = new GMeterCol(
			new ColorImp(45, 16, 16),
			new ColorImp(127, 16, 60).shade(0.5),
			new ColorImp(127, 16, 60));
	
	public final static GMeterCol C_REDBLUE = new GMeterCol(
			new ColorImp(45, 16, 16),
			new ColorImp(0, 0, 85),
			new ColorImp(0, 40, 127));
	
	public final static GMeterCol C_GREENRED = new GMeterCol(
			new ColorImp(31, 82, 35),
			new ColorImp(45, 16, 16),
			new ColorImp(100, 16, 16));
	
	public final static GMeterCol C_GREENISH = new GMeterCol(
			new ColorImp(0, 0, 12),
			new ColorImp(9, 30, 30),
			new ColorImp(0, 60, 60),
			new ColorImp(0, 80, 80));
	
	public final static GMeterCol C_RED = new GMeterCol(
			new ColorImp(12, 0, 0),
			new ColorImp(45, 9, 9),
			new ColorImp(85, 0, 0),
			new ColorImp(127, 40, 0));
	
	public final static GMeterCol C_GRAY = new GMeterCol(
			new ColorImp(15, 15, 15),
			new ColorImp(30, 30, 30),
			new ColorImp(100, 100, 100));
	
	public final static GMeterCol C_GREEN = new GMeterCol(
			new ColorImp(16, 16, 16),
			new ColorImp(16, 45, 16),
			new ColorImp(16, 100, 16));
	
	public final static GMeterCol C_GREEN_DARK = new GMeterCol(
			new ColorImp(10, 10, 10),
			new ColorImp(0, 22, 0),
			new ColorImp(0, 45, 0));
	

	
	private GMeter() {
		
	}
	
	public static void render(SPRITE_RENDERER r, GMeterCol color, double d, RECTANGLE body) {
		render(r, color, d, body.x1(), body.x2(), body.y1(), body.y2());
	}
	
	public static void render(SPRITE_RENDERER r, GMeterCol color, double d, int x1, int x2, int y1, int y2) {

		d = CLAMP.d(d, 0, 1);
		
		int w = x2-x1;
		int h = y2-y1;
		
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		GCOLOR.UI().bg().render(r, x1+1, x1+w-1, y1+1, y2-1);
		

		if (h <= 4) {

			color.dark.render(r, x1+2, x2-2, y1+1, y2-1);
			
			
			w = (int) Math.ceil((x2-x1-4)*d);
			if (w > 0) {
				color.bright.render(r, x1+2, x1+2+w, y1+1, y2-1);
			}
		}
		
		if (h <= 6) {

			color.dark.render(r, x1+2, x2-2, y1+2, y2-2);
			
			
			w = (int) Math.ceil((x2-x1-4)*d);
			if (w > 0) {
				color.bright.render(r, x1+2, x1+2+w, y1+2, y2-2);
			}
		}else {
			color.bg.render(r, x1+2, x2-2, y1+2, y2-2);
			
			color.dark.render(r, x1+2, x2-2, y1+3, y2-3);
			
			
			w = (int) Math.ceil((x2-x1-4)*d);
			if (w > 0) {
				color.bright.render(r, x1+2, x1+2+w, y1+3, y2-3);
			}
		}
		
		
		
		
//		w = (int) Math.ceil((x2-x1-6)*d);
//		
//		int dy = y2-y1 > 12 ? 4: 3;
//		if (y2-y1 > 20)
//			dy = 5;
//		
//		if (w > 0) {
//			color.bright.render(r, x1+3, x1+3+w, y1+dy, y2-dy);
//		}
	}
	
	public static void renderC(SPRITE_RENDERER r, double from, double to, RECTANGLE body) {

		renderC(r, from, to, body.x1(), body.x2(), body.y1(), body.y2());
	}
	
	public static void renderC(SPRITE_RENDERER r, double from, double to, int x1, int x2, int y1, int y2) {

		from = CLAMP.d(from, 0, 1);
		to = CLAMP.d(to, 0, 1);
		int w = x2-x1;
		int cx = x1 + w/2;
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		GCOLOR.UI().bg().render(r, x1+1, x1+w-1, y1+1, y2-1);
		w -= 4;
		
		
		C_RED.bg.render(r, x1+2, cx, y1+2, y2-2);
		C_BLUE.bg.render(r, cx, x2-2, y1+2, y2-2);
		
		if (from < 0.5) {
			C_RED.dark.render(r, (int) (x1+2+(w/2)*from*2), cx, y1+3, y2-3);
		}else {
			C_BLUE.dark.render(r, cx, (int) (cx+(w/2)*(from-0.5)*2), y1+3, y2-3);
		}
		
		if (to < 0.5) {
			C_RED.dark.render(r, (int) (x1+2+(w/2)*to*2), cx, y1+4, y2-4);
			C_RED.bright.render(r, (int) (x1+2+(w/2)*to*2), cx, y1+4, y2-4);
		}else {
			C_BLUE.dark.render(r, cx, (int) (cx+(w/2)*(from-0.5)*2), y1+4, y2-4);
			C_BLUE.bright.render(r, cx, (int) (cx+(w/2)*(from-0.5)*2), y1+4, y2-4);
		}

		GCOLOR.UI().border().render(r, cx-1, cx+1, y1, y2);
	}
	
	public static void renderH(SPRITE_RENDERER r, GMeterCol color, double d, int x1, int x2, int y1, int y2) {

		int h = y2-y1;
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		color.bg.render(r, x1+1, x2-1, y1+1, y2-1);
		
		int dh = (int) ((h-4)*d);
		
		if (dh > 0) {
			color.dark.render(r, x1+2, x2-2, y2-2-dh, y2-2);
		}
		dh = (int) ((h-6)*d);
		if (dh > 0) {
			color.bright.render(r, x1+3, x2-3, y2-3-dh, y2-3);
		}
	}
	
	public static void renderH(SPRITE_RENDERER r, GMeterCol color, double d, RECTANGLE body) {
		renderH(r, color, d, body.x1(), body.x2(), body.y1(), body.y2());
	}
	
	public static void render(SPRITE_RENDERER r, GMeterCol color, double d1, double d2, int x1, int x2, int y1, int y2) {

		int w = x2-x1;
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		if (w <= 2)
			return;
		
		color.bg.render(r, x1+1, x2-1, y1+1, y2-1);
		
		if (w <= 4)
			return;
		d1 = CLAMP.d(d1, 0, 1);
		d2 = CLAMP.d(d2, 0, 1);
		double max = Math.max(d1, d2);
		double min = Math.min(d1, d2);
		int dx1 = (int) ((w-4)*min);
		int dx2 = (int) ((w-4)*max);
		
		color.between.render(r, x1+2, x1+2+dx2, y1+2, y2-2);
		
		COLOR c = color.bright;
		if (d2 < d1) {
			c = color.dark;
		}
		
		if (dx2-dx1 > 0) {
			OPACITY.O75TO100.bind();
			c.render(r, x1+dx1+2, x1+dx2+2, y1+2, y2-2);
			OPACITY.unbind();
		}
		
		
		
		
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, RECTANGLE body) {
		renderDelta(r, now, next, body.x1(), body.x2(), body.y1(), body.y2());
	}
	
	
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, int x1, int x2, int y1, int y2) {
		renderDelta(r, now, next, x1, x2, y1, y2, C_REDGREEN, C_BLUE, C_ORANGE, true);
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, int x1, int x2, int y1, int y2, boolean flash) {
		renderDelta(r, now, next, x1, x2, y1, y2, C_REDGREEN, C_BLUE, C_ORANGE, flash);
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, int x1, int x2, int y1, int y2, boolean flash, boolean frame) {
		renderDelta(r, now, next, x1, x2, y1, y2, C_REDGREEN, C_BLUE, C_ORANGE, flash, frame);
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, int x1, int x2, int y1, int y2, GMeterCol bg, GMeterCol good, GMeterCol bad, boolean flash) {
		renderDelta(r, now, next, x1, x2, y1, y2, bg, good, bad, flash, true);
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, int x1, int x2, int y1, int y2, GMeterCol bg, GMeterCol good, GMeterCol bad, boolean flash, boolean frame) {
		int w = x2-x1;
		now = CLAMP.d(now, 0, 1);
		next = CLAMP.d(next, 0, 1);
		if (frame) {
			GCOLOR.UI().border().render(r, x1, x2, y1, y2);
			GCOLOR.UI().bg().render(r, x1+1, x1+w-1, y1+1, y2-1);
		}
		bg.bg.render(r, x1+2, x2-2, y1+2, y2-2);
		
		renderFraction(r, bg.bright, 0, Math.max(now, next), x1, x2, y1, y2);
		
		if (flash)
			OPACITY.O25TO100.bind();
		if (next > now) {
			renderFraction(r, good.bright, now, next, x1, x2, y1, y2);
		}else {
			renderFraction(r, bad.bright, next, now, x1, x2, y1, y2);
			
		}
		OPACITY.unbind();
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, RECTANGLE body, GMeterCol col) {
		
		renderDelta(r, now, next, body.x1(), body.x2(), body.y1(), body.y2(), col);
	}
	
	public static void renderDelta(SPRITE_RENDERER r, double now, double next, int x1, int x2, int y1, int y2, GMeterCol col) {
		int w = x2-x1;
		now = CLAMP.d(now, 0, 1);
		next = CLAMP.d(next, 0, 1);
		GCOLOR.UI().border().render(r, x1, x2, y1, y2);
		GCOLOR.UI().bg().render(r, x1+1, x1+w-1, y1+1, y2-1);
		col.bg.render(r, x1+2, x2-2, y1+2, y2-2);
		
		renderFraction(r, col.between, 0, Math.max(now, next), x1, x2, y1, y2);
		
		if (next > now) {
			renderFraction(r, col.bright, now, next, x1, x2, y1, y2);
		}else {
			renderFraction(r, col.dark, next, now, x1, x2, y1, y2);
			
		}
	}
	
	
	private static void renderFraction(SPRITE_RENDERER r, COLOR color, double from, double to, int x1, int x2, int y1, int y2) {

		if (from > to) {
			double f = from;
			from = to;
			to = f;
		}
		
		
		int w = (x2-x1)-4;
		
		x1 += 2 + Math.ceil(from*w);
		x2 = (int) (x1 + Math.ceil((to-from)*w));
				
		
		w = x2-x1;
		if (w > 0) {
			ColorImp.TMP.set(color).shadeSelf(0.5);
			ColorImp.TMP.render(r, x1, x2, y1+2, y2-2);
			color.render(r, x1, x2, y1+3, y2-3);
		}
		
	}
	
	public static class GMeterCol {
		
		public final COLOR bg;
		public final COLOR dark;
		public final COLOR bright;
		public final COLOR between;
		private GMeterCol(COLOR bg, COLOR bg1, COLOR bg2) {
			this.bg = bg; 
			this.dark = bg1;
			this.bright = bg2;
			between = new ColorImp(dark).shadeSelf(1.5);
		}
		
		private GMeterCol(COLOR bg, COLOR dark, COLOR normal, COLOR bright) {
			this.bg = bg; 
			this.dark = dark;
			this.bright = bright;
			between = normal;
		}
	}
	
	public static SPRITE sprite(GMeterCol c, DOUBLE d, int width, int height) {
		return new GMeterSprite(c, d, width, height);
	}
	
	public static class GMeterSprite implements SPRITE{
		
		private final int width,height;
		private final DOUBLE d;
		private final GMeterCol c;
		
		public GMeterSprite(GMeterCol c, DOUBLE d, int width, int height){
			this.c = c;
			this.d = d;
			this.width = width;
			this.height = height;
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
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			double dd = d.getD();
			GMeter.render(r, dd < 0 ? C_INACTIVE : c, dd, X1, X2, Y1, Y2);
		}
	}
	

	


}
