package game.raiding;

import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import view.main.VIEW;
import view.ui.message.MessageSection;

final class MessArmyAppear extends MessageSection{

	private static CharSequence ¤¤title = "Raiders Arrived";
	private static CharSequence ¤¤desc = "{0} has been spotted at our borders Milord. Death and destruction will follow in {1} path to our capital. We must put a stop to this now.";
	
	static {
		D.ts(MessArmyAppear.class);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Raider raider;
	private final int x;
	private final int y;
	
	public MessArmyAppear(Raider raider, int x, int y) {
		super(¤¤title);
		this.raider = raider;
		this.x = x;
		this.y = y;
	}

	@Override
	protected void make(GuiSection section) {
		
		Str.TMP.clear().add(¤¤desc);
		Str.TMP.insert(0, raider.name);
		Str.TMP.insert(1, raider.indu.race().info.pHIS.get(raider.indu, false));
		paragraph(Str.TMP);
		section.addRelBody(32, DIR.N, new RaiderPortrait(4).set(raider));
		
		section.addRelBody(16, DIR.S, new GButt.ButtPanel(UI.icons().m.crossair){
			
			@Override
			protected void clickA() {
				VIEW.world().activate();
				VIEW.world().window.setZoomout(0);
				VIEW.world().window.centererTile.set(x, y);
			};
			
			
		}.setDim(48));
		
		
	}

}
