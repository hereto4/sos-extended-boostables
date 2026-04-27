package view.sett.ui.room;

import init.sprite.UI.UI;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomInstance;
import settlement.room.water.RoomPumpable.ROOM_PUMPABLE;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.sett.ui.room.Modules.ModuleMaker;

final class ModulePumpable implements ModuleMaker{

	private final CharSequence ¤¤Name = "¤Water Supply";
	private final CharSequence ¤¤Desc = "¤Water Supply is gained from adjacent canals and drains that are connected to pumps.";
	private final CharSequence ¤¤Preasure = "¤Pressure required per tile:";
	private final SPRITE icon = UI.icons().s.drop.createColored(COLOR.BLUEISH);
	
	public ModulePumpable(Init init) {
		
		D.t(this);
	}
	
	@Override
	public void make(RoomBlueprint p, LISTE<UIRoomModule> l) {
		
		if (p instanceof ROOM_PUMPABLE) {
			l.add(new I((ROOM_PUMPABLE) p));
		}
		
	}
	
	
	private class I extends UIRoomModule {
		
		private final ROOM_PUMPABLE p;
		
		I(ROOM_PUMPABLE b){
			this.p = b;
		}
		
		
		@Override
		public void appendPanel(GuiSection section, GETTER<RoomInstance> get, int x1, int y1) {
			
			section.addRelBody(8, DIR.S, new GStat() {
				
				@Override
				public void update(GText text) {
					int x = get.get().mX();
					int y = get.get().mY();
					GFORMAT.perc(text, p.pumpable(x, y).irrigation(x, y));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(¤¤Name);
					b.text(¤¤Desc);
					
					int x = get.get().mX();
					int y = get.get().mY();
					double d = p.pumpable(x, y).suckAmount(x, y);
					b.NL();
					b.textLL(Dic.¤¤Current);
					b.tab(6);
					b.add(GFORMAT.iofkInv(b.text(), (long) Math.ceil(d*p.pumpable(x, y).irrigation(x, y)*get.get().area()), (long)Math.ceil(d*get.get().area())));
					b.NL();
					b.textLL(¤¤Preasure);
					b.NL();
					b.add(GFORMAT.f(b.text(), d));
					
					
				};
				
			}.hh(UI.icons().s.drop.createColored(COLOR.BLUEISH)));
			
			
			
		}
		
		@Override
		public void hover(GBox box, Room room, int rx, int ry) {
			box.add(icon);
			box.textL(¤¤Name);
			box.tab(6);
			box.add(GFORMAT.perc(box.text(), p.pumpable(rx, ry).irrigation(rx, ry)));
			box.NL();
		}
	}
	
}
