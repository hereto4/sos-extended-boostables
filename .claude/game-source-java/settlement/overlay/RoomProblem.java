package settlement.overlay;

import init.sprite.UI.UI;
import settlement.main.SETT;
import settlement.room.main.Room;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import util.GUTIL;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;

final class RoomProblem extends Addable{

	private static CharSequence ¤¤desc = "Show problems that exists with your rooms.";
	static {
		D.ts(RoomProblem.class);
	}
	
	
	RoomProblem(){
		super(UI.icons().s.alert, "PROBLEM", Dic.¤¤Problem, ¤¤desc, true, false);
		exclusive = true;
	}
	
	@Override
	public void initBelow(RenderData data) {
		GUTIL.flooder().init(this);
	}

	@Override
	public boolean render(Renderer r, RenderIterator it) {

		return false;
	}
	
	@Override
	public void renderBelow(Renderer r, RenderIterator it) {
		Room ro = SETT.ROOMS().map.get(it.tx(), it.ty());
		if (ro != null) {
			int mx = ro.mX(it.tx(), it.ty());
			int my = ro.mY(it.tx(), it.ty());
			if (!GUTIL.flooder().hasBeenPushed(mx, my)) {
				int v = 0;
				if (VIEW.s().ui.rooms.problem(ro, mx, my))
					v = 1;
				else if (VIEW.s().ui.rooms.warning(ro, mx, my))
					v = 2;
				GUTIL.flooder().close(mx, my,v);
			}
			
			double v = GUTIL.flooder().getValue(mx, my);
			if (v == 0)
				renderUnder(GCOLOR.MAP().OVERLAY_GOOD, r, it);
			else if (v == 1)
				renderUnder(GCOLOR.MAP().OVERLAY_BAD, r, it);
			else
				renderUnder(GCOLOR.MAP().SOSO, r, it);
		}else {
			renderUnder(COLOR.WHITE15, r, it);
		}
	}
	
	@Override
	public void finishBelow() {
		GUTIL.flooder().done();
	}
	
}
