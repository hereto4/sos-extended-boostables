package world.map.landmark;

import init.sprite.UI.Icons.S.IconS;
import init.sprite.UI.UI;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.STRING_RECIEVER;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.data.INT;
import util.data.INT.IntImp;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.slider.GSliderInt;
import util.text.Dic;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import world.WORLD;

class Placers extends ArrayListGrower<PLACABLE>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final WorldLandmarks ll;
	private final INT.IntImp ii = new IntImp(1, WorldLandmarks.MAX);

	
	Placers(WorldLandmarks ll, PlacerOverlay overlay){
		
		this.ll = ll;
		
		LinkedList<CLICKABLE> butts = new LinkedList<>();
		GSliderInt sl = new GSliderInt(ii, 100, true, true) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				super.hoverInfoGet(text);
				GBox b = (GBox) text;
				b.add(b.text().add(':').add(get().name));
			}
			
		};
		butts.add(sl);
		butts.add(new GButt.ButtPanel(Dic.¤¤name) {
			
			@Override
			protected void clickA() {
				VIEW.inters().input.requestInput(new STRING_RECIEVER() {
					
					@Override
					public void acceptString(CharSequence string) {
						if (string != null)
							get().name.clear().add(string);
					}
				}, Dic.¤¤name);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(get().name);
			}
			
		});
		butts.add(new GButt.ButtPanel("...") {
			
			@Override
			protected void clickA() {
				VIEW.inters().input.requestInput(new STRING_RECIEVER() {
					
					@Override
					public void acceptString(CharSequence string) {
						if (string != null)
							get().description.clear().add(string);
					}
				}, "description");
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(ll.getByIndex(ii.get()).description);
			}
			
		});
		
		butts.add(new GButt.ButtPanel(UI.icons().m.crossair) {
			
			@Override
			protected void clickA() {
				for (COORDINATE c : WORLD.TBOUNDS()) {
					if (ll.setter.get(c) != null && ll.setter.get(c).index() == ii.get()) {
						VIEW.world().window.centererTile.set(c);
						return;
					}
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title("find landmark");
			}
			
		});
		
		butts.add(new GButt.ButtPanel(UI.icons().m.skull) {
			
			@Override
			protected void clickA() {
				for (COORDINATE c : WORLD.TBOUNDS()) {
					if (ll.setter.get(c) != null && ll.setter.get(c).index() == ii.get())
						ll.setter.set(c, null);
				}
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title("remove whole landmark");
			}
			
		});
		
		PLACABLE undo = new PlacableMulti(Dic.¤¤remove + ": " + ll.name, "", UI.icons().m.place_ellispse.resized(IconS.L).twin(UI.icons().m.anti, DIR.C, 0)) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				ll.setter.set(tx, ty, null);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return get() != null ? null : E;
			}
			
			@Override
			public void updateRegardless(GameWindow window, AREA selected) {
				overlay.hovered = get();
			}
			
			@Override
			public void placeInfo(GBox b, int oktiles, AREA a) {
				hover(b, oktiles, a);
			}
		};
		
		PLACABLE p = new PlacableMulti(ll.name, "", UI.icons().m.place_ellispse.resized(IconS.L)) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				ll.setter.set(tx, ty, get());
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
				overlay.hovered = get();
			}
			
			@Override
			public void placeInfo(GBox b, int oktiles, AREA a) {
				hover(b, oktiles, a);
			}
			
		};
		

		add(p);
		add(undo);
	}
	
	private void hover(GBox b, int oktiles, AREA a) {
		if (a.area() == 1) {
			for (COORDINATE c : a.body()) {
				WorldLandmark m = ll.setter.get(c);
				if (m != null) {
					b.NL();
					b.add(b.text().add(Dic.¤¤Current).add(':').s().add(m.index()).s().add(m.name));
					return;
				}
					
			}
		}
		
	}
	
	private WorldLandmark get() {
		return ll.getByIndex(ii.get());
	}
	
}
