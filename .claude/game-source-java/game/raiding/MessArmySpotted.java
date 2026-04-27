package game.raiding;

import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GButt;
import util.text.D;
import view.main.VIEW;
import view.ui.message.MessageSection;

final class MessArmySpotted extends MessageSection{

	private static CharSequence ¤¤title = "Raiders Approaching";
	private static CharSequence ¤¤desc = "{0} and {1} band of raiders are approaching our borders Milord. They are only 3 days march away. We must prepare for {2} arrival.";
	
	static {
		D.ts(MessArmySpotted.class);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Raider raider;
	private final int x;
	private final int y;
	
	public MessArmySpotted(Raider raider, int x, int y) {
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
		Str.TMP.insert(2, raider.indu.race().info.pHIS.get(raider.indu, false));
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
