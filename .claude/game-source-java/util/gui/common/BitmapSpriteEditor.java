package util.gui.common;

import init.sprite.BitmapSprite;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.gui.panel.GFrame;
import util.text.D;
import view.keyboard.KEYS;

public class BitmapSpriteEditor extends GuiSection{

	private BitmapSprite sprite;
	private static CharSequence ¤¤hovInfo = "¤Hold left mouse button to draw. Hold ({0}) to erase.";
	
	static {
		D.ts(BitmapSpriteEditor.class);
	}
	
	public BitmapSpriteEditor(BitmapSprite s) {
		this.sprite = s;
		
		final ColorImp col = new ColorImp();
		int pixelDim = 24;
		for (int y = 0; y < BitmapSprite.HEIGHT; y++) {
			for (int x = 0; x < BitmapSprite.WIDTH; x++) {
				final int x1 = x;
				final int y1 = y;
				
				CLICKABLE c = new ClickableAbs(pixelDim, pixelDim) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
						int i = 80;
						if(sprite != null) {
							i = sprite.is(x1, y1) ? 20 : 80;
							if (isHovered && MButt.LEFT.isDown()) {
								sprite.set(x1, y1, !KEYS.MAIN().MOD.isPressed());
							}
							if (isHovered)
								i+= 30;
						}
						
						col.set(i, i, i);
						col.render(r, body());
						
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						GText t = b.text();
						t.add(¤¤hovInfo);
						t.insert(0, KEYS.MAIN().MOD.repr());
						b.add(t);
					}
				};
				c.hoverSoundSet(null);
				add(c, x*pixelDim, y*pixelDim);
			}
		}
		
		
		
		GFrame f = new GFrame(body());
		add(f);
	}
	
	public BitmapSpriteEditor() {
		this(null);
	}
	
	
	public void spriteSet(BitmapSprite s) {
		this.sprite = s;
	}
	
	public BitmapSprite spriteGet() {
		return sprite;
	}
	
}
