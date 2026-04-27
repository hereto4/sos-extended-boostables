package view.world.ui.panels;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.trade.ITYPE;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.world.ui.WorldHoverer;
import world.WORLD;
import world.entity.WEntity;
import world.entity.caravan.Shipment;

public class UICaravanList extends ISidePanel{

	private final ArrayList<Shipment> alll = new ArrayList<>(512);
	private final GAME.Cache cache = new GAME.Cache(120);
	public UICaravanList() {
		titleSet(Dic.¤¤Inbound);
		
		GTableBuilder b = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return all().size();
			}
		};
		
		b.column(null, 300, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Row(ier);
			}
		});
		section.add(b.createHeight(ISidePanel.HEIGHT, true));
		
		
		
	}
	
	private class Row extends HOVERABLE.HoverableAbs {

		private final GETTER<Integer> g;
		
		Row(GETTER<Integer> g){
			body.setDim(300, 32);
			this.g = g;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			if (g.get() >= all().size())
				return;
			Shipment s = all().get(g.get());
			SPRITE icon = SPRITES.icons().m.urn;
			if (s.type() == ITYPE.spoils)
				icon = SPRITES.icons().m.shield;
			else if (s.type() == ITYPE.tax)
				icon = SPRITES.icons().m.raw_materials;
			icon.renderCY(r, 10, body().cY());
			
			int m = 0;
			int x1 = body().x1()+64;
			for (RESOURCE res : RESOURCES.ALL()) {
				if (m > 12)
					break;
				if (s.loadGet(res) > 0) {
					m++;
					res.icon().renderCY(r, x1, body().cY());
					x1 += init.sprite.UI.Icon.M;
					if (m > 12)
						break;
						
				}
			}
			for (Race race : RACES.all()) {
				if (m > 12)
					break;
				if (s.loadGet(race) > 0) {
					m++;
					race.appearance().icon.renderCY(r, x1, body().cY());
					x1 += init.sprite.UI.Icon.M;
					if (m > 12)
						break;
						
				}
			}
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			
			if (super.hover(mCoo)) {
				if (g.get() >= all().size())
					return true;
				Shipment s = all().get(g.get());
				WORLD.OVERLAY().hoverEntity(s);
				VIEW.world().window.centererTile.set(s.ctx(), s.cty());
				return true;
			}
			return false;
		};
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (g.get() >= all().size())
				return;
			
			Shipment s = all().get(g.get());
			WorldHoverer.hover(text, s);
		}
		
		
	}
	
	
	public LIST<Shipment> all(){
		if (cache.shouldAndReset()) {
			alll.clearSloppy();
			for (WEntity e : WORLD.ENTITIES().allFast()) {
				if (e != null && e.added() && e instanceof Shipment) {
					Shipment s = (Shipment) e;
					if (s.destination() != null && s.destination() == FACTIONS.player().capitolRegion()) {
						alll.add(s);
						if (!alll.hasRoom())
							break;
					}
				}
				
			}
			
		}
		return alll;
	}
	
}
