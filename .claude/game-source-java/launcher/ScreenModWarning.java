package launcher;

import launcher.GUI.BText;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Text;
import util.text.D;

final class ScreenModWarning extends GuiSection{
	
	private static CharSequence ¤¤warn = "You are about to launch the game with code mods enabled. These mods contain code that could harm your PC, and you play at your own risk. Make sure you trust the author and the source of the mod.";
	private static CharSequence ¤¤launch = "Launch";
	private static CharSequence ¤¤cancel = "Cancel";
	
	static {
		D.ts(ScreenModWarning.class);
	}
	
	
	public ScreenModWarning(Launcher l) {
		
		Text t = new Text(l.res.font, ¤¤warn).setScale(1);
		t.setMaxWidth(600);
		add(t, 0, 0);
		
		GuiSection bb = new GuiSection();
		
		bb.add(new BText(l.res, ¤¤launch, 200){
			
			@Override
			protected void clickA() {
				
				l.s.save();
				Launcher.startGame = true;
				CORE.annihilate();
			}

		});
		bb.addRightC(0, new BText(l.res, ¤¤cancel, 200){
			
			@Override
			protected void clickA() {
				l.setMods();
			}

		});
		
		addRelBody(8, DIR.S, bb);
		
		body().moveC(Sett.WIDTH/2, Sett.HEIGHT/2);
	}
	
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		OPACITY.O75.bind();
		COLOR.BLACK.render(r, 0, Sett.WIDTH, 0, Sett.HEIGHT);
		OPACITY.unbind();
		super.render(r, ds);
	}
	
}
