package init.sprite.UI;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Font;
import util.spritecomposer.ComposerFonter;
import util.spritecomposer.ComposerThings.IFont;
import util.spritecomposer.ComposerUtil;

public final class UIFonts{
	
	
	public final Font H2;
	public final Font H1;
	public final Font S;
	public final Font M;
	public final LIST<Font> all;
	
	UIFonts() throws IOException{

		Json json = new Json(PATHS.CONFIG().get("Charset"));
		CharSequence cs = json.text("CHARS");
		final int trail = json.i("SPACING", 0, 32, 0);
		
		Font.setCharset(cs);
		PATH g = PATHS.SPRITE().getFolder("font");
		
		KeyMap<Boolean> map = new KeyMap<>();
		
		for (String s : g.getFiles()) {
			map.put(s, true);
		}
		
		S = get(g, "Small", 2*trail/3); 
		M = get(g, "Medium", trail); 
		
		if (map.containsKey("Header1")) {
			H1 = get(g, "Header1", trail); 
		}else {
			H1 = M;
		}

		if (map.containsKey("Header2")) {
			H2 = get(g, "Header2", 2*trail/3); 
		}else {
			H2 = M; 
		}

				
		all = new ArrayList<Font>(H2,H1,M,
				S);
		
	}
	
	private Font get(PATH g, String name, int trail) throws IOException {
		if (g.exists(name))
			return new IFont(g.get(name)) {
			@Override
			protected Font init(ComposerUtil c, ComposerFonter f) {
				
				return f.save(0, 0, trail);
			}
		}.get(trail);
		return M;
	}
	
}