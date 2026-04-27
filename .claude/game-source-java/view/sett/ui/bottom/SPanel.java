package view.sett.ui.bottom;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.gui.GuiSection;
import util.colors.GCOLOR;

class SPanel extends GuiSection{
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		COLOR.WHITE20.render(r, body());
		GCOLOR.UI().borderH(r, body(), 0);
		super.render(r, ds);
	};
}
