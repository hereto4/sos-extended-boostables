package view.world.generator.tools;

import static world.WORLD.FOREST;
import static world.WORLD.MOUNTAIN;
import static world.WORLD.WATER;

import game.boosting.BoostSpec;
import game.boosting.BoostableCat;
import init.race.Race;
import init.resources.Minable;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAIN;
import init.type.TERRAINS;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.GUTIL;
import util.data.BOOLEANO;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.WORLD;
import world.map.regions.centre.WCentre;
import world.map.terrain.WorldTerrainInfo;

public class UIWorldToolCapitolPlaceInfo {

	private static CharSequence ¤¤moisture = "Moisture";
	private static CharSequence ¤¤moistureD = "High natural moisture spots have little need for irrigation. Low moisture spots have more fertile soil.";
	
	
	
	private static CharSequence ¤¤climate = "¤{0} do not prefer climate: {1}. It will be a bit harder to please them.";
	private static CharSequence ¤¤isolated = "¤This location is isolated. Initially you will be left alone by other factions, but there will be little opportunity for trade.";
	private static CharSequence ¤¤neigh = "¤You will have few of your own species nearby, which might make expanding harder in late game.";
	

	static {D.ts(UIWorldToolCapitolPlaceInfo.class);}

	private final GuiSection s = new GuiSection();
	private final WorldTerrainInfo info = new WorldTerrainInfo();
	private final WorldTerrainInfo area = new WorldTerrainInfo();
	
	public UIWorldToolCapitolPlaceInfo() {

		int m = 170;
		
		s.addDown(0, new GStat() {
			
			@Override
			public void update(GText text) {
				text.add(WORLD.CLIMATE().getter.get(info.tx, info.ty).name);
			}
			
		}.hh(CLIMATES.INFO().name, m));
		
		s.addDown(2, new GHeader.HeaderHorizontal(Dic.¤¤Fertility, new GMeter.GMeterSprite(GMeter.C_REDGREEN, info.fertility(), 64, 12), m));
		
		s.body().incrH(14);
		
		for (TERRAIN t : TERRAINS.ALL()) {
			s.add(t.icon(), 0, s.body().y2()-2);
			s.addRightC(4, new GText(UI.FONT().S, t.name).lablifySub());
			s.addCentredY(new GMeter.GMeterSprite(GMeter.C_ORANGE, info.get(t), 64, 12), m);
		}

		s.body().incrW(48);
		
	}
	
	private final BOOLEANO<BoostSpec> filter = new BOOLEANO<BoostSpec>() {

		@Override
		public boolean is(BoostSpec t) {
			return (t.boostable.cat.typeMask & BoostableCat.TYPE_SETT) != 0;
		}
		
	};
	
	public void placeInfo(GBox b, int tx1, int ty1, Race race) {
		
	
		info.initCity(tx1, ty1);
		int cx = tx1+WCentre.TILE_DIM/2;
		int cy = ty1+WCentre.TILE_DIM/2;
		
		{
			CLIMATE climate = WORLD.CLIMATE().getter.get(cx, cy);
			
			b.textLL(CLIMATES.INFO().name);
			b.tab(6);
			b.text(WORLD.CLIMATE().getter.get(cx, cy).name);
			b.NL();
			
			climate.boosters.hover(b, 1.0, null, filter, -1);
			
			b.NL();
			
			double cl = race.population().climate(climate);
			if (cl < race.population().maxClimate()) {
				GText t = b.text();
				t.add(¤¤climate);
				t.insert(0, race.info.names);
				t.insert(1, climate.name);
				t.warnify();
				b.add(t);
				
			}
			b.sep();
		}
		
		{
			
			for (Minable m : RESOURCES.minables().all()) {
				double d = 0;
				for (TERRAIN te : TERRAINS.ALL()) {
					d += info.get(te).getD()*m.terrain(te);
				}
				b.add(m.resource.icon());
				b.add(GFORMAT.perc(b.text(), 4.0*d));
			}
			
			b.NL(4);
			
			b.textLL(¤¤moisture);
			b.tab(6);
			b.add(GFORMAT.percGood(b.text(), info.fertility().getD()));
			b.NL();
			b.text(¤¤moistureD);
			b.NL(8);
			
			double v = 0;
			for (TERRAIN te : TERRAINS.ALL()) {
				
				if (info.get(te).getD() > 0) {
					b.textLL(te.name);
					b.tab(6);
					b.add(GFORMAT.percGood(b.text(), info.get(te).getD()));
					b.NL();
					b.text(te.desc);
					b.NL(8);
				}
				double t = area.get(te).getD()*race.population().terrain(te);
				for (CLIMATE c : CLIMATES.ALL())
					v += t * race.population().climate(c)*area.get(c).getD();
			}
			v /= (race.population().maxClimate()*race.population().maxTerrain());
			
			if (v < 0.25) {
				b.add(b.text().warnify().add(¤¤neigh));
				
			}
			b.sep();
		}
		
//		{
//			boolean mi = false;
//			for (Minable min : RESOURCES.minables().all()) {
//				
//				if (info.minable(min) > 0) {
//					b.add(min.resource.icon());
//					b.textL(min.resource.name);
//					b.tab(6);
//					b.add(GFORMAT.iIncr(b.text(), info.minable(min)));
//					b.NL();
//					mi = true;
//				}
//			}
//			
//			if (!mi) {
//				b.add(b.text().warnify().add(¤¤Minerals));
//				b.NL();
//			}
//			
//		}
		
		b.NL(8);
		
		int size = getSize(cx, cy,  1500);
		
		if (size < 750) {
			b.add(b.text().warnify().add(¤¤isolated));
			b.NL(8);
		}
		
//		if (info.fertility().getD() < 0.3) {
//			b.add(b.text().warnify().add(¤¤Fertility));
//			b.NL();
//		}
//		
//		
//		if (info.get(TERRAINS.FOREST()).getD() < 0.1) {
//			b.add(b.text().warnify().add(¤¤Forest));
//			b.NL();
//		}
//		
//		if (info.get(TERRAINS.WET()).getD() <= 0.1) {
//			b.add(b.text().warnify().add(¤¤Water));
//			b.NL();
//		}
	
	}
	
	private int getSize(int sx, int sy, int max) {
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(sx, sy, 0);
		area.clear();
		int size = 0;
		while(GUTIL.flooder().hasMore() && size < max) {
			PathTile t = GUTIL.flooder().pollSmallest();
			size ++;
			area.add(t.x(), t.y());
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				int toX = t.x()+d.x();
				int toY = t.y()+d.y();
				if (!WORLD.IN_BOUNDS(toX, toY))
					continue;
				if (WATER().has.is(t.x(), t.y())) {
					if (!WATER().canCrossByLand(t.x(), t.y(), toX, toY))
						continue;
				}
				if (MOUNTAIN().coversTile(toX, toY))
					continue;
				
				if (FOREST().amount.get(t.x(), t.y()) == 1.0 && FOREST().amount.get(toX, toY) == 1)
					continue;
				
				GUTIL.flooder().pushSmaller(toX,  toY, t.getValue()+d.tileDistance());
				
			}
		}
		area.divide(size);
		GUTIL.flooder().done();
		return size;
		
	}
	
	
}
