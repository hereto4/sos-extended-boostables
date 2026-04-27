package view.ui.top;

import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GStat;

public abstract class UIPanelTopButtS extends UIPanelTopButtAbs {
	
	public UIPanelTopButtS(SPRITE icon) {
		super(icon, width(), 24);
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, SPRITE label, GStat stat, boolean active) {
		label.renderCY(r, body().x1() + 4, body.cY());

		if (active) {
			OPACITY.O35.bind();
			stat.adjust();
			COLOR.BLACK.render(r, body().x1() + 4 + label.width() + 0,
					body().x1() + 4 + label.width() + 2 + stat.width() + 2, body.y1() + 4, body.y2() - 4);
			OPACITY.unbind();
			stat.renderCY(r, body().x1() + 4 + label.width() + 2, body.cY());
		}
	}

	public static int width() {
		return Icon.S + UI.FONT().S.height() * 4-6;
	}
	
}