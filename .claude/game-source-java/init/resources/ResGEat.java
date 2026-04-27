package init.resources;

import java.io.IOException;

import init.paths.PATH;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;

public final class ResGEat extends ResG{

	public final boolean serve;
	
	private ResGEat(String key, int index, Json json) throws IOException {
		super(index, key, RESOURCES.map().get(key, json));
		serve = !json.bool("DONT_SERVE", false);
	}
	
	static ResGroup<ResGEat> make(final PATH pathData) throws IOException{
		String folder = "edible";
		final PATH pd = pathData.getFolder(folder);
		
		String[] files = pd.getFiles();
		final ArrayList<ResGEat> res = new ArrayList<>(files.length);
		
		for (String p : files) {
			Json j = new Json(pd.get(p));
			ResGEat g = new ResGEat(p, res.size(), j);
			res.add(g);
		}
		
		return new ResGroup<>("EDIBLE", res);
		
	}

	
}
