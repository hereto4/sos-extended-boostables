package game.boosting.tmp;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.INDEXED;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.D;

public class TmpBoostingButt{

	private static CharSequence ¤¤no = "Nothing out of the ordinary is having an effect."; 
	
	static {
		D.ts(TmpBoostingButt.class);
	}
	
	public static<T extends INDEXED> CLICKABLE make(GETTER<T> get, TmpBoostable<T> type) {
		
		GButt.ButtPanel p = new GButt.ButtPanel(UI.icons().l.event) {
			
			@Override
			protected void renAction() {
				
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				super.render(r, ds, isActive, isSelected, isHovered);
				if (!type.any(get.get())) {
					OPACITY.O50.bind();
					COLOR.BLACK.render(r, body, -2);
				}
			}
			
			@Override
			protected void clickA() {
				super.clickA();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (type.any(get.get())) {
					GBox b = (GBox) text;
					type.hover(b, get.get());
				}else {
					text.text(¤¤no);
				}
			}
			
		};
		
		return p;
	}
	
}
