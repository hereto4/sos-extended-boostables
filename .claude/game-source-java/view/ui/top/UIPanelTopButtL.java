package view.ui.top;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GStat;

public abstract class UIPanelTopButtL extends UIPanelTopButtAbs {
	
	public UIPanelTopButtL(SPRITE icon) {
		super(icon, 60, 48);
	}
	
	@Override
	void render(SPRITE_RENDERER r, SPRITE label, GStat stat, boolean active) {
		COLOR.BLACK.bind();
		label.renderC(r, body().cX()+1, body.y1() + 17);
		COLOR.unbind();
		label.renderC(r, body().cX(), body.y1() + 16);

		if (active) {
			OPACITY.O50.bind();
			
			int w = stat.width();
			
			int y2 = body.y2()-6;
			int y1 = y2-stat.height();
			
			int x1 = body.cX() - w/2;
			int x2 = x1 + w;
			
			COLOR.BLACK.render(r, x1-2,
					x2+2, y1 -1, y2 +1);
			OPACITY.unbind();
			stat.render(r, x1, y1);
		}
	}

}