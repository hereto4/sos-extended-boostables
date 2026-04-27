package world.region;

import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import init.sprite.UI.UI;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.IntImp;
import util.gui.common.BitmapSpriteEditor;
import util.gui.misc.GButt;
import util.gui.misc.GColorPicker;
import util.gui.slider.GSliderInt;
import view.main.VIEW;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import view.tool.PlacableSimpleTile;
import world.WORLD;
import world.map.regions.Region;
import world.region.pop.RDRace;

class Placers extends ArrayListGrower<PLACABLE>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Placers() {
		
		IntImp ii = new IntImp(1, FACTIONS.MAX());
		GSliderInt sl = new GSliderInt(ii, 100, true);
		LinkedList<CLICKABLE> butts = new LinkedList<>();
		butts.add(sl);
		
		PLACABLE undo = new PlacableMulti("remove faction") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null && reg.faction() != null) {
					RD.setFaction(reg, null, false);
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null && reg.faction() != null) {
					return null;
				}
				return E;
			}
			
			@Override
			public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
				Region reg = WORLD.REGIONS().map.get(fromX, fromY);
				return reg != null && reg.is(toX, toY);
			}
		};
		
		PLACABLE set = new PlacableMulti("set faction", "", UI.icons().m.flag) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null) {
					RD.setFaction(reg, FACTIONS.getByIndex(ii.get()), false);
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null) {
					return null;
				}
				return E;
			}
			
			@Override
			public boolean expandsTo(int fromX, int fromY, int toX, int toY) {
				Region reg = WORLD.REGIONS().map.get(fromX, fromY);
				return reg != null && reg.is(toX, toY);
			}
			
			@Override
			public PLACABLE getUndo() {
				return undo;
			}
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
			
		};
		
		PLACABLE setRace = new PlacableSimpleTile("generate stats race") {
			
			private RDRace race = RD.RACES().all.get(0);
			LinkedList<CLICKABLE> butts = new LinkedList<>();
			{
				for (RDRace r : RD.RACES().all) {
					butts.add(new GButt.ButtPanel(r.race.appearance().icon) {
						
						@Override
						protected void clickA() {
							race = r;
						};
						
						@Override
						protected void renAction() {
							selectedSet(race == r);
						};
						
					}.hoverInfoSet(r.race.info.name));
					
				}
			}
			
			@Override
			public void place(int tx, int ty) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null && reg.faction() instanceof FactionNPC) {
					((FactionNPC)reg.faction()).generate(race, false);
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null && reg.faction() instanceof FactionNPC) {
					return null;
				}
				return E;
			}
			
			@Override
			public SPRITE getIcon() {
				return UI.icons().m.citizen;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
		};
		
		PLACABLE setName = new PlacableSimpleTile("set faction name") {
			
			@Override
			public void place(int tx, int ty) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null && reg.faction() instanceof FactionNPC) {
					STRING_RECIEVER str = new STRING_RECIEVER() {

						@Override
						public void acceptString(CharSequence string) {
							if (string != null)
								reg.faction().name.clear().add(string);
							reg.faction().capitolRegion().info.name().clear().add(reg.faction().name);
						}
						
					};
					VIEW.inters().input.requestInput(str, "set faction name");
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null && reg.faction() instanceof FactionNPC) {
					return null;
				}
				return E;
			}
			
			@Override
			public SPRITE getIcon() {
				return UI.icons().m.menu;
			}
		};
		
		PLACABLE setBanner = new PlacableSimpleTile("set faction visuals") {
			GETTER_IMP<FactionNPC> g = new GETTER_IMP<>();
			GuiSection s = new GuiSection();
			BitmapSpriteEditor ee = new BitmapSpriteEditor();
			{
				s.add(ee);
				s.addRelBody(8, DIR.S, new GColorPicker(true) {
					
					@Override
					public ColorImp color() {
						return g.get().banner().colorBG();
					}
				});
				s.addRelBody(8, DIR.S, new GColorPicker(true) {
					
					@Override
					public ColorImp color() {
						return g.get().banner().colorFG();
					}
				});
			}
			

			
			@Override
			public void place(int tx, int ty) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null && reg.faction() instanceof FactionNPC) {
					g.set((FactionNPC) reg.faction());
					ee.spriteSet(reg.faction().banner().sprite);
					VIEW.inters().popup.show(s, null);
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null && reg.faction() instanceof FactionNPC) {
					return null;
				}
				return E;
			}
			
			@Override
			public SPRITE getIcon() {
				return UI.icons().m.flag;
			}
		};
		
		
		
		add(set);
		add(undo);
		add(setRace);
		add(setName);
		add(setBanner);
		
	}
	
}
