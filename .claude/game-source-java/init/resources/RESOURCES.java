package init.resources;

import java.io.IOException;

import init.INIT;
import init.INIT.InitResource;
import init.paths.PATH;
import init.paths.PATHS;
import init.resources.Growable.GrowableGroup;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.keymap.RMAP;
import util.keymap.RMAPS;
import util.text.D;

public final class RESOURCES extends InitResource{
	
	private static Data data;
	
	public static final String KEY = "RESOURCE";
	public static final String KEYS = "RESOURCES";
	
	private static CharSequence ¤¤helpStone = "Stone can be obtained by manually clearing rocks on the ground.";
	private static CharSequence ¤¤helpWood = "Wood can be obtained by manually clearing trees.";
	private static CharSequence ¤¤helpGrow = "This crop might be growing in the wild. Have a look, maybe you can harvest some manually.";
	
	static {
		D.ts(RESOURCES.class);
	}
	
	
	private static final class Data{
		
		private final LIST<RESOURCE> all;
		private final RMAPS<RESOURCE> map;
		private final RMAP<Minable> minable;
		private final ResGroup<ResGDrink> drinks;
		private final GrowableGroup growable;
		private final ResGroup<ResGEat> edibles;
		private final RESOURCE STONE,WOOD,LIFESTOCK;
		private final int catAmount;
		private final ResSupplies supplies;
		

		Data() throws IOException{
			data = this;
			
			PATH gInit = PATHS.INIT().getFolder("resource");
			PATH gText = PATHS.TEXT().getFolder("resource");
			PATH gSprite = PATHS.SPRITE().getFolder("resource");
			PATH gDebris = gSprite.getFolder("debris");
			
			String[] files;
			
			{
				String[] fixed = new String[] {
					"_STONE",
					"_WOOD",
					"_LIVESTOCK",
				};
				String[] mod = gInit.getFiles();
				files = new String[fixed.length+mod.length];
				for (int i = 0; i < fixed.length; i++)
					files[i] = fixed[i];
				for (int i = 0; i < mod.length; i++) {
					files[i+fixed.length] = mod[i];
				}
				
				final String[][] resources = new String[10][64];
				int[] catI = new int[10];
				boolean[] categories = new boolean[10];
				int cats = 0;
				
				for (String s : files) {
					Json in = new Json(gInit.get(s));
					int c = in.i("CATEGORY_DEFAULT", 0, 10);
					resources[c][catI[c]] = s;
					catI[c]++;
					if (!categories[c]) {
						cats++;
						categories[c] = true;
					}
				}
				
				catAmount = cats;

				int q = 0;
				for (int i = 0; i < resources.length; i++) {
					for (int k = 0; k < catI[i]; k++) {
						files[q++] = resources[i][k];
					}
				}
				
			}
			
			
			{
				
				
				ArrayList<RESOURCE> all = new ArrayList<>(128); 
				KeyMap<Sprite> spriteMap = new KeyMap<>();
				KeyMap<TILE_SHEET> debrisMap = new KeyMap<>();
				
				
				

				
				for (String key : files) {
					new RESOURCE(all, key, gInit, gText, gSprite, gDebris, spriteMap, debrisMap);
				}
				
				
				this.all = new ArrayList<RESOURCE>(all);
				
				

				
				map = new RMAPS<>(KEY, this.all);
				
				
				
				STONE = map.get("_STONE", null);
				WOOD = map.get("_WOOD", null);
				LIFESTOCK = map.get("_LIVESTOCK", null);
				
				//map.debug();
			}
			
			{
				minable = Minable.make(gInit, gSprite);
				growable = Growable.make(gInit, gSprite);
				drinks = ResGDrink.make(gInit);
				edibles = ResGEat.make(gInit);
			}
			
			{
//				ArrayListGrower<RESOURCE> edi = new ArrayListGrower<>();
//				ArrayListGrower<RESOURCE> drinki = new ArrayListGrower<>();
//				for (RESOURCE res : all) {
//					if (res.isEdible())
//						edi.add(res);
//					if (res.drinkable)
//						drinki.add(res);
//				}
//				if (edi.size() == 0)
//					throw new Errors.DataError("not enough edible resources have been declared");
//				
//				if (drinki.size() == 0)
//					throw new Errors.DataError("not enough drinkable resources have been declared");
//				
//				edibles = new ResGroup("EDIBLE_RESOURCE", edi);
				
			}
			supplies = new ResSupplies();
			
			STONE.specialHelpText = ¤¤helpStone;
			WOOD.specialHelpText = ¤¤helpWood;
			for (Growable g : growable.all()) {
				g.resource.specialHelpText = ¤¤helpGrow;
			}
		}

	

		
	}
	
	public RESOURCES(INIT init) throws IOException{
		super(init);
		new Data();
	}
	
	public static LIST<RESOURCE> ALL(){
		return data.all;
	}
	
	public static RMAP<Minable> minables(){
		return data.minable;
	}
	
	public static GrowableGroup growable(){
		return data.growable;
	}
	
	public static ResGroup<ResGDrink> DRINKS() {
		return data.drinks;
	}
	
	public static ResGroup<ResGEat> EDI() {
		return data.edibles;
	}
	
	public static RESOURCE STONE() {
		return data.STONE;
	}
	
	public static RESOURCE WOOD() {
		return data.WOOD;
	}
	
	public static RESOURCE LIVESTOCK() {
		return data.LIFESTOCK;
	}
	
	public static int CATEGORIES() {
		return data.catAmount;
	}
	
	public static ResSupplies SUP() {
		return data.supplies;
	}
	
	public static RMAPS<RESOURCE> map(){
		return data.map;
	}

	
}
