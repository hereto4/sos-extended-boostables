package view.sett.ui.room;

import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomInstance;
import settlement.room.main.job.ROOM_RADIUS;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUSE;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;
import snake2d.util.sprite.text.Str;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.slider.GSliderInt;
import util.text.D;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleRadius implements ModuleMaker{

	private final CharSequence ¤¤NAME = "¤Radius";
	private final CharSequence ¤¤PROBLEM = "¤No work is within the radius!";
	private final CharSequence ¤¤DESC = "¤Set the work radius of this room. Subjects will look for work within the radius. A big radius can be ineffective and workers will have a hard time getting back to services when work is done.";
	
	private RoomInstance i;

	
	public ModuleRadius(Init init) {
		
		D.t(this);
	}
	
	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		
		if (p instanceof ROOM_RADIUS) {
			l.add(new I(p));
		}
		
	}
	
	
	private class I extends UIRoomModule {
		
		private final RoomBlueprint p;
		
		I(RoomBlueprint b){
			this.p = b;
		}
		
		
		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			
			
			
			
			if (p instanceof ROOM_RADIUSE) {
				
				GuiSection s = new GuiSection() {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						
						text.title(¤¤NAME);
						text.text(¤¤DESC);
					}
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						i = get.get();
						SETT.OVERLAY().roomRadius(get.get(), ((ROOM_RADIUS_INSTANCE) get.get()).radius());
						super.render(r, ds);
					}
					
				};
				
				GHeader h = new GHeader(¤¤NAME) {
					@Override
					protected void render(snake2d.SPRITE_RENDERER r, float ds, boolean isHovered) {
						super.render(r, ds, isHovered);
						
					}; 
				};
				h.hoverInfoSet(¤¤DESC);
				s.add(UI.icons().m.place_ellispse, 0, 0);
				
				ROOM_RADIUSE r = (ROOM_RADIUSE) p;
				INTE i = new INTE() {
					
					@Override
					public int min() {
						return 0;
					}
					
					@Override
					public int max() {
						return 100;
					}
					
					@Override
					public int get() {
						return r.radiusInstance(get.get()).radiusRaw();
					}
					
					@Override
					public void set(int t) {
						r.radiusInstance(get.get()).radiusRawSet((byte) t);
					}
				};
				GSliderInt m = new GSliderInt(i, 200, true, false);
				s.addRightC(8, m);
				section.addRelBody(2, DIR.S, s);
				
			}else if (p instanceof ROOM_RADIUS) {
				section.add(new RENDEROBJ.RenderImp() {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						i = get.get();
						SETT.OVERLAY().roomRadius(get.get(), ((ROOM_RADIUS_INSTANCE) get.get()).radius());
					}
				});
				
			}
			
			
			
			
		}
		
		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			ModuleRadius.this.i = (RoomInstance) i;
			//ren.add();
		}
		
		@Override
		public void problem(Stack<Str> free, LISTE<CharSequence> errors, LISTE<CharSequence> warnings, Room room, int rx, int ry) {
			ROOM_RADIUS_INSTANCE i = (ROOM_RADIUS_INSTANCE) room;
			if (!i.searching()) {
				errors.add(¤¤PROBLEM);
			}
		}
	}
	
}
