package game.faction;

import init.paths.PATHS;
import init.sprite.BitmapSprite;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LIST;
import world.region.RD;
import world.region.pop.RDRace;

final class Initer {
	
	Initer(LIST<Faction> all) {
		
		
		setColors(all);
		
		RDRace race = RD.RACE(FACTIONS.player().race());
		FACTIONS.player().name.clear().add("Jakaton");
		if (race != null)
			FACTIONS.player().name.clear().add(race.names.fNames.next());
		
		{
			
			

			Bitmap2D[] datas = BitmapSprite.read(PATHS.SPRITE().getFolder("ui").get("FactionBanners"));

			for (int i = 0; i < datas.length; i++) {
				int i2 = RND.rInt(datas.length);
				Bitmap2D old = datas[i];
				datas[i] = datas[i2];
				datas[i2] = old;
			}
			
			int ki = 0;
			for (Faction f : all) {
				f.banner().sprite.paint(datas[ki++]);
				ki = ki % datas.length;
			}
			
		}
		
		
		
	}
	
	private void setColors(LIST<Faction> all) {
		ArrayList<ColorImp> cols = new ArrayList<>(ColorImp.cols(new Json(PATHS.WORLD().init.getFolder("config").get("Faction")), "COLORS"));
		for (int i = 0; i < cols.size(); i++)
			cols.swap(i, RND.rInt(cols.size()));
		
		int i = 0;
		for (Faction f : all) {
			
			int kk = i/cols.size();
			COLOR col = cols.getC(i);
			if (kk > 0) {
				col = new ColorImp().interpolate(col, cols.getC(i+kk), 0.5);
			}
			
			f.banner().colorFG().set(cols.getC(i + cols.size()/2));
			f.banner().colorBG().set(col);
			i++;
		}
	}
	
}
