package util.gui.table;

import init.constant.C;
import snake2d.util.gui.clickable.Scrollable;
import util.data.INT.INTE;
import util.gui.slider.GSliderVer;

public abstract class GScrollable extends Scrollable implements INTE{

	
	public GScrollable(ScrollRow... rows) {
		super(
				
				null,
				rows);
		GSliderVer slider = new GSliderVer(this, getView().body().height());
		slider.body().moveX1(getView().body().x2()+C.SG*4);
		slider.body().moveY1(getView().body().y1());
		getView().add(slider);
	}

}
