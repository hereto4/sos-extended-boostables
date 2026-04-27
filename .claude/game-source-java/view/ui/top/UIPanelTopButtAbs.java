package view.ui.top;

import snake2d.SPRITE_RENDERER;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;

abstract class UIPanelTopButtAbs extends GButt {

	private final GStat stat = new GStat() {
		@Override
		public void update(GText text) {
			GFORMAT.i(text, getNumber());
			text.lablify();
		}
	}.decrease();


	
	public UIPanelTopButtAbs(SPRITE icon, int width, int height) {
		super(icon);
		body.setWidth(width);
		body.setHeight(height);
	}

	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {

		renAction();
		
		GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
		GButt.ButtPanel.renderFrame(r, isActive, isSelected, isHovered, body);
		

		
		boolean active = isActive();
		if (active) {
			double cu = CLAMP.d(value(), 0, 1);
			double ta = CLAMP.d(valueNext(), 0, 1);

		
			
			if (ta >= 1 && cu >= 1) {
				GMeter.renderDelta(r, 1, 1, body.x1()+2, body.x2()-2, body.y1()+2, body.y2()-2, GMeter.C_GREENISH, GMeter.C_GREENISH, GMeter.C_GREENISH, true, false);
			} else {
				GMeter.renderDelta(r, cu, ta, body.x1()+2, body.x2()-2, body.y1()+2, body.y2()-2, true, false);
			}
			
			//col.render(r, body.x1() + 4, (int) (body.x1() + 4 + (body.width() - 8) * cu), body.y1() + 4, body.y2() - 4);
		}
		
		

		
		stat.adjust();
		render(r, label, stat, active);
		

	}
	
	abstract void render(SPRITE_RENDERER r, SPRITE label, GStat stat, boolean active);

	protected abstract int getNumber();

	protected abstract double value();

	protected abstract double valueNext();

	protected abstract boolean isActive();

}