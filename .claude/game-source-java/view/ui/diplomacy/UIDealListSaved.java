package view.ui.diplomacy;

import java.util.LinkedList;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.deal.DealBool;
import game.faction.diplomacy.deal.DealSave;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.HGROUP;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.WORLD;
import world.map.regions.Region;

public class UIDealListSaved extends GuiSection{

	private static CharSequence ¤¤YouGet = "We Give you";
	private static CharSequence ¤¤FactionGets = "You give us";

	static {
		D.ts(UIDealListSaved.class);
	}
	
	public UIDealListSaved(DealSave deal, int height){
		
		LinkedList<RENDEROBJ> rowsp = new LinkedList<>();
		LinkedList<RENDEROBJ> rowsnpc = new LinkedList<>();
		
		for (int i = 0; i < deal.bools.length; i++) {
			if (deal.bools[i]) {
				rowsp.add(bool(DIP.TMP().bools.all().get(i)));
				rowsnpc.add(bool(DIP.TMP().bools.all().get(i)));
			}
		}
		
		party(rowsp, deal.player, FACTIONS.player());
		party(rowsnpc, deal.npc, deal.f());
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		if (rowsnpc.size() != 0) {
			rows.add(row(new GHeader(¤¤YouGet)));
			for (RENDEROBJ o : rowsnpc)
				rows.add(row(o));
		}
		
		if (rowsp.size() != 0) {
			rows.add(new GHeader(¤¤FactionGets));
			for (RENDEROBJ o : rowsp)
				rows.add(row(o));
		}
		
		add(new GScrollRows(rows, height).view());
		
	}
	
	private RENDEROBJ row(RENDEROBJ o) {
		return new HOVERABLE.HoverableAbs(400, 32) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				
				o.body().moveX1Y1(body);
				o.body().moveCY(body.cY());
				o.render(r, ds);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (o instanceof HOVERABLE) {
					((HOVERABLE) o).hoverInfoGet(text);
				}
			}
		};
	}
	
	private void party(LinkedList<RENDEROBJ> rows, DealSave.Party p, Faction f) {
		
		if (p.creditsP != 0) {
			rows.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, p.creditsP);
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(Dic.¤¤Currs);
				};
				
			}.hh(UI.icons().s.money));
		}
		
		for (int i : p.regsP) {
			
			if (i >= 0 && WORLD.REGIONS().getByIndex(i).active()) {
				final Region reg = WORLD.REGIONS().getByIndex(i);
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						text.add(reg.info.name());
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(reg.info.name());
					};
					
				}.hh(UI.icons().s.world));
			}
		}
		
		for (RESOURCE res : RESOURCES.ALL()) {
			if (p.resP[res.index()] != 0) {
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, p.resP[res.index()]);
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(res.names);
						
						if (f != null) {
							b.textLL(Dic.¤¤Available);
							b.add(GFORMAT.i(b.text(), f.res().getAvailable(res)));
						}
						
					};
					
				}.hh(res.icon()));
			}
		}
		
		for (Race res : RACES.all()) {
			if (p.raceP[res.index()] != 0) {
				rows.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, p.raceP[res.index()]);
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(HGROUP.get(HCLASSES.SLAVE(), res).name);
						if (f != null) {
							b.textLL(Dic.¤¤Available);
							b.add(GFORMAT.i(b.text(), f.slaves().available(res)));
						}
					};
					
				}.hh(res.appearance().icon));
			}
		}
		
	}
	
	private static RENDEROBJ bool(DealBool bo) {
		GText t = new GText(UI.FONT().M, bo.info.name);
		GTextR tt = new GTextR(t);
		tt.hoverInfoSet(bo.info.desc);
		return tt;
	}

	
}
