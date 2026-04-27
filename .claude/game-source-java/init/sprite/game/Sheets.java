package init.sprite.game;

import java.io.IOException;

import init.sprite.SPRITES;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class Sheets {

	public final LIST<SheetPair> sheets;

	
	private static final ArrayList<COLOR> shades = new ArrayList<>(48);
	static {
		
		for (int i = 0; i < 48; i++) {
			int d = i /3;
			int q = i %3;
			shades.add(new ColorImp(127-d*2-2*(q&1), 127-d*2-2*((q>>1)&1), 127-d*2-2*((q>>2)&1)));
		}
		
	}
	
	public Sheets(SheetType type, Json json) throws IOException{
		sheets = SPRITES.GAME().sheets(type, json);
	}
	
	public Sheets(Sheet s, SheetData d) throws IOException{
		sheets = new ArrayList<SheetPair>(new SheetPair(s, d));
	}
	
	public SheetPair get(int random) {
		if (sheets.size() == 0)
			return null;
		return sheets.getC(random);
	}


	
}
