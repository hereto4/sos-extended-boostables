package game.event.engine;

import game.event.actions.EventAction;
import game.time.TIME;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GText;
import util.text.D;
import util.text.DicTime;
import view.main.VIEW;

final class Butt extends CLICKABLE.ClickableAbs{

	private final EVENT_HANDLER e;
	
	private static CharSequence ¤¤timeRemaining = "Time Remaining";
	
	static {
		D.ts(Butt.class);
	}
	
	Butt(EVENT_HANDLER e){
		super(64, 48);
		this.e = e;
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		if (e.current() != null) {
			GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
			e.current().info.icon.renderC(r, body.cX(), body.cY());
			GButt.ButtPanel.renderFrame(r, body);
		}	
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (e.current() == null)
			return;
		GBox b = (GBox) text;
		b.title(e.current().info.name);
		b.text(e.current().info.desc);
		b.NL();
		if (e.current().info.showRemaining && e.current().duration.seconds > 0) {
			b.text(¤¤timeRemaining);
			double t = e.current().duration.seconds -e.timeElapsed();
			int days = (int) (t/TIME.secondsPerDay());
			GText te = b.text();
			if (days > 0) {
				DicTime.setDays(te, days);
				b.add(te);
				te = b.text();
			}
			t -= days*TIME.secondsPerDay();
			DicTime.setHours(te, t/TIME.secondsPerHour());
			b.add(te);
			te = b.text();
			b.NL();
		}
		b.NL(8);
		for (EventAction a : e.current().on_spawn)
			if (!a.hideUI)
				a.hover(b, e.current(), e.context());
		
		b.NL();
		for (EventAction a : e.current().on_spawn) {
			if (a.hideUI)
				continue;
			CharSequence s = a.problem(e.current(), e.context());
			if (s != null) {
				b.error(s);
				b.NL();
			}
		}
		
	}
	
	@Override
	protected void clickA() {
		if (e.context() == null || e.mess() == null)
			return;
		VIEW.messages().reopen(e.mess());
		super.clickA();
	}

}
