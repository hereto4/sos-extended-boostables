package game.raiding;

import init.sprite.UI.UI;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GText;
import util.text.D;
import view.ui.message.MessageSection;

final class MessDemandTY extends MessageSection{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static CharSequence ¤¤title = "Raider Satisfied";
	private static CharSequence ¤¤body = "Since we paid the ransom, this raider will leave us alone. For now...";
	
	static {
		D.ts(MessDemandTY.class);
	}
	
	private final Raider raider;
	
	public MessDemandTY(Raider raider) {
		super(¤¤title);
		this.raider = raider;
	}

	@Override
	protected void make(GuiSection section) {
		
		for (String s : raider.text.payed)
			paragraph(s);
		
		section.addRelBody(16, DIR.S, new GText(UI.FONT().S, ¤¤body).lablifySub().setMaxWidth(WIDTH));
		
		section.addRelBody(32, DIR.N, new RaiderPortrait(4).set(raider));
	}

}
