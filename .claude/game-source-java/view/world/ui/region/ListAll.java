package view.world.ui.region;

import game.faction.FACTIONS;
import game.faction.FBanner;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import init.constant.C;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.text.StringInputSprite;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.misc.GInput;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.gui.table.GTableSorter;
import util.gui.table.GTableSorter.GTFilter;
import util.gui.table.GTableSorter.GTSort;
import util.text.D;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;

final class ListAll extends ISidePanel{

	private final int width = C.SG*240;
	private final int height = C.SG*30;
	{D.gInit(this);}
	
	private final GTableSorter<Region> sorter = new GTableSorter<Region>(WREGIONS.MAX) {
		@Override
		protected Region getUnsorted(int index) {
			Region f = WORLD.REGIONS().getByIndex(index);
			if (f.info.area() > 0 && !(f.faction() == FACTIONS.player() && f.capitol()))
				return f;
			return null;
		};
	};
	
	private final StringInputSprite s;
	
	private GTFilter<Region> filterName = new GTFilter<Region>(D.g("search")) {
		@Override
		public boolean passes(Region h) {
			return h.info.name().startsWithIgnoreCase(s.text());
		}
		
	};
	
	private final GTSort<Region> sort = new GTSort<Region>("blabla") {

		@Override
		public int cmp(Region current, Region cmp) {
			return get(current) - get(cmp);
		}
		
		private int get(Region current) {
			int m = WREGIONS.MAX*FACTIONS.MAX();
			int res = current.index();
//			if (!Region.DATA().marked.is(current)) {
//				res += 4*m;
//			}
			Faction f = current.faction();
			if (f == null) {
				res += 3*m;
			}else if (f != FACTIONS.player()) {
				res += m;
				res += WREGIONS.MAX*f.index();
			}
			return res;
			
		}

		@Override
		public void format(Region h, GText text) {
			// TODO Auto-generated method stub
			
		}
		
	};

	private final GTableBuilder builder;
	
	ListAll() {
		
		sorter.setSort(sort);
		
		s = new StringInputSprite(8, UI.FONT().H2){
			@Override
			protected void change() {
				sorter.setFilter(filterName);
			};
		}.placeHolder(filterName.name);
		
		GInput in = new GInput(s);
		
		section.add(in);
		
		builder = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return sorter.size();
			}
			
		};

		builder.column(null, width, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new Button(ier);
			}
		});
		
		GuiSection s = builder.createHeight(HEIGHT-in.body.height()-C.SG*4, true);
		section.addDown(2, s);

		
		
		titleSet(D.g("Regions"));
	}
	
	@Override
	protected void update(float ds) {
		
		sorter.sort();
	}
	
	private final GText textH = new GText(UI.FONT().H2, 30);
	
	private final class Button extends ClickableAbs {
		
		private final GETTER<Integer> ier;
		
		Button(GETTER<Integer> ier){
			this.ier = ier;
			body.setWidth(width);
			body.setHeight(height);
			

			
			
		}
		
		@Override
		protected void clickA() {
			
			Region reg = sorter.get(ier);
			VIEW.world().window.centererTile.set(reg.cx(), reg.cy());
			ISidePanel p = VIEW.world().UI.regions.get(reg);
			VIEW.world().panels.add(ListAll.this, true);
			VIEW.world().panels.add(p, false);
			
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			Region f = sorter.get(ier);
			selectedSet(VIEW.world().UI.regions.active(f));
			GCOLOR.UI().border().render(r, body());
			
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			
			COLOR col = COLOR.WHITE85;
			if (f.faction() == null) {
				FBanner.rebel.MEDIUM.renderCY(r, 8, body().cY());
			}else {
				f.faction().banner().MEDIUM.renderCY(r, 8, body().cY());
				if (f.faction() == null) {
					col = GCOLOR.T().INORMAL;
					
				}else if (DIP.WAR().is(FACTIONS.player(), f.faction())){
					col = GCOLOR.T().IBAD;
				}else {
					col = GCOLOR.T().IGOOD;
				}
			}
			
			textH.clear();
			textH.color(col);
			textH.add(f.info.name());
			
			textH.renderCY(r, 40, body().cY());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			Region f = sorter.get(ier);
			VIEW.world().UI.regions.hover(f, text);
		}
		
	}
	
	public ISidePanel get(Region f) {
		
		sorter.sortForced();
		if (f != null) {
			builder.set(sorter.getIndex(f));
		}
		
		return this;

	}
	
	
}
