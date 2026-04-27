package util.gui.slider;

import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GuiSection;
import util.data.INT.INTE;
import util.gui.misc.GInputInt;

public class GSliderIntInput extends GuiSection{


	public GSliderIntInput(INTE in){
		addRightC(2, new GSliderInt(in, 80, true, false));
		addRightC(8, new GInputInt(in));
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		super.render(r, ds);
	}
	
}
