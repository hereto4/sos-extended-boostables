package settlement.tilemap.ground;

import init.constant.C;
import init.sprite.UI.Icon;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.info.INFO;

public class GroundType extends INFO implements MAP_BOOLEAN{

	public final int index;
	public final TILE_SHEET sheet;
	
	public final ColorImp miniC = new ColorImp();
	boolean special;
	final ColorImp[] tmps = new ColorImp[Ground.MOISTURE_MAX+1];
	public final SPRITE icon;
	
	public final double vegitation;
	public final double farm;
	
	protected GroundType(int index, TILE_SHEET sheet, CharSequence name, CharSequence desc, double vegitation, double farm){
		super(name, desc);
		this.index = index;
		this.sheet = sheet;
		for (int i = 0; i < tmps.length; i++)
			tmps[i] = new ColorImp();
		
		icon = new SPRITE.Imp(Icon.L) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				tmps[8].bind();
				sheet.render(r, 0, X1, X1+C.T_PIXELS, Y1, Y1+C.T_PIXELS);
				sheet.render(r, 1, X1+C.T_PIXELS, X2, Y1, Y1+C.T_PIXELS);
				sheet.render(r, 2, X1, X1+C.T_PIXELS, Y1+C.T_PIXELS, Y2);
				sheet.render(r, 3, X1+C.T_PIXELS, X2, Y1+C.T_PIXELS, Y2);
				COLOR.unbind();
			}
		};
		this.vegitation = vegitation;
		this.farm = farm;
	}
	
	public void setColors(COLOR dry, COLOR wet, double add) {
		if (special)
			return;
		for (int i = 0; i < tmps.length; i++) {
			
			double d = i*Ground.MOISTURE_MAXI;
			
			tmps[i].interpolate(dry, wet, CLAMP.d(d + add, 0, 1));
		}
		miniC.interpolate(dry, wet, 0.5 - index/8.0);
	}
	
	GroundType setColors(Json json) {
		
		COLOR dry = new ColorImp(json, "DRY");
		COLOR wet = new ColorImp(json, "WET");
		setColors(dry, wet, 0);
		special = true;
		return this;
	}

	public void placeFixed(int x, int y) {
		SETT.GROUND().MAP.set(x, y, this);
		
	}

	@Override
	public boolean is(int tile) {
		return SETT.GROUND().MAP.get(tile) == this;
	}

	@Override
	public boolean is(int tx, int ty) {
		return SETT.GROUND().MAP.get(tx, ty) == this;
	}

	public COLOR col(int tile) {
		return tmps[SETT.GROUND().mapMoistureCurrent.get(tile)];
	}
	

}
