package view.world.ui.region;

import init.constant.Config;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;
import world.army.WDIV;
import world.map.regions.Region;
import world.region.RD;
import world.region.building.RDBuilding;
import world.region.building.RDBuildingLevel;


final class MiscMore extends GuiSection{


	public static RENDEROBJ garrison(GETTER_IMP<Region> g, int width) {
		
		GuiSection ss = new GuiSection();

		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		int row = (width/Icon.M);
		
		if (Config.battle().REGION_MAX_DIVS/row >= 3)
			row = ((width-24)/Icon.M);
		
		
		for (int i = 0; i < Config.battle().REGION_MAX_DIVS; i++) {
			
			GuiSection s = new GuiSection();
			rows.add(s);
			for (int k = 0; k < row && i <Config.battle().REGION_MAX_DIVS; k++) {
				s.addRightC(0, new DivCard(i, g));
				i++;
			}
			
		}
		
		if (rows.size() < 3) {
			GuiSection s = new GuiSection();
			for (RENDEROBJ o : rows)
				s.addDown(0, o);
			ss.addRightCAbs(48,s);
		}else {
			ss.addRightCAbs(48, new GScrollRows(rows, rows.get(0).body().height()*3).view());
		}
		return ss;
	}
	
	public static RENDEROBJ buildings(GETTER_IMP<Region> g) {
		
		int cols = 11;
		ArrayList<RDBuilding> buildings = new ArrayList<>(RD.BUILDINGS().all.size());
		
		GuiSection ss = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				buildings.clear();
				for (RDBuilding bu : RD.BUILDINGS().all) {
					if (bu.level.get(g.get()) > 0)
						buildings.add(bu);
				}
				super.render(r, ds);
			}
			
		};
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return (int) Math.ceil((double)buildings.size()/cols);
			}
		};
		
		bu.column(null, cols*32, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GuiSection s = new GuiSection();
				for (int i = 0; i < cols; i++) {
					final int k = i;
					s.addRightC(0, new HOVERABLE.HoverableAbs(32, 32) {
						
						@Override
						protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
							
							RDBuilding bu = buildings.get(k + ier.get()*cols);
							if (bu != null) {
								bu.levels.get(bu.level.get(g.get())).icon.render(r, body);
							}
						}
						
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							RDBuilding bu = buildings.get(k + ier.get()*cols);
							if (bu != null) {
								RDBuildingLevel l = bu.levels.get(bu.level.get(g.get()));
								text.title(l.name);
								text.text(bu.info.desc);
							}
						}
						
					});
				}
				return s;
			}
		});
		
		ss.add(bu.create(3, false));
		
		ss.addRelBody(2, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.i(text, RD.DEVASTATION().raidCredits(g.get()));
			}
			
		}.hh(UI.icons().s.money).hoverTitleSet(Dic.¤¤Spoils));
		
		return ss;
	}
	
	public static class DivCard extends HoverableAbs {

		private final int di;
		private final GETTER_IMP<Region> g;
		
		DivCard(int di, GETTER_IMP<Region> g){
			super(Icon.M, Icon.M);
			this.g = g;
			this.di = di;
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			LIST<WDIV> divs = RD.MILITARY().divisions(g.get());
			
			if (di < divs.size()) {
				WDIV d = divs.get(di);
				d.race().appearance().icon.render(r, body);
				int width = (int) ((body().width()*d.menTarget())/(double)Config.battle().MEN_PER_DIVISION);
				double dd = (double)d.men()/d.menTarget();
				GMeter.render(r, GMeter.C_REDGREEN, dd, body.x1(), body.x1()+width, body.y2()-8, body.y2());
			}
				
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			LIST<WDIV> divs = RD.MILITARY().divisions(g.get());
			
			if (di < divs.size()) {
				WDIV d = divs.get(di);
				VIEW.UI().div.world.hover(d, b);
				
			}
			
			super.hoverInfoGet(text);
		}
		
	}
	
}
