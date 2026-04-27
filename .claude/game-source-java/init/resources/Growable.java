package init.resources;

import java.io.IOException;

import init.paths.PATH;
import init.type.CLIMATE;
import init.type.CLIMATES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public final class Growable extends ResG{


	public final double seasonalOffset;
	public final double growthValue;
	public final COLOR colorMinimap;

	private final double[] climate;
	public final GrowableSprite sprite;
	
	private Growable(String key, int index, Json json, KeyMap<TILE_SHEET> sheetMap) throws IOException{
		super(index, key, RESOURCES.map().read(json));
		seasonalOffset = json.d("SEASONAL_OFFSET", 0, 1); 
		growthValue = json.d("GROWTH_VALUE", 0, 1.0);
		this.colorMinimap = new ColorImp(json, "MINIMAP_COLOR");
		climate = new double[CLIMATES.ALL().size()];
		CLIMATES.MAP().readFill("CLIMATE_BONUS", climate, json, 0, 10000);
		
		{
			json = json.json("SPRITE");
			
			
			double poll = json.d("POLLEN", 0, 10);
			double wind = json.dTry("WIND_SWAY", 0, 10, 1);
			
			sprite = new GrowableSprite(json.value("SPRITE"), wind, poll, sheetMap);
			
			sprite.setPollenColor(new ColorImp(json, "COLOR_POLLEN"));
			
			set(json.json("STEM"), sprite.trunk);
			set(json.json("GROWTH"), sprite.growth);
			
			
		}
	}
	
	private static void set(Json json, GrowableSprite.Part part) {
		part.sheightoverGround = json.d("SHADOW_HEIGHT", 0, 32);
		part.sheight = json.d("SHADOW_LENGTH", 0, 32);
		part.setColors(new ColorImp(json, "DEAD"), new ColorImp(json, "LIVE"), new ColorImp(json, "RIPE"));
		if (json.has("WIND_SWAY"))
			part.sway = json.d("WIND_SWAY", 0, 10);
	}

	public void render(SPRITE_RENDERER r, ShadowBatch shadowBatch, RenderData.RenderIterator it, int amount, boolean ripe) {
		
	}
	
	public double availability(CLIMATE c) {
		return climate[c.index()];
	}


	static GrowableGroup make(final PATH pathData, final PATH pathSprites) throws IOException{
		String folder = "growable";
		final PATH pd = pathData.getFolder(folder);
		
		
		KeyMap<TILE_SHEET> sheetMap = new KeyMap<TILE_SHEET>();
		String[] files = pd.getFiles();
		final ArrayList<Growable> res = new ArrayList<>(files.length);
		
		for (String p : files) {
			Json j = new Json(pd.get(p));
			Growable g = new Growable(p, res.size(), j, sheetMap);
			res.add(g);
		}
		
		return new GrowableGroup(sheetMap, res);
		
	}
	
	public static class GrowableGroup extends ResGroup<Growable> {

		private final KeyMap<TILE_SHEET> sheetMap;
		
		GrowableGroup(KeyMap<TILE_SHEET> sheetMap, ArrayList<Growable> res) {
			super("GROWABLE", res);
			this.sheetMap = sheetMap;
		}
		
		public GrowableSprite sprite(String ssheet, double wind, double pollen) throws IOException {

			return new GrowableSprite(ssheet, wind, pollen, sheetMap);
		}
	}
	
}
