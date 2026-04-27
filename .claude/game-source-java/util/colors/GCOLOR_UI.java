package util.colors;

import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.ColorShifting;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;

public final class GCOLOR_UI {
	
	private Json d = new Json(PATHS.SPRITE_UI().getLikeHell("Colors.txt")).json("UI");
	
	public final GColorUIModel NORMAL = new GColorUIModel(new ColorImp(d, ("NORMAL")));
	public final GColorUIModel BAD = new GColorUIModel(new ColorImp(d, ("BAD")));
	public final GColorUIModel GOOD = new GColorUIModel(new ColorImp(d, ("GOOD")));
	public final GColorUIModel NEUTRAL = new GColorUIModel(new ColorImp(d, ("NEUTRAL")));
	public final GColorUIModel GOOD2 = new GColorUIModel(new ColorImp(d, ("GOOD2")));
	public final GColorUIModel GREAT = new GColorUIModel(new ColorImp(d, ("GREAT")));
	public final GColorUIModel SOSO = new GColorUIModel(new ColorImp(d, ("SOSO")));
	private static final ColorImp tmp = new ColorImp();
	private final COLOR badShift = new ColorShifting(bg(), new ColorImp(d, ("BAD_SHIFT"))).setSpeed(1);
	private final COLOR goodShift = new ColorShifting(bg(), new ColorImp(d, ("GOOD_SHIFT"))).setSpeed(1);
	
	private final COLOR border = COLOR.WHITE35;
	private final COLOR borderB = border.shade(1.5);
	private final COLOR borderD = border.shade(0.5);
	
	public final COLOR panBG = COLOR.WHITE15;
	
	GCOLOR_UI() {
		
	}
	public final COLOR gold = new ColorImp(d, ("GOLD"));
	
	public static class GColorUIModel {
		
		public final COLOR normal;
		public final COLOR hovered;
		public final COLOR selected;
		public final COLOR inactive;
		
		private GColorUIModel(COLOR color) {
			this.inactive = color.shade(0.55);
			this.normal = color.shade(0.8);
			this.hovered = color;
			this.selected = color.shade(1.2);
		}
		
		public COLOR get(boolean isActive, boolean isSelected, boolean isHovered) {
			if (!isActive)
				return inactive;
			if (isHovered)
				return hovered;
			if (isSelected)
				return selected;
			return normal;
		}
		
	}
	
	public COLOR border() {
		return COLOR.WHITE35;
	}
	
	public COLOR bg() {
		return COLOR.WHITE10;
	}
	
	public COLOR bg(boolean isActive, boolean isSelected, boolean isHovered) {
		if (!isActive)
			return COLOR.WHITE10;
		if (isSelected)
			return COLOR.WHITE30;
		if (isHovered)
			return COLOR.WHITE25;
		return COLOR.WHITE15;
	}
	
	public static COLOR color(COLOR color, boolean isActive, boolean isSelected, boolean isHovered) {
		
		if (isSelected)
			return tmp.set(color).add(36);
		if (!isActive) {
			tmp.set(color).saturateSelf(0.7);
			return tmp.add(-5);
		}
		if (isHovered)
			return tmp.set(color).add(20);
		return color;
	}
	
	public GColorUIModel bgHov() {
		return NORMAL;
	}
	
	public COLOR badFlash() {
		return badShift;
	}
	
	public COLOR goodFlash() {
		return goodShift;
	}
	
	public GColorUIModel BAD() {
		return BAD;
	}
	
	public GColorUIModel GOOD() {
		return GOOD;
	}
	
	public GColorUIModel SOSO() {
		return SOSO;
	}

	
	public void badToGood(ColorImp imp, double v) {
		v = CLAMP.d(v, 0, 1);
		if (v < 0.5) {
			imp.interpolate(BAD.normal, SOSO.normal, v*2);
		}else {
			imp.interpolate(SOSO.normal, GOOD.normal, (v-0.5)*2);
		}
	}
	
	
	public void border(SPRITE_RENDERER ren, int X1, int X2, int Y1, int Y2) {
		borderB.render(ren, X1, X1+1, Y1, Y2);
		borderB.render(ren, X1, X2, Y1, Y1+1);
		borderD.render(ren, X2-1, X2, Y1+1, Y2);
		borderD.render(ren, X1+1, X2, Y2-1, Y2);
		border.render(ren, X1+1, X2-1, Y1+1, Y2-1);
	}
	
	public void border(SPRITE_RENDERER ren, RECTANGLE b, int m) {
		border(ren, b.x1()+m, b.x2()-m, b.y1()+m, b.y2()-m);
	}
	
	public void borderH(SPRITE_RENDERER ren, int X1, int X2, int Y1, int Y2) {
		borderB.render(ren, X1, X1+1, Y1, Y2);
		border.render(ren, X1+1, X1+2, Y1+1, Y2-1);
		borderD.render(ren, X1+2, X1+3, Y1+2, Y2-2);
		
		borderD.render(ren, X2-1, X2, Y1, Y2);
		border.render(ren, X2-2, X2-1, Y1+1, Y2-1);
		borderB.render(ren, X2-3, X2-2, Y1+2, Y2-2);
		
		borderB.render(ren, X1, X2, Y1, Y1+1);
		border.render(ren, X1+1, X2-1, Y1+1, Y1+2);
		borderD.render(ren, X1+2, X2-2, Y1+2, Y1+3);
		
		borderD.render(ren, X1, X2, Y2-1, Y2);
		border.render(ren, X1+1, X2-1, Y2-2, Y2-1);
		borderB.render(ren, X1+2, X2-2, Y2-3, Y2-2);
		
		
	}
	
	public void borderH(SPRITE_RENDERER ren, RECTANGLE b, int m) {
		borderH(ren, b.x1()+m, b.x2()-m, b.y1()+m, b.y2()-m);
	}
	
}
