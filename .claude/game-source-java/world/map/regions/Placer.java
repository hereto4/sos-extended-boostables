package world.map.regions;

import game.faction.FACTIONS;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.Icons.S.IconS;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.data.INT;
import util.data.INT.IntImp;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.slider.GSliderInt;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import util.text.Dic;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableFixedImp;
import view.tool.PlacableMulti;
import view.tool.PlacableSimpleTile;
import view.world.generator.tools.UIWorldToolCapitolPlaceInfo;
import world.WORLD;
import world.map.regions.centre.WCentre;
import world.map.regions.centre.WorldCentrePlacablity;
import world.overlay.WorldOverlays.OverlayTile;

class Placer extends ArrayListGrower<PLACABLE>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final INT.IntImp ii = new IntImp(1, WREGIONS.MAX-1);
	private static CharSequence ¤¤name = "Place Player";
	private static CharSequence ¤¤locate = "locate";
	private static CharSequence ¤¤removeC = "Remove Completely";
	private static CharSequence ¤¤remove = "Remove";
	private static CharSequence ¤¤namem = "Name";
	private static CharSequence ¤¤centre = "Centre";
	
	static {
		D.ts(Placer.class);
	}
	
	public Placer() {
		
		LinkedList<CLICKABLE> butts = new LinkedList<>();
		GSliderInt sl = new GSliderInt(ii, 100, true, true) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.add(b.text().add(ii.get()).add(':').s().add(get().info.name()));
			}
			
		};
		HovOverlay hov = new HovOverlay();
		
		butts.add(sl);
		butts.add(new GButt.ButtPanel(Dic.¤¤name) {
			
			@Override
			protected void clickA() {
				VIEW.inters().input.requestInput(new STRING_RECIEVER() {
					
					@Override
					public void acceptString(CharSequence string) {
						if (string != null)
							get().info.name().clear().add(string);
					}
				}, Dic.¤¤name);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(get().info.name());
			}
			
		});
		
		butts.add(new GButt.ButtPanel(UI.icons().m.crossair) {
			
			@Override
			protected void clickA() {
				for (COORDINATE c : WORLD.TBOUNDS())
					if (WORLD.REGIONS().map.get(c) == get()) {
						VIEW.world().window.centererTile.set(c);
						return;
					}
			}
			
		}.hoverTitleSet(¤¤locate));
		
		butts.add(new GButt.ButtPanel(UI.icons().m.skull) {
			
			@Override
			protected void clickA() {
				for (COORDINATE c : WORLD.TBOUNDS()) {
					Region r = WORLD.REGIONS().pmap.get(c);
					if (r != null && r.index() == ii.get())
						WORLD.REGIONS().pmap.set(c.x(), c.y(), null);
				}
			}
			
		}.hoverInfoSet(¤¤removeC));
		
		PLACABLE undo = new PlacableMulti(¤¤remove + ": " + Dic.¤¤Region, "", UI.icons().m.place_ellispse.resized(IconS.L).twin(UI.icons().m.anti, DIR.C, 0)) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				WORLD.REGIONS().pmap.set(tx, ty, null);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void updateRegardless(GameWindow window, AREA selected) {
				hov.hovered = null;
				hov.add();
			}
		};
		
		PLACABLE p = new PlacableMulti(Dic.¤¤Region, "", UI.icons().m.place_ellispse.resized(IconS.L)) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				WORLD.REGIONS().pmap.set(tx, ty, get());
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
			

			
			@Override
			public PLACABLE getUndo() {
				return undo;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return butts;
			}
			
			@Override
			public void updateRegardless(GameWindow window, AREA selected) {
				hov.hovered = get();
				hov.add();
			}
			
			@Override
			public void placeInfo(GBox b, int oktiles, AREA a) {
				if (a.area() == 1)
					hover(b, a.body().x1(), a.body().y1());
				super.placeInfo(b, oktiles, a);
			}
			
		};
		add(p);
		add(undo);
		p = new PlacableSimpleTile(¤¤namem + ": " + Dic.¤¤Region) {
			
			@Override
			public void place(int tx, int ty) {
				
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				VIEW.inters().input.requestInput(new STRING_RECIEVER() {
				
					@Override
					public void acceptString(CharSequence string) {
						if (string != null)
							reg.info.name().clear().add(string);
					}
				}, Dic.¤¤name);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return WORLD.REGIONS().map.get(tx, ty) != null ? null : E;
			}
			
			@Override
			public void hoverInfo(int tx, int ty, GBox hoverBox) {
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				if (reg != null) {
					hoverBox.add(hoverBox.text().add(reg.index()));
					hoverBox.NL();
					hoverBox.text(reg.info.name());
				}
				super.hoverInfo(tx, ty, hoverBox);
			}
			
			
			
			@Override
			public void renderOverlay(GameWindow window) {
				hov.hovered = WORLD.REGIONS().map.get(window.tile());
				hov.add();
			}
			
			@Override
			public SPRITE getIcon() {
				return UI.icons().m.menu;
			}
		
			
		};
		add(p);
		
		p = new PlacableSimpleTile(¤¤centre + ": " + Dic.¤¤Region) {
			
			@Override
			public void place(int tx, int ty) {
				
				Region reg = WORLD.REGIONS().map.get(tx, ty);
				reg.info.centreSet(tx, ty);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				if (WORLD.REGIONS().map.get(tx, ty) == null)
					return E;
				return WorldCentrePlacablity.regionC(tx, ty);
			}
			
			@Override
			public void hoverInfo(int tx, int ty, GBox hoverBox) {
				super.hoverInfo(tx, ty, hoverBox);
			}
			
			@Override
			public void renderOverlay(GameWindow window) {
				hov.hovered = WORLD.REGIONS().map.get(window.tile());
				hov.add();
			}
			
			@Override
			public SPRITE getIcon() {
				return UI.icons().m.crossair;
			}
			
		};
		add(p);
		
		p = new PlacableFixedImp(¤¤name, 1, 1, "", UI.icons().m.flag){

			

			final UIWorldToolCapitolPlaceInfo info = new UIWorldToolCapitolPlaceInfo();

			
			@Override
			public int width() {
				return WCentre.TILE_DIM;
			}
			
			@Override
			public void place(int tx, int ty, int rx, int ry) {
				if (rx == 0 && ry == 0)
					clear();
				WORLD.REGIONS().pmap.set(tx, ty, WORLD.REGIONS().getByIndex(0));
				if (rx == WCentre.TILE_DIM/2 && ry == WCentre.TILE_DIM/2)
					WORLD.REGIONS().getByIndex(0).info.centreSet(tx, ty);
				
				
			}

			
			@Override
			public int height() {
				return WCentre.TILE_DIM;
			}
			
			@Override
			public LIST<CLICKABLE> getAdditionalButt() {
				return null;
			}

			@Override
			public CharSequence placable(int tx, int ty, int rx, int ry) {
				if (rx == 0 && ry == 0)
					return WorldCentrePlacablity.terrain(tx, ty);
				return null;
			}
			
			@Override
			public void placeInfo(GBox b, int x1, int y1) {
				info.placeInfo(b, x1, y1, FACTIONS.player().race());
			}
			
			public void clear() {
				for (COORDINATE c : WORLD.TBOUNDS()) {
					if (WORLD.REGIONS().map.get(c) == WORLD.REGIONS().getByIndex(0))
						WORLD.REGIONS().pmap.set(c, null);
				}
			}
			
			@Override
			public void updateRegardless(GameWindow window) {
				hov.hovered = null;
				hov.add();
			}
		
		};
		add(p);
	}
	
	private static class HovOverlay extends OverlayTile{
		
		public HovOverlay() {
			super(true, false);
		}
		private Region hovered;
		@Override
		protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
			Region reg = WORLD.REGIONS().map.get(it.tile());
			if (reg != null) {
				COLOR c = reg == hovered ? COLOR.WHITE100 : COLOR.WHITE30;
				c.bind();
				if (it.tx() == reg.cx() && it.ty() == reg.cy()) {
					for (DIR d : DIR.ALL) {
						int m = d.mask();
						if (!d.isOrtho())
							m = d.next(1).mask()|d.next(-1).mask();
						m = ~m;
						m &= 0x0F;
						SPRITES.cons().BIG.outline.render(r, m, it.x()+d.x()*C.TILE_SIZE, it.y()+d.y()*C.TILE_SIZE);
					}
				}else {
					int m = 0;
					for (DIR d : DIR.ORTHO) {
						if (WORLD.REGIONS().map.get(it.tx(), it.ty(), d) == reg)
							m |= d.mask();
					}
					SPRITES.cons().BIG.dashed.render(r, m, it.x(), it.y());
				}
				
				
			}
		}
	}
	
	private void hover(GBox b, int tx, int ty) {
		Region rr = WORLD.REGIONS().map.get(tx, ty);
		if (rr != null) {
			b.NL();
			b.add(b.text().add(Dic.¤¤Current).add(':').s().add(rr.index()).add(rr.info.name()));
			b.NL();
		}
	}
	
	private Region get() {
		return WORLD.REGIONS().getByIndex(ii.get());
	}
	
}
