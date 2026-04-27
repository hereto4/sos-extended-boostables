package view.sett.ui.standing;

import init.type.BUILDING_PREF;
import init.type.BUILDING_PREFS;
import init.type.HCLASS;
import settlement.main.SETT;
import settlement.room.water.pool.ROOM_POOL;
import settlement.stats.STATS;
import settlement.stats.colls.StatsAccess.StatMonument;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import util.gui.misc.GBox;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import view.sett.ui.standing.Cats.Cat;

final class CatEnv extends Cat {
	
	CatEnv(HCLASS cl){
		super(new StatCollection[] { STATS.ENV(), STATS.ACCESS(), STATS.ACCESS().ACCESS, STATS.ACCESS().MONUMENTS, STATS.BATTLE(), STATS.STORED()});
		titleSet(cs[0].info.name);
		
		LinkedList<RENDEROBJ> rens = new LinkedList<>();
		
		for (StatCollection c : cs){
			if (c.all().size() == 0)
				continue;
			if (c == STATS.ACCESS().MONUMENTS) {
				rens.add(new StatRow.Title(c.info));
				for (StatMonument s : STATS.ACCESS().MONUMENTS.ALL()) {
					rens.add(new StatRow(s, cl));
					if (s.statUpgrade != null)
						rens.add(new StatRow(s.statUpgrade, cl));
				}
			}else if (c != STATS.STORED()) {
				rens.add(new StatRow.Title(c.info));
				for (STAT s : c.all()) {
					if (s == STATS.ENV().BUILDING_PREF) {
						rens.add(new StatRow(s, cl) {
							
							@Override
							public void hoverInfoGet(GUI_BOX text) {
								
								super.hoverInfoGet(text);
								if (CitizenMain.current != null) {
									GBox b = (GBox) text;
									b.NL(8);
									for (BUILDING_PREF p : BUILDING_PREFS.ALL()) {
										b.add(p.icon());
										b.add(GFORMAT.perc(b.text(), CitizenMain.current.pref().structure(p)));
									}
								}
							}
							
						});
					}else if (s == STATS.ENV().POOL_PREF) {
						rens.add(new StatRow(s, cl) {
							
							@Override
							public void hoverInfoGet(GUI_BOX text) {
								
								super.hoverInfoGet(text);
								if (CitizenMain.current != null) {
									GBox b = (GBox) text;
									b.NL(8);
									for (ROOM_POOL p : SETT.ROOMS().POOLS) {
										b.add(p.icon);
										b.add(GFORMAT.perc(b.text(), CitizenMain.current.pref().pool(p)));
									}
								}
							}
							
						});
						
						
					}else if (s.info().matters()){
						rens.add(new StatRow(s, cl));
					}
				}
			}
		}
		
		rens.add(new StatRow.Title(STATS.STORED().info));
		
		for (STAT s : STATS.STORED().createTheOnesThatMatter(cl)) {
			rens.add(new StatRow(s, cl));
		}

		section.addDown(4, new GScrollRows(rens, HEIGHT, 0).view());
		
	}

}