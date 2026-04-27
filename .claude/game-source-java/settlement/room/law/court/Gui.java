package settlement.room.law.court;

import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GGrid;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<CourtInstance, ROOM_COURT> {
	

	Gui(ROOM_COURT s) {
		super(s);
	}

	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<CourtInstance> g, int x1, int y1) {
		
		
		
		RENDEROBJ r = null;
		
		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, g.get().executions(), g.get().total());
			}
		}.hh(blueprint.constructor.prisoners.name()).hoverInfoSet(blueprint.constructor.prisoners.desc());
		grid.add(r);
		
		
		
	}
	
	@Override
	protected void appendMain(GGrid grid, GGrid text, GuiSection sExtra) {
		RENDEROBJ r = null;
		
		r = new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.iofk(text, blueprint.punishUsed(), blueprint.punishTotal());
			}
		}.hh(blueprint.constructor.prisoners.name()).hoverInfoSet(blueprint.constructor.prisoners.desc());
		text.add(r);
	}
	
	@Override
	protected void hover(GBox box, CourtInstance i) {
		box.NL();
		box.text(blueprint.constructor.prisoners.name());
		box.add(GFORMAT.iofk(box.text(), i.executions(), i.total()));
	}
	


}
