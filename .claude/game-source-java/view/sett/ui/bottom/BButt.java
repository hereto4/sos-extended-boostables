package view.sett.ui.bottom;

import init.sprite.UI.UI;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;
import util.gui.misc.GButt;

class BButt extends GButt.ButtPanel {
	
	public static final int HEIGHT = 44;
	public static final int WIDTH = 350;

	
	public BButt(SPRITE icon, CharSequence label) {
		super((SPRITE)new Text(UI.FONT().H2, label).setMaxWidth(WIDTH-50).setMultipleLines(false));
		SPRITE i = new SPRITE.Wrap(icon, 32, 32);
		icon(i);
		setDim(WIDTH, HEIGHT);
	}
	
	public BButt(SPRITE icon, CharSequence label, int dwidth) {
		super((SPRITE)new Text(UI.FONT().H2, label).setMaxWidth(WIDTH-50-dwidth).setMultipleLines(false));
		SPRITE i = new SPRITE.Wrap(icon, 32, 32);
		icon(i);
		setDim(WIDTH-dwidth, HEIGHT);
	}

}
