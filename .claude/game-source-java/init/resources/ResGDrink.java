package init.resources;

import java.io.IOException;

import init.paths.PATH;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;

public final class ResGDrink extends ResG{

	public final COLOR color;
	public final boolean serve; 
	
	private ResGDrink(String key, int index, Json json) throws IOException {
		super(index, key, RESOURCES.map().get(key, json));
		this.color = new ColorImp(json);
		serve = !json.bool("DONT_SERVE", false);
	}
	
	static ResGroup<ResGDrink> make(final PATH pathData) throws IOException{
		String folder = "drinkable";
		final PATH pd = pathData.getFolder(folder);
		
		String[] files = pd.getFiles();
		final ArrayList<ResGDrink> res = new ArrayList<>(files.length);
		
		for (String p : files) {
			Json j = new Json(pd.get(p));
			ResGDrink g = new ResGDrink(p, res.size(), j);
			res.add(g);
		}
		
		return new ResGroup<>("DRINKABLE", res);
		
	}

	
}
