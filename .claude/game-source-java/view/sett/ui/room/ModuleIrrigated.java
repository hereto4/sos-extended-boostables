package view.sett.ui.room;

import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomInstance;
import settlement.room.water.RoomIrrigated;
import settlement.room.water.RoomIrrigated.ROOM_IRRIGATED;
import settlement.tilemap.ground.Ground;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModuleIrrigated implements ModuleMaker{

	private final CharSequence ¤¤Desc = "¤This room depends on the moisture of its ground tiles. All tiles have a default moisture percentage, which can be boosted by sweet water access emanating from natural bodies of water and canals. This room needs an average moisture of {0}% to get a full boost. Current average is {1}%, which will result in a {2}% target value. The current value of {3}% will take some time to move to the target.";
	
	public ModuleIrrigated(Init init) {
		
		D.t(this);
	}
	
	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		
		if (p instanceof ROOM_IRRIGATED) {
			l.add(new I((ROOM_IRRIGATED) p));
		}
		
	}
	
	
	private class I extends UIRoomModule {
		
		private final ROOM_IRRIGATED p;
		
		I(ROOM_IRRIGATED b){
			this.p = b;
		}
		
		
		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			
			section.addRelBody(8, DIR.S, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, CLAMP.d(p.irrigation().prospectFlat(get.get()), 0, 1));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(Ground.¤¤moisture);
					GText t = b.text();
					
					RoomInstance i = get.get();
					
					double target = p.irrigation().needed(i)/i.area();
					double current = RoomIrrigated.rawValue(i);
					double value = p.irrigation().prospectFlat(i);
					
					t.add(¤¤Desc);
					t.insert(0, (int)(Math.round(100*target)));
					t.insert(1, (int)(Math.round(100*current)));
					t.insert(2, (int)(Math.round(100*value)));
					t.insert(3, (int)(Math.round(100*p.irrigation().current(i))));
					b.add(t);
					SETT.OVERLAY().MOISTURE.add();
				};
				
				
				
			}.hh(SETT.ENV().map.WATER_SWEET.icon));
			
			
			
		}
		
		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			box.add(SETT.ENV().map.WATER_SWEET.icon);
			box.textL(Ground.¤¤moisture);
			box.tab(6);
			if (room instanceof RoomInstance) {
				box.add(GFORMAT.perc(box.text(), CLAMP.d(p.irrigation().prospectFlat((RoomInstance)room), 0, 1)));
			}
			box.NL();
		}
	}
	
}
