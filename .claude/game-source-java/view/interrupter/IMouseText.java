package view.interrupter;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.gui.misc.GBox;
import util.gui.misc.GTextR;
import util.gui.panel.GPanel;
import view.keyboard.KEYS;
import view.main.VIEW;

/**
 * A text box appearing top-centred. Will disappear after once cycle
 * automatically.
 * 
 * @author mail__000
 *
 */
public class IMouseText extends Interrupter {

	private final GTextR text = new GTextR(UI.FONT().M, 200);
	private RENDEROBJ ren = text;
	private final GPanel box = new GPanel();
	private final InterManager manager;

	public IMouseText(InterManager manager) {
		this.manager = manager;
		box.inner().moveY1(C.TILE_SIZE * 2);
		text.text().setMaxWidth(C.WIDTH() / 3);
		text.text().lablify();
	}

	@Override
	protected void mouseClick(MButt button) {
		hide();
	}

	@Override
	protected boolean otherClick(MButt button) {
		hide();
		return false;
	}

	public void activate(CharSequence t) {
		this.text.text().set(t);
		ren = text;
		set();
		show(manager);
	}

	public void activate(RENDEROBJ ren) {

		this.ren = ren;
		if (ren == null)
			return;
		set();
		show(manager);
	}

	private void set() {

		box.inner().set(ren);
		
		box.inner().moveX1Y1(VIEW.mouse().x() + C.SG * 20, VIEW.mouse().y() + C.SG * 20);

		if (box.inner().x2() > C.WIDTH()) {
			box.inner().moveX2(VIEW.mouse().x() - C.SG * 5);
		}

		if (box.inner().y2() > C.HEIGHT()) {
			box.inner().moveY2(C.HEIGHT());
		}

		ren.body().centerIn(box);
		
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return mCoo.isWithinRec(box);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		box.render(r, ds);
		ren.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		if (KEYS.anyDown())
			hide();
		return true;
	}

	@Override
	protected void hoverTimer(GBox text) {

	}

}
