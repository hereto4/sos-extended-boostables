package view.sett.ui.room.construction;

import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.Room.RoomInstanceImp;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.TmpArea;
import settlement.room.main.category.RoomCategorySub;
import settlement.room.main.furnisher.FurnisherItem;
import settlement.room.main.util.RoomState;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.Dic;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableFixed;
import view.tool.PlacableMulti;

public class UIRoomPlacer {

	private final State state;
	
	public UIRoomPlacer() {
		state = new State();
	}
	
	public void init(RoomBlueprintImp b, int tx, int ty) {
		state.init(b, false);
		
		state.placement.placer.init(b, 0);
		if (b.constructor().usesArea())
			VIEW.s().tools.place(state.placement.placer.area(), state.config);
		else {
			if (b.constructor().isSpecialAreaPlacable()) {
				if (b.constructor().groups().size() == 1 && b.constructor().groups().get(0).rotations() == 1 && b.constructor().groups().size() == 1) {
					if (b.employment() == null) {
						ipla.set(b);
						VIEW.s().tools.place(ipla, null);
						return;
					}
				}else
					throw new RuntimeException();
				
			}
			
			FurnisherItem it = SETT.ROOMS().fData.item.get(tx, ty);
			if (it != null) {
				PlacableFixed pp = state.placement.placer.item(it.group.index());
				pp.rotSet(it.rotation);
				int size = 0;
				for (int i = 0; i < it.group.size(); i++) {
					if (it.group.item(i, it.rotation) == it) {
						size = i;
						break;
					}
				}
				pp.sizeSet(size);
				state.setItem(it.group().index());
				VIEW.s().tools.place(pp, state.config);
			}else{
				PlacableFixed pp = state.placement.placer.item(state.item());
				VIEW.s().tools.place(pp, state.config);
			}
			
				
		}
	}
	
	public void init(RoomBlueprintImp b, RoomCategorySub bb) {
		state.init(b, bb);
		
		state.placement.placer.init(b, 0);
		if (b.constructor().usesArea())
			VIEW.s().tools.place(state.placement.placer.area(), state.config);
		else {
			PlacableFixed pp = state.placement.placer.item(state.item());
			VIEW.s().tools.place(pp, state.config);
		}
	}
	
	public void init(RoomInstanceImp ins) {
		state.init(ins.constructor().blue(), true);
		
		RoomBlueprintImp b = ins.constructor().blue();
		RoomState stat = ins.makeState(ins.mX(), ins.mY(), false);
		int up = ins.upgrade();
		int deg = ins.degrader(ins.mX(), ins.mY()) == null ? 0 : ins.degrader(ins.mX(), ins.mY()).getData();
		TmpArea a = ins.remove(ins.mX(), ins.mY(), false, this, false);
		if (a == null)
			return;
		if (a.area() <= 0) {
			a.clear();
			return;
		}
			
		SETT.ROOMS().placement.placer.reconstruct(a, up, deg, stat, b);
		
		VIEW.s().tools.place(state.placement.placer.area(), state.config);
		
	}
	
	
	public void init(int tx, int ty) {
		if (state.placement.canReconstruct(tx, ty)) {
			RoomInstanceImp r = (RoomInstanceImp) SETT.ROOMS().map.get(tx, ty);
			init(r);
			
		}
	}
	
	public boolean isActive(RoomBlueprintImp b) {
		return VIEW.s().tools.configCurrent() == state.config && b == this.state.b;
	}
	
	public boolean isActive() {
		return VIEW.s().tools.configCurrent() == state.config;
	}
	
	public RoomBlueprintImp blue() {
		if (VIEW.s().tools.configCurrent() == state.config) {
			return this.state.b;
		}
		return null;
	}
	
	private final PlacerItemSingleArea ipla = new PlacerItemSingleArea();
	
	private class PlacerItemSingleArea extends PlacableMulti{

		private RoomBlueprintImp blueprint;
		private final ArrayList<CLICKABLE> li = new ArrayList<CLICKABLE>(5);
		
		private final CLICKABLE bOverlay = new GButt.ButtPanel(UI.icons().s.eye.sized(Icon.M)) {
			
			@Override
			protected void clickA() {
				SETT.ROOMS().placement.placer.showOverlay.toggle();
			}
			
			@Override
			protected void renAction() {
				selectedSet(SETT.ROOMS().placement.placer.showOverlay.is());
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(Dic.¤¤Overlay);
				SETT.ROOMS().placement.placer.structure.get();
				if (blueprint.constructor().overlay() != null && blueprint.constructor().overlay().desc != null) {
					text.text(blueprint.constructor().overlay().desc);
				}
			};
		};
		
		private final CLICKABLE bFoundation = new GButt.ButtPanel(SETT.OVERLAY().FOUNDATION.icon.resized(Icon.M)) {
			
			@Override
			protected void clickA() {
				SETT.ROOMS().placement.placer.showFoundation.toggle();
			}
			
			@Override
			protected void renAction() {
				selectedSet(SETT.ROOMS().placement.placer.showFoundation.is());
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(SETT.OVERLAY().FOUNDATION.name);
				text.text(SETT.OVERLAY().FOUNDATION.desc);
			};
		};
		
		public PlacerItemSingleArea() {
			super("");
		}
		
		public void set(RoomBlueprintImp b) {
			this.blueprint = b;
			
		}
		
		@Override
		public CharSequence name() {
			return blueprint.info.names;
		}

		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return init().placable(tx, ty, 0, 0);
		}

		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			init().place(tx, ty, 0, 0);
		}
		
		@Override
		public PLACABLE getUndo() {
			return init().getUndo();
		}
		
		@Override
		public void placeInfo(GBox b, int oktiles, AREA a) {
			for (int i = 0; i < blueprint.constructor().resources(); i++) {
				if (blueprint.constructor().groups().get(0).item(0, 0).cost2(i, 0) > 0) {
					b.setResource(blueprint.constructor().resource(i),oktiles*Math.ceil(blueprint.constructor().groups().get(0).item(0, 0).cost2(i, 0)));
					b.space();
				}
			}
			super.placeInfo(b, oktiles, a);
		}

		private PlacableFixed init() {
			state.placement.placer.init(blueprint, 0);
			PlacableFixed pp = state.placement.placer.item(0);
			return pp;
		}
		
		@Override
		public void updateRegardless(GameWindow window, AREA selected) {
			if (blueprint.constructor().overlay() != null && state.placement.placer.showOverlay.is())
				blueprint.constructor().overlay().add();
			if (blueprint.constructor().isHeavy() && state.placement.placer.showFoundation.is())
				SETT.OVERLAY().FOUNDATION.add();
		}
		
		@Override
		public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, AREA area,
				PLACER_TYPE type, boolean isPlacable, boolean areaIsPlacable) {
			super.renderPlaceHolder(r, mask, x, y, tx, ty, area, type, isPlacable, areaIsPlacable);
			blueprint.constructor().renderExtra(r, x, y, tx, ty, 0, 0, blueprint.constructor().groups().get(0).item(0, 0));
		}
		
		@Override
		public LIST<CLICKABLE> getAdditionalButt() {
			li.clearSloppy();
			if (blueprint.constructor().overlay() != null)
				li.add(bOverlay);
			if (blueprint.constructor().isHeavy())
				li.add(bFoundation);
			return super.getAdditionalButt();
		}
		

	}
	
}
