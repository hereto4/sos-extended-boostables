package view.world.ui;

import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.gui.GUI_BOX;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;
import world.entity.WEntity;
import world.entity.army.WArmy;
import world.entity.caravan.Shipment;
import world.entity.haven.WHaven;
import world.map.regions.Region;

public class WorldHoverer {

	private WorldHoverer() {
		// TODO Auto-generated constructor stub
	}


	
	public static void hover(GUI_BOX box, WEntity e) {
		
		if (e instanceof WArmy)
			VIEW.world().UI.armies.hover(box, (WArmy) e);
		else if (e instanceof Shipment)
			hover(box, (Shipment) e);
		else if (e instanceof WHaven) {
			VIEW.world().UI.camps.hover(box, (WHaven)e);
		}
			
		
		
	}
	
	private static void hover(GUI_BOX box, Shipment e) {
		GBox b = (GBox) box;
		b.title(Dic.¤¤Caravan);
		b.textL(e.type().name);
		b.NL(8);
		Region c = e.destination();
		if (c == null || c.faction() == null)
			return;
		
		GText t = b.text();
		t.color(c.faction().banner().colorBG());
		t.add(Dic.¤¤BoundFor).insert(0, c.info.name());
		box.add(t);
		box.NL(4);
		
		int i =0 ;
		for (RESOURCE r :  RESOURCES.ALL()) {
			if (e.loadGet(r) > 0) {
				box.add(r.icon());
				box.add(GFORMAT.i(b.text(), e.loadGet(r)));
				i++;
				if (i > 8) {
					i = 0;
					box.NL();
				} 
			}
			
		}
		box.NL();
		i = 0;
		for (Race r :  RACES.all()) {
			if (e.loadGet(r) > 0) {
				box.add(r.appearance().icon);
				box.add(GFORMAT.i(b.text(), e.loadGet(r)));
				i++;
				if (i > 8) {
					i = 0;
					box.NL();
				}
			}
			
		}
	}

	
}
