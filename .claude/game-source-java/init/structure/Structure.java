package init.structure;

import game.faction.player.PlayerColors.PlayerColor;
import init.constant.C;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.tilemap.terrain.TBuilding;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;
import util.info.INFO;
import util.keymap.MAPPED;
import util.text.Dic;

public final class Structure extends INFO implements MAPPED {

	public final String key;
	public final CharSequence nameWall;
	public final CharSequence nameCeiling;
	public final double durability;
	public final RESOURCE resource;
	public final int resAmount;
	public final PlayerColor tint;
	public final COLOR miniColor;
	private final int index;

	public final double constructTime;

	Structure(String key, LISTE<Structure> all, Json data, Json text) {
		super(text);
		this.key = key;
		nameWall = text.text("NAME_WALL");
		nameCeiling = text.text("NAME_CEILING");
		
		constructTime = data.d("BUILD_TIME", 0, 10000);
		durability = data.d("DURABILITY", 0, 1.0)*C.TILE_SIZE;
		if (data.has("RESOURCE")) {
			resource = RESOURCES.map().read(data);
			resAmount = data.i("RESOURCE_AMOUNT", 0, 16);
		}else {
			resource = null;
			resAmount = 0;
		}
		
		tint = new PlayerColor(new ColorImp(data), "BUILDING_" + key, Dic.¤¤Structures, name);
		miniColor = new ColorImp(data, "MINIMAP_COLOR");
		
		index = all.add(this);
	}

	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String key() {
		return key;
	}
	
	public TBuilding terrain() {
		return SETT.TERRAIN().BUILDINGS.get(this);
	}

}
