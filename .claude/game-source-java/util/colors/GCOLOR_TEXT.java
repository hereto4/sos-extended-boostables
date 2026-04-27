package util.colors;

import game.faction.Faction;
import init.paths.PATHS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.ColorShifting;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;

public final class GCOLOR_TEXT {

	private Json d = new Json(PATHS.SPRITE_UI().getLikeHell("Colors.txt")).json("TEXT");
	
	public final COLOR IGREAT = new ColorImp(d, "IGREAT");
	public final COLOR IGOOD = new ColorImp(d, "IGOOD");
	public final COLOR INORMAL = new ColorImp(d, "INORMAL");
	public final COLOR IBAD = new ColorImp(d, "IBAD");
	public final COLOR IWORST = new ColorImp(d, "IWORST");
	
	public final COLOR HOVERABLE = new ColorImp(d, "HOVERABLE");
	public final COLOR H1 = new ColorImp(d, "H1"); //105,65,7
	public final COLOR H2 = new ColorImp(d, "H2");
	public final COLOR ERROR = new ColorImp(d, "ERROR");
	public final COLOR WARNING = new ColorImp(d, "WARNING");
	
	public final COLOR CLICKABLE = new ColorImp(d, "CLICKABLE");
	public final COLOR HOVERED = new ColorShifting(new ColorImp(d, "HOVERED"),
			new ColorImp(d, "HOVERED_SELECTED"));
	public final COLOR SELECTED = new ColorImp(d, "SELECTED");
	public final COLOR HOVER_SELECTED = new ColorShifting(new ColorImp(d, "HOVERED"),
			new ColorImp(d, "HOVERED_SELECTED"));
	public final COLOR INACTIVE = new ColorImp(d, "INACTIVE"); //COLOR.BROWN; //72, 58, 33
	public final COLOR NORMAL = new ColorImp(d, "NORMAL");//new ColorImp(110,75,25);
	public final COLOR NORMAL2 = new ColorImp(d, "NORMAL2");
	
	public COLOR faction(Faction faction) {
		
		if (faction == null)
			return COLOR.WHITE65;
		return ColorImp.TMP.set(faction.banner().colorBG()).shadeSelf(1.5);
		
	}
	
	private final ColorImp tmp = new ColorImp();
	
	public COLOR bronzeGold(double d) {
		d = CLAMP.d(d, 0, 1);
		if (d < 0.5) {
			tmp.interpolate(INACTIVE, H2, d*2);
		}else {
			tmp.interpolate(H2, H1, (d-0.5)*2);
		}
		return tmp;
	}
	
	GCOLOR_TEXT () {
		
	}
	
}
