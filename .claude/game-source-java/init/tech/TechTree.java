package init.tech;

import java.io.IOException;

import game.GAME;
import init.tech.TechCurrency.TechCurrencies;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

public class TechTree {

	public final static int MAX_COLS = 10;
	
	public final COLOR color;
	public final TECH[][] nodes;
	public final String key;
	public final CharSequence name;
	public final int cat;
	
	TechTree(TechCurrencies cc, String key, Json jData, Json jText, LISTE<TECH> all) throws IOException{
		this.key = key;
		
		color = new ColorImp(jData);
		cat = jData.i("CATEGORY", 0, 5, 0);
		name = jText.text("NAME");
		
		Json rows = jData.json("TREE");
		
		Json techs = jData.has("TECHS") ? jData.json("TECHS") : null;
		Json texts = jText.has("TECHS") ? jText.json("TECHS") : null;
		
		nodes = new TECH[rows.keys().size()][];
		int ri = 0;

		for (String __ : rows.keys()) {
			String[] values = rows.values(__);
			if (values.length > MAX_COLS)
				rows.error("Max columns in a row must be " + MAX_COLS + "to fit on smaller screeens", key);
			nodes[ri] = new TECH[values.length];
			
			for (int ci = 0; ci < values.length; ci++) {
				String v = values[ci];
				if (v.equals("_____"))
					continue;
				Json data;
				Json text;
				if (techs != null && techs.has(v)) {
					data = techs.json(v);
					text = texts.has(v) ? texts.json(v) : null;
				}else {
					GAME.Warn(rows.errorGet("there is no tech in the nodes folder named: " + v, v));
					continue;
				}
				TECH t = new TECH(cc, this.key + "_" + v, all, data, text, this, ci, ri);
				nodes[ri][ci] = t;
				
				
			}
			
			ri++;
			
		}
		
	}
	
}
